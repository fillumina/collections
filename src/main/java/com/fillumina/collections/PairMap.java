package com.fillumina.collections;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is a very minimal size map backed by an array. It should be used mainly as a small immutable
 * object. It's easy to clone and easy to pass and save. It's characteristic is that entries are in
 * fact a <i>cursor</i> that is one single <i>mutable</i> object: don't use it with parallel streams
 * and never save or use entries outside loops! Every operation is O(n) so very inefficient for many
 * items (but good enough for a few ones). The map keeps insertion order until a sorting method is
 * called. This map is not thread safe.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PairMap<K, V> extends AbstractArrayMap<K, V> implements Iterable<Entry<K, V>> {

    public static final PairMap<?, ?> EMPTY = new Immutable<>();

    public static class Immutable<K, V> extends PairMap<K, V> {

        public static <K, V> Builder<PairMap<K, V>, K, V> builder() {
            return new Builder<>(o -> new PairMap.Immutable<>(o));
        }

        public Immutable() {
        }

        public Immutable(PairMap<K, V> copy) {
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

    public static <K, V> PairMap<K, V> empty() {
        return (PairMap<K, V>) EMPTY;
    }

    public static <K, V> Builder<PairMap<K, V>, K, V> builder() {
        return new Builder<>(o -> new PairMap<K, V>(o));
    }

    public PairMap() {
    }

    public PairMap(AbstractArrayMap<K, V> copy) {
        super(copy);
    }

    public PairMap(Object... o) {
        super(o);
    }

    public PairMap(Map<K, V> map) {
        super(map);
    }

    @Override
    public V put(K key, V value) {
        readOnlyCheck();
        int index = getIndexOfKey(key);
        if (array == null) {
            array = new Object[2];
            array[0] = key;
            array[1] = value;
            return null;
        } else if (index == -1) {
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

    public PairMap<K, V> immutable() {
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
