package com.fillumina.collections;

import com.fillumina.collections.AbstractSimpleMap.SimpleMap;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BiMap<K,V> extends SimpleMap<K,V> {
    
    private final BiMap<V,K> inverseMap;
    
    public BiMap() {
        super();
        this.inverseMap = new BiMap(this);
    }

    private BiMap(BiMap<V,K> inverseMap) {
        this.inverseMap = inverseMap;
    }

    public BiMap(int initialSize) {
        super(initialSize);
        this.inverseMap = new BiMap(this, initialSize);
    }

    private BiMap(BiMap<V,K> inverseMap, int initialSize) {
        super(initialSize);
        this.inverseMap = inverseMap;
    }

    public BiMap<V,K> inverse() {
        return inverseMap;
    }
    
    @Override
    protected V innerPut(K key, V value) {
        inverseMap.inverseInnerPut(value, key);
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
