package delivery;

import com.sun.security.ntlm.Server;

import javax.rmi.CORBA.Util;
import javax.sound.sampled.Port;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 类名称: Blockchain
 * 功能描述:
 * 日期:  2018/10/19 9:55
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class Blockchain {

    public static final int numProcesses = 3;
    public static final String serverName = "127.0.0.1";
    public static int PID = 0;

    public static KeyPair pidKeyPair;

    private static List<PIDKey> pidKeyList = Collections.synchronizedList (new ArrayList<> ());

    private static final BlockingQueue<String> NON_VERIFY_QUEUE = new PriorityBlockingQueue<> ();

    public static void main(String[] args) throws Exception {

//        String blockRecord = BlockRecord.blockChainFromSource ("E:\\code\\example\\src\\main\\java\\delivery\\BlockInput0.txt");
        // 进程号
        String publicKey;
        int PID ;
        int q_len = 6;

        // 校验参数
        if(args.length ==1 ){
            PID = Integer.valueOf (args[0]);
        } else {
            throw new IllegalArgumentException ("args error");
        }

        pidKeyPair = Utils.generateKeyPair (PID);
        Ports.setPorts (PID);

        // 开启一个服务监听PIDKEY
        new Thread (()->{
            try {
                ServerSocket servsock = new ServerSocket(Ports.KeyServerPort);
                System.out.println (" key server start at port ： " + Ports.KeyServerPort);

                while (true){
                    Socket sock = servsock.accept ();
                    // 读取key
                    new Thread (()->{
                        try {
                            InputStream is = sock.getInputStream();
                            ObjectInputStream ois=new ObjectInputStream(is);
                            PIDKey getKey = (PIDKey) ois.readObject ();
                            pidKeyList.add (getKey);
                            System.out.println ("get key :" + getKey.getPublicKey ());
                            sock.close();
                        } catch (Exception e) {
                            e.printStackTrace ();
                        }
                    }).start ();
                }
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }).start ();

        // 启动UnverifiedBlockServer
        new Thread (()->{
            try {
                ServerSocket serverSocket = new ServerSocket (Ports.UnverifiedBlockServerPort);
                while (true){
                    Socket socket = serverSocket.accept ();
                    System.out.println (" unverified block server start at port ： " + Ports.KeyServerPort);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String data = in.readLine ();
                    System.out.println("Put in unverified block priority queue: " + data + "\n");
                    NON_VERIFY_QUEUE.put(data);
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }).start ();

        // 如果pid=2 广播消息
        if (PID ==2) {
            MultiSend ();
        }
    }



    // Multicast some data to each of the processes.
    public static void MultiSend() throws Exception {
        Socket sock;
        PrintStream toServer;
        // Send our key to all servers.
        for (int i = 0; i < numProcesses; i++) {
            // 发送 pidKey
            PIDKey pidKey = new PIDKey ();
            pidKey.setPID (numProcesses);
            KeyPair keyPair = Utils.generateKeyPair (i);
            pidKey.setPublicKey (Base64.getEncoder ().encodeToString (keyPair.getPublic ().getEncoded ()));
            try {
                int port = Ports.KeyServerPortBase + i;
                Socket socket = new Socket ("127.0.0.1", port);
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                oos.writeObject(pidKey);
                os.flush();
                socket.shutdownOutput();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
        Thread.sleep (1000); // wait for keys to settle, normally would wait for an ack

        //读取Block
        Utils .


//        //Fancy arithmetic is just to generate identifiable blockIDs out of numerical sort order:
//        String fakeBlockA = "(Block#" + Integer.toString (((PID + 1) * 10) + 4) + " from P" + PID + ")";
//        String fakeBlockB = "(Block#" + Integer.toString (((PID + 1) * 10) + 3) + " from P" + PID + ")";
////            for(int i=0; i< numProcesses; i++){// Send a sample unverified block A to each server
////                sock = new Socket(serverName, Ports.UnverifiedBlockServerPortBase + (i * 1000));
//        sock = new Socket (serverName, Ports.UnverifiedBlockServerPortBase);
//
//        toServer = new PrintStream (sock.getOutputStream ());
//        toServer.println (fakeBlockA);
//        toServer.flush ();
//        sock.close ();
////            }
////            for(int i=0; i< numProcesses; i++){// Send a sample unverified block B to each server
////                sock = new Socket(serverName, Ports.UnverifiedBlockServerPortBase + (i * 1000));
//        sock = new Socket (serverName, Ports.UnverifiedBlockServerPortBase);
//
//        toServer = new PrintStream (sock.getOutputStream ());
//        toServer.println (fakeBlockB);
//        toServer.flush ();
//        sock.close ();

    }

}


