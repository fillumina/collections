package com.fillumina.collections;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Mutable {@link Map.Entry} implementation with cached hashCode.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class CachedHashCodeEntry<K, V> implements Map.Entry<K,V>, Serializable {
    private static final long serialVersionUID = 1L;

    private final K key;
    private V value;
    private int hashcode;

    public CachedHashCodeEntry(K k, V v) {
        this.key = k;
        this.value = v;
        this.hashcode = innerHashCode(k, v);
    }

    public CachedHashCodeEntry(Map.Entry<? extends K, ? extends V> entry) {
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
        V oldValue = this.value;
        this.value = value;
        this.hashcode = innerHashCode(key, value);
        return oldValue;
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
