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
public class UnmodifiableTableMap<K, V> extends VieweableMap<K, V> {

    protected UnmodifiableTableMap() {
        super();
    }

    /**
     * Classic {@code java.util} style copy constructor.
     */
    protected UnmodifiableTableMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    /**
     * copy constructor.
     */
    protected UnmodifiableTableMap(AbstractEntryMap<? extends K, ? extends V,
            AbstractMap.SimpleImmutableEntry<? extends K, ? extends V>, ?, ?> map) {
        super(map);
    }

    /**
     * Used for views. Be careful: the map would not be immutable because the original one
     * could change it.
     */
    protected UnmodifiableTableMap(InternalState<AbstractMap.SimpleImmutableEntry<K, V>> internalState) {
        super(internalState);
    }

    protected UnmodifiableTableMap(List<?> list) {
        super(list);
    }

    @Override
    protected void readOnlyCheck() {
        throw new UnsupportedOperationException("read only");
    }

    /** @return a immutable <i>clone<i> of this map. */
    public ImmutableTableMap<K, V> immutable() {
        return new ImmutableTableMap<K, V>(this);
    }

    /** @return a immutable <i>clone<i> of this map. */
    public UnmodifiableTableMap<K, V> clone() {
        return new ImmutableTableMap<K, V>(this);
    }
}
