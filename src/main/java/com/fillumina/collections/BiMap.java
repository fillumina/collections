package com.fillumina.collections;

import com.fillumina.collections.AbstractSimpleMap.SimpleMap;
import java.util.Map;
import java.util.Set;

/**
 * A BiMap it's not only a double mapping between keys and values but also assures their biunivocal
 * unicity: if an existing value is inserted with a new key the old mapping is substituted.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BiMap<K, V> extends SimpleMap<K, V> {

    public static BiMap<?, ?> EMPTY = immutable();
    private static final Object[] EMPTY_ARRAY = new Object[0];

    public static <K, V> BiMap<K, V> empty() {
        return (BiMap<K, V>) EMPTY;
    }

    public static <K,V> BiMap<K,V> immutable(Map<K,V> map) {
        return new BiMap<>(true, map);
    }

    public static <K,V> BiMap<K,V> immutable(Object... array) {
        return new BiMap<>(true, array);
    }
    
    private final BiMap<V, K> inverseMap;
    private final boolean immutable;

    public BiMap(Map<K, V> map) {
        this();
        map.forEach((k, v) -> put(k, v));
    }

    private BiMap(boolean immutable, Map<K, V> map) {
        super();
        this.inverseMap = new BiMap(this, immutable, 0, map);
        this.immutable = immutable;
    }

    private BiMap(boolean immutable, Object... array) {
        super();
        this.inverseMap = new BiMap(this, immutable, 0, array);
        this.immutable = immutable;
    }
    
    public BiMap() {
        super();
        this.inverseMap = new BiMap(this, false);
        this.immutable = false;
    }

    private BiMap(BiMap<V, K> inverseMap, boolean immutable) {
        this.inverseMap = inverseMap;
        this.immutable = immutable;
    }

    public BiMap(int initialSize) {
        super(initialSize);
        this.inverseMap = new BiMap(this, false, initialSize, EMPTY_ARRAY);
        this.immutable = false;
    }

    private BiMap(BiMap<V, K> inverseMap, boolean immutable, int initialSize, Map<K,V> map) {
        super(initialSize);
        this.inverseMap = inverseMap;
        if (map != null) {
            map.forEach((k,v) -> put(k,v));
        }
        this.immutable = immutable;
    }

    private BiMap(BiMap<V, K> inverseMap, boolean immutable, int initialSize, Object... array) {
        super(initialSize);
        this.inverseMap = inverseMap;
        if (array.length > 0) {
            for (int i=0;i<array.length;i+=2) {
                put((K)array[i], (V)array[i+1]);
            }
        }
        this.immutable = immutable;
    }

    public BiMap<V, K> inverse() {
        return inverseMap;
    }

    @Override
    public void readOnlyCheck() {
        if (immutable) {
            throw new UnsupportedOperationException("read only instance");
        }
    }

    @Override
    protected V innerPut(K key, V value) {
        readOnlyCheck();
        K prevKey = inverseMap.inverseInnerPut(value, key);
        if (prevKey != null) {
            innerRemove(prevKey);
        }
        return super.innerPut(key, value);
    }

    private V inverseInnerPut(K key, V value) {
        return super.innerPut(key, value);
    }

    @Override
    public boolean containsValue(Object value) {
        return inverseMap.containsKey(value);
    }

    @Override
    public Set<V> values() {
        return inverseMap.keySet();
    }

    @Override
    public V remove(Object key) {
        V value = super.remove(key);
        inverseMap.innerRemove(value);
        return value;
    }

    private V innerRemove(Object key) {
        return super.remove(key);
    }

    @Override
    public void clear() {
        super.clear();
        inverseMap.innerClear();
    }

    private void innerClear() {
        super.clear();
    }
}
