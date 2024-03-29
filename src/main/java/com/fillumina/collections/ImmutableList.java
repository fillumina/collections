package com.fillumina.collections;

import java.util.AbstractList;
import java.util.Collection;

/**
 * An immutable list. This list guarantees its immutability and can be safely shared between
 * objects.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public final class ImmutableList<T> extends AbstractList<T> {

    public static final ImmutableList<?> EMPTY = new ImmutableList<Object>();
    private static final Object[] EMPTY_ARRAY = new Object[0];

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> empty() {
        return (ImmutableList<T>) EMPTY;
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> of(T... values) {
        if (values == null || values.length == 0) {
            return (ImmutableList<T>) EMPTY;
        }
        return new ImmutableList<T>(values);
    }

    @SuppressWarnings("unchecked")
    public static <T> ImmutableList<T> of(Collection<? extends T> list) {
        if (list == null || list.isEmpty()) {
            return (ImmutableList<T>) EMPTY;
        }
        if (list instanceof ImmutableList) {
            return (ImmutableList<T>) list;
        }
        return new ImmutableList<T>(list);
    }

    private T[] array;

    // for kryo
    private ImmutableList() {
    }

    @SuppressWarnings("unchecked")
    public ImmutableList(T... array) {
        this.array = array == null ? null : array.clone();
    }

    @SuppressWarnings("unchecked")
    public ImmutableList(Collection<? extends T> list) {
        this.array = (list == null || list.isEmpty()) ?
                (T[]) EMPTY_ARRAY :
                (T[]) list.toArray().clone();
    }

    @Override
    public T get(int index) {
        return array[index];
    }

    @Override
    public int size() {
        return array == null ? 0 : array.length;
    }
}
