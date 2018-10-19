package executorservice;

/**
 * 类名称: ThreadPool
 * 功能描述:
 * 日期:  2018/10/16 21:41
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public interface ThreadPool {

    //提交任务到线程池
    void execute(Runnable runnable);

    //关闭线程池
    void shutdown();

}
