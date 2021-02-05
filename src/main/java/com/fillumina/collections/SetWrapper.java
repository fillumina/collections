package com.fillumina.collections;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SetWrapper<T> implements Set<T> {
    private static final int CUT_OFF = 7;
    
    private Set<T> delegate;

    public SetWrapper() {
        delegate = new ArraySet<>();
    }

    public SetWrapper(T... array) {
        if (array.length > CUT_OFF) {
            delegate = KeyOnlyMap.createSet(array); 
        } else {
            delegate = new ArraySet<>(array);
        }
    }
    
    public SetWrapper(Collection<T> coll) {
        if (coll.size() > CUT_OFF) {
            delegate = KeyOnlyMap.createSet(coll); 
        } else {
            delegate = new ArraySet<>(coll);
        }
    }

    /**
     * Starts with a very small and efficient for few elements set and
     * eventually grows into a full blown, faster and more efficient for
     * many elements one.
     * 
     * @param e
     * @return 
     */
    @Override
    public boolean add(T e) {
        if (size() > CUT_OFF) {
            Set<T> set = KeyOnlyMap.createSet(CUT_OFF + 2);
            set.addAll(this);
            delegate = set;
        }
        return delegate.add(e);
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
        return delegate.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(
            Collection<?> clctn) {
        return delegate.containsAll(clctn);
    }

    @Override
    public boolean addAll(
            Collection<? extends T> clctn) {
        return delegate.addAll(clctn);
    }

    @Override
    public boolean retainAll(
            Collection<?> clctn) {
        return delegate.retainAll(clctn);
    }

    @Override
    public boolean removeAll(
            Collection<?> clctn) {
        return delegate.removeAll(clctn);
    }

    @Override
    public void clear() {
        delegate.clear();
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
    public Spliterator<T> spliterator() {
        return delegate.spliterator();
    }

    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        return delegate.toArray(generator);
    }

    @Override
    public boolean removeIf(
            Predicate<? super T> filter) {
        return delegate.removeIf(filter);
    }

    @Override
    public Stream<T> stream() {
        return delegate.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return delegate.parallelStream();
    }

    @Override
    public void forEach(
            Consumer<? super T> action) {
        delegate.forEach(action);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
    
}
