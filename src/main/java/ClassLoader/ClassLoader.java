package ClassLoader;

import sun.misc.Launcher;
import sun.reflect.Reflection;

import java.util.concurrent.Executors;

public  class ClassLoader {
    protected Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException
    {
        synchronized (getClassLoadingLock(name)) {
            // 首先查看是否已经被加载
            Class<?>  c = findLoadedClass(name);
            if (c == null) {
                long t0 = System.nanoTime();
                try {
                    if (parent != null) {
                        c = parent.loadClass(name, false);
                    } else {
                        c = findBootstrapClassOrNull(name);
                    }
                } catch (ClassNotFoundException e) {
                    //如果类加载器 抛出ClassNotFoundException
                    // 父类无法加载
                }

                if (c == null) {
                    // 如仍然无法加载 调用本身进行类加载
                    long t1 = System.nanoTime();
                    c = findClass(name);
//                    StackOverflowError

                    sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                    sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                    sun.misc.PerfCounter.getFindClasses().increment();
                }
            }
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
    }


}
