package delivery;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.util.*;
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

    public static String BLOCK_INPUT = "E:\\code\\example\\src\\main\\java\\delivery\\BlockInput%s.txt" ;

    public static KeyPair currentKeyPair;

    private static List<PIDKey> pidKeyList = Collections.synchronizedList (new ArrayList<> ());

    private static BlockingQueue<BlockRecord> NON_VERIFY_QUEUE = new PriorityBlockingQueue<> ();

    private static LinkedList<BlockRecord> blockChain = new LinkedList<> ();



//    public static void main(String[] args) throws Exception {
//
//        BlockRecordPriorityComparator comparator = new BlockRecordPriorityComparator ();
//        NON_VERIFY_QUEUE = new PriorityBlockingQueue<BlockRecord> (20,comparator);
//
//        int q_len = 6;
//        // 校验参数
//        if (args.length == 1) {
//            PID = Integer.valueOf (args[0]);
//        } else {
//            throw new IllegalArgumentException ("args error");
//        }
//
//        System.out.println("Clark Elliott's BlockFramework control-c to quit.\n");
//        System.out.println("Using processID " + PID + "\n");
//
//
//        //初始化 blockinput filename
//        BLOCK_INPUT = String.format (BLOCK_INPUT, PID);
//        // 初始化blockChain
//        BlockRecord firstRecord = new BlockRecord ();
//        blockChain.add (firstRecord);
//
//        currentKeyPair = Utils.generateKeyPair (PID);
//        Ports.setPorts (PID);
//
//        // 开启一个服务监听PIDKEY
//        new Thread (() -> {
//            try {
//                System.out.println ("Starting Key Server input thread using ： " + Ports.KeyServerPort);
//                ServerSocket servsock = new ServerSocket (Ports.KeyServerPort,q_len);
//
//                while (true) {
//                    Socket sock = servsock.accept ();
//                    // 读取key
//                    new Thread (() -> {
//                        try {
//                            InputStream is = sock.getInputStream ();
//                            ObjectInputStream ois = new ObjectInputStream (is);
//                            PIDKey getKey = (PIDKey) ois.readObject ();
//                            pidKeyList.add (getKey);
//                            System.out.println ("get key :" + getKey.getPublicKey ());
//                            sock.close ();
//                        } catch (Exception e) {
//                            e.printStackTrace ();
//                        }
//                    }).start ();
//                }
//            } catch (IOException e) {
//                e.printStackTrace ();
//            }
//        }).start ();
//
//        // 启动UnverifiedBlockServer
//        new Thread (() -> {
//            try {
//                System.out.println ("Starting the Unverified Block Server input thread using " + Ports.UnverifiedBlockServerPort);
//                ServerSocket serverSocket = new ServerSocket (Ports.UnverifiedBlockServerPort,q_len);
//                while (true) {
//                    Socket socket = serverSocket.accept ();
//                    //写入 unverify queue
//                    new Thread (()->{
//                        try {
//                            BufferedReader in = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
//                            String data = in.readLine ();
//                            System.out.println ("Put in priority queue: " + data + "\n");
//                            NON_VERIFY_QUEUE.put (data);
//                            socket.close ();
//                        } catch (IOException e) {
//                            e.printStackTrace ();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace ();
//                        }
//                    }).start ();
//                }
//            } catch (Exception e) {
//                e.printStackTrace ();
//            }
//        }).start ();
//
//        // 启动 BlockChainServer
//        new Thread (() -> {
//            Socket sock;
//            System.out.println ("Starting the blockchain server input thread using " + Integer.toString (Ports.BlockchainServerPort));
//            try {
//                ServerSocket servsock = new ServerSocket (Ports.BlockchainServerPort,q_len);
//                while (true) {
//                    sock = servsock.accept ();
//                    try{
//                        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
//                        String data = "";
//                        String data2;
//                        while((data2 = in.readLine()) != null){
//                            data = data + data2;
//                        }
//                        System.out.println("         --NEW BLOCKCHAIN--\n" + data + "\n\n");
//                        // 得到blockchain后处理 TODO
//
//                        sock.close();
//                    } catch (IOException x){x.printStackTrace();}
//                }
//            } catch (IOException ioe) {
//                System.out.println (ioe);
//            }
//        }).start ();
//
//        // Wait for servers to start.
//        try{Thread.sleep(10000);}catch(Exception e){}
//
//        MultiSend();
//
//        // Wait for multicast to fill incoming queue for our example.
//        try{Thread.sleep(1000);}catch(Exception e){}
//
//        // Start consuming the queued-up unverified blocks
//        // 开始计算队列中未确认的block
//        new Thread(()->{
//            String data;
//            PrintStream toServer;
//            Socket sock;
//            String newblockchain;
//            String fakeVerifiedBlock;
//
//            System.out.println("Starting the Unverified Block Priority Queue Consumer thread.\n");
//            try{
//                while(true){
//
//
//                    //广播 出去 ,xml string的形式 TODO
//
//                }
//            }catch (Exception e) {System.out.println(e);}
//        }).start();
//    }


    public static void main(String[] args) throws Exception {
        BlockRecordPriorityComparator comparator = new BlockRecordPriorityComparator ();
        NON_VERIFY_QUEUE = new PriorityBlockingQueue<BlockRecord> (20,comparator);
        BlockRecord record = new BlockRecord ();
        record.setTimeStamp (System.currentTimeMillis ());
        record.setABlockID ("1");
        Thread.sleep (1000);
        BlockRecord record1 = new BlockRecord ();
        record1.setTimeStamp (System.currentTimeMillis ());
        record1.setABlockID ("2");
        NON_VERIFY_QUEUE.put (record1);
        NON_VERIFY_QUEUE.put (record);

        BlockRecord takeResult  = NON_VERIFY_QUEUE.take ();
        System.out.println (takeResult.getABlockID ());

    }

    // Multicast some data to each of the processes.
    public static void MultiSend() throws Exception {
        // Send our key to all servers.
        for (int i = 0; i < numProcesses; i++) {
            // 发送 pidKey
            PIDKey pidKey = new PIDKey ();
            pidKey.setPublicKey (Base64.getEncoder ().encodeToString (currentKeyPair.getPublic ().getEncoded ()));
            try {
                int port = Ports.KeyServerPortBase + (i*1000);
                Socket socket = new Socket ("127.0.0.1", port);
                OutputStream os = socket.getOutputStream ();
                ObjectOutputStream oos = new ObjectOutputStream (os);
                oos.writeObject (pidKey);
                os.flush ();
                socket.shutdownOutput ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
        Thread.sleep (1000); // wait for keys to settle, normally would wait for an ack

        //读取文件,并发送 Unverify Block
        try (BufferedReader br = new BufferedReader (new FileReader (BLOCK_INPUT))) {
            String inputLineStr;
            while ((inputLineStr = br.readLine ()) != null) {
                BlockRecord blockRecord = BlockRecord.createBlock (inputLineStr,String.valueOf (PID));
                //直接发送String字符串
                for (int i = 0; i < numProcesses; i++) {
                    int port = Ports.UnverifiedBlockServerPortBase + i*1000;
                    Socket socket = new Socket ("127.0.0.1", port);
                    OutputStream os = socket.getOutputStream ();
                    PrintWriter pw = new PrintWriter (os);
                    pw.write (inputLineStr);
                    pw.flush ();
                    socket.shutdownOutput ();
                    socket.close ();
                }
            }
        }

        //延迟
        Thread.sleep (1000);


    }

    // verify BlockRecord
    public static void verifyRecord( ) throws Exception {

        // verify work , 每个进程 读取不同的文件，并广播给所有进程
        BlockRecord blockRecord = NON_VERIFY_QUEUE.take();
        System.out.println("Consumer got unverified: " + blockRecord);

        // TODO 解决该难题
//        BlockPuzzleResult blockPuzzleResult = Utils.solvePuzzle (data);
        BlockPuzzleResult blockPuzzleResult = new BlockPuzzleResult ();

        blockRecord.setABlockID (blockPuzzleResult.getBlockId ());
        blockRecord.setASHA256String (blockPuzzleResult.getHashString ());
        // 加密的Hash
        byte[] signHashBytes = Utils.signData (blockPuzzleResult.getBytesHash (), currentKeyPair.getPrivate ());
        blockRecord.setASignedSHA256 (Base64.getEncoder ().encodeToString (signHashBytes));
        blockRecord.setAVerificationProcessID (String.valueOf (PID));
        blockRecord.setACreatingProcess (String.valueOf (PID));



        // 判断blockChain是否存在BlockID相同的区块
        for (BlockRecord br : blockChain) {
            if (br.getABlockID ().equals (blockRecord.getABlockID ())) {
                return;
            }
        }

    }

    /* Token indexes for input: */
    private static final int iFNAME = 0;
    private static final int iLNAME = 1;
    private static final int iDOB = 2;
    private static final int iSSNUM = 3;
    private static final int iDIAG = 4;
    private static final int iTREAT = 5;
    private static final int iRX = 6;
    // 根据输入的文本 按行 生成 BlockRecord
    public static BlockRecord createBlock(String text,String pid){
        BlockRecord blockRecord = new BlockRecord ();
        blockRecord.setACreatingProcess (pid);

        String suuid = UUID.randomUUID ().toString ();
        blockRecord.setABlockID(suuid);
        byte[] sigUuid = Utils.signData (suuid.getBytes (), currentKeyPair.getPrivate ());
        blockRecord.setSignedBlockID (Base64.getEncoder ().encodeToString (sigUuid));

        Date date = new Date();
        String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
        blockRecord.setTimeStampString (T1);
        blockRecord.setTimeStamp (date.getTime ());

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

}

class BlockRecordPriorityComparator implements Comparator<BlockRecord>{
    @Override
    public int compare(BlockRecord record1, BlockRecord record2) {

        if (record1.getTimeStamp () < record2.getTimeStamp ()) {
            return -1;
        } else if(record1.getTimeStamp () > record2.getTimeStamp ()){
            return 1;
        } else {
            return 0;
        }
    }
}
