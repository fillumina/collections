package com.fillumina.collections;

import java.util.Map;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SortedArrayMapGTest extends GenericMapTest {

    @Override
    protected <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> create(Map<K, V> m) {
        return new SortedArrayMap<>(m);
    }

}
