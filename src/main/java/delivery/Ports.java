package delivery;

import myBitCorn.bc;

/**
 * 类名称: Ports
 * 功能描述:
 * 日期:  2018/10/19 13:48
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class Ports {

    // 接受公钥端口号
    public static int KeyServerPortBase = 4710;
    // 接受未验证的块端口号
    public static int UnverifiedBlockServerPortBase = 4820;
    // 接受更新的区块链
    public static int BlockchainServerPortBase = 4930;

    public static int KeyServerPort;
    public static int UnverifiedBlockServerPort;
    public static int BlockchainServerPort;

    public static void setPorts(int PID) {
        KeyServerPort = KeyServerPortBase + (PID * 1000);
        UnverifiedBlockServerPort = UnverifiedBlockServerPortBase + (PID * 1000);
        BlockchainServerPort = BlockchainServerPortBase + (PID * 1000);
    }
}
