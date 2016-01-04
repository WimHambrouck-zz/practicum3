package org.hambrouck.wim;

import com.sun.org.apache.regexp.internal.RE;
import com.sun.org.apache.xml.internal.security.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

/**
 * Created by Wim Hambrouck
 */
public class IntegriteitsModule {


    private static final Logger LOGGER = LoggerFactory.getLogger(IntegriteitsModule.class);
    public static final String UITVOERBESTAND = "integrity.xml";
    public static final String ZOUT = "adrYq%|Sq6XLqa~kAYs=XV7n^blthYy4XztOTRLg-G5Vx^ReHcfl6MZr8uEl";
    public static final String KEY_NAME = "quietly-graded-donkey";

    public void maakHandtekening(File map, String wachtwoord) throws Exception {

        SecretKey key = maakSleutel(wachtwoord, ZOUT);

        // generate DOM document
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element rootElement = document.createElementNS("urn:test", "test:Root");
        rootElement.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:test", "urn:test");
        document.appendChild(rootElement);
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM");

        // XML Signature references
        List<Reference> references = new LinkedList<>();

        //filter opdat integriteitsbestand zelf niet wordt opgenomen
        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.equals(UITVOERBESTAND))
                    return false;
                return true;
            }
        };

        //alle bestanden in de map afgaan en reference maken op basis van naam
        for (File bestand : map.listFiles(filter)) {
            List<Transform> transforms = new LinkedList<>();
            //Als onderstaande code uit commentaar wordt gehaald, werkt het niet meer
            /*Transform envTransform = xmlSignatureFactory.newTransform(
                    CanonicalizationMethod.ENVELOPED,
                    (C14NMethodParameterSpec) null
            );
            transforms.add(envTransform);
            Transform exclTransform = xmlSignatureFactory.newTransform(
                    CanonicalizationMethod.EXCLUSIVE,
                    (C14NMethodParameterSpec) null
            );
            transforms.add(exclTransform);*/

            Reference reference = xmlSignatureFactory.newReference(
                    bestand.getName(),
                    xmlSignatureFactory.newDigestMethod(DigestMethod.SHA256, null),
                    transforms,
                    null,
                    null
            );
            references.add(reference);
        }


        // XML Signature SignedInfo
        SignedInfo signedInfo = xmlSignatureFactory.newSignedInfo(
                xmlSignatureFactory.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE,
                        (C14NMethodParameterSpec) null),
                xmlSignatureFactory.newSignatureMethod("http://www.w3.org/2000/09/xmldsig#hmac-sha1", null),
                // xmlSignatureFactory.newSignatureMethod(SignatureMethod.HMAC_SHA1, null)
                references);

        // XML Signature
        KeyInfoFactory keyInfoFactory = xmlSignatureFactory.getKeyInfoFactory();
        KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyInfoFactory.newKeyName(KEY_NAME)));
        Element parentElement = document.getDocumentElement();
        DOMSignContext domSignContext = new DOMSignContext(key, parentElement);
        domSignContext.setDefaultNamespacePrefix("ds");
        domSignContext.setURIDereferencer(new CustomURIDereferencer(map));
        XMLSignature xmlSignature = xmlSignatureFactory.newXMLSignature(signedInfo, keyInfo);
        xmlSignature.sign(domSignContext);

        //LOGGER.debug("{}", toString(document));

        File uitvoer = new File(map, UITVOERBESTAND);
        PrintWriter out = new PrintWriter(uitvoer);
        out.print(documentToString(document));
        out.close();
    }

    public boolean controleerIntegriteit(File map, String wachtwoord) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException, ParserConfigurationException, SAXException, MarshalException, XMLSignatureException {
        SecretKey key = maakSleutel(wachtwoord, ZOUT);

        File signatureFile = new File(map, UITVOERBESTAND);
        if (!signatureFile.exists()) {
            throw new FileNotFoundException(String.format("Bestand met handtekening (%s) niet gevonden!", UITVOERBESTAND));
        }

        // load signed DOM document
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(signatureFile);


        // locate XML signature element
        NodeList signatureNodeList = document.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
        if (signatureNodeList.getLength() != 1) {
            throw new SecurityException("Ongeldig aantal Signature nodes");
        }
        Element signatureElement = (Element) signatureNodeList.item(0);

        // validate the XML signature
        MyKeySelector keySelector = new MyKeySelector(key);
        DOMValidateContext domValidateContext = new DOMValidateContext(keySelector, signatureElement);
        domValidateContext.setURIDereferencer(new CustomURIDereferencer(map));
        XMLSignatureFactory xmlSignatureFactory = XMLSignatureFactory.getInstance("DOM");
        XMLSignature xmlSignature = xmlSignatureFactory.unmarshalXMLSignature(domValidateContext);

        //alle referenties uit signature file halen
        SignedInfo signedInfo = xmlSignature.getSignedInfo();
        List<Reference> references = signedInfo.getReferences();
        List<String> refs = new ArrayList<>(); //was List<File>, maar dan werkt containsAll het niet correct

        //referentielijst omzetten naar lijst met bestanden
        for (Reference reference : references) {
            refs.add(reference.getURI());
        }

        //filter opdat integriteitsbestand zelf niet wordt opgenomen
        final FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.equals(UITVOERBESTAND))
                    return false;
                return true;
            }
        };

        //alle bestanden in map oplijsten
        File[] files = map.listFiles(filter);
        List<String> bestanden = new ArrayList<>();

        for(File file : files)
        {
            bestanden.add(file.getName());
        }

        //check of alle bestanden opgenomen zijn in referenties
        if(refs.containsAll(bestanden))
        {
            return xmlSignature.validate(domValidateContext);
        } else {
            return false;
        }
    }

    public static SecretKey maakSleutel(String wachtwoord, String zout) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec keySpec = new PBEKeySpec(wachtwoord.toCharArray(), zout.getBytes(), 1000, 128);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return keyFactory.generateSecret(keySpec);
    }

    private static String documentToString(Node node) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(node), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

}

