package com.fillumina.collections;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class TableMapValuesGTest extends GenericCollectionTest {

    @Override
    protected boolean isReadOnly() {
        return true;
    }

    @Override
    protected <T extends Comparable<T>> Collection<T> create(Collection<T> collection) {
        Map<String,T> map = new TableMap<>();
        for (T t: collection) {
            map.put(""+t, t);
        }
        return map.values();
    }


}
