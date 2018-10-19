package delivery;

import java.io.Serializable;

/**
 * 类名称: PIDKey
 * 功能描述:
 * 日期:  2018/10/19 15:07
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class PIDKey implements Serializable {

    private int PID;

    // base64加密后的字符串
    private String publicKey;

    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
