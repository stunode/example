package myBitCorn;

/*

Version 1.0 2017-09-03

Author: Clark Elliott, with ample help from the below web sources.

You are free to use this code in your assignment, but you MUST add
your own comments. Leave in the web source references.

This is pedagogical code and should not be considered current for secure applications.

The web sources:

http://www.java2s.com/Code/Java/Security/SignatureSignAndVerify.htm
https://www.mkyong.com/java/java-digital-signatures-example/ (not so clear)
https://javadigest.wordpress.com/2012/08/26/rsa-encryption-example/
https://www.programcreek.com/java-api-examples/index.php?api=java.security.SecureRandom
https://www.mkyong.com/java/java-sha-hashing-example/
https://stackoverflow.com/questions/19818550/java-retrieve-the-actual-value-of-the-public-key-from-the-keypair-object

XML validator:
https://www.w3schools.com/xml/xml_validator.asp

XML / Object conversion:
https://www.mkyong.com/java/jaxb-hello-world-example/
*/

/* CDE: The JAXB libraries: */

import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.channels.ScatteringByteChannel;
import java.security.*;
import java.util.*;

/* CDE: The encryption needed for signing the hash: */
/* CDE Some other uitilities: */
// Produces a 64-bye string representing 256 bits of the hash output. 4 bits per character


@XmlRootElement
class BlockRecord {
    /* Examples of block fields: */
    String VerificationProcessID;
    String PreviousHash;
    String BlockID;
    String Fname;
    String Lname;
    String SSNum;
    String DOB;

    /* Examples of accessors for the BlockRecord fields: */
    public String getSSNum() {
        return SSNum;
    }

    @XmlElement
    public void setSSNum(String SS) {
        this.SSNum = SS;
    }

    public String getFname() {
        return Fname;
    }

    @XmlElement
    public void setFname(String FN) {
        this.Fname = FN;
    }

    public String getLname() {
        return Lname;
    }

    @XmlElement
    public void setLname(String LN) {
        this.Lname = LN;
    }

    public String getVerificationProcessID() {
        return VerificationProcessID;
    }

    @XmlElement
    public void setVerificationProcessID(String VID) {
        this.VerificationProcessID = VID;
    }

    public String getBlockID() {
        return BlockID;
    }

    @XmlElement
    public void setBlockID(String BID) {
        this.BlockID = BID;
    }

    // xml 转换为javabean
    public static BlockRecord convertFromXML(String xml, Class cla) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (cla);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller ();
            StringReader reader = new StringReader (xml);
            return (BlockRecord) jaxbUnmarshaller.unmarshal (reader);
        } catch (JAXBException e) {
            System.out.println (e.getMessage ());
            return null;
        }
    }
}

public class BlockH {

