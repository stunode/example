package executorservice;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 类名称: LinkedRunnableQueue
 * 功能描述: 线程池等待队列
 * 日期:  2018/10/23 18:21
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class LinkedRunnableQueue implements RunnableQueue {

    // 等待队列最大长度
    private final int limit;

    // 拒绝策略
    private DenyPolicy denyPolicy = new DenyPolicy.AbortDenyPolicy();

    // 存放runnable对象
    private final LinkedBlockingQueue<Runnable> container = new LinkedBlockingQueue<> ();

    public LinkedRunnableQueue(int limit, DenyPolicy denyPolicy) {
        this.limit = limit;
        this.denyPolicy = denyPolicy;
    }

    @Override
    public void offer(Runnable runnable) {
        container.add (runnable);
    }

    @Override
    public Runnable take() throws InterruptedException {
        return container.take ();
    }

    @Override
    public int size() {
        return container.size ();
    }
}
