package com.fillumina.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An immutable linked hash set. This set guarantees its immutability and can be safely shared
 * between objects. It maintains insertion order.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public final class ImmutableLinkedTableSet<T> implements Set<T> {

    public static final ImmutableLinkedTableSet<?> EMPTY = new ImmutableLinkedTableSet<Object>();

    private static class LinkedEntry<K> implements Map.Entry<K, Integer> {
        private final K key;
        private final int index;
        private LinkedEntry<K> next;

        public LinkedEntry(K value, int index) {
            this.key = value;
            this.index = index;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public Integer getValue() {
            return index;
        }

        @Override
        public Integer setValue(Integer value) {
            throw new UnsupportedOperationException();
        }
    }

    private static class LinkedEntryMap<K>
            extends AbstractEntryMap<K, Integer, LinkedEntry<K>, VieweableMap<K, Integer>> {
        
        private LinkedEntry<K> head;
        
        public LinkedEntryMap(K[] values) {
            super(values.length);
            if (values.length != 0) {
                int index = 0;
                LinkedEntry<K> current = null;
                for (K k : values) {
                    if (!containsKey(k)) {
                        LinkedEntry<K> entry = new LinkedEntry<>(k, index);
                        innerPutEntry(entry);
                        if (head == null) {
                            head = entry;
                        } else {
                            current.next = entry;
                        }
                        current = entry;
                        index++;
                    }
                }
            }
        }

        @Override
        protected void readOnlyCheck() {
            throw new UnsupportedOperationException("read only");
        }
        
        @Override
        protected LinkedEntry<K> createEntry(K k, Integer v) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        protected LinkedEntryMap<K> createMap(int size) {
            throw new UnsupportedOperationException("not used");
        }
    }
    
    
    public static <T> ImmutableLinkedTableSet<T> empty() {
        return (ImmutableLinkedTableSet<T>) EMPTY;
    }

    public static <T> ImmutableLinkedTableSet<T> of(T... values) {
        if (values == null || values.length == 0) {
            return (ImmutableLinkedTableSet<T>) EMPTY;
        }
        return new ImmutableLinkedTableSet<T>(values);
    }

    public static <T> ImmutableLinkedTableSet<T> of(Collection<? extends T> list) {
        if (list == null || list.isEmpty()) {
            return (ImmutableLinkedTableSet<T>) EMPTY;
        }
        if (list instanceof ImmutableList) {
            return (ImmutableLinkedTableSet<T>) list;
        }
        return new ImmutableLinkedTableSet<T>(list);
    }

    private LinkedEntryMap<T> delegate;

    // for kryo
    public ImmutableLinkedTableSet() {
        delegate = new LinkedEntryMap<>((T[])new Object[0]);
    }
    
    public ImmutableLinkedTableSet(T... elements) {
        delegate = new LinkedEntryMap<>(elements);
    }

    public ImmutableLinkedTableSet(Collection<? extends T> collection) {
        delegate = new LinkedEntryMap<>((T[])collection.toArray());
    }
    
    /** This operation takes O(n) */
    public T get(int index) {
        Iterator<T> it = iterator();
        for (int i=0; i<index; i++) {
            it.next();
        }
        return it.next();
    }

    /** This operation takes O(1) */
    public int indexOf(T value) {
        LinkedEntry<T> entry = delegate.getEntry(value);
        return entry == null ? -1 : entry.index;
    }
    
    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.containsKey(o);
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private LinkedEntry<T> current = delegate.head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                T value = current.getKey();
                current = current.next;
                return value;
            }
        };
    }

    @Override
    public Object[] toArray() {
        return toArray(new Object[size()]);
    }

    @Override
    public <S> S[] toArray(S[] a) {
        int index = 0;
        for (T t : this) {
            a[index] = (S)t;
            index++;
        }
        return a;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("read only");
    }

    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return "[" + stream()
                .map(t -> Objects.toString(t))
                .collect(Collectors.joining(",")) + "]";
    }
}
