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
import java.util.LinkedList;
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
    String SignedBlockID;
    String Seed;
    String TimeStampString;
    long TimeStamp;
    Integer blockNum;


    String PreviousHash;
    String Fname;
    String Lname;
    String SSNum;
    String DOB;
    String Diag;
    String Treat;
    String Rx;


    public Integer getBlockNum() {
        return blockNum;
    }

    @XmlElement
    public void setBlockNum(Integer blockNum) {
        this.blockNum = blockNum;
    }

    public String getPreviousHash() {
        return PreviousHash;
    }

    @XmlElement
    public void setPreviousHash(String previousHash) {
        PreviousHash = previousHash;
    }

    @XmlElement
    public String getTimeStampString() {
        return TimeStampString;
    }

    public void setTimeStampString(String timeStampString) {
        TimeStampString = timeStampString;
    }

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

    public String getSHA256String() {
        return SHA256String;
    }

    @XmlElement
    public void setSHA256String(String SH) {
        this.SHA256String = SH;
    }

    public String getSignedSHA256() {
        return SignedSHA256;
    }

    @XmlElement
    public void setSignedSHA256(String SH) {
        this.SignedSHA256 = SH;
    }

    public String getCreatingProcess() {
        return CreatingProcess;
    }

    @XmlElement
    public void setCreatingProcess(String CP) {
        this.CreatingProcess = CP;
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

    public String getDOB() {
        return DOB;
    }

    @XmlElement
    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getDiag() {
        return Diag;
    }

    @XmlElement
    public void setDiag(String D) {
        this.Diag = D;
    }

    public String getTreat() {
        return Treat;
    }

    @XmlElement
    public void setTreat(String D) {
        this.Treat = D;
    }

    public String getRx() {
        return Rx;
    }

    @XmlElement
    public void setRx(String D) {
        this.Rx = D;
    }

    @Override
    public String toString() {
        return "BlockRecord{" +
                "SHA256String='" + SHA256String + '\'' +
                ", SignedSHA256='" + SignedSHA256 + '\'' +
                ", BlockID='" + BlockID + '\'' +
                ", VerificationProcessID='" + VerificationProcessID + '\'' +
                ", CreatingProcess='" + CreatingProcess + '\'' +
                ", PreviousHash='" + PreviousHash + '\'' +
                ", Fname='" + Fname + '\'' +
                ", Lname='" + Lname + '\'' +
                ", SSNum='" + SSNum + '\'' +
                ", DOB='" + DOB + '\'' +
                ", Diag='" + Diag + '\'' +
                ", Treat='" + Treat + '\'' +
                ", Rx='" + Rx + '\'' +
                ", SignedBlockID='" + SignedBlockID + '\'' +
                ", Seed='" + Seed + '\'' +
                ", TimeStampString='" + TimeStampString + '\'' +
                ", TimeStamp=" + TimeStamp +
                '}';
    }

    /* Token indexes for input: */
    private static final int iFNAME = 0;
    private static final int iLNAME = 1;
    private static final int iDOB = 2;
    private static final int iSSNUM = 3;
    private static final int iDIAG = 4;
    private static final int iTREAT = 5;
    private static final int iRX = 6;
    public static String XMLHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";


    // blockRecord 转换 stirng
    public static String converRecord2XmlStr(BlockRecord record) {
        StringWriter sw = new StringWriter ();
        JAXBContext jaxbContext = null;
        String result = null;
        try {
            jaxbContext = JAXBContext.newInstance (BlockRecord.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal (record, sw);
            result = sw.toString ();
            result = result.replace (XMLHeader, "");
            System.out.println (result);
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {
            return result;
        }
    }

    /**
     * 转为list
     *
     * @param data
     * @return
     */
    public static LinkedList<BlockRecord> convert2BlockChain(String data) {

        LinkedList<BlockRecord> blockChainList = new LinkedList<> ();
        String[] records = data.split ("</blockRecord>");
        for (int i = 0; i < records.length; i++) {
            String temp = records[i];
            temp = temp + "</blockRecord>";
            BlockRecord record = convertFromXML (temp, BlockRecord.class);
            System.out.println (record);
            blockChainList.add (record);
        }
        return blockChainList;

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
        xml = xml.replaceAll (" +", "");
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
