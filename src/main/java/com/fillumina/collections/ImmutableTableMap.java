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
public final class ImmutableTableMap<K, V> extends UnmodifiableTableMap<K, V> {

    public static final ImmutableTableMap<?,?> EMPTY = new ImmutableTableMap<Object, Object>();
    
    public static <K,V> ImmutableTableMap<K,V> empty() {
        return (ImmutableTableMap<K, V>) EMPTY;
    }
    
    public static <K,V> ImmutableTableMap<K,V> of(Object... values) {
        MapBuilder<ImmutableTableMap<K,V>, K,V> builder = builder();
        for (int i=0; i<values.length; i+=2) {
            builder.put((K)values[i], (V)values[i+1]);
        }
        return builder.build();
    }
    
    public static <K,V> ImmutableTableMap<K,V> of(Map<? extends K, ? extends V> map) {
        return new ImmutableTableMap<>(map);
    }
    
    public static <K, V> MapBuilder<ImmutableTableMap<K,V>, K, V> builder() {
        return new MapBuilder<>(l -> new ImmutableTableMap<>(l));
    }

    /** Empty immutable map. */
    private ImmutableTableMap() {
        super();
    }

    /**
     * Classic {@code java.util} style copy constructor.
     */
    public ImmutableTableMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    /**
     * copy constructor.
     */
    public ImmutableTableMap(AbstractEntryMap<? extends K, ? extends V,
            AbstractMap.SimpleImmutableEntry<? extends K, ? extends V>, ?> map) {
        super(map);
    }

    protected ImmutableTableMap(List<?> list) {
        super(list);
    }
    
    @Override
    public ImmutableTableMap<K, V> immutable() {
        return this;
    }

    @Override
    public UnmodifiableTableMap<K, V> unmodifiable() {
        return this;
    }
    
    @Override
    public ImmutableTableMap<K, V> clone() {
        return this;
    }
}
