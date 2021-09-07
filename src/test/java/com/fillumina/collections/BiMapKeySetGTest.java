package com.fillumina.collections;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BiMapKeySetGTest extends GenericSetTest {

    @Override
    protected <T extends Comparable<T>> Set<T> create(Collection<T> collection) {
        BiMap<T,String> map = new BiMap<>();
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
