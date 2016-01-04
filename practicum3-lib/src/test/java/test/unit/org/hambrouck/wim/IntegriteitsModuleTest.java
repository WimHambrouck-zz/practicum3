package test.unit.org.hambrouck.wim;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hambrouck.wim.IntegriteitsModule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import sun.nio.ch.IOUtil;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.*;

/**
 * Created by Wim Hambrouck on 02/01/2016.
 */
public class IntegriteitsModuleTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntegriteitsModuleTest.class);
    public static final String WACHTWOORD = "correct horse battery stable"; //cfr: http://xkcd.com/936/

    @org.junit.Before
    public void setUp() throws Exception {

    }

    @org.junit.After
    public void tearDown() throws Exception {

    }

    @Test
    public void testMaakHandtekening() throws Exception {
        File tempDir = File.createTempFile("integrity-", "dir");
        tempDir.delete();
        tempDir.mkdir();

        LOGGER.debug(tempDir.getAbsolutePath());

        try {
            File test1File = new File(tempDir, "test.txt");
            File test2File = new File(tempDir, "test2.txt");
            FileUtils.writeStringToFile(test1File, "Dit is een testbestand.");
            FileUtils.writeStringToFile(test2File, "Dit is ook een testbestand.");
            try {
                IntegriteitsModule testedInstance = new IntegriteitsModule();

                testedInstance.maakHandtekening(tempDir, WACHTWOORD);

                File resultFile = new File(tempDir, IntegriteitsModule.UITVOERBESTAND);
                assertTrue(resultFile.exists());
            } finally {
                test1File.delete();
                test2File.delete();
            }
        } finally {
            //FileUtils.deleteDirectory(tempDir);
            tempDir.delete();
        }
    }

    @Test
    public void testMaakSleutel() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String wachtwoord = WACHTWOORD;
        String zout = "adrYq%|Sq6XLqa~kAYs=XV7n^blthYy4XztOTRLg-G5Vx^ReHcfl6MZr8uEl";
        PBEKeySpec keySpec = new PBEKeySpec(wachtwoord.toCharArray(), zout.getBytes(), 1000, 128);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        assertEquals(keyFactory.generateSecret(keySpec), IntegriteitsModule.maakSleutel(wachtwoord, zout));
    }


    @Test
    public void testControleerIntegriteit() throws Exception {
        File tempDir = File.createTempFile("integrity-", "dir");
        tempDir.delete();
        tempDir.mkdir();

        LOGGER.debug(tempDir.getAbsolutePath());

        try {
            File test1File = new File(tempDir, "test.txt");
            File test2File = new File(tempDir, "test2.txt");
            FileUtils.writeStringToFile(test1File, "Dit is een testbestand.");
            FileUtils.writeStringToFile(test2File, "Dit is ook een testbestand.");

            try {
                IntegriteitsModule testedInstance = new IntegriteitsModule();

                testedInstance.maakHandtekening(tempDir, WACHTWOORD); //cfr: http://xkcd.com/936/

                assertEquals(true, testedInstance.controleerIntegriteit(tempDir, WACHTWOORD));
            } finally {
                test1File.delete();
                test2File.delete();
            }
        } finally {
            //FileUtils.deleteDirectory(tempDir);
            tempDir.delete();
        }

    }

    @Test
    public void testControleerIntegriteitMetAangetasteIntegriteitBestandsinhoud() throws Exception {
        File tempDir = File.createTempFile("integrity-", "dir");
        tempDir.delete();
        tempDir.mkdir();

        LOGGER.debug(tempDir.getAbsolutePath());

        try {
            File test1File = new File(tempDir, "test.txt");
            File test2File = new File(tempDir, "test2.txt");
            FileUtils.writeStringToFile(test1File, "Dit is een testbestand.");
            FileUtils.writeStringToFile(test2File, "Dit is ook een testbestand.");

            try {
                IntegriteitsModule testedInstance = new IntegriteitsModule();



                testedInstance.maakHandtekening(tempDir, WACHTWOORD); //cfr: http://xkcd.com/936/

                FileUtils.writeStringToFile(test1File, "ER IS MEE GEFOEFELD!!!");

                assertEquals(false, testedInstance.controleerIntegriteit(tempDir, WACHTWOORD));
            } finally {
                test1File.delete();
                test2File.delete();
            }
        } finally {
            //FileUtils.deleteDirectory(tempDir);
            tempDir.delete();
        }
    }

    @Test
    public void testControleerIntegriteitMetAangetasteIntegriteitExtraBestand() throws Exception {
        File tempDir = File.createTempFile("integrity-", "dir");
        tempDir.delete();
        tempDir.mkdir();

        LOGGER.debug(tempDir.getAbsolutePath());

        try {
            File test1File = new File(tempDir, "test.txt");
            File test2File = new File(tempDir, "test2.txt");
            FileUtils.writeStringToFile(test1File, "Dit is een testbestand.");
            FileUtils.writeStringToFile(test2File, "Dit is ook een testbestand.");

            try {
                IntegriteitsModule testedInstance = new IntegriteitsModule();


                testedInstance.maakHandtekening(tempDir, WACHTWOORD); //cfr: http://xkcd.com/936/

                File testFile3 = new File(tempDir, "test3.txt");
                FileUtils.writeStringToFile(testFile3, "ER IS MEE GEFOEFELD!!!");

                assertEquals(false, testedInstance.controleerIntegriteit(tempDir, WACHTWOORD));
            } finally {
                test1File.delete();
                test2File.delete();
            }
        } finally {
            //FileUtils.deleteDirectory(tempDir);
            tempDir.delete();
        }
    }
}