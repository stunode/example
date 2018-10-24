package executorservice;

/**
 * 类名称: RunnableTask
 * 功能描述:
 * 日期:  2018/10/24 10:53
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
// 从线程队列中取出Runnable对象 包装对象
public class RunnableTask implements Runnable {

    private final RunnableQueue runnableQueue;

    private volatile boolean running = true;

    public RunnableTask(RunnableQueue runnableQueue) {
        this.runnableQueue = runnableQueue;
    }

    @Override
    public void run() {

        // 循环获取等待队列中的runnable对象，并运行run()方法
        while( running && !Thread.currentThread ().isInterrupted ()) {
            Runnable task = null;
            try {
                task = runnableQueue.take ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
            task.run ();
        }
    }
}
