package delivery;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;

/**
 * 类名称: FunctionTest
 * 功能描述:
 * 日期:  2018/10/19 15:14
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class FunctionTest {

    public static void main(String[] args) {

        new Thread (()->{

            // 发送 pidKey
            PIDKey pidKey = new PIDKey ();
            pidKey.setPID (0);
            pidKey.setPublicKey ("thisistest");
            try {
                Socket socket = new Socket ("127.0.0.1", 9999);
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(pidKey);
                os.flush();
                socket.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }).start ();
    }
}
