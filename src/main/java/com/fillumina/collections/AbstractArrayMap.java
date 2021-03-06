package com.fillumina.collections;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A very minimal size map backed by a single array interleaved with both keys and values. It should
 * be used mainly as a small immutable object useful to pass pairs of values around without having
 * to revert to a full blown {@link HashMap} which can use quite a lot of memory. It's very fast to
 * clone. Instead of <i>entries</i> it uses a <i>cursor</i> that is a single <i>mutable</i>
 * {@link Map.Entry}: for this reason <b>don't use its {@link Map.Entry} outside loops and never
 * save them!</b> Every access is O(n). The map keeps insertion order until a sorting method is
 * called. It's not thread safe.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public abstract class AbstractArrayMap<K, V> extends AbstractMap<K, V>
        implements Iterable<Map.Entry<K, V>> {

    private class CursorIterator<K, V> implements Iterator<Entry<K, V>>, Entry<K, V> {

        private int index;

        public CursorIterator() {
            this(0);
        }

        public CursorIterator(int index) {
            this.index = index - 2;
        }

        @Override
        public boolean hasNext() {
            return array == null ? false : index < array.length - 2;
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
        public void remove() {
            removeAtIndex(index);
        }

        @Override
        public K getKey() {
            return (K) array[index];
        }

        @Override
        public V getValue() {
            return (V) array[index + 1];
        }

        @Override
        public V setValue(V value) {
            readOnlyCheck();
            V prev = (V) array[index + 1];
            array[index + 1] = value;
            return prev;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 19 * hash + this.index;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CursorIterator<?, ?> other = (CursorIterator<?, ?>) obj;
            if (this.index != other.index) {
                return false;
            }
            return true;
        }
    }

    private class PairEntrySet<K, V> extends AbstractSet<Entry<K, V>> {

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new CursorIterator<>();
        }

        @Override
        public int size() {
            return array.length;
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
            this(0, array.length);
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
        public K getKey() {
            return (K) array[startIdx];
        }

        @Override
        public V getValue() {
            return (V) array[startIdx + 1];
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("cannot set into splititerator");
        }
    }

    protected Object[] array;
    protected Set<Entry<K, V>> entrySet;

    public AbstractArrayMap() {
    }

    public AbstractArrayMap(AbstractArrayMap<? extends K, ? extends V> copy) {
        array = copy.array == null ? null : copy.array.clone();
    }

    /**
     * Warning: unchecked copy!
     */
    public AbstractArrayMap(Object... o) {
        array = o.clone();
    }

    /**
     * Warning: unchecked copy!
     */
    protected AbstractArrayMap(List<?> list) {
        array = list.toArray();
    }

    public AbstractArrayMap(Map<? extends K, ? extends V> map) {
        array = new Object[map.size() << 1];
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

    public boolean containsEntry(K k, V v) {
        return v.equals(get(k));
    }

    public AbstractArrayMap<K, V> assertEntry(K k, V v) throws AssertionError {
        if (!containsEntry(k, v)) {
            throw new AssertionError("entry not present: key=" + k + " => value=" + v);
        }
        return this;
    }

    public AbstractArrayMap<K, V> assertSize(int size) throws AssertionError {
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
    public V get(Object key) {
        final int idx = getIndexOfKey((K) key);
        return idx < 0 ? null : (V) array[idx + 1];
    }

    protected int getIndexOfKey(K key) {
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
    public V remove(Object key) {
        readOnlyCheck();
        int pos = getIndexOfKey((K) key);
        if (pos >= 0) {
            return removeAtIndex(pos);
        }
        return null;
    }

    protected V removeAtIndex(int index) {
        readOnlyCheck();
        if (index < 0) {
            return null;
        } else {
            Object[] newArray = new Object[array.length - 2];
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
    public Iterator<Entry<K, V>> iterator() {
        return new CursorIterator();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new PairEntrySet<K, V>();
        }
        return entrySet;
    }

    /**
     * Implements a very simple bubble sort.
     */
    protected void sortByKeys(Comparator<K> comparator) {
        //readOnlyCheck();
        if (array == null) {
            return;
        }
        boolean swapped;
        do {
            swapped = false;
            for (int i = array.length - 4; i >= 0; i -= 2) {
                if (comparator.compare((K) array[i], (K) array[i + 2]) > 0) {
                    swap(i, i + 2);
                    swapped = true;
                }
            }
        } while (swapped);
    }

    protected void swap(int a, int b) {
        Object tmpKey = array[a];
        Object tmpValue = array[a + 1];
        array[a] = array[b];
        array[a + 1] = array[b + 1];
        array[b] = tmpKey;
        array[b + 1] = tmpValue;
    }

}
