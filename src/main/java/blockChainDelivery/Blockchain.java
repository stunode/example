import sun.misc.BASE64Decoder;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;


public class Blockchain {

    public static final String localhost = "localhost";
    // get package path
    public static final String filePathPrefix = Blockchain.class.getResource ("").getPath ();
//    public static final String filePathPrefix = "E:\\code\\example\\src\\main\\java\\delivery\\";

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

    private static  final  ThreadLocal<Boolean> trrigerFlag = new ThreadLocal<> ();
    public static void main(String[] args) throws Exception {


        trrigerFlag.set (false);
        BlockRecordPriorityComparator comparator = new BlockRecordPriorityComparator ();

        NON_VERIFY_QUEUE = new PriorityBlockingQueue<BlockRecord> (20, comparator);

        int q_len = 6;
        // Calibration  args parameters
        if (args.length == 1) {
            PID = Integer.valueOf (args[0]);
        } else {
            throw new IllegalArgumentException ("args error");
        }

        System.out.println ("BlockFramework control-c to quit.\n");
        System.out.println ("Using processID " + PID + "\n");

        if (PID == 2) {
            trrigerFlag.set (true);
        }

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
                System.out.println (stringIn);
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

                            // pid ==2 trriger
                            if(getKey.getPID ()==2){
                                trrigerFlag.set (true);
                            }

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

        // judge trigger flag
        while (trrigerFlag.get ()) {
            MultiSend ();
            break;
        }


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

    // generate blockRecord with BlockInput line by line
    public static BlockRecord createBlock(String text, String pid) {
        BlockRecord blockRecord = new BlockRecord ();
        blockRecord.setCreatingProcess (pid);

        // blockId and signBlockId
        String suuid = UUID.randomUUID ().toString ();
        blockRecord.setBlockID (suuid);
        byte[] sigUuid = Utils.signData (suuid.getBytes (), currentKeyPair.getPrivate ());
        blockRecord.setSignedBlockID (Base64.getEncoder ().encodeToString (sigUuid));

        // create timestamp
        Date date = new Date ();
        String T1 = String.format ("%1$s %2$tF.%2$tT", "", date);
        blockRecord.setTimeStampString (T1);
        blockRecord.setTimeStamp (date.getTime ());

        // set data from text for BlockRecord
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

    // verify blockchain whether already has some blockId
    private static boolean checkBRResolved(BlockRecord blockRecord) {
        for (BlockRecord chainRecord : blockChain) {
            if (chainRecord.getBlockID ().equals (blockRecord.getBlockID ())) {
                return true;
            }
        }
        return false;
    }

    // console input V then judge blockchain whether has been verified
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
        // Get the hash value
        byte[] bytesHash = MD.digest (UB.getBytes ("UTF-8"));
        // Turn into a string of hex values
        String verifyHashString = DatatypeConverter.printHexBinary (bytesHash);
        if(blockRecord.getSHA256String ().equals (verifyHashString)){
            return true;
        }
        return false;
    }

    // console v signature
    public static boolean verifySign(BlockRecord blockRecord){
        // verify signed blockId
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
        return result;
    }

    // if 16bits format number < 5000 , solved
    public static void solvePuzzle(BlockRecord blockRecord) {

        PrintStream toServer;
        int initialBlockChainSize = blockChain.size ();

        // If it's resolved, give up
        if (checkBRResolved (blockRecord)) {
            return;
        }

        // verify signed blockId
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
        // Signature verification failed, not handled
        if (result == false) {
            return;
        }
        // set blockNum
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
                    // solved，set sha256string to blockRecord
                    blockRecord.setSHA256String (hashString);
                    blockRecord.setSeed (seed);
                    byte[] signHashByte = Utils.signData (hashString.getBytes (), currentKeyPair.getPrivate ());
                    String signHashStr = Base64.getEncoder ().encodeToString (signHashByte);
                    blockRecord.setSignedSHA256 (signHashStr);
                    // add preHash
                    blockRecord.setPreviousHash (preSHA256String);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace ();
        }
        if (initialBlockChainSize == blockChain.size () && !checkBRResolved (blockRecord)) {
            // prepend to blockChain,  then multicate
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
                // wait for current blockChainServer accept message
                Thread.sleep (5000);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        } else {
            solvePuzzle (blockRecord);
        }
    }

