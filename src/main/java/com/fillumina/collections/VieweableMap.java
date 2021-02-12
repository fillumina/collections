package com.fillumina.collections;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

/**
 * It's a {@link java.util.Map} implementation that uses unmodifiable {@link Entry} and can produce
 * <b>unmodifiable</b> view.
 *
 * @param <K>
 * @param <V>
 */
public class VieweableMap<K, V>
        extends AbstractEntryMap<K, V, AbstractMap.SimpleImmutableEntry<K, V>, VieweableMap<K, V>> {

    // cache the view
    private transient UnmodifiableHashMap<K, V> readOnlyView;

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

    protected VieweableMap(List<?> list) {
        super(list);
    }

    protected VieweableMap(Object... array) {
        super(array);
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
    public UnmodifiableHashMap<K, V> unmodifiable() {
        if (readOnlyView != null) {
            return readOnlyView;
        }
        return readOnlyView = new UnmodifiableHashMap<K, V>(getInternalState());
    }

    /** @return an immutable <i>clone<i> of this map. */
    public ImmutableHashMap<K, V> immutable() {
        return new ImmutableHashMap<K, V>(this);
    }

    @Override
    public VieweableMap<K, V> clone() {
        return new VieweableMap<K, V>(this);
    }

}
