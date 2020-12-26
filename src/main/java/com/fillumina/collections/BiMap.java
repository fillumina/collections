package com.fillumina.collections;

import com.fillumina.collections.AbstractSimpleMap.SimpleMap;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BiMap<K,V> extends SimpleMap<K,V> implements InvertibleBiMap<K,V> {
    
    public class InverseMap extends SimpleMap<V,K> 
            implements InvertibleBiMap<V,K>{

        public InverseMap() {
            super();
        }

        public InverseMap(int initialSize) {
            super(initialSize);
        }

        public InvertibleBiMap<K,V> inverse() {
            return BiMap.this;
        }
        
        @Override
        protected K innerPut(V key, K value) {
            BiMap.this.inversePut(value, key);
            return super.innerPut(key, value);
        }

        @Override
        public K remove(Object key) {
            Entry<V,K> entry = getEntry(key);
            K value = super.remove(key);
            BiMap.this.innerRemove(entry.getValue());
            return value;
        }

        protected K innerRemove(Object key) {
            return super.remove(key);
        }

        @Override
        public Set<K> values() {
            return BiMap.this.keySet();
        }

        @Override
        public boolean containsValue(Object value) {
            return BiMap.this.containsKey(value);
        }

        @Override
        public void clear() {
            BiMap.this.clear();
        }
        
        protected void innerClear() {
            super.clear();
        }
        
    }
    
    private final InverseMap inverseMap;
    
    public BiMap() {
        super();
        this.inverseMap = new InverseMap();
    }

    public BiMap(int initialSize) {
        super(initialSize);
        this.inverseMap = new InverseMap(initialSize);
    }

    public InvertibleBiMap<V,K> inverse() {
        return inverseMap;
    }
    
    @Override
    protected V innerPut(K key, V value) {
        inverseMap.innerPut(value, key);
        return super.innerPut(key, value);
    }

    private V inversePut(K key, V value) {
        return super.innerPut(key, value);
    }
    
    @Override
    public boolean containsValue(Object value) {
        return inverseMap.containsKey(value);
    }

    @Override
    public V remove(Object key) {
        Entry<K,V> entry = getEntry(key);
        V value = super.remove(key);
        inverseMap.innerRemove(entry.getValue());
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
}
