package delivery;

/**
 * 类名称: BlockPuzzleResult
 * 功能描述:
 * 日期:  2018/10/19 18:23
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class BlockPuzzleResult {

    private String randString;

    private byte[] bytesHash;

    // 经过base64编码 的hash值
    private String hashString;

    private String signHashString;

    public String getSignHashString() {
        return signHashString;
    }

    public void setSignHashString(String signHashString) {
        this.signHashString = signHashString;
    }

    private String blockId;

    public String getBlockId() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public String getRandString() {
        return randString;
    }

    public void setRandString(String randString) {
        this.randString = randString;
    }

    public byte[] getBytesHash() {
        return bytesHash;
    }

    public void setBytesHash(byte[] bytesHash) {
        this.bytesHash = bytesHash;
    }

    public String getHashString() {
        return hashString;
    }

    public void setHashString(String hashString) {
        this.hashString = hashString;
    }
}
