package com.fillumina.collections;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable {@link Map.Entry} implementation with cached hashCode.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableMapEntry<K, V> implements Map.Entry<K,V> {

    private final K key;
    private final V value;
    private final int hashcode;

    public ImmutableMapEntry(K k, V v) {
        this.key = k;
        this.value = v;
        this.hashcode = innerHashCode(k, v);
    }

    public ImmutableMapEntry(Map.Entry<? extends K, ? extends V> entry) {
        this(entry.getKey(), entry.getValue());
    }

    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        throw new UnsupportedOperationException("Not supported.");
    }

    public static <K, V> int innerHashCode(K key, V value) {
        return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    @Override
    public int hashCode() {
        return this.hashcode;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Map.Entry)) {
            return false;
        }
        Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
        return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
    }

    /**
     * Returns a String representation of this map entry. This implementation returns the string
     * representation of this entry's key followed by the equals character ("<tt>=</tt>") followed
     * by the string representation of this entry's value.
     *
     * @return a String representation of this map entry
     */
    public String toString() {
        return key + "=" + value;
    }
}
