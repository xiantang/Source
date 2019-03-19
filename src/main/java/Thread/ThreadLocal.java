package Thread;

import java.lang.ref.WeakReference;

public class ThreadLocal <T>{

    //ThreadLocal内部类ThreadLocalMap才是存储数据的容器
    // 由Thread 维护
    static class ThreadLocalMap {
        // 为什么要使用弱引用
        // 当一个对象
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /**
             * The value associated with this ThreadLocal.
             */
            Object value;
            // Key是ThreadLocal对象 没有指向key的强引用就会被回收
            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }


        private void set(ThreadLocal<?> key, Object value) {

            ThreadLocal.ThreadLocalMap.Entry[] tab = table;
            int len = tab.length;

            // 根据ThreadLocal的散列值进行hash
            int i = key.threadLocalHashCode & (len-1);

            // 采用“线性探测法”，寻找合适位置
            for (ThreadLocal.ThreadLocalMap.Entry e = tab[i];
                 e != null;
                 e = tab[i = nextIndex(i, len)]) {

                ThreadLocal<?> k = e.get();

                // key 存在，直接覆盖
                if (k == key) {
                    e.value = value;
                    return;
                }

                // key == null，但是存在值（因为此处的e != null），说明之前的ThreadLocal对象已经被回收了
                if (k == null) {
                    // 用新元素替换陈旧的元素
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            // ThreadLocal对应的key实例不存在也没有陈旧元素，new 一个
            tab[i] = new ThreadLocal.ThreadLocalMap.Entry(key, value);

            int sz = ++size;

            // cleanSomeSlots 清楚陈旧的Entry（key == null）
            // 如果没有清理陈旧的 Entry 并且数组中的元素大于了阈值，则进行 rehash
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }

    }

    public void set(T value) {
        // 当前线程
        Thread t = Thread.currentThread();
        // 获取当前线程的ThreadLocalMap对象
        ThreadLocalMap map = getMap(t);
        if (map != null)
            // 当前ThreadLocal对象为key
            // 存储到ThreadLocalMap中
            // 以当前的ThreadLocalMap 作为Key

            map.set(this, value);
        else

            createMap(t, value);
    }
    public T get() {
        // 当前线程
        Thread t = Thread.currentThread();
        // 获取当前线程的ThreadLocalMap对象
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            // 以当前线程的ThreadLocalMap作为Key 去获得对应的Entry
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;
            }
        }
        return setInitialValue();
    }
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

}
