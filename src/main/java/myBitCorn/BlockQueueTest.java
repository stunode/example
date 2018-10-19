package myBitCorn;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 类名称: BlockQueueTest
 * 功能描述:
 * 日期:  2018/10/18 22:59
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class BlockQueueTest {

    public static void main(String[] args) throws Exception {

        BlockingQueue<String> queue = new PriorityBlockingQueue<> ();
        queue.put ("aa");
        for (int i = 0; i < 10; i++) {
            String temp = queue.take ();
            System.out.println (temp);
        }

    }
}
