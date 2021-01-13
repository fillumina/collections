package com.fillumina.collections;

import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class PairMapTest {
    
    public PairMapTest() {
    }

    @Test
    public void shouldInitWithArray() {
        PairMap<Integer,String> map = new PairMap<>(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }

    @Test
    public void shouldInitWithMap() {
        PairMap<Integer,String> map = new PairMap<>(
            Map.of(1, "one", 2, "two", 3, "three"));
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }
    
    @Test
    public void testPut() {
        PairMap<Integer,String> map = new PairMap<>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");
        
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }
    
    @Test
    public void testPutExistingKey() {
        PairMap<Integer,String> map = new PairMap<>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");

        map.put(1, "ONE");
        map.put(2, "TWO");
        map.put(3, "THREE");
        
        assertEquals(3, map.size());
        assertEquals("ONE", map.get(1));
        assertEquals("TWO", map.get(2));
        assertEquals("THREE", map.get(3));
    }

    @Test
    public void testReadOnlyCheck() {
        PairMap<Integer,String> map = new PairMap.Immutable<>(
            Map.of(1, "one", 2, "two", 3, "three"));
        
        assertThrows(UnsupportedOperationException.class,
            () -> map.put(4, "four"));
    }

    @Test
    public void testRemoveFirst() {
        PairMap<Integer,String> map = new PairMap<>(1, "one", 2, "two", 3, "three");
        map.remove(1);
        
        assertEquals(2, map.size());
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }

    @Test
    public void testRemoveMiddle() {
        PairMap<Integer,String> map = new PairMap<>(1, "one", 2, "two", 3, "three");
        map.remove(2);

        assertEquals(2, map.size());
        assertEquals("one", map.get(1));
        assertEquals("three", map.get(3));
    }

    @Test
    public void testRemoveLast() {
        PairMap<Integer,String> map = new PairMap<>(1, "one", 2, "two", 3, "three");
        map.remove(3);

        assertEquals(2, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
    }

    @Test
    public void testRemoveNotPresent() {
        PairMap<Integer,String> map = new PairMap<>(1, "one", 2, "two", 3, "three");
        assertNull(map.remove(4));
    }
    
    @Test
    public void testGetIndexOfKey() {
    }

    @Test
    public void testToArray() {
        Object[] objects = new Object[] {1, "one", 2, "two", 3, "three"};
        PairMap<Integer,String> map = new PairMap<>(objects);
        Object[] array = map.toArray();
        Assertions.assertArrayEquals(objects, array);
    }

    @Test
    public void testSortByKeys() {
        PairMap<Integer,String> map = new PairMap<>(2, "two", 3, "three",1, "one");
        map.sortByKeys((a,b) -> a.compareTo(b));
        
        assertEquals(1, map.getKeyAtIndex(0));
        assertEquals("one", map.getValueAtIndex(0));
        
        assertEquals(2, map.getKeyAtIndex(1));
        assertEquals("two", map.getValueAtIndex(1));
        
        assertEquals(3, map.getKeyAtIndex(2));
        assertEquals("three", map.getValueAtIndex(2));
    }

    @Test
    public void testSortByValues() {
        PairMap<Integer,String> map = new PairMap<>(1, "c", 2, "a", 3, "b");
        map.sortByValues((a,b) -> a.compareTo(b));
        
        assertEquals(2, map.getKeyAtIndex(0));
        assertEquals("a", map.getValueAtIndex(0));
        
        assertEquals(3, map.getKeyAtIndex(1));
        assertEquals("b", map.getValueAtIndex(1));
        
        assertEquals(1, map.getKeyAtIndex(2));
        assertEquals("c", map.getValueAtIndex(2));
    }

    @Test
    public void testClear() {
        PairMap<Integer,String> map = new PairMap<>(2, "two", 3, "three",1, "one");
        map.clear();
        assertEquals(0, map.size());
    }
    
    @Test
    public void testGetValueAtIndex() {
        PairMap<Integer,String> map = new PairMap<>(1, "one", 2, "two", 3, "three");
        
        assertEquals("one", map.getValueAtIndex(0));
        assertEquals("two", map.getValueAtIndex(1));
        assertEquals("three", map.getValueAtIndex(2));
    }

    @Test
    public void testKeyAtIndex() {
        PairMap<Integer,String> map = new PairMap<>(1, "one", 2, "two", 3, "three");
        
        assertEquals(1, map.getKeyAtIndex(0));
        assertEquals(2, map.getKeyAtIndex(1));
        assertEquals(3, map.getKeyAtIndex(2));
    }

    @Test
    public void testEntryAtIndex() {
        PairMap<Integer,String> map = new PairMap<>(1, "one", 2, "two", 3, "three");
        
        Entry<Integer,String> e = map.getEntryAtIndex(1);
        assertEquals("two", e.getValue());
        assertEquals(2, e.getKey());
    }
    
}
