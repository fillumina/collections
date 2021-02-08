package com.fillumina.collections;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link AbstractArrayMap} implementation.
 * 
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ArrayMap<K, V> extends AbstractArrayMap<K, V> implements Iterable<Entry<K, V>> {

    public static final ArrayMap<?, ?> EMPTY = new Immutable<>();

    public static class Immutable<K, V> extends ArrayMap<K, V> {

        public static <K, V> Builder<ArrayMap<K, V>, K, V> builder() {
            return new Builder<>(o -> new ArrayMap.Immutable<>(o));
        }

        public Immutable() {
        }

        public Immutable(ArrayMap<K, V> copy) {
            super(copy);
        }

        public Immutable(Object... objects) {
            super(objects);
        }

        public Immutable(Map<K, V> map) {
            super(map);
        }

        @Override
        protected void readOnlyCheck() throws UnsupportedOperationException {
            throw new UnsupportedOperationException("read only");
        }
    }

    public static <K, V> ArrayMap<K, V> empty() {
        return (ArrayMap<K, V>) EMPTY;
    }

    public static <K, V> Builder<ArrayMap<K, V>, K, V> builder() {
        return new Builder<>(o -> new ArrayMap<K, V>(o));
    }

    public ArrayMap() {
    }

    public ArrayMap(AbstractArrayMap<K, V> copy) {
        super(copy);
    }

    public ArrayMap(Object... o) {
        super(o);
    }

    public ArrayMap(Map<K, V> map) {
        super(map);
    }

    @Override
    public V put(K key, V value) {
        readOnlyCheck();
        if (array == null) {
            array = new Object[2];
            array[0] = key;
            array[1] = value;
            return null;
        } 
        int index = getIndexOfKey(key);
        if (index == -1) {
            index = array.length;
            Object[] newArray = new Object[array.length + 2];
            System.arraycopy(array, 0, newArray, 0, array.length);
            array = newArray;
            array[index] = key;
            array[index + 1] = value;
            return null;
        } else {
            V prev = (V) array[index + 1];
            array[index + 1] = value;
            return prev;
        }
    }

    public ArrayMap<K, V> immutable() {
        if (this instanceof Immutable) {
            return this;
        }
        return new Immutable<>(this);
    }

    public V getValueAtIndex(int index) {
        return (V) array[1 + (index << 1)];
    }

    public K getKeyAtIndex(int index) {
        return (K) array[index << 1];
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
    public void sortByValues(Comparator<V> comparator) {
        readOnlyCheck();
        if (array == null) {
            return;
        }
        boolean swapped;
        do {
            swapped = false;
            for (int i = array.length - 3; i > 0; i -= 2) {
                if (comparator.compare((V) array[i], (V) array[i + 2]) > 0) {
                    swap(i - 1, i + 1);
                    swapped = true;
                }
            }
        } while (swapped);
    }
}
