package com.fillumina.collections;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A very minimal size map backed by a single array containing interleaved keys and values. Ideal as
 * an immutable object containing few entries to pass pairs of values around without having to
 * revert to a full blown {@link HashMap} which has a far bigger memory footprint.
 * <p>
 * It doens't support {@link #put(java.lang.Object, java.lang.Object) } operations.
 * It's extended by:
 * <ul>
 * <li>{@link ArrayMap} which is backed by a simple array with access time of O(N)
 * <li>{@link SortedArrayMap} which is backed by a key-sorted array with access time of O(LogN)
 * </ul>
 * <p>
 * Instead of <i>entries</i> it uses a <i>cursor</i> that is a single <i>mutable</i>
 * {@link Map.Entry}: for this reason <b>don't use its {@link Map.Entry} outside loops and never
 * save them!</b>
 * <p>
 * Every access is O(n). The map keeps insertion order until a sorting method is called. It's not
 * thread safe.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BaseArrayMap<K, V> extends AbstractMap<K, V>
        implements Iterable<Map.Entry<K, V>> {

    public class CursorListIterator<K, V> implements ListIterator<Entry<K, V>>, Entry<K, V> {

        private int index;

        public CursorListIterator() {
            this(0);
        }

        public CursorListIterator(int index) {
            this.index = index - 2;
        }

        @Override
        public boolean hasNext() {
            return array == null ? false : index < array.length - 2;
        }

        @Override
        public boolean hasPrevious() {
            return array == null ? false : index > 1;
        }

        @Override
        public Entry<K, V> next() {
            index += 2;
            if (index > array.length) {
                throw new NoSuchElementException();
            }
            return this;
        }

        @Override
        public Entry<K, V> previous() {
            index -= 2;
            if (index < 0) {
                throw new NoSuchElementException();
            }
            return this;
        }

        @Override
        public int nextIndex() {
            return index + 2;
        }

        @Override
        public int previousIndex() {
            return index - 2;
        }

        @Override
        public void remove() {
            removeEntryAtAbsoluteIndex(index);
        }

        @Override
        @SuppressWarnings("unchecked")
        public K getKey() {
            return (K) array[index];
        }

        @Override
        @SuppressWarnings("unchecked")
        public V getValue() {
            return (V) array[index + 1];
        }

        @Override
        @SuppressWarnings("unchecked")
        public V setValue(V value) {
            readOnlyCheck();
            V prev = (V) array[index + 1];
            array[index + 1] = value;
            return prev;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void set(Entry<K, V> e) {
            readOnlyCheck();
            array[index] = e.getKey();
            array[index + 1] = e.getValue();
        }

        /** Not supported! */
        @Override
        public void add(Entry<K, V> e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        /**
         * Implementation copied from {@link java.util.HashMap.Node#hashCode()}.
         * Must be this code otherwise cannot compare to other map implementations.
         */
        @Override
        public int hashCode() {
            return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
        }

        /**
         * Compares with other {@link Map.Entry} objects.
         * <b>DO NOT COMPARE WITH A CURSOR FROM THE SAME COLLECTION!</b>
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Entry)) {
                return false;
            }
            final Entry<?, ?> other = (Entry<?, ?>) obj;
            if (!Objects.equals(this.getKey(), other.getKey())) {
                return false;
            }
            if (!Objects.equals(this.getValue(), other.getValue())) {
                return false;
            }
            return true;
        }
    }

    private class PairEntrySet<K, V> extends AbstractSet<Entry<K, V>> {

        @Override
        public CursorListIterator<K, V> iterator() {
            return new CursorListIterator<>();
        }

        @Override
        public int size() {
            return array.length / 2;
        }

        @Override
        public Spliterator<Entry<K, V>> spliterator() {
            return new PairSpliterator<>();
        }
    }

    private class PairSpliterator<K, V> implements Spliterator<Entry<K, V>>, Entry<K, V> {

        private int startIdx;
        private int endIdx;

        public PairSpliterator() {
            this(0, array.length); // using full length here
        }

        public PairSpliterator(int startIdx, int endIdx) {
            this.startIdx = startIdx;
            this.endIdx = endIdx;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Entry<K, V>> action) {
            if (startIdx < endIdx) {
                action.accept(this);
                this.startIdx += 2;
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<Entry<K, V>> trySplit() {
            if (estimateSize() > 1) {
                int half = ((startIdx + endIdx) / 2) & (Integer.MAX_VALUE - 1);
                if (half != startIdx && half != endIdx) {
                    int splittedEnd = endIdx;
                    this.endIdx = half;
                    return new PairSpliterator<>(half, splittedEnd);
                }
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return (endIdx - startIdx) / 2;
        }

        @Override
        public int characteristics() {
            return Spliterator.DISTINCT;
        }

        @Override
        @SuppressWarnings("unchecked")
        public K getKey() {
            return (K) array[startIdx];
        }

        @Override
        @SuppressWarnings("unchecked")
        public V getValue() {
            return (V) array[startIdx + 1];
        }

        @Override
        @SuppressWarnings("unchecked")
        public V setValue(V value) {
            readOnlyCheck();
            V old = (V) array[startIdx + 1];
            array[startIdx + 1] = value;
            return old;
        }
    }

    protected Object[] array;
    protected PairEntrySet<K, V> entrySet;

    public BaseArrayMap() {
    }

    public BaseArrayMap(BaseArrayMap<? extends K, ? extends V> copy) {
        array = (copy.array == null) ? null : copy.array.clone();
    }

    /**
     * Warning: unchecked copy!
     */
    public BaseArrayMap(Object... objectArray) {
        array = objectArray;
    }

    /**
     * Warning: unchecked copy!
     */
    public BaseArrayMap(Collection<?> collection) {
        array = collection.toArray();
    }

    /**
     * Warning: unchecked copy!
     */
    public BaseArrayMap(Iterable<?> iterable) {
        List<Object> list = new ArrayList<>();
        Iterator<?> it = iterable.iterator();
        while (it.hasNext()) {
            list.add(it.next());
        }
        array = list.toArray();
    }

    public BaseArrayMap(Map<? extends K, ? extends V> map) {
        array = new Object[map.size() * 2];
        int idx = 0;
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            array[idx] = e.getKey();
            array[idx + 1] = e.getValue();
            idx += 2;
        }
    }

    /**
     * Override to provide immutability.
     */
    protected void readOnlyCheck() throws UnsupportedOperationException {
    }

    @SuppressWarnings("unchecked")
    public boolean containsEntry(K k, V v) {
        final int idx = getAbsoluteIndexOfKey((K) k);
        return (idx < 0) ? false : Objects.equals(v, (V) array[idx + 1]);
    }

    public BaseArrayMap<K, V> assertEntry(K k, V v) throws AssertionError {
        if (!containsEntry(k, v)) {
            throw new AssertionError("entry not present: key=" + k + " => value=" + v);
        }
        return this;
    }

    public BaseArrayMap<K, V> assertSize(int size) throws AssertionError {
        if (size != size()) {
            throw new AssertionError("expected size=" + size + " but was " + size());
        }
        return this;
    }

    @Override
    public int size() {
        return array == null ? 0 : array.length >> 1;
    }

    @Override
    public void clear() {
        readOnlyCheck();
        array = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        final int idx = getAbsoluteIndexOfKey((K) key);
        return idx < 0 ? null : (V) array[idx + 1];
    }

    /**
     * <b>NOTE: don't compare to -1 because overloading methods might use negative values!</b>
     *
     * @return < 0 if not found otherwise the absolute index of the key in the array.
     */
    protected int getAbsoluteIndexOfKey(K key) {
        if (array == null) {
            return -1;
        }
        for (int i = array.length - 2; i >= 0; i -= 2) {
            if (key.equals(array[i])) {
                return i;
            }
        }
        return -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        readOnlyCheck();
        int pos = getAbsoluteIndexOfKey((K) key);
        if (pos >= 0) {
            return removeEntryAtAbsoluteIndex(pos);
        }
        return null;
    }

    protected V removeEntryAtAbsoluteIndex(int index) {
        readOnlyCheck();
        if (array == null || index < 0 || index > array.length - 2) {
            throw new IndexOutOfBoundsException("index=" + index + ", array length=" +
                    (array == null ? 0 : array.length));
        } else {
            Object[] newArray = new Object[array.length - 2];
            @SuppressWarnings("unchecked")
            V prev = (V) array[index + 1];
            if (index == 0) {
                System.arraycopy(array, 2, newArray, 0, array.length - 2);
                array = newArray;
            } else if (index == array.length - 1) {
                System.arraycopy(array, 0, newArray, 0, array.length - 2);
                array = newArray;
            } else {
                System.arraycopy(array, 0, newArray, 0, index);
                System.arraycopy(array, index + 2, newArray, index,
                        array.length - index - 2);
                array = newArray;
            }
            return prev;
        }
    }

    public Object[] toArray() {
        return array == null ? null : array.clone();
    }

    @Override
    public CursorListIterator<K, V> iterator() {
        return new CursorListIterator<>();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new PairEntrySet<K, V>();
        }
        return entrySet;
    }

    /** Implements a very simple bubble sort (fast for few entries). */
    @SuppressWarnings("unchecked")
    protected void sortByKeys(Comparator<K> comparator) {
        // readOnlyCheck(); it's used by SortedArrayMap to manage immutable objets
        if (array == null) {
            return;
        }
        Object[] larray = array; // for faster operations
        boolean swapped;
        do {
            swapped = false;
            for (int i = larray.length - 4; i >= 0; i -= 2) {
                if (comparator.compare((K) larray[i], (K) larray[i + 2]) > 0) {
                    Object tmpKey = larray[i];
                    Object tmpValue = larray[i+1];
                    larray[i] = larray[i+2];
                    larray[i+1] = larray[i+3];
                    larray[i+2] = tmpKey;
                    larray[i+3] = tmpValue;
                    swapped = true;
                }
            }
        } while (swapped);
    }

    // equals(), hashCode() and toString() are all inherited from AbstractMap
}
