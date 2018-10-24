package executorservice;

/**
 * 类名称: RunnableQueue
 * 功能描述: 用于存放提交的Runnable
 * 日期:  2018/10/23 17:40
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public interface RunnableQueue {

    // 新任务进来时放入队列中
    void offer(Runnable runnable);

    // 工作线程通过take 获取Runnable
    Runnable take() throws InterruptedException;

    // 获取任务队列中任务的数量
    int size();
}
