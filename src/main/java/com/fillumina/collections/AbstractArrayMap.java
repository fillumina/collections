package com.fillumina.collections;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public abstract class AbstractArrayMap<K, V> extends AbstractMap<K, V> 
    implements Iterable<Map.Entry<K, V>> {

    public static class Builder<M extends AbstractArrayMap<K,V>,K,V> {
        private final List<Object> list = new ArrayList<>();
        private final Function<Object[],M> creator;

        public Builder(Function<Object[],M> creator) {
            this.creator = creator;
        }
        
        public Builder<M,K,V> put(K key, V value) {
            list.add(key);
            list.add(value);
            return this;
        }
        
        public M build() {
            return creator.apply(list.toArray());
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
    
    protected Object[] array;
    protected Set<Entry<K, V>> entrySet;

    public AbstractArrayMap() {
    }

    public AbstractArrayMap(AbstractArrayMap<K,V> copy) {
        array = copy.array == null ? null : copy.array.clone();
    }

    /**
     * Warning: unchecked copy!
     */
    public AbstractArrayMap(Object... o) {
        array = o.clone();
    }

    public AbstractArrayMap(Map<K, V> map) {
        array = new Object[map.size() << 1];
        int idx = 0;
        for (Entry<K, V> e : map.entrySet()) {
            array[idx] = e.getKey();
            array[idx + 1] = e.getValue();
            idx += 2;
        }
    }
    
    /** Override to provide immutability. */
    protected void readOnlyCheck() throws UnsupportedOperationException {
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
        return array.clone();
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
