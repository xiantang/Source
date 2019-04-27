package Lock;

/**
 * @Author: xiantang
 * @Date: 2019/4/24 14:47
 */
public abstract class AbstractQueuedSynchronizer {


//    void release() {
//       // 更新同步器状态
//       // if(新的状态允许某个被阻塞的线程获得成功)
//            // 接触队列中一个或者多个线程的阻塞状态
//    }

    // 需要用户自己实现的方法
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }


    public final void acquire(int arg) {
        // 如果独占获取失败就进行排队
        // 排队完成之后对线程进行挂起
    
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }


    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (; ; ) {
                /*获得当前节点pred节点*/
                final Node p = node.predecessor();
                /*如果当前节点正好是第二个节点，那么则再次尝试获取锁*/
                if (p == head && tryAcquire(arg)) {
                    /*获取锁成功，*/
                    setHead(node); /*将当前节点设置为头结点*/
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                /*当前节点不是第二个节点 或者 再次获取锁失败*/
                /*判断是否需要挂起，在挂起后，判断线程是否中断*/
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }


    private static shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;
        // 当前节点被设置为等待唤醒，就可以安全的挂起了
        if (ws == Node.SIGNAL)
            return true;
        if (ws > 0) {
            /*
                * 当前节点node的前任节点被取消，那么【跳过】这些取消的节点，
                * 当跳过之后，重新尝试获取锁
                */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            /*
                * 通过前面的判断，waitStatus一定不是 SIGNAL 或 CANCELLED。
                * 推断出一定是 0 or PROPAGATE
                * 调用者需要再次尝试，在挂起之前能不能获取到锁，
                * 因此，将当前pred的状态设为SIGNAL，再次尝试获取锁之后，如果还没有得到锁那么挂起
                *
                */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
 
    }



    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        /*如果被中断，则返回true，interrupted()方法返回后，中断状态被取消，变为false*/
        return Thread.interrupted();
    }



    // 释放独占锁
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            if (h != null && h.waitStatus != 0)
                unparkSuccessor(h);
            return true;
        }
        return false;
    }

    private Node addWaiter(Node mode) {
        // mode 指定共享的还是独占的
        // 将当前线程包装为Node
        Node node = new Node(Thread.currentThread(), mode);
        Node pred = tail;
        if (pred != null) {
            /*将节点加入到队列尾部*/
            node.prev = pred;
            /*尝试使用CAS方式修改尾节点，但是在【并发情况】下，可能修改失败*/
            //比较tail 与 pred 指向的是否是同一个节点
            if (compareAndSetTail(pred, node)) {
                // 此时node已经是尾节点了
                pred.next = node;
                return node;
            }
        }
        /*修改失败，说明有并发，那么进入enq，以自旋方式修改*/
        enq(node);
        return node;
    }


    // 自旋操作
    private Node enq(final Node node) {
        for (; ; ) {
            Node pred = tail;
            if (pred == null) { // Must initialize
                // 初始化操作
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = pred;
                // 使用cas的方式设置尾节点
                if (compareAndSetTail(pred, node)) {
                    pred.next = node;
                    return pred;
                }
            }
        }
    }


}
