package com.fillumina.collections;

import java.util.Collection;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableListGTest extends GenericCollectionTest {

    @Override
    protected boolean isReadOnly() {
        return true;
    }

    @Override
    protected <T extends Comparable<T>> Collection<T> create(Collection<T> collection) {
        return new ImmutableList<>(collection);
    }

}
