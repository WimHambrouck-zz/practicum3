package org.hambrouck.wim;

import javax.xml.crypto.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Wim Hambrouck
 */
public class CustomURIDereferencer implements URIDereferencer {

    public final File tempDir;


    public CustomURIDereferencer(File tempDir) {
        this.tempDir = tempDir;
    }

    @Override
    public Data dereference(URIReference uriReference, XMLCryptoContext context) throws URIReferenceException {
        String uri = uriReference.getURI();

        File bestand = new File(tempDir, uri);


        if (uri.equals(bestand.getName())) {
            InputStream dataInputStream = null;
            try {
                dataInputStream = new FileInputStream(bestand);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return new OctetStreamData(dataInputStream);
        }

        return null;
    }
}
