package com.fillumina.collections;

import java.util.AbstractMap;
import java.util.Map;

/**
 * It's a read only implementation of {@link java.util.Map}. It can be built via the provided
 * builder, by using one of its constructors or directly from {@link VieweableMap}.
 *
 * @param <K>
 * @param <V>
 */
public class ImmutableHashMap<K, V> extends VieweableMap<K, V> implements ImmutableMap<K,V> {

    public static final ImmutableHashMap<?,?> EMPTY = new ImmutableHashMap<Object, Object>();
    
    public static <K,V> ImmutableHashMap<K,V> empty() {
        return (ImmutableHashMap<K, V>) EMPTY;
    }
    
    public static <K,V> ImmutableHashMap<K,V> of(Object... values) {
        VieweableMap<K,V> builder = ImmutableHashMap.builder();
        for (int i=0; i<values.length; i+=2) {
            builder.put((K)values[i], (V)values[i+1]);
        }
        return builder.immutable();
    }
    
    public static <K,V> ImmutableHashMap<K,V> of(Map<? extends K, ? extends V> map) {
        return new ImmutableHashMap<>(map);
    }
    
    public static <K, V> VieweableMap<K, V> builder() {
        return new VieweableMap<>();
    }

    public ImmutableHashMap() {
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

    /**
     * Used for views. Be careful: the map would not be immutable because the original one
     * could change it.
     */
    protected ImmutableHashMap(InternalState<AbstractMap.SimpleImmutableEntry<K, V>> internalState) {
        super(internalState);
    }

    @Override
    protected void readOnlyCheck() {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public ImmutableHashMap<K, V> immutable() {
        return this;
    }

    @Override
    public ImmutableHashMap<K, V> clone() {
        // just in case this immutable is a view it might be cloned into an endependent object
        return new ImmutableHashMap<>(this);
    }
}
