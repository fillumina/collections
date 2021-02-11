package com.fillumina.collections;

import java.util.Comparator;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public interface LinkedSet<T> extends ImmutableLinkedSet<T> {
    
    T removeAtIndex(int i);
    void sort(Comparator<T> comparator);
    void sort();
}
