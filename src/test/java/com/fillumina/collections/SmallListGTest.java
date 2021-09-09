package com.fillumina.collections;

import java.util.Collection;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SmallListGTest extends GenericCollectionTest {

    @Override
    protected <T extends Comparable<T>> Collection<T> create(Collection<T> collection) {
        return new SmallList<>(collection);
    }

}
