package com.fillumina.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableArrayMap<K, V> extends ArrayMap<K, V> implements ImmutableMap<K,V> {
    
    // this is a drop in replacement for ImmutableHashMap so the builder was adapted to match
    public static class Builder<K, V> extends ArrayMap.Builder<ImmutableArrayMap<K,V>,K,V> {
        private final List<Object> list = new ArrayList<>();

        public Builder() {
            super(null);
        }

        public Builder<K, V> put(K key, V value) {
            return add(key,value);
        }
        
        public Builder<K, V> add(K key, V value) {
            list.add(key);
            list.add(value);
            return this;
        }

        public ImmutableArrayMap<K,V> build() {
            return immutable();
        }

        public ImmutableArrayMap<K,V> immutable() {
            return new ImmutableArrayMap<>(list.toArray());
        }
    }

    public static final ImmutableArrayMap<?,?> EMPTY = new ImmutableArrayMap<Object, Object>();
    
    public static <K,V> ImmutableArrayMap<K,V> empty() {
        return (ImmutableArrayMap<K, V>) EMPTY;
    }
    
    public static <K,V> ImmutableArrayMap<K,V> of(Object... values) {
        Builder<K, V> builder = new Builder<>();
        for (int i=0; i<values.length; i+=2) {
            builder.add((K)values[i], (V)values[i+1]);
        }
        return builder.immutable();
    }
    
    public static <K,V> ImmutableArrayMap<K,V> of(Map<? extends K, ? extends V> map) {
        return new ImmutableArrayMap<>(map);
    }
    
    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    public ImmutableArrayMap() {
    }

    public ImmutableArrayMap(ArrayMap<? extends K, ? extends V> copy) {
        super(copy);
    }

    public ImmutableArrayMap(Object... objects) {
        super(objects);
    }

    public ImmutableArrayMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    @Override
    protected void readOnlyCheck() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("read only");
    }
}
