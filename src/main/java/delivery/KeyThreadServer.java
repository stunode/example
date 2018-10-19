package delivery;

import javax.tools.Tool;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;

/**
 * 类名称: KeyThread
 * 功能描述:
 * 日期:  2018/10/19 14:35
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class KeyThreadServer implements Runnable {

    // 进程号
    private int PID;

    public int getPID() {
        return PID;
    }

    public void setPID(int PID) {
        this.PID = PID;
    }

    public KeyThreadServer(int PID) {
        this.PID = PID;
    }

    @Override
    public void run() {

        int q_len = 6;
        Socket sock;
        System.out.println("Starting Key Server input thread using " + PID);
        try{
            ServerSocket servsock = new ServerSocket(PID, q_len);
            while (true) {
                sock = servsock.accept();
                new PublicKeyWorker (sock,PID).start();
            }
        }catch (IOException ioe) {System.out.println(ioe);}

    }
}

class PublicKeyWorker extends Thread {
    Socket sock;
    int PID;
    PublicKeyWorker (Socket s,int pid) {
        sock = s;
        this.PID = pid;
    }
    @Override
    public void run(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader (sock.getInputStream()));
            String data = in.readLine ();
            System.out.println("Got key: " + data);
//            Blockchain.keyContainer.put (PID, data);
            sock.close();
        } catch (IOException x){
            x.printStackTrace();
        }
    }
}
