package com.fillumina.collections;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Full {@link java.util.Map} conform implementation.
 */
public class TableMap<K, V> extends AbstractEntryMap<K, V, Map.Entry<K, V>, TableMap<K, V>> {

    public TableMap() {
        super();
    }

    public TableMap(int initialSize) {
        super(initialSize);
    }

    /**
     * Copy constructor from other {@link Map}.
     */
    public TableMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    /**
     * Homologous copy constructor.
     */
    public TableMap(AbstractEntryMap<? extends K, ? extends V, 
            Entry<? extends K, ? extends V>, TableMap<? extends K, ? extends V>> map) {
        super(map);
    }

    /**
     * View constructor.
     */
    protected TableMap(InternalState<Entry<K, V>> internalState) {
        super(internalState);
    }

    @Override
    protected Entry<K, V> createEntry(K k, V v) {
        if (k == null && v == null) {
            return (Entry<K, V>) AbstractEntryMap.NULL_ENTRY;
        }
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    @Override
    protected TableMap<K, V> createMap(int size) {
        return new TableMap<>(size);
    }

    /** @return an immutable <i>clone</i> of this map. */
    public ImmutableTableMap<K, V> immutable() {
        return new ImmutableTableMap<K, V>(this);
    }
    
    @Override
    public TableMap<K, V> clone() {
        return new TableMap<K, V>(this);
    }
    
}
