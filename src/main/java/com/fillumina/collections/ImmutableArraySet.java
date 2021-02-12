package com.fillumina.collections;

import java.util.Collection;

/**
 * An immutable array set. This set guarantees its immutability and can be safely shared
 * between objects. It maintains insertion order and uses very little memory but it's quite slow
 * to access.
 * 
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public final class ImmutableArraySet<T> extends ArraySet<T> {
    
    private static final long serialVersionUID = 1L;
    
    public static <T> ImmutableArraySet<T> empty() {
        return (ImmutableArraySet<T>) EMPTY;
    }

    public static <T> ImmutableArraySet<T> of(T... values) {
        if (values == null || values.length == 0) {
            return (ImmutableArraySet<T>) EMPTY;
        }
        return new ImmutableArraySet<T>(values);
    }

    public static <T> ImmutableArraySet<T> of(Collection<? extends T> collection) {
        if (collection == null || collection.isEmpty()) {
            return (ImmutableArraySet<T>) EMPTY;
        }
        if (collection instanceof ImmutableList) {
            return (ImmutableArraySet<T>) collection;
        }
        return new ImmutableArraySet<T>(collection);
    }
    
    public ImmutableArraySet() {
        super();
    }

    public ImmutableArraySet(T... elements) {
        super(elements);
    }

    public ImmutableArraySet(ArraySet<T> arraySet) {
        super(arraySet);
    }

    public ImmutableArraySet(Collection<? extends T> elements) {
        super(elements);
    }

    @Override
    protected void readOnlyCheck() {
        throw new UnsupportedOperationException("read only");
    }
    
}
