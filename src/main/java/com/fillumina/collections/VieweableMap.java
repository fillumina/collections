package com.fillumina.collections;

import java.util.AbstractMap;
import java.util.Map;

/**
 * It's a {@link java.util.Map} implementation that uses immutable {@link Entry} and can produce
 * <b>immutable</b> views. Be careful that the immutable view would still change if this class is
 * modified.
 *
 * @param <K>
 * @param <V>
 */
public class VieweableMap<K, V>
        extends AbstractEntryMap<K, V, AbstractMap.SimpleImmutableEntry<K, V>, VieweableMap<K, V>> {

    // cache the view
    private transient ImmutableHashMap<K, V> readOnlyView;

    public VieweableMap() {
        super();
    }

    public VieweableMap(int initialSize) {
        super(initialSize);
    }

    public VieweableMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    /** Copy constructor. */
    public VieweableMap(AbstractEntryMap<K, V, AbstractMap.SimpleImmutableEntry<K, V>, ?> map) {
        super(map);
    }

    protected VieweableMap(
            AbstractEntryMap.InternalState<AbstractMap.SimpleImmutableEntry<K, V>> internalState) {
        super(internalState);
    }

    @Override
    protected AbstractMap.SimpleImmutableEntry<K, V> createEntry(K k, V v) {
        if (k == null && v == null) {
            return (AbstractMap.SimpleImmutableEntry<K, V>) AbstractEntryMap.NULL_ENTRY;
        }
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    @Override
    protected VieweableMap<K, V> createMap(int size) {
        return new VieweableMap<>(size);
    }

    /** @return a read-only <i>view<i> of this map. */
    public ImmutableHashMap<K, V> immutable() {
        if (readOnlyView != null) {
            return readOnlyView;
        }
        return readOnlyView = new ImmutableHashMap<K, V>(getInternalState());
    }

    @Override
    public VieweableMap<K, V> clone() {
        return new VieweableMap<K, V>(this);
    }

}
