package delivery;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * 类名称: BlockRecord
 * 功能描述:
 * 日期:  2018/10/19 13:46
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
@XmlRootElement
public class BlockRecord {

    String SHA256String;
    String SignedSHA256;
    String BlockID;
    String VerificationProcessID;
    String CreatingProcess;
    String PreviousHash;
    String Fname;
    String Lname;
    String SSNum;
    String DOB;
    String Diag;
    String Treat;
    String Rx;

    String SignedBlockID;
    String Seed;
    String TimeStampString;
    long TimeStamp;

    @XmlElement
    public String getTimeStampString() {
        return TimeStampString;
    }

    public void setTimeStampString(String timeStampString) {
        TimeStampString = timeStampString;
    }

    @XmlElement
    public long getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        TimeStamp = timeStamp;
    }

    @XmlElement
    public String getSignedBlockID() {
        return SignedBlockID;
    }

    public void setSignedBlockID(String signedBlockID) {
        SignedBlockID = signedBlockID;
    }

    @XmlElement
    public String getSeed() {
        return Seed;
    }

    public void setSeed(String seed) {
        this.Seed = seed;
    }

    public String getASHA256String() {return SHA256String;}
    @XmlElement
    public void setASHA256String(String SH){this.SHA256String = SH;}

    public String getASignedSHA256() {return SignedSHA256;}
    @XmlElement
    public void setASignedSHA256(String SH){this.SignedSHA256 = SH;}

    public String getACreatingProcess() {return CreatingProcess;}
    @XmlElement
    public void setACreatingProcess(String CP){this.CreatingProcess = CP;}

    public String getAVerificationProcessID() {return VerificationProcessID;}
    @XmlElement
    public void setAVerificationProcessID(String VID){this.VerificationProcessID = VID;}

    public String getABlockID() {return BlockID;}
    @XmlElement
    public void setABlockID(String BID){this.BlockID = BID;}

    public String getFSSNum() {return SSNum;}
    @XmlElement
    public void setFSSNum(String SS){this.SSNum = SS;}

    public String getFFname() {return Fname;}
    @XmlElement
    public void setFFname(String FN){this.Fname = FN;}

    public String getFLname() {return Lname;}
    @XmlElement
    public void setFLname(String LN){this.Lname = LN;}

    public String getFDOB() {return DOB;}
    @XmlElement
    public void setFDOB(String DOB){this.DOB = DOB;}

    public String getGDiag() {return Diag;}
    @XmlElement
    public void setGDiag(String D){this.Diag = D;}

    public String getGTreat() {return Treat;}
    @XmlElement
    public void setGTreat(String D){this.Treat = D;}

    public String getGRx() {return Rx;}
    @XmlElement
    public void setGRx(String D){this.Rx = D;}

    /* Token indexes for input: */
    private static final int iFNAME = 0;
    private static final int iLNAME = 1;
    private static final int iDOB = 2;
    private static final int iSSNUM = 3;
    private static final int iDIAG = 4;
    private static final int iTREAT = 5;
    private static final int iRX = 6;
    private static String XMLHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";

    public static String blockChainFromSource(String filePath ) {

        // processID 默认端口为port
        int pnum = 0;

        try (BufferedReader br = new BufferedReader(new FileReader (filePath))) {
            String[] tokens = new String[10];
            String stringXML;
            String InputLineStr;
            String suuid;
            UUID idA;

            BlockRecord[] blockArray = new BlockRecord[20];

            JAXBContext jaxbContext = JAXBContext.newInstance(BlockRecord.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            StringWriter sw = new StringWriter();

            // CDE Make the output pretty printed:
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            int n = 0;

            while ((InputLineStr = br.readLine()) != null) {




                blockArray[n] = new BlockRecord ();

//                blockArray[n].setASHA256String("SHA string goes here...");
//                blockArray[n].setASignedSHA256("Signed SHA string goes here...");

                /* CDE: Generate a unique blockID. This would also be signed by creating process: */
                idA = UUID.randomUUID();
                suuid = new String(UUID.randomUUID().toString());
                blockArray[n].setABlockID(suuid);
                blockArray[n].setACreatingProcess("Process" + Integer.toString(pnum));
//                blockArray[n].setAVerificationProcessID("To be set later...");
                /* CDE put the file data into the block record: */
                InputLineStr.split (" +");
                tokens = InputLineStr.split(" +"); // Tokenize the input
                blockArray[n].setFSSNum(tokens[iSSNUM]);
                blockArray[n].setFFname(tokens[iFNAME]);
                blockArray[n].setFLname(tokens[iLNAME]);
                blockArray[n].setFDOB(tokens[iDOB]);
                blockArray[n].setGDiag(tokens[iDIAG]);
                blockArray[n].setGTreat(tokens[iTREAT]);
                blockArray[n].setGRx(tokens[iRX]);
                n++;
            }
            System.out.println(n + " records read.");
            System.out.println("Names from input:");
            for(int i=0; i < n; i++){
                System.out.println("  " + blockArray[i].getFFname() + " " +
                        blockArray[i].getFLname());
            }
            System.out.println("\n");

            stringXML = sw.toString();
            for(int i=0; i < n; i++){
                jaxbMarshaller.marshal(blockArray[i], sw);
            }
            String fullBlock = sw.toString();
            String XMLHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";
            String cleanBlock = fullBlock.replace(XMLHeader, "");
            // Show the string of concatenated, individual XML blocks:
            String XMLBlock = XMLHeader + "\n<BlockLedger>" + cleanBlock + "</BlockLedger>";
            System.out.println(XMLBlock);
            return XMLBlock;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // blockRecord 转换 stirng
    public static String converRecord2XmlStr(BlockRecord record) {
        StringWriter sw = new StringWriter ();
        JAXBContext jaxbContext = null;
        String result = null;
        try {
            jaxbContext = JAXBContext.newInstance(BlockRecord.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.marshal (record,sw);
            result = sw.toString ();
            result = result.replace(XMLHeader, "");
            System.out.println (result);
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {
            return result;
        }
    }

    public static void main(String[] args) {
        BlockRecord record = new BlockRecord ();
        record.setACreatingProcess ("test");
        String result = converRecord2XmlStr (record);
        System.out.println (result);
        BlockRecord record1 = convertFromXML (result, BlockRecord.class);
        System.out.println (record1);
    }



    // 根据输入的文本 按行 生成 BlockRecord
    public static BlockRecord createBlock(String text,String pid){
        BlockRecord blockRecord = new BlockRecord ();
        blockRecord.setACreatingProcess (pid);
        //TODO 私钥加密
        String suuid = UUID.randomUUID ().toString ();
        blockRecord.setABlockID(suuid);
        String[] tokens = text.split (" +");
        blockRecord.setFSSNum(tokens[iSSNUM]);
        blockRecord.setFFname(tokens[iFNAME]);
        blockRecord.setFLname(tokens[iLNAME]);
        blockRecord.setFDOB(tokens[iDOB]);
        blockRecord.setGDiag(tokens[iDIAG]);
        blockRecord.setGTreat(tokens[iTREAT]);
        blockRecord.setGRx(tokens[iRX]);
        return blockRecord;
    }

    // 获取block的sha256,转换字符串使用Base64加解密
    public static String getSHA256(String block) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance ("SHA-256");
            md.update (block.getBytes ());
            // base64转换为字符串
            return Base64.getEncoder ().encodeToString (md.digest ());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        }
        return null;
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
