package com.fillumina.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BaseArrayMapValuesGTest extends GenericCollectionTest {

    @Override
    protected boolean isReadOnly() {
        return true;
    }

    @Override
    protected <T extends Comparable<T>> Collection<T> create(Collection<T> collection) {
        Map<String,T> m = new HashMap<>();
        for (T t: collection) {
            m.put("" + t, t);
        }
        Map<String,T> map = new BaseArrayMap<>(m);
        return map.values();
    }

}
