package multithread.guardedSuspension;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 类名称: ClientThread
 * 功能描述:
 * 日期:  2018/10/16 22:43
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class ClientThread extends Thread {

    private final Random random;

    private final RequestQueue requestQueue;

    public ClientThread( RequestQueue requestQueue,String name,long seed) {
        super (name);
        this.random = new Random(seed);
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10000; i++) {
            Request request = new Request ("No: " + i);
            requestQueue.putRequest (request);
            try {
                TimeUnit.SECONDS.sleep (2);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
    }
}
