package com.fillumina.collections;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * This is a very minimal size map backed by a simple array. It should be used mainly as a small
 * immutable object. It's easy to clone and easy to pass and save. It's characteristic is that
 * entries are in fact <i>cursors</i> in which they are <i>mutable</i> objects. Don't use it in a
 * parallel stream or with many entries. Every operation is O(n) so very inefficient, use only for
 * iteration over pairs.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PairMap<K, V> extends AbstractMap<K, V> implements Iterable<Entry<K, V>> {

    public static class Immutable<K, V> extends PairMap<K, V> {

        public Immutable() {
        }

        public Immutable(PairMap<K,V> copy) {
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
            return index < array.length-2;
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
    }

    private Object[] array;
    private Set<Entry<K, V>> entrySet;

    public PairMap() {
    }

    public PairMap(PairMap<K,V> copy) {
        array = copy.array == null ? null : copy.array.clone();
    }

    /**
     * Warning: unchecked copy!
     */
    public PairMap(Object... o) {
        array = o.clone();
    }

    public PairMap(Map<K, V> map) {
        array = new Object[map.size() << 1];
        int idx = 0;
        for (Entry<K, V> e : map.entrySet()) {
            array[idx] = e.getKey();
            array[idx + 1] = e.getValue();
            idx += 2;
        }
    }

    @Override
    public int size() {
        return array == null ? 0 : array.length >> 1;
    }

    @Override
    public void clear() {
        array = null;
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

    protected void readOnlyCheck() throws UnsupportedOperationException {
    }

    @Override
    public V remove(Object key) {
        readOnlyCheck();
        int pos = getIndexOfKey((K) key);
        return removeAtIndex(pos);
    }

    private V removeAtIndex(int index) {
        readOnlyCheck();
        if (index == -1) {
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

    public int getIndexOfKey(K key) {
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

    public Object[] toArray() {
        return array.clone();
    }

    public PairMap<K,V> immutable() {
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

    /** A new entry is created at each call!! */
    public Entry<K,V> getEntryAtIndex(int index) {
        return new SimpleEntry<>(getKeyAtIndex(index), getValueAtIndex(index));
    }

    /** This object is <b>highly</b> volatile and can change its value if the map is changed. */
    public Entry<K,V> getCursorAtIndex(int index) {
        return new CursorIterator<>(index);
    }
    
    /**
     * Implements a very simple bubble sort.
     */
    public void sortByKeys(Comparator<K> comparator) {
        readOnlyCheck();
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

    /**
     * Implements a very simple bubble sort.
     */
    public void sortByValues(Comparator<V> comparator) {
        readOnlyCheck();
        boolean swapped;
        do {
            swapped = false;
            for (int i = array.length - 3; i > 0; i -= 2) {
                if (comparator.compare((V) array[i], (V) array[i + 2]) > 0) {
                    swap(i-1, i + 1);
                    swapped = true;
                }
            }
        } while (swapped);
    }

    private void swap(int a, int b) {
        Object tmpKey = array[a];
        Object tmpValue = array[a + 1];
        array[a] = array[b];
        array[a + 1] = array[b + 1];
        array[b] = tmpKey;
        array[b + 1] = tmpValue;
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

}
