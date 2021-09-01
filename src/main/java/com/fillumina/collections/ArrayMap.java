package com.fillumina.collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link BaseArrayMap} implementation. Be aware that entries are shown having all the same
 * value in IDE debuggers because of the use of a single cursor instead of the usual different
 * entry for each mapping.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ArrayMap<K, V> extends BaseArrayMap<K, V> implements Iterable<Entry<K, V>> {

    public static final ArrayMap<?, ?> EMPTY = new ImmutableArrayMap<>();

    @SuppressWarnings("unchecked")
    public static <K, V> ArrayMap<K, V> empty() {
        return (ArrayMap<K, V>) EMPTY;
    }

    public static <K, V> MapBuilder<? extends ArrayMap<K, V>, K, V> builder() {
        return new MapBuilder<>(l -> new ArrayMap<K, V>(l));
    }

    public ArrayMap() {
    }

    public ArrayMap(BaseArrayMap<? extends K, ? extends V> copy) {
        super(copy);
    }

    public ArrayMap(Object... o) {
        super(o);
    }

    public ArrayMap(Collection<?> collection) {
        super(collection);
    }

    public ArrayMap(Iterable<?> iterable) {
        super(iterable);
    }

    public ArrayMap(Map<? extends K, ? extends V> map) {
        super(map);
    }

    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        readOnlyCheck();
        if (array == null) {
            array = new Object[2];
            array[0] = key;
            array[1] = value;
            return null;
        }
        int index = getAbsoluteIndexOfKey(key);
        if (index == -1) {
            index = array.length;
            Object[] newArray = new Object[array.length + 2];
            System.arraycopy(array, 0, newArray, 0, index);
            newArray[index] = key;
            newArray[index + 1] = value;
            array = newArray;
            return null;
        } else {
            V prev = (V) array[index + 1];
            array[index + 1] = value;
            return prev;
        }
    }

    /**
     * @return an immutable <b>clone</b>.
     */
    public ArrayMap<K, V> immutable() {
        if (this instanceof ImmutableArrayMap) {
            return this;
        }
        return new ImmutableArrayMap<>(this);
    }

    @SuppressWarnings("unchecked")
    public V getValueAtIndex(int index) {
        return (V) array[1 + (index << 1)];
    }

    @SuppressWarnings("unchecked")
    public K getKeyAtIndex(int index) {
        return (K) array[index << 1];
    }

    /** @return -1 if not found otherwise the index of the key. */
    public int getIndexOfKey(K key) {
        final int idx = getAbsoluteIndexOfKey(key);
        return idx < 0 ? -1 : idx / 2;
    }

    public V removeEntryAtIndex(int index) {
        return removeEntryAtAbsoluteIndex(index * 2);
    }

    /**
     * A new entry is created at each call!!
     */
    public Entry<K, V> getEntryAtIndex(int index) {
        return new SimpleEntry<>(getKeyAtIndex(index), getValueAtIndex(index));
    }

    /**
     * Implements a very simple bubble sort.
     */
    public void sortByKeys(Comparator<K> comparator) {
        readOnlyCheck();
        super.sortByKeys(comparator);
    }

    /**
     * Implements a very simple bubble sort.
     */
    @SuppressWarnings("unchecked")
    public void sortByValues(Comparator<V> comparator) {
        readOnlyCheck();
        if (array == null) {
            return;
        }
        Object[] larray = array; // for faster operations
        boolean swapped;
        do {
            swapped = false;
            for (int i = larray.length - 3; i > 0; i -= 2) {
                if (comparator.compare((V) larray[i], (V) larray[i + 2]) > 0) {
                    Object tmpKey = larray[i-1];
                    Object tmpValue = larray[i];
                    larray[i-1] = larray[i+1];
                    larray[i] = larray[i+2];
                    larray[i+1] = tmpKey;
                    larray[i+2] = tmpValue;
                    swapped = true;
                }
            }
        } while (swapped);
    }
}
