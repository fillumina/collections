package com.fillumina.collections;

import java.util.List;
import java.util.Map;

/**
 * Very slow compact immutable map to be used with few items.
 * It uses a <i>cursor</i> instead of <i>entries</i> so don't use {@link Map.Entry} outside loops.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public final class ImmutableArrayMap<K, V> extends ArrayMap<K, V> {

    public static final ImmutableArrayMap<?,?> EMPTY = new ImmutableArrayMap<Object, Object>();

    @SuppressWarnings("unchecked")
    public static <K,V> ImmutableArrayMap<K,V> empty() {
        return (ImmutableArrayMap<K, V>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <K,V> ImmutableArrayMap<K,V> of(Object... values) {
        MapBuilder<ImmutableArrayMap<K,V>, K, V> builder = builder();
        for (int i=0; i<values.length; i+=2) {
            builder.put((K)values[i], (V)values[i+1]);
        }
        return builder.build();
    }

    public static <K,V> ImmutableArrayMap<K,V> of(Map<? extends K, ? extends V> map) {
        return new ImmutableArrayMap<>(map);
    }

    public static <K, V> MapBuilder<ImmutableArrayMap<K, V>, K, V> builder() {
        return new MapBuilder<>(l -> new ImmutableArrayMap<K, V>(l));
    }

    public ImmutableArrayMap() {
    }

    public ImmutableArrayMap(ArrayMap<? extends K, ? extends V> copy) {
        super(copy);
    }

    protected ImmutableArrayMap(Object... objects) {
        super(objects);
    }

    protected ImmutableArrayMap(List<?> list) {
        super(list);
    }

    public ImmutableArrayMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    @Override
    protected void readOnlyCheck() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("read only");
    }
}
