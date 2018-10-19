package delivery;

import java.security.*;

/**
 * 类名称: Utils
 * 功能描述:
 * 日期:  2018/10/19 14:09
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class Utils {

    // 私钥签名
    public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
        Signature signer = Signature.getInstance ("SHA1withRSA");
        signer.initSign (key);
        signer.update (data);
        return (signer.sign ());
    }

    // 公钥解密，用于确认发送者是不是同一个人，防止其他中间人发送的数据
    public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
        Signature signer = Signature.getInstance ("SHA1withRSA");
        signer.initVerify (key);
        signer.update (data);
        return (signer.verify (sig));
    }

    // 公私钥生成
    public static KeyPair generateKeyPair(long seed) throws Exception {
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance ("RSA");
        SecureRandom rng = SecureRandom.getInstance ("SHA1PRNG", "SUN");
        rng.setSeed (seed);
        keyGenerator.initialize (1024, rng);
        return (keyGenerator.generateKeyPair ());
    }

    public static void main(String[] args) throws Exception {

        KeyPair keyPair = generateKeyPair (9);
        PublicKey publicKey = keyPair.getPublic ();
        PrivateKey privateKey = keyPair.getPrivate ();
        String data = "abcdefg123456";
        byte[] datasbytes = data.getBytes ();
        byte[] signDataBytes = signData (datasbytes, privateKey);
        boolean verify = verifySig (datasbytes, publicKey, signDataBytes);
        System.out.println (verify);
    }
}
