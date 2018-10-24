package executorservice;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 类名称: MyThreadPool
 * 功能描述:
 * 日期:  2018/10/23 17:36
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class MyThreadPool extends Thread implements ThreadPool {

    // 初始化线程数量
    private final int initSize;

    // 线程池最大线程数量
    private final int maxSize;

    // 线程池核心线程数量
    private final int coreSize;

    // 活跃线程数量
    private int activeCount;

    // 线程工厂
    private ThreadFactory threadFactory;

    // 任务队列
    private final RunnableQueue waitThreadQueue;

    // 线程池是否已经被关闭
    private volatile boolean isShutdown = false;

    private final Queue<ThreadTask> activeThreadQueue = new ArrayDeque<> ();

    private final static DenyPolicy DEFAULT_DENY_POLICY = new DenyPolicy.AbortDenyPolicy ();

    // 初始化时设置线程池属性
    public MyThreadPool(int initSize, int maxSize, int coreSize, ThreadFactory threadFactory) {
        this.initSize = initSize;
        this.maxSize = maxSize;
        this.coreSize = coreSize;
        this.threadFactory = threadFactory;
        this.waitThreadQueue = new LinkedRunnableQueue (10,new DenyPolicy.AbortDenyPolicy());
        this.init ();

        start ();
        for (int i = 0; i < initSize; i++) {
            newThread ();
        }

    }

    // 提交任务： 任务放入Runnable queue即可
    @Override
    public void execute(Runnable runnable) {
        if(this.isShutdown){
            throw new IllegalStateException (" The thread pool is destroy ");
        }
        this.waitThreadQueue.offer (runnable);
    }

    @Override
    public void run() {

        while (!isShutdown && !isInterrupted ()) {

            synchronized (this) {
                // 判断任务队列中是否有等待的队列
                if (waitThreadQueue.size () > 0 && activeCount < coreSize) {
                    for (int i = initSize; i < coreSize; i++) {
                        newThread ();
                    }
                }
            }
        }
    }

    @Override
    public void shutdown() {

    }

    private static class ThreadTask{

        Thread thread;

        RunnableTask runnableTask;

        public ThreadTask(Thread thread, RunnableTask runnableTask) {
            this.thread = thread;
            this.runnableTask = runnableTask;
        }
    }

    private void init(){

        start ();

        for (int i = 0; i < initSize; i++) {
            newThread ();
        }

    }

    // 线程池中新增一个运行线程
    private void newThread(){

        // 从等待队列中不停的获取任务 ，并放在这个线程中进行执行
        RunnableTask runnableTask = new RunnableTask (waitThreadQueue);

        Thread thread = this.threadFactory.createThread (runnableTask);
        ThreadTask threadTask = new ThreadTask (thread, runnableTask);
        activeThreadQueue.offer (threadTask);
        this.activeCount++;
        thread.start ();

    }

    //  简单工厂 创建 线程对象,调用端不需要关心创建细节
    private static class MyThreadFactory implements ThreadFactory {

        private static final ThreadGroup myGroup = new ThreadGroup ("MyThreadPool");

        private static final AtomicInteger counter = new AtomicInteger (0);

        @Override
        public Thread createThread(Runnable runnable) {
            return new Thread (myGroup, runnable, "thread-pool-"+counter.getAndDecrement ());
        }
    }

    public static void main(String[] args) throws Exception {

        MyThreadFactory threadFactory = new MyThreadFactory ();
        MyThreadPool threadPool = new MyThreadPool (2, 10, 5, threadFactory);
        threadPool.execute (()->{
            System.out.println ("my threadPool test");
        });

        for (int i = 0; i < 100; i++) {
            TimeUnit.SECONDS.sleep (2);
            threadPool.execute (()->{
                System.out.println ("my threadPool test num：" + UUID.randomUUID ());
            });
        }
    }
}


