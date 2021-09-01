package com.fillumina.collections;

import java.util.Collection;

/**
 * An immutable array set. This set guarantees its immutability and can be safely shared
 * between objects. It maintains insertion order and uses very little memory but it's quite slow
 * to access.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public final class ImmutableSmallSet<T> extends SmallSet<T> {

    private static final long serialVersionUID = 1L;


    @SuppressWarnings("unchecked")
    public static <T> ImmutableSmallSet<T> empty() {
        return (ImmutableSmallSet<T>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableSmallSet<T> of(T... values) {
        if (values == null || values.length == 0) {
            return (ImmutableSmallSet<T>) EMPTY;
        }
        return new ImmutableSmallSet<T>(values);
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableSmallSet<T> of(Collection<? extends T> collection) {
        if (collection == null || collection.isEmpty()) {
            return (ImmutableSmallSet<T>) EMPTY;
        }
        if (collection instanceof ImmutableList) {
            return (ImmutableSmallSet<T>) collection;
        }
        return new ImmutableSmallSet<T>(collection);
    }

    public ImmutableSmallSet() {
        super();
    }

    @SuppressWarnings("unchecked")
    public ImmutableSmallSet(T... elements) {
        super(elements);
    }

    public ImmutableSmallSet(SmallSet<T> arraySet) {
        super(arraySet);
    }

    public ImmutableSmallSet(Collection<? extends T> elements) {
        super(elements);
    }

    @Override
    protected void readOnlyCheck() {
        throw new UnsupportedOperationException("read only");
    }

}
