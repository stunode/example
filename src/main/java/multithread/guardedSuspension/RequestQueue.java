package multithread.guardedSuspension;

import java.util.LinkedList;
import java.util.Queue;

/**
 * 通过 将 wait 和 notify 放在RequestQueue类中 ，达到可服用，对于使用RequestQueue的其他类，无需考虑 wait和notify的问题，只要调用相关方法即可
 * 但是可能会造成线程处于死锁的状态
 */
public class RequestQueue {
    private final Queue<Request> queue = new LinkedList<> ();

    // guarded method
    public synchronized Request getRequest(){
        // queue.peek()==null为守护条件，当守护条件满足时 线程wait，释放锁，重要的是while 和wait的组合，如果被notify后 进行while守护条件判断，不满足，继续执行wait()
        while (queue.peek ()==null) {
            try {
                wait ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
        // 目标处理 ，当守护条件不满足时 不处理
        return queue.remove ();
    }

    //state change method
    public synchronized void putRequest(Request request){
        queue.offer (request);
        // 状态变化后，通知其他需要获取锁的线程
        notifyAll ();
    }
}
