package com.fillumina.collections;

import java.util.AbstractList;
import java.util.List;

/**
 * An immutable list. This list guarantee its immutability and can be safely shared between objects.
 * 
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableList<T> extends AbstractList<T> {
    
    public static final ImmutableList<?> EMPTY = new ImmutableList<Object>();
    private static final Object[] EMPTY_ARRAY = new Object[0];
    
    public static <T> ImmutableList<T> of(T... values) {
        if (values == null || values.length == 0) {
            return (ImmutableList<T>) EMPTY;
        }
        return new ImmutableList<T>(values);
    }
    
    public static <T> ImmutableList<T> of(List<? extends T> list) {
        if (list == null || list.isEmpty()) {
            return (ImmutableList<T>) EMPTY;
        }
        if (list instanceof ImmutableList) {
            return (ImmutableList<T>) list;
        }
        return new ImmutableList<T>(list);
    }
    
    private final T[] array;

    private ImmutableList(T... array) {
        this.array = array == null ? null : array.clone();
    }

    public ImmutableList(List<? extends T> list) {
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
