package executorservice;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * 类名称: ThreadTest
 * 功能描述:
 * 日期:  2018/10/17 9:41
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class InfiniteThreadTest {
    public static void main(String[] args) {
        int threadName = 0;
        while (true){
            new Thread (()->{
                try {
                    String[] strArray = new String[1000];
                    System.out.println (Thread.currentThread ().getName ());
                    TimeUnit.MINUTES.sleep (5);
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
            },"thread name : "+ threadName++).start ();
        }
    }
}
