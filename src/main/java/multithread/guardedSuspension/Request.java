package multithread.guardedSuspension;

/**
 * 类名称: Request
 * 功能描述:
 * 日期:  2018/10/16 22:38
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
public class Request {

    private final String name;

    public Request(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