    public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
        Signature signer = Signature.getInstance ("SHA1withRSA");
        signer.initSign (key);
        signer.update (data);
        return (signer.sign ());
    }

    public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
        Signature signer = Signature.getInstance ("SHA1withRSA");
        signer.initVerify (key);
        signer.update (data);

        return (signer.verify (sig));
    }

    public static KeyPair generateKeyPair(long seed) throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance ("RSA");
        SecureRandom rng = SecureRandom.getInstance ("SHA1PRNG", "SUN");
        rng.setSeed (seed);
        keyGenerator.initialize (1024, rng);

        return (keyGenerator.generateKeyPair ());
    }

    // 获取block的sha256,转换字符串使用Base64加解密
    public static byte[] getSHA256(String blockRecord) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance ("SHA-256");
            md.update (blockRecord.getBytes ());
            return md.digest ();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        }
        return null;
    }

    // 获取前16位无符号整数值 ,返回值 -1表示block hash验证失败
    public static int getBlockHashValid(byte[] bytes) {

        if(bytes.length<15){
            throw new IllegalArgumentException (" bytes illegal ");
        }

        int byte1 = bytes[0] & 0x00ff;
        int byte2 = (bytes[1] & 0x00ff) << 8;

        if (byte1 + byte2 < 5000) {
            return byte1 + byte2;
        } else {
            return -1;
        }
    }

    public static String CSC435Block =
            "We will build this dynamically: <?xml version = \"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public static final String ALGORITHM = "RSA"; /* Name of encryption algorithm used */

    /* Header fields for the block: */
    public static String SignedSHA256;


    public static void main(String[] args) throws Exception {

//        byte[] sha256 = getSHA256 ("this is test");
//        System.out.println (sha256);
//
//        //base64加解密
//        String base64data = Base64.getEncoder ().encodeToString (sha256);
//        byte[] dataDecode = Base64.getDecoder ().decode (base64data);
//
//        int result = getBlockHashValid (dataDecode);
//        System.out.println (result);
        /* CDE: Process numbers and port numbers to be used: */
        int pnum = 4708;
        int UnverifiedBlockPort;
        int BlockChainPort;

        /* CDE: Example of generating a unique blockID. This would also be signed by creating process: */
        UUID idA = UUID.randomUUID ();
        String suuid = UUID.randomUUID ().toString ();
        System.out.println ("Unique Block ID: " + suuid + "\n");

        /* CDE For the timestamp in the block entry: */
        Date date = new Date ();
        //String T1 = String.format("%1$s %2$tF.%2$tT", "Timestamp:", date);
        String T1 = String.format ("%1$s %2$tF.%2$tT", "", date);
        String TimeStampString = T1 + "." + pnum + "\n"; // No timestamp collisions!
        System.out.println ("Timestamp: " + TimeStampString);

        /* CDE: Here is a way for us to simulate computational "work" */
        System.out.println ("How much work we did: ");
        int randval;
        Random r = new Random ();
        for (int i = 0; i < 1000; i++) { // safety upper limit of 1000
            Thread.sleep (100); // not really work, but OK for our purposes.
            randval = r.nextInt (100); // Higher val = more work
            if (randval < 4) {       // Lower threshold = more work
                System.out.println (i + " tenths of a second.\n");
                break;
            }
        }

        try {

            /* CDE put some data into the block record: */
            BlockRecord blockRecord = new BlockRecord ();
            blockRecord.setVerificationProcessID ("Process2");
            blockRecord.setBlockID (suuid);
            blockRecord.setSSNum ("123-45-6789");
            blockRecord.setFname ("Joseph");
            blockRecord.setLname ("Chang");

            /* The XML conversion tools: */
            JAXBContext jaxbContext = JAXBContext.newInstance (BlockRecord.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();
            StringWriter sw = new StringWriter ();

            // CDE Make the output pretty printed:
            jaxbMarshaller.setProperty (Marshaller.JAXB_FORMATTED_OUTPUT, true);

            /* CDE We marshal the block object into an XML string so it can be sent over the network: */
            jaxbMarshaller.marshal (blockRecord, sw);
            String stringXML = sw.toString ();
            CSC435Block = stringXML;

            /* Make the SHA-256 Digest of the block: */

            MessageDigest md = MessageDigest.getInstance ("SHA-256");
            md.update (CSC435Block.getBytes ());
            byte byteData[] = md.digest ();

            // CDE: Convert the byte[] to hex format. THIS IS NOT VERFIED CODE:
            StringBuffer sb = new StringBuffer ();
            for (int i = 0; i < byteData.length; i++) {
                sb.append (Integer.toString ((byteData[i] & 0xff) + 0x100, 16).substring (1));
            }

            String SHA256String = sb.toString ();

            KeyPair keyPair = generateKeyPair (999);

            byte[] digitalSignature = signData (SHA256String.getBytes (), keyPair.getPrivate ());

            boolean verified = verifySig (SHA256String.getBytes (), keyPair.getPublic (), digitalSignature);
            System.out.println ("Has the signature been verified: " + verified + "\n");

            System.out.println ("Original SHA256 Hash: " + SHA256String + "\n");

              /* Add the SHA256String to the header for the block. We turn the
             byte[] signature into a string so that it can be placed into
             the block, but also show how to return the string to a
             byte[], which you'll need if you want to use it later.
             Thanks Hugh Thomas for the fix! */
            //base64把数组转为string
            SignedSHA256 = Base64.getEncoder ().encodeToString (digitalSignature);
            System.out.println ("The signed SHA-256 string: " + SignedSHA256 + "\n");
            byte[] testSignature = Base64.getDecoder ().decode (SignedSHA256);
            System.out.println ("Testing restore of signature: " + Arrays.equals (testSignature, digitalSignature));
            verified = verifySig (SHA256String.getBytes (), keyPair.getPublic (), testSignature);
            System.out.println ("Has the restored signature been verified: " + verified + "\n");

            String fullBlock = stringXML.substring (0, stringXML.indexOf ("<blockID>")) +
                    "<SignedSHA256>" + SignedSHA256 + "</SignedSHA256>\n" +
                    "    <SHA256String>" + SHA256String + "</SHA256String>\n    " +
                    stringXML.substring (stringXML.indexOf ("<blockID>"));

            System.out.println (fullBlock); // Show what it looks like.

            /* CDE Here's how we put the XML back into java object form: */
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller ();
            StringReader reader = new StringReader (stringXML);
            BlockRecord blockRecord2 = (BlockRecord) jaxbUnmarshaller.unmarshal (reader);

            System.out.println ("SSNum: " + blockRecord2.getSSNum ()); // Show a piece of the new block object

        } catch (Exception e) {
            e.printStackTrace ();
        }
    }
}
