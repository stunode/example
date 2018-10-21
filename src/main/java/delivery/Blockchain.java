package delivery;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;


public class Blockchain {

    public static final String localhost = "localhost";
    // get package path
    public static final String filePathPrefix = MyWebServer.class.getResource ("").getPath ();
    // ledgerFile path
    public static final String ledgerFilePath = filePathPrefix + "BlockchainLedger.xml";

    // process num
    public static final int numProcesses = 3;
    // current process num
    public static int PID = 0;

    // input unverified blocks file
    public static String BLOCK_INPUT = filePathPrefix + "BlockInput%s.txt";

    // current proccess keyPair
    public static KeyPair currentKeyPair;

    // process pidkey list
    private static List<PIDKey> pidKeyList = Collections.synchronizedList (new ArrayList<> ());

    private static BlockingQueue<BlockRecord> NON_VERIFY_QUEUE = new PriorityBlockingQueue<> ();

    private static LinkedList<BlockRecord> blockChain = new LinkedList<> ();


    public static void main(String[] args) throws Exception {

        BlockRecordPriorityComparator comparator = new BlockRecordPriorityComparator ();

        NON_VERIFY_QUEUE = new PriorityBlockingQueue<BlockRecord> (20, comparator);

        int q_len = 6;
        // Calibration  args parameters
        if (args.length == 1) {
            PID = Integer.valueOf (args[0]);
        } else {
            throw new IllegalArgumentException ("args error");
        }

        System.out.println ("Clark Elliott's BlockFramework control-c to quit.\n");
        System.out.println ("Using processID " + PID + "\n");


        //initial blockinput filename
        BLOCK_INPUT = String.format (BLOCK_INPUT, PID);
        //  initial one-block (dummy entry) form of the blockchain
        BlockRecord firstRecord = getFristBlock ();
        blockChain.add (firstRecord);

        // get current process KeyPair
        currentKeyPair = Utils.generateKeyPair (PID);
        Ports.setPorts (PID);

        // Accept console input and output results
        new Thread (() -> {
            while (true) {
                Scanner ourInput = new Scanner (System.in);
                String stringIn = ourInput.nextLine ();
                consoleOut (stringIn);

            }
        }).start ();

        Thread.sleep (1000);

        // Open a service listening public key
        new Thread (() -> {
            try {
                System.out.println ("Starting Key Server input thread using ： " + Ports.KeyServerPort);
                ServerSocket servsock = new ServerSocket (Ports.KeyServerPort, q_len);

                while (true) {
                    Socket sock = servsock.accept ();
                    // read publicKey from other process
                    new Thread (() -> {
                        try {
                            InputStream is = sock.getInputStream ();
                            ObjectInputStream ois = new ObjectInputStream (is);
                            PIDKey getKey = (PIDKey) ois.readObject ();
                            pidKeyList.add (getKey);
                            System.out.println ("Get a publickey :" + getKey.getPublicKey () + " from process " + getKey.getPID () + "\n");
                            sock.close ();
                        } catch (Exception e) {
                            e.printStackTrace ();
                        }
                    }).start ();
                }
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }).start ();

        // open UnverifiedBlockServer for listening unverified blocks from other process
        new Thread (() -> {
            try {
                System.out.println ("Starting the Unverified Block Server input thread using " + Ports.UnverifiedBlockServerPort);
                ServerSocket serverSocket = new ServerSocket (Ports.UnverifiedBlockServerPort, q_len);
                while (true) {
                    Socket socket = serverSocket.accept ();
                    //write into NON_VERIFY_QUEUE
                    new Thread (() -> {
                        try {
                            BufferedReader in = new BufferedReader (new InputStreamReader (socket.getInputStream ()));
                            StringBuilder recordXml = new StringBuilder ();
                            String temp = "";
                            while ((temp = in.readLine ()) != null) {
                                recordXml.append (temp);
                            }
                            String data = recordXml.toString ();
                            BlockRecord record = BlockRecord.convertFromXML (data, BlockRecord.class);
                            System.out.println ("Put in priority queue: " + data + "\n");
                            NON_VERIFY_QUEUE.put (record);
                            socket.close ();
                        } catch (IOException e) {
                            e.printStackTrace ();
                        } catch (InterruptedException e) {
                            e.printStackTrace ();
                        }
                    }).start ();
                }
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }).start ();

        // start BlockChainServer for accept blockChain created by other process
        new Thread (() -> {
            System.out.println ("Starting the blockchain server input thread using " + Integer.toString (Ports.BlockchainServerPort));
            try {
                ServerSocket servsock = new ServerSocket (Ports.BlockchainServerPort, q_len);
                while (true) {
                    Socket sock = servsock.accept ();
                    new Thread (() -> {
                        try {
                            BufferedReader in = new BufferedReader (new InputStreamReader (sock.getInputStream ()));
                            String data = "";
                            String data2;
                            while ((data2 = in.readLine ()) != null) {
                                data = data + data2;
                            }
                            System.out.println (" Get a new blockchain : " + data + "\n");

                            // handler blockchain data from other process
                            blockChain = BlockRecord.convert2BlockChain (data);
                            sock.close ();
                            if (PID == 0) {
                                // if current process num is 0 , write into BlockchainLedger.xml file
                                System.out.println ("Write new blockchain into file :" + data + "\n");
                                writeBlockchainLedger (data);
                            }
                        } catch (IOException x) {
                            x.printStackTrace ();
                        }
                    }).start ();
                }
            } catch (IOException ioe) {
                System.out.println (ioe);
            }
        }).start ();

        // Wait for servers to start.
        try {
            Thread.sleep (5000);
        } catch (Exception e) {
        }

        MultiSend ();

        // Wait for multicast to fill incoming queue for our example.
        try {
            Thread.sleep (10000);
        } catch (Exception e) {
        }

        // Start consuming the queued-up unverified blocks
        new Thread (() -> {
            BlockRecord data;

            System.out.println ("Starting the Unverified Block Priority Queue Consumer thread.\n");

            while (true) {
                try {
                    data = NON_VERIFY_QUEUE.take ();
                    System.out.println ("Consumer got unverified: " + data);
                    // Solved the block puzzle and send notifications to other processes
                    solvePuzzle (data);
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }

            }

        }).start ();
    }

    // Multicast some data to each of the processes.
    public static void MultiSend() throws Exception {
        // Send our key to all servers.
        for (int i = 0; i < numProcesses; i++) {
            // assemble pidKey for multicast
            PIDKey pidKey = new PIDKey ();
            pidKey.setPID (PID);
            pidKey.setPublicKey (Base64.getEncoder ().encodeToString (currentKeyPair.getPublic ().getEncoded ()));
            try {
                int port = Ports.KeyServerPortBase + (i * 1000);
                Socket socket = new Socket (localhost, port);
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

        // read unverified blocks form input file , and multicast
        try (BufferedReader br = new BufferedReader (new FileReader (BLOCK_INPUT))) {
            String inputLineStr;
            while ((inputLineStr = br.readLine ()) != null) {
                BlockRecord blockRecord = createBlock (inputLineStr, String.valueOf (PID));
                //  marshald as XML and multicast to all processes
                String blockXml = BlockRecord.converRecord2XmlStr (blockRecord);
                for (int i = 0; i < numProcesses; i++) {
                    int port = Ports.UnverifiedBlockServerPortBase + i * 1000;
                    Socket socket = new Socket (localhost, port);
                    OutputStream os = socket.getOutputStream ();
                    PrintWriter pw = new PrintWriter (os);
                    pw.write (blockXml);
                    pw.flush ();
                    socket.shutdownOutput ();
                    socket.close ();
                }
            }
        }

    }

    // generate a fake first block record for blockChain
    private static BlockRecord getFristBlock(){
        BlockRecord firstRecord = new BlockRecord ();
        firstRecord.setSHA256String ("abcdef");
        firstRecord.setBlockID ("123");
        firstRecord.setBlockNum (1);
        firstRecord.setDiag ("test");
        firstRecord.setFname ("test");
        firstRecord.setLname ("test");
        firstRecord.setTreat ("test");
        firstRecord.setRx ("test");
        firstRecord.setDOB ("test");
        firstRecord.setVerificationProcessID ("1");
        return firstRecord;
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
    public static BlockRecord createBlock(String text, String pid) {
        BlockRecord blockRecord = new BlockRecord ();
        blockRecord.setCreatingProcess (pid);

        // blockId and signBlockId
        String suuid = UUID.randomUUID ().toString ();
        blockRecord.setBlockID (suuid);
        byte[] sigUuid = Utils.signData (suuid.getBytes (), currentKeyPair.getPrivate ());
        blockRecord.setSignedBlockID (Base64.getEncoder ().encodeToString (sigUuid));

        // timestamp
        Date date = new Date ();
        String T1 = String.format ("%1$s %2$tF.%2$tT", "", date);
        blockRecord.setTimeStampString (T1);
        blockRecord.setTimeStamp (date.getTime ());

        // data
        String[] tokens = text.split (" +");
        blockRecord.setSSNum (tokens[iSSNUM]);
        blockRecord.setFname (tokens[iFNAME]);
        blockRecord.setLname (tokens[iLNAME]);
        blockRecord.setDOB (tokens[iDOB]);
        blockRecord.setDiag (tokens[iDIAG]);
        blockRecord.setTreat (tokens[iTREAT]);
        blockRecord.setRx (tokens[iRX]);
        return blockRecord;
    }

    // 判断Blockchain里是否包含相同的blokId
    private static boolean checkBRResolved(BlockRecord blockRecord) {
        for (BlockRecord chainRecord : blockChain) {
            if (chainRecord.getBlockID ().equals (blockRecord.getBlockID ())) {
                return true;
            }
        }
        return false;
    }

    // 控制台输入 V 判断blockchain have been verified
    private static boolean verifyBlockChain(BlockRecord blockRecord) throws Exception {
        if(blockRecord.getBlockNum ()==1){
            return true;
        }
        String UB = "";
        // concrete UB
        String updateData = "" + blockRecord.getFname ()
                + blockRecord.getLname ()
                + blockRecord.getSSNum ()
                + blockRecord.getDOB ()
                + blockRecord.getDiag ()
                + blockRecord.getRx ()
                + blockRecord.getTreat ();
        UB = UB.concat (blockRecord.getPreviousHash ()).concat (updateData).concat (blockRecord.getSeed ());
        MessageDigest MD = MessageDigest.getInstance ("SHA-256");
        byte[] bytesHash = MD.digest (UB.getBytes ("UTF-8")); // Get the hash value
        String verifyHashString = DatatypeConverter.printHexBinary (bytesHash); // Turn into a string of hex values
        if(blockRecord.getSHA256String ().equals (verifyHashString)){
            return true;
        }
        return false;
    }

    // 判断 前16bits 小于5000时，认为解出难题
    public static void solvePuzzle(BlockRecord blockRecord) {

        PrintStream toServer;
        int initialBlockChainSize = blockChain.size ();

        // 如果已经被解决，放弃
        if (checkBRResolved (blockRecord)) {
            return;
        }

        // 验证signed blockId
        String signBlockId = blockRecord.getSignedBlockID ();
        String blockID = blockRecord.getBlockID ();
        String creatingProcess = blockRecord.getCreatingProcess ();
        // creater publicKey
        String createPIDpublicKey = "";
        PublicKey createKey = null;
        boolean result = false;
        for (PIDKey pidKey : pidKeyList) {
            if (pidKey.getPID () == Integer.valueOf (creatingProcess)) {
                createPIDpublicKey = pidKey.getPublicKey ();
                createKey = Utils.getPublicKey (createPIDpublicKey);
                break;
            }
        }
        if (createKey != null) {
            try {
                result = Utils.verifySig (blockID.getBytes (), createKey, Base64.getDecoder ().decode (signBlockId));
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }
        // 签名验证失败，不处理
        if (result == false) {
            return;
        }
        // 设置blockNum
        blockRecord.setBlockNum (blockChain.size () + 1);
        blockRecord.setVerificationProcessID (String.valueOf (PID));

        // concrete UB
        String updateData = "" + blockRecord.getFname ()
                + blockRecord.getLname ()
                + blockRecord.getSSNum ()
                + blockRecord.getDOB ()
                + blockRecord.getDiag ()
                + blockRecord.getRx ()
                + blockRecord.getTreat ();
        String preSHA256String = blockChain.getFirst ().getSHA256String ();
        String subUB = preSHA256String.concat (updateData);
        String UB = "";
        try {
            MessageDigest MD = MessageDigest.getInstance ("SHA-256");
            while (true) {
                String seed = Utils.randomAlphaNumeric (8);
                UB = subUB + seed;
                byte[] bytesHash = MD.digest (UB.getBytes ("UTF-8")); // Get the hash value
                String hashString = DatatypeConverter.printHexBinary (bytesHash); // Turn into a string of hex values
                int workNumber = Integer.parseInt (hashString.substring (0, 4), 16); // Between 0000 (0) and FFFF (65535)
                System.out.println ("Hash is: " + hashString);
                System.out.println ("First 16 bits " + hashString.substring (0, 4) + ": " + workNumber + "\n");
                if (workNumber < 5000) {
                    System.out.println ("Puzzle solved!");
                    System.out.println ("The seed was: " + seed);
                    // 解出难题，添加信息 给blockRecord
                    blockRecord.setSHA256String (hashString);
                    blockRecord.setSeed (seed);
                    byte[] signHashByte = Utils.signData (hashString.getBytes (), currentKeyPair.getPrivate ());
                    String signHashStr = Base64.getEncoder ().encodeToString (signHashByte);
                    blockRecord.setSignedSHA256 (signHashStr);
                    // 添加preHash
                    blockRecord.setPreviousHash (preSHA256String);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace ();
        }
        if (initialBlockChainSize == blockChain.size () && !checkBRResolved (blockRecord)) {
            // prepend 添加到blockChain  发送
            StringBuilder sb = new StringBuilder ();
            blockChain.addFirst (blockRecord);
            for (BlockRecord record : blockChain) {
                String blockXmlStr = BlockRecord.converRecord2XmlStr (record);
                sb.append (blockXmlStr);
            }
            // send to each process in group, including us:
            for (int i = 0; i < numProcesses; i++) {
                Socket socket = null;
                try {
                    socket = new Socket (localhost, Ports.BlockchainServerPortBase + (i * 1000));
                    toServer = new PrintStream (socket.getOutputStream ());
                    toServer.println (sb.toString ());
                    toServer.flush (); // make the multicast
                    socket.close ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }
            try {
                // 等待 blockChain 收到消息
                Thread.sleep (5000);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        } else {
            solvePuzzle (blockRecord);
        }
    }

    /**
     * write message into stream-file
     *
     * @param message
     */
    static void writeBlockchainLedger(String message) {
        new Thread (() -> {
            String ledger = BlockRecord.XMLHeader + "\n<BlockLedger>\n" + message + "\n</BlockLedger>\n";

            PrintWriter pfp = null;
            FileWriter fw = null;
            try {
                File ledgerFile = new File (ledgerFilePath);
                fw = new FileWriter (ledgerFile, false);
                pfp = new PrintWriter (fw);
                pfp.println (ledger);
                pfp.flush ();
            } catch (Exception e) {
                e.printStackTrace ();
            } finally {
                try {
                    fw.flush ();
                    pfp.close ();
                    fw.close ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }
        }).start ();
    }


    private static void consoleOut(String stringIn){
        String[] strin = new String[]{};
        if (stringIn.startsWith ("R")) {
            strin = stringIn.split (" +");
            if (strin.length > 1) {
                String filename = filePathPrefix + strin[1];
                int cnt = 0;
                InputStream is = null;
                try {
                    is = new BufferedInputStream (new FileInputStream (filename));
                    byte[] c = new byte[1024];
                    int readChars = 0;
                    while ((readChars = is.read (c)) != -1) {
                        for (int i = 0; i < readChars; ++i) {
                            if (c[i] == '\n') {
                                ++cnt;
                            }
                        }
                    }
                    System.out.println (String.valueOf (cnt) + " records have been added to unverified blocks. ");
                } catch (Exception e) {
                    e.printStackTrace ();
                    System.out.println (e.getMessage ());
                }
            }
        } else if (stringIn.startsWith ("L")) {
            StringBuilder sb = new StringBuilder ();
            for (BlockRecord record : blockChain) {
                sb.append (record.getBlockNum ()).append (". ").append (record.getTimeStampString ()).append (" ")
                        .append (record.getFname ()).append (" ").append (record.getDOB ()).append (" ")
                        .append (record.getSSNum ()).append (" ").append (record.getDiag ()).append (" ")
                        .append (record.getTreat ()).append (" ").append (record.getRx ()).append ("\n");
            }
            System.out.println (sb.toString ());
        } else if (stringIn.startsWith ("C")) {
            StringBuilder sb = new StringBuilder ();
            sb.append ("Verification credit:");
            blockChain.forEach ((block) -> {
                sb.append ("P");
                sb.append (block.getVerificationProcessID ());
                sb.append ("=");
                sb.append (block.getBlockNum ());
                sb.append (", ");
            });
            String out = sb.toString ().substring (0, sb.length () - 2);
            System.out.println (out);
        } else if (stringIn.startsWith ("V")) {
            //  a note that the error output:
            String out = "Blocks 1-" + blockChain.size () + " in the blockchain have been verified.";
            System.out.println (out);
        } else {
            return;
        }
    }

}

class BlockRecordPriorityComparator implements Comparator<BlockRecord> {
    @Override
    public int compare(BlockRecord record1, BlockRecord record2) {
        if (record1.getTimeStamp () < record2.getTimeStamp ()) {
            return -1;
        } else if (record1.getTimeStamp () > record2.getTimeStamp ()) {
            return 1;
        } else {
            return 0;
        }
    }
}
