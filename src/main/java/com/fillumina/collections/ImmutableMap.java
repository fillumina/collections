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
public class ImmutableMap<K, V> extends VieweableMap<K, V> {

    public static final ImmutableMap<?,?> EMPTY = new ImmutableMap<Object, Object>();
    
    public static <K,V> ImmutableMap<K,V> empty() {
        return (ImmutableMap<K, V>) EMPTY;
    }
    
    public static <K,V> ImmutableMap<K,V> of(Object... values) {
        VieweableMap<K,V> builder = ImmutableMap.builder();
        for (int i=0; i<values.length; i+=2) {
            builder.put((K)values[i], (V)values[i+1]);
        }
        return builder.immutable();
    }
    
    public static <K,V> ImmutableMap<K,V> of(Map<? extends K, ? extends V> map) {
        return new ImmutableMap<>(map);
    }
    
    public static <K, V> VieweableMap<K, V> builder() {
        return new VieweableMap<>();
    }

    public ImmutableMap() {
        super();
    }

    /**
     * Classic {@code java.util} style copy constructor.
     */
    public ImmutableMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    /**
     * copy constructor.
     */
    public ImmutableMap(AbstractEntryMap<? extends K, ? extends V,
            AbstractMap.SimpleImmutableEntry<? extends K, ? extends V>, ?> map) {
        super(map);
    }

    /**
     * Used for views.
     */
    protected ImmutableMap(InternalState<AbstractMap.SimpleImmutableEntry<K, V>> internalState) {
        super(internalState);
    }

    @Override
    public void readOnlyCheck() {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public ImmutableMap<K, V> immutable() {
        return this;
    }

    @Override
    public ImmutableMap<K, V> clone() {
        // just in case this immutable is a view it might be cloned into an endependent object
        return new ImmutableMap<>(this);
    }
}
