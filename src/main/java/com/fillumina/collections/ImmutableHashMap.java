package com.fillumina.collections;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * It's a read only implementation of {@link java.util.Map}. It can be built via the provided
 * builder, by using one of its constructors or directly from {@link VieweableMap}.
 *
 * @param <K>
 * @param <V>
 */
public class ImmutableHashMap<K, V> extends UnmodifiableHashMap<K, V> {

    public static final ImmutableHashMap<?,?> EMPTY = new ImmutableHashMap<Object, Object>();
    
    public static <K,V> ImmutableHashMap<K,V> empty() {
        return (ImmutableHashMap<K, V>) EMPTY;
    }
    
    public static <K,V> ImmutableHashMap<K,V> of(Object... values) {
        MapBuilder<ImmutableHashMap<K,V>, K,V> builder = builder();
        for (int i=0; i<values.length; i+=2) {
            builder.put((K)values[i], (V)values[i+1]);
        }
        return builder.build();
    }
    
    public static <K,V> ImmutableHashMap<K,V> of(Map<? extends K, ? extends V> map) {
        return new ImmutableHashMap<>(map);
    }
    
    public static <K, V> MapBuilder<ImmutableHashMap<K,V>, K, V> builder() {
        return new MapBuilder<>(l -> new ImmutableHashMap<>(l));
    }

    /** Empty immutable map. */
    private ImmutableHashMap() {
        super();
    }

    /**
     * Classic {@code java.util} style copy constructor.
     */
    public ImmutableHashMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    /**
     * copy constructor.
     */
    public ImmutableHashMap(AbstractEntryMap<? extends K, ? extends V,
            AbstractMap.SimpleImmutableEntry<? extends K, ? extends V>, ?> map) {
        super(map);
    }

    protected ImmutableHashMap(List<?> list) {
        super(list);
    }
    
    @Override
    public ImmutableHashMap<K, V> immutable() {
        return this;
    }

    @Override
    public UnmodifiableHashMap<K, V> unmodifiable() {
        return this;
    }
    
    @Override
    public ImmutableHashMap<K, V> clone() {
        return this;
    }
}
