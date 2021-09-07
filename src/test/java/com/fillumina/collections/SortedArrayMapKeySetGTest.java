package com.fillumina.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SortedArrayMapKeySetGTest extends GenericSetTest {

    @Override
    protected <T extends Comparable<T>> Set<T> create(Collection<T> collection) {
        Map<T,String> map = new SortedArrayMap<T,String>();
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
