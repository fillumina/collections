package com.fillumina.collections;

import java.util.AbstractMap;
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
        extends AbstractEntryMap<K, V, AbstractMap.SimpleImmutableEntry<K, V>, CopyOnWriteMap<K, V>> {

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
            AbstractEntryMap<? extends K, ? extends V, ? extends AbstractMap.SimpleImmutableEntry<K, V>, ? extends VieweableMap<K, V>> map) {
        super(map);
    }

    public CopyOnWriteMap(InternalState<AbstractMap.SimpleImmutableEntry<K, V>> internalState) {
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
    protected AbstractMap.SimpleImmutableEntry<K, V> createEntry(K k, V v) {
        if (k == null && v == null) {
            return (AbstractMap.SimpleImmutableEntry<K, V>) AbstractEntryMap.NULL_ENTRY;
        }
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    @Override
    protected AbstractEntryMap<K, V, AbstractMap.SimpleImmutableEntry<K, V>, CopyOnWriteMap<K, V>> createMap(
            int size) {
        return new CopyOnWriteMap<>(size);
    }

    @Override
    protected synchronized void setInternalState(
            InternalState<AbstractMap.SimpleImmutableEntry<K, V>> otherState) {
        super.setInternalState(otherState);
    }

    @Override
    protected InternalState<AbstractMap.SimpleImmutableEntry<K, V>> getInternalStateClone() {
        return new InternalState<>(getInternalState());
    }

    @Override
    protected synchronized InternalState<AbstractMap.SimpleImmutableEntry<K, V>> getInternalState() {
        return super.getInternalState();
    }

}
