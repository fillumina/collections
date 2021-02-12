package com.fillumina.collections;

import java.util.List;
import java.util.Map;

/**
 * Very tight map based on a sorted array map. It's keys must implement {@link java.util.Comparable}
 * and its access time is {@code O(log N)}.
 * It uses a <i>cursor</i> instead of <i>entries</i> so don't use {@link Map.Entry} outside loops.
 * 
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public final class ImmutableSortedArrayMap<K, V> extends SortedArrayMap<K, V> {
    
    public static final ImmutableSortedArrayMap<?,?> EMPTY = 
            new ImmutableSortedArrayMap<Object, Object>();
    
    public static <K,V> ImmutableSortedArrayMap<K,V> empty() {
        return (ImmutableSortedArrayMap<K, V>) EMPTY;
    }
    
    public static <K,V> ImmutableSortedArrayMap<K,V> of(Object... values) {
        MapBuilder<SortedArrayMap<K,V>, K, V> builder = builder();
        for (int i=0; i<values.length; i+=2) {
            builder.put((K)values[i], (V)values[i+1]);
        }
        return (ImmutableSortedArrayMap<K, V>) builder.build();
    }
    
    public static <K,V> ImmutableArrayMap<K,V> of(Map<? extends K, ? extends V> map) {
        return new ImmutableArrayMap<>(map);
    }

    public static <K, V> MapBuilder<SortedArrayMap<K, V>, K, V> builder() {
        return new MapBuilder<>(o -> new ImmutableSortedArrayMap<>(o));
    }

    private ImmutableSortedArrayMap() {
        super();
    }

    public ImmutableSortedArrayMap(SortedArrayMap<K, V> copy) {
        super(copy);
    }

    public ImmutableSortedArrayMap(Object... objects) {
        super(objects);
    }

    public ImmutableSortedArrayMap(Map<K, V> map) {
        super(map);
    }

    private ImmutableSortedArrayMap(List<?> list) {
        super(list);
    }

    @Override
    protected void readOnlyCheck() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("read only");
    }
    
}
