package com.fillumina.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BiMapInverseKeySetGTest extends GenericSetTest {

    @Override
    protected <T extends Comparable<T>> Set<T> create(Collection<T> collection) {
        BiMap<String,T> reversedMap = new BiMap<>();
        Map<T,String> map = reversedMap.inverse();
        for (T t: collection) {
            map.put(t, ""+t);
        }
        return map.keySet();
    }

    @Override
    protected boolean isReadOnly() {
        return true;
    }

}
