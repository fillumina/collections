package com.fillumina.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class EmptyIterator<T> implements Iterator<T> {

    public static final EmptyIterator<?> INSTANCE = new EmptyIterator<Object>();
    
    public static <T> Iterator<T> empty() {
        return (Iterator<T>) INSTANCE;
    }
    
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public T next() {
        throw new NoSuchElementException();
    }
}
