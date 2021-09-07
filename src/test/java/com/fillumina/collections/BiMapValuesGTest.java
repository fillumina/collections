package com.fillumina.collections;

import java.util.Collection;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BiMapValuesGTest extends GenericCollectionTest {

    @Override
    protected <T extends Comparable<T>> Collection<T> create(Collection<T> collection) {
        BiMap<String,T> map = new BiMap<>();
        for (T t: collection) {
            map.put(""+t, t);
        }
        return map.values();
    }

    @Override
    protected boolean isReadOnly() {
        return true;
    }

}
