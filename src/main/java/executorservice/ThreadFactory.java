package executorservice;

import javax.swing.text.html.HTMLDocument;

/**
 * 类名称: ThreadFactory
 * 功能描述:
 * 日期:  2018/10/17 22:30
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public interface ThreadFactory {

    //创建线程接口
    Thread createThread(Runnable runnable);
}
