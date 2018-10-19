package executorservice;

import com.sun.xml.internal.ws.policy.privateutil.RuntimePolicyUtilsException;

/**
 * 类名称: DenyPolicy
 * 功能描述:
 * 日期:  2018/10/17 22:32
 *
 * @author: renpengfei
 * @since: JDK1.8
 */
@FunctionalInterface
public interface DenyPolicy {

    void reject(Runnable runnable);

    class AbortDenyPolicy implements DenyPolicy {

        @Override
        public void reject(Runnable runnable) {
            throw new RuntimeException ();
        }
    }
}
