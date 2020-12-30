package com.fillumina.collections;

import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BiMapTest {

    @Test
    public void shouldPutItems() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        assertEquals(3, biMap.size());
        assertEquals(1, biMap.get("one"));
        assertEquals(2, biMap.get("two"));
        assertEquals(3, biMap.get("three"));
        
        assertEquals(3, biMap.inverse().size());
        assertEquals("one", biMap.inverse().get(1));
        assertEquals("two", biMap.inverse().get(2));
        assertEquals("three", biMap.inverse().get(3));
    }

    @Test
    public void shouldInversePutItems() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.inverse().put(1, "one");
        biMap.inverse().put(2, "two");
        biMap.inverse().put(3, "three");

        assertEquals(3, biMap.size());
        assertEquals(1, biMap.get("one"));
        assertEquals(2, biMap.get("two"));
        assertEquals(3, biMap.get("three"));
        
        assertEquals(3, biMap.inverse().size());
        assertEquals("one", biMap.inverse().get(1));
        assertEquals("two", biMap.inverse().get(2));
        assertEquals("three", biMap.inverse().get(3));
    }
    
    @Test
    public void shuouldChangeBothParts() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.put("A", 1);
        biMap.put("B", 1);
        biMap.put("A", 2);

        assertEquals(2, biMap.size());
        assertEquals(2, biMap.get("A"));
        assertEquals(1, biMap.get("B"));
        
        assertEquals(2, biMap.inverse().size());
        assertEquals("A", biMap.inverse().get(2));
        assertEquals("B", biMap.inverse().get(1));
    }
    
    public void shouldKeySetAndValuesBeingReciprocal() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        assertTrue(biMap.keySet().containsAll(
                biMap.inverse().values()));
        assertTrue(biMap.inverse().values().containsAll(
                biMap.keySet()));

        assertTrue(biMap.values().containsAll(
                biMap.inverse().keySet()));
        assertTrue(biMap.inverse().keySet().containsAll(
                biMap.values()));
    }
    
    @Test
    public void shouldRemoveKey() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.put("A", 1);
        biMap.put("B", 1);
        biMap.put("A", 2);

        biMap.remove("A");
        assertEquals(1, biMap.size());
        assertNull(biMap.get("A"));
        assertNull(biMap.inverse().get(2));
    }
    
    @Test
    public void shouldRemoveKeyInverse() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.put("A", 1);
        biMap.put("B", 1);
        biMap.put("A", 2);

        biMap.inverse().remove(2);
        assertEquals(1, biMap.size());
        assertNull(biMap.get("A"));
        assertNull(biMap.inverse().get(2));
    }
    
    @Test
    public void shouldClear() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.put("A", 1);
        biMap.put("B", 1);
        biMap.put("A", 2);

        biMap.clear();
        assertTrue(biMap.isEmpty());
        assertTrue(biMap.inverse().isEmpty());
    }
    
    @Test
    public void shouldClearInverse() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.put("A", 1);
        biMap.put("B", 1);
        biMap.put("A", 2);

        biMap.inverse().clear();
        assertTrue(biMap.isEmpty());
        assertTrue(biMap.inverse().isEmpty());
    }

    @Test
    public void shouldContainsValue() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);
        
        assertTrue(biMap.inverse().containsValue("one"));
        assertTrue(biMap.inverse().containsValue("two"));
        assertTrue(biMap.inverse().containsValue("three"));

        assertTrue(biMap.containsValue(1));
        assertTrue(biMap.containsValue(2));
        assertTrue(biMap.containsValue(3));
    }

    @Test
    public void shouldContainsValueInverse() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.inverse().put(1, "one");
        biMap.inverse().put(2, "two");
        biMap.inverse().put(3, "three");

        assertTrue(biMap.inverse().containsValue("one"));
        assertTrue(biMap.inverse().containsValue("two"));
        assertTrue(biMap.inverse().containsValue("three"));

        assertTrue(biMap.containsValue(1));
        assertTrue(biMap.containsValue(2));
        assertTrue(biMap.containsValue(3));
    }
    
    @Test
    public void shouldInvertInverted() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        assertEquals(1, biMap.inverse().inverse().get("one"));
    }
    
    @Test
    public void shouldRemoveFromValueCollection() {
        BiMap<String,Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        Set<Integer> values = biMap.values();
        values.remove(2);
        
        assertFalse(biMap.containsKey("two"));
    }
}
