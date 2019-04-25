package Collection;

import java.util.Map;

public interface ConcurrentMap<K, V> extends Map<K, V> {

    // 当K没有相应的映射时插入
    V putIfAbsent(K key, V value);

    // 当K被映射到V时候才移除
    V remove(Object key);

    // 当K被映射到oldValue时候才会被替换
    boolean replace(K key, V oldValue, V newValue);

    // 当K被映射到某个值才替换
    V replace(K key, V value);

}

