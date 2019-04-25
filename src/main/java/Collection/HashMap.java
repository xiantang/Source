

import java.io.Serializable;

import java.util.AbstractMap;
import java.util.Map;



public class HashMap<K,V> extends AbstractMap<K,V>
    implements Map<K,V>, Cloneable, Serializable {
        //加载因子
        //0.75f是官方给出的一个比较好的临界值
        // 理想情况下属于泊松分布
        // 在桶为8个的情况下，概率会很小
        // 在链表长度为8的情况是基本不可能的
        final float loadFactor;
        
        static final int MIN_TREEIFY_CAPACITY = 64;

        // JDK 1.8  
        static final int hash(Object key) {
            int h;
            // 防止实现较差的hashcode
            return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
        }

        // JDK 1.7
        static int hash(int h) {
            // 性能较差
            h ^= (h >>> 20) ^ (h >>> 12);
            return h ^ (h >>> 7) ^ (h >>> 4);
        }
   
   
}