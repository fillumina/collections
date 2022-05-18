package com.fillumina.collections;

import com.fillumina.collections.AbstractEntryMap.InternalState;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;

/**
 * It's a concurrent map that copy its internal state on modification. It's pretty fast if
 * readings are much more frequent than writings and more space efficient than JDK
 * {@link java.util.concurrent.ConcurrentHashMap}.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class CopyOnWriteMap<K,V>
        extends AbstractEntryMap<K, V, SimpleImmutableEntry<K, V>, CopyOnWriteMap<K, V>,
                                    InternalState<AbstractMap.SimpleImmutableEntry<K, V>> > {

    public CopyOnWriteMap() {
        super();
    }

    public CopyOnWriteMap(int initialSize) {
        super(initialSize);
    }

    public CopyOnWriteMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    public CopyOnWriteMap(
            AbstractEntryMap<
                    ? extends K,
                    ? extends V,
                    ? extends SimpleImmutableEntry<K, V>,
                    ? extends CopyOnWriteMap<K, V>,
                    ? extends InternalState<SimpleImmutableEntry<K, V>>> map) {
        super(map);
    }

    public CopyOnWriteMap(InternalState<SimpleImmutableEntry<K, V>> internalState) {
        super(internalState);
    }

    public CopyOnWriteMap(List<?> list) {
        super(list);
    }

    public CopyOnWriteMap(Object... array) {
        super(array);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected SimpleImmutableEntry<K, V> createEntry(
            K k, V v, InternalState<SimpleImmutableEntry<K, V>> internalState) {
        if (k == null && v == null) {
            return (SimpleImmutableEntry<K, V>) AbstractEntryMap.NULL_ENTRY;
        }
        return new SimpleImmutableEntry<>(k, v);
    }

    @Override
    protected AbstractEntryMap<K, V, SimpleImmutableEntry<K, V>, CopyOnWriteMap<K, V>,
        InternalState<SimpleImmutableEntry<K, V>>> createMap(
            int size) {
        return new CopyOnWriteMap<>(size);
    }

    @Override
    protected synchronized void setInternalState(
            InternalState<SimpleImmutableEntry<K, V>> otherState) {
        super.setInternalState(otherState);
    }

    @Override
    protected InternalState<SimpleImmutableEntry<K, V>> getInternalStateClone() {
        return new InternalState<>(getInternalState());
    }

}