    // write message into blockchainLedger.xml file
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


    // On each console, display output based on the commands you provide (such as C,R,V,L)
    private static void consoleOut(String stringIn){
        String[] strin = new String[]{};
        // if input command type is 'R'
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
        } else if (stringIn.startsWith ("L")) { // if input command type is 'L'
            StringBuilder sb = new StringBuilder ();
            for (BlockRecord record : blockChain) {
                sb.append (record.getBlockNum ()).append (". ").append (record.getTimeStampString ()).append (" ")
                        .append (record.getFname ()).append (" ").append (record.getDOB ()).append (" ")
                        .append (record.getSSNum ()).append (" ").append (record.getDiag ()).append (" ")
                        .append (record.getTreat ()).append (" ").append (record.getRx ()).append ("\n");
            }
            System.out.println (sb.toString ());
        } else if (stringIn.startsWith ("C")) { // if input command type is 'C'
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
        } else if (stringIn.startsWith ("V")) { // if input command type is 'V'
            String out = "";
            strin = stringIn.split (" +");
            if(strin.length == 1){
                for (BlockRecord blockRecord : blockChain) {
                    try {
                        boolean verifyResult = verifyBlockChain (blockRecord) && verifySign(blockRecord);
                        if(verifyResult != true){
                            out = "Blocks 1-" + String.valueOf (Integer.valueOf (blockRecord.getBlockNum ())-1) + " in the blockchain have been verified.\n";
                            out += "Block " + Integer.valueOf (blockRecord.getBlockNum ()) + " invalid.\n";
                            out += "Blocks " + String.valueOf (Integer.valueOf (blockRecord.getBlockNum ()) + 1) +"-"+ blockChain.size () + " follow an invalid block\n";
                            System.out.println (out);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace ();
                    }
                }
                out = "Blocks 1-" + blockChain.size () + " in the blockchain have been verified.\n";
                System.out.println (out);
                return;
            }
            if (strin.length > 1) {
                if("hash".equals (strin[1])){
                    for (BlockRecord blockRecord : blockChain) {
                        try {
                            boolean verifyResult = verifyBlockChain (blockRecord);
                            if(verifyResult != true){
                                out = "Blocks 1-" + String.valueOf (Integer.valueOf (blockRecord.getBlockNum ())-1) + " in the blockchain have been verified.\n";
                                out += "Block " + Integer.valueOf (blockRecord.getBlockNum ()) + " invalid: SHA256 hash does not match.\n";
                                out += "Blocks " + String.valueOf (Integer.valueOf (blockRecord.getBlockNum ()) + 1) +"-"+ blockChain.size () + " follow an invalid block\n";
                                System.out.println (out);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace ();
                        }
                    }
                    out = "Blocks 1-" + blockChain.size () + " in the blockchain have been verified.\n";
                    System.out.println (out);
                    return;
                } else if ("signature".equals (strin[1])){
                    for (BlockRecord blockRecord : blockChain) {
                        try {
                            boolean verifyResult = verifySign (blockRecord);
                            if(verifyResult != true){
                                out = "Blocks 1-" + String.valueOf (Integer.valueOf (blockRecord.getBlockNum ())-1) + " in the blockchain have been verified.\n";
                                out += "Block " + Integer.valueOf (blockRecord.getBlockNum ()) + " invalid: signature does not match the verifying process.\n";
                                out += "Blocks " + String.valueOf (Integer.valueOf (blockRecord.getBlockNum ()) + 1) +"-"+ blockChain.size () + " follow an invalid block\n";
                                System.out.println (out);
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace ();
                        }
                    }
                    out = "Blocks 1-" + blockChain.size () + " in the blockchain have been verified.\n";
                    System.out.println (out);
                    return;
                } else if ( "threshold".equals (strin[1])){
                    for (BlockRecord blockRecord : blockChain) {
                        int workNumber = Integer.parseInt (blockRecord.getSHA256String ().substring (0, 4), 16);
                        // if  workNumber > 5000 threshold error
                        if(workNumber>5000){
                            out = "Blocks 1-" + String.valueOf (Integer.valueOf (blockRecord.getBlockNum ())-1) + " in the blockchain have been verified.\n";
                            out += "Block " + Integer.valueOf (blockRecord.getBlockNum ()) + " SHA256 confirmed, but does not meet the work threshold\n";
                            out += "Blocks " + String.valueOf (Integer.valueOf (blockRecord.getBlockNum ()) + 1) +"-"+ blockChain.size () + " follow an invalid block\n";
                            System.out.println (out);
                            return;
                        }
                    }
                    out = "Blocks 1-" + blockChain.size () + " in the blockchain have been verified.\n";
                    System.out.println (out);
                    return;
                }
            }
        } else {
            return;
        }
    }

}

// a comparator for PriorityBlockingQueue, which is make PriorityBlockingQueue record sorted by timeStamp
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

// Ports will incremented by 1000 for each additional process added to the multicast group:
class Ports {

    public static int KeyServerPortBase = 4710;
    public static int UnverifiedBlockServerPortBase = 4820;
    public static int BlockchainServerPortBase = 4930;

    public static int KeyServerPort;
    public static int UnverifiedBlockServerPort;
    public static int BlockchainServerPort;

    // set process server port by PID
    public static void setPorts(int PID) {
        KeyServerPort = KeyServerPortBase + (PID * 1000);
        UnverifiedBlockServerPort = UnverifiedBlockServerPortBase + (PID * 1000);
        BlockchainServerPort = BlockchainServerPortBase + (PID * 1000);
    }
}

class PIDKey implements Serializable {

    private int PID;

    // base64 encoded string
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

@XmlRootElement
class BlockRecord {

    String SHA256String;
    String SignedSHA256;
    String BlockID;
    String VerificationProcessID;
    String CreatingProcess;
    String SignedBlockID;
    String Seed;
    String TimeStampString;
    long TimeStamp;
    Integer blockNum;


    String PreviousHash;
    String Fname;
    String Lname;
    String SSNum;
    String DOB;
    String Diag;
    String Treat;
    String Rx;


    public Integer getBlockNum() {
        return blockNum;
    }

    @XmlElement
    public void setBlockNum(Integer blockNum) {
        this.blockNum = blockNum;
    }

    public String getPreviousHash() {
        return PreviousHash;
    }

    @XmlElement
    public void setPreviousHash(String previousHash) {
        PreviousHash = previousHash;
    }

    @XmlElement
    public String getTimeStampString() {
        return TimeStampString;
    }

    public void setTimeStampString(String timeStampString) {
        TimeStampString = timeStampString;
    }

    public long getTimeStamp() {
        return TimeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        TimeStamp = timeStamp;
    }

    @XmlElement
    public String getSignedBlockID() {
        return SignedBlockID;
    }

    public void setSignedBlockID(String signedBlockID) {
        SignedBlockID = signedBlockID;
    }

    @XmlElement
    public String getSeed() {
        return Seed;
    }

    public void setSeed(String seed) {
        this.Seed = seed;
    }

    public String getSHA256String() {
        return SHA256String;
    }

    @XmlElement
    public void setSHA256String(String SH) {
        this.SHA256String = SH;
    }

    public String getSignedSHA256() {
        return SignedSHA256;
    }

    @XmlElement
    public void setSignedSHA256(String SH) {
        this.SignedSHA256 = SH;
    }

    public String getCreatingProcess() {
        return CreatingProcess;
    }

    @XmlElement
    public void setCreatingProcess(String CP) {
        this.CreatingProcess = CP;
    }

    public String getVerificationProcessID() {
        return VerificationProcessID;
    }

    @XmlElement
    public void setVerificationProcessID(String VID) {
        this.VerificationProcessID = VID;
    }

    public String getBlockID() {
        return BlockID;
    }

    @XmlElement
    public void setBlockID(String BID) {
        this.BlockID = BID;
    }

    public String getSSNum() {
        return SSNum;
    }

    @XmlElement
    public void setSSNum(String SS) {
        this.SSNum = SS;
    }

    public String getFname() {
        return Fname;
    }

    @XmlElement
    public void setFname(String FN) {
        this.Fname = FN;
    }

    public String getLname() {
        return Lname;
    }

    @XmlElement
    public void setLname(String LN) {
        this.Lname = LN;
    }

    public String getDOB() {
        return DOB;
    }

    @XmlElement
    public void setDOB(String DOB) {
        this.DOB = DOB;
    }

    public String getDiag() {
        return Diag;
    }

    @XmlElement
    public void setDiag(String D) {
        this.Diag = D;
    }

    public String getTreat() {
        return Treat;
    }

    @XmlElement
    public void setTreat(String D) {
        this.Treat = D;
    }

    public String getRx() {
        return Rx;
    }

    @XmlElement
    public void setRx(String D) {
        this.Rx = D;
    }

    @Override
    public String toString() {
        return "BlockRecord{" +
                "SHA256String='" + SHA256String + '\'' +
                ", SignedSHA256='" + SignedSHA256 + '\'' +
                ", BlockID='" + BlockID + '\'' +
                ", VerificationProcessID='" + VerificationProcessID + '\'' +
                ", CreatingProcess='" + CreatingProcess + '\'' +
                ", PreviousHash='" + PreviousHash + '\'' +
                ", Fname='" + Fname + '\'' +
                ", Lname='" + Lname + '\'' +
                ", SSNum='" + SSNum + '\'' +
                ", DOB='" + DOB + '\'' +
                ", Diag='" + Diag + '\'' +
                ", Treat='" + Treat + '\'' +
                ", Rx='" + Rx + '\'' +
                ", SignedBlockID='" + SignedBlockID + '\'' +
                ", Seed='" + Seed + '\'' +
                ", TimeStampString='" + TimeStampString + '\'' +
                ", TimeStamp=" + TimeStamp +
                '}';
    }

    /* Token indexes for input: */
    private static final int iFNAME = 0;
    private static final int iLNAME = 1;
    private static final int iDOB = 2;
    private static final int iSSNUM = 3;
    private static final int iDIAG = 4;
    private static final int iTREAT = 5;
    private static final int iRX = 6;
    public static String XMLHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>";


    // blockRecord 转换 stirng
    public static String converRecord2XmlStr(BlockRecord record) {
        StringWriter sw = new StringWriter ();
        JAXBContext jaxbContext = null;
        String result = null;
        try {
            jaxbContext = JAXBContext.newInstance (BlockRecord.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller ();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal (record, sw);
            result = sw.toString ();
            result = result.replace (XMLHeader, "");
            System.out.println (result);
        } catch (Exception e) {
            e.printStackTrace ();
        } finally {
            return result;
        }
    }

    /**
     * 转为list
     *
     * @param data
     * @return
     */
    public static LinkedList<BlockRecord> convert2BlockChain(String data) {

        LinkedList<BlockRecord> blockChainList = new LinkedList<> ();
        String[] records = data.split ("</blockRecord>");
        for (int i = 0; i < records.length; i++) {
            String temp = records[i];
            temp = temp + "</blockRecord>";
            BlockRecord record = convertFromXML (temp, BlockRecord.class);
            System.out.println (record);
            blockChainList.add (record);
        }
        return blockChainList;

    }

    // 获取block的sha256,转换字符串使用Base64加解密
    public static String getSHA256(String block) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance ("SHA-256");
            md.update (block.getBytes ());
            // base64转换为字符串
            return Base64.getEncoder ().encodeToString (md.digest ());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        }
        return null;
    }

    // xml 转换为javabean
    public static BlockRecord convertFromXML(String xml, Class cla) {
        xml = xml.replaceAll (" +", "");
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance (cla);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller ();
            StringReader reader = new StringReader (xml);
            return (BlockRecord) jaxbUnmarshaller.unmarshal (reader);
        } catch (JAXBException e) {
            System.out.println (e.getMessage ());
            return null;
        }
    }
}

class Utils {

    // 私钥签名
    public static byte[] signData(byte[] data, PrivateKey key)  {
        Signature signer = null;
        try {
            signer = Signature.getInstance ("SHA1withRSA");
            signer.initSign (key);
            signer.update (data);
            return (signer.sign ());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        } catch (InvalidKeyException e) {
            e.printStackTrace ();
        } catch (SignatureException e) {
            e.printStackTrace ();
        }
        return null;
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

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    // 64 编码的publicKey String转换为PublicKey对象
    public static PublicKey getPublicKey(String key) {
        try {
            byte[] keyBytes;
            keyBytes = (new BASE64Decoder ()).decodeBuffer(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec (keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch (IOException e) {
            e.printStackTrace ();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace ();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace ();
        }
        return null;
    }

}
