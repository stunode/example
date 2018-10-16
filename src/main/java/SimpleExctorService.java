import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author Ryan
 * @Date 2018/10/15 23:26
 * @Function
 */
public class SimpleExctorService {

    public static void main(String[] args) {
        ExecutorService es = Executors.newCachedThreadPool();
        for (int i = 0; i < 10; i++) {
            es.submit(() -> {
                System.out.println("this is test");
            });
        }
    }
}
