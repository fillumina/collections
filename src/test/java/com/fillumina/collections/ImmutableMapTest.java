package com.fillumina.collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableMapTest {

    @Test
    public void testOfValues() {
        Map<Integer, String> map = ImmutableMap
                .of(1, "one", 2, "two", 3, "three");

        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }

    @Test
    public void testOfMap() {
        Map<Integer, String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");
        
        Map<Integer, String> map = ImmutableMap.of(m);

        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }
    
    @Test
    public void testBuilder() {
        Map<Integer, String> map = ImmutableMap.<Integer, String>builder()
                .add(1, "one")
                .add(2, "two")
                .add(3, "three")
                .immutable();

        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }

    @Test
    public void shouldNotModify() {
        Map<Integer, String> map = ImmutableMap.<Integer, String>builder()
                .add(1, "one")
                .add(2, "two")
                .add(3, "three")
                .immutable();

        assertThrows(UnsupportedOperationException.class, () -> map.put(4, "four"));
        assertThrows(UnsupportedOperationException.class, () -> map.remove(2));
        assertThrows(UnsupportedOperationException.class, () -> map.clear());
        assertThrows(UnsupportedOperationException.class, () -> map.keySet().clear());
        assertThrows(UnsupportedOperationException.class, () -> map.values().clear());
        assertThrows(UnsupportedOperationException.class, () -> {
            Iterator<String> it = map.values().iterator();
            it.next();
            it.remove();
        });
        assertThrows(UnsupportedOperationException.class, () -> {
            Iterator<Integer> it = map.keySet().iterator();
            it.next();
            it.remove();
        });
    }

}