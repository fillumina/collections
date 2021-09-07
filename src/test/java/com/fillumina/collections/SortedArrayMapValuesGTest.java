package com.fillumina.collections;

import java.util.Collection;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SortedArrayMapValuesGTest extends GenericCollectionTest {

    @Override
    protected boolean isReadOnly() {
        return true;
    }

    @Override
    protected <T extends Comparable<T>> Collection<T> create(Collection<T> collection) {
        SortedArrayMap<String,T> map = new SortedArrayMap<>();
        for (T t: collection) {
            map.put(""+t, t);
        }
        return map.values();
    }

}
