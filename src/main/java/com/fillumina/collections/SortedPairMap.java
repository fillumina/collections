package com.fillumina.collections;

import java.util.Map;
import java.util.Objects;

/**
 * Map backed by a sorted array. Very compact and accessible in O(Log n).
 * Useful to store large maps in a very small space with decent access time.
 * It uses cursors as iterators so don't use it with parallel streams.
 * It <b>requires</b> a {@link Comparable} implementing key.
 * 
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SortedPairMap<K,V> extends AbstractArrayMap<K,V> {
    private static final int PAIR_MASK = Integer.MAX_VALUE - 1;
    
    public static final SortedPairMap<?,?> EMPTY = new SortedPairMap.Immutable<>();

    public static class Immutable<K, V> extends SortedPairMap<K, V> {

        public static <K,V> Builder<SortedPairMap<K,V>,K,V> builder() {
            return new Builder<>(o -> new SortedPairMap.Immutable<>(o));
        }

        public Immutable() {
        }

        public Immutable(SortedPairMap<K,V> copy) {
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
    
    public static <K,V> Builder<SortedPairMap<K,V>,K,V> builder() {
        return new Builder<>(o -> new SortedPairMap<>(o));
    }
    
    public SortedPairMap() {
    }

    public SortedPairMap(SortedPairMap<K, V> copy) {
        super(copy);
        sortByKeys((a,b) -> ((Comparable<K>) a).compareTo((K) b));
    }

    public SortedPairMap(Object... o) {
        super(o);
        sortByKeys((a,b) -> ((Comparable<K>) a).compareTo((K) b));
    }

    public SortedPairMap(Map<K, V> map) {
        super(map);
        sortByKeys((a,b) -> ((Comparable<K>) a).compareTo((K) b));
    }

    @Override
    public V put(K key, V value) {
        readOnlyCheck();
        Objects.requireNonNull(key, "key cannot be null");
        int index = getIndexOfKey(key);
        if (array == null) {
            array = new Object[2];
            array[0] = key;
            array[1] = value;
            return null;
        } else if (index >= 0) {
            // the exact key was found
            V prev = (V) array[index + 1];
            array[index + 1] = value;
            return prev;
        } else {
            // the index where the new element should be
            index = -index - 2;
            Object[] newArray = new Object[array.length + 2];
            if (index == 0) {
                System.arraycopy(array, 0, newArray, 2, array.length);
            } else if (index >= array.length) {
                System.arraycopy(array, 0, newArray, 0, array.length);
                index = array.length;
            } else {
                if (index > 1) {
                    System.arraycopy(array, 0, newArray, 0, index);
                }
                if (index < array.length) {
                    System.arraycopy(array, index, newArray, index + 2, array.length - index);
                }
            }
            array = newArray;
            array[index] = key;
            array[index + 1] = value;
            return null;
        }
    }

    /** @return index if found, -index-2 if not found */
    @Override
    protected int getIndexOfKey(K key) {
        if (array == null || array.length == 0) {
            return -2;
        }
        
        final int length = array.length;
        int range = (length == 2) ? 0 : (length / 2) & PAIR_MASK;
        int idx = range;
        do {
            int cmp = ((Comparable<K>) key).compareTo((K) array[idx]);
            if (cmp == 0) {
                return idx;
            }
            idx += (cmp > 0) ? range: -range;
            if (range < 2) {
                return -idx - ((cmp > 0) ? 4 : 2);
            }
            if (idx < 0) {
                return -2;
            }
            if (idx >= length) {
                idx = length - 2;
            }
            range = (range >> 1) & PAIR_MASK;
        } while (true);
    }
    
}
