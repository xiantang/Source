package Lock;

/**
 * @Author: xiantang
 * @Date: 2019/4/24 14:47
 */
public class AbstractQueuedSynchronizer {
    boolean acquire() throws InterruptedException {
       // while (!tryAcquire(arg)) {
            //nqueue thread if it is not already queued
            //possibly block current thread
        // }
        return true;
    }

    void release() {
       // 更新同步器状态
       // if(新的状态允许某个被阻塞的线程获得成功)
            // 接触队列中一个或者多个线程的阻塞状态
    }
}
