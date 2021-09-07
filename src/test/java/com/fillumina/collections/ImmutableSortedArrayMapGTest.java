package com.fillumina.collections;

import java.util.Map;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableSortedArrayMapGTest extends GenericMapTest {

    @Override
    protected boolean isReadOnly() {
        return true;
    }

    @Override
    protected <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> create(Map<K, V> m) {
        return new ImmutableSortedArrayMap<>(m);
    }

}
