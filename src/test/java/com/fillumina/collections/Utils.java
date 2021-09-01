package com.fillumina.collections;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
// TODO fix unchecked or unsafe
class Utils {

    @SuppressWarnings("unchecked")
    public static <T> Set<T> setOf(T... array) {
        Set<T> set = new HashSet<>();
        for (T t : array) {
            set.add(t);
        }
        return Collections.unmodifiableSet(set);
    }

    @SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> mapOf(Object... array) {
        Map<K,V> map = new HashMap<>();
        for (int i=0; i<array.length; i+=2) {
            K key = (K) array[i];
            V value = (V) array[i+1];
            map.put(key, value);
        }
        return Collections.unmodifiableMap(map);
    }

}
