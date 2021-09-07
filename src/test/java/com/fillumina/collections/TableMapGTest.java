package com.fillumina.collections;

import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class TableMapGTest extends GenericMapTest {

    @Override
    protected <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> create(Map<K, V> m) {
        return new TableMap<>(m);
    }

    @Override
    public void shouldNotAddToEntrySet() {
        // this map can add into entrySet, check that

        Map<Integer,String> map = new TableMap<>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));

        Set<Map.Entry<Integer,String>> entries = map.entrySet();
        entries.add(new ImmutableMapEntry<>(4, "four"));
        assertEquals(4, map.size());
        assertEquals("four", map.get(4));
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }

    @Override
    public void shouldNotAddToKeySet() {
        // this map can add into keySet, check that

        Map<Integer,String> map = new TableMap<>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));

        Set<Integer> keys = map.keySet();
        keys.add(4);
        assertEquals(4, map.size());
        assertTrue(map.containsKey(4));
        assertEquals(null, map.get(4));
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }

}
