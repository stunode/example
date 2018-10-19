import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * @Author Ryan
 * @Date 2018/10/15 23:26
 * @Function
 */
public class SimpleExctorService {

    public static void main(String[] args) {

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder ()
                .setNameFormat("myTest-pool-%d").build();
            ExecutorService service = new ThreadPoolExecutor (20, 200,
                    60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable> (1024),namedThreadFactory);
            service.execute (()->{
            System.out.println (Thread.currentThread ().getName ());
        });

        service.execute (()->{
            System.out.println (Thread.currentThread ().getName ());
        });

        service.execute (()->{
            System.out.println (Thread.currentThread ().getName ());
        });
    }
}


