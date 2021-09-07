package com.fillumina.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BaseArrayMapKeySetGTest extends GenericSetTest {

    @Override
    protected <T extends Comparable<T>> Set<T> create(Collection<T> collection) {
        Map<T,String> m = new HashMap<>();
        for (T t: collection) {
            m.put(t, ""+t);
        }
        Map<T,String> map = new BaseArrayMap<>(m);
        return map.keySet();
    }

    @Override
    protected boolean isReadOnly() {
        return true;
    }


}
