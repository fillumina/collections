package com.fillumina.collections;

import java.util.Map;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BiMapInverseGTest extends GenericMapTest {

    @Override
    protected <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> create(Map<K, V> m) {
        BiMap<V,K> reversedMap = new BiMap<>();
        Map<K,V> map = reversedMap.inverse();
        map.putAll(m);
        return map;
    }

}
