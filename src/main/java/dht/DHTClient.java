package dht;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @Author Ryan
 * @Date 2018/10/29 22:39
 * @Function
 */
public class DHTClient {

    private static String localhost = "127.0.0.1";

    // 客户端发送信息
    public static void multiSend() throws Exception {
        Socket socket = new Socket(localhost, 8888);

        OutputStream os = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject("this is test");
        oos.flush();
        socket.shutdownOutput();
    }

    public static void main(String[] args) throws Exception {


        // 服务端
        new Thread(()-> {
            try {
                ServerSocket serverSocket = new ServerSocket(8888, 6);

                while (true) {

                    Socket socket = serverSocket.accept();
                    new Thread(()-> {
                        InputStream is = null;
                        try {
                            is = socket.getInputStream();
                            ObjectInputStream ois = new ObjectInputStream(is);
                            String temp = (String)ois.readObject();
                            System.out.println(temp);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        multiSend();

    }
}
