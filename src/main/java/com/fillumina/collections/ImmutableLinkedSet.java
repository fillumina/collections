package com.fillumina.collections;

import java.util.Set;

/**
 * It's a marker interface that states that the set is immutable and keeps input order.
 * 
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public interface ImmutableLinkedSet<T> extends Set<T> {
    
    T get(int index);
    int indexOf(T t);
}
