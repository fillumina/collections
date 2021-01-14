package com.fillumina.collections;

import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public abstract class AbstractArrayMapTestHelper {
    
    abstract <T extends AbstractArrayMap<Integer,String>> T create(Object... o);
    
    @Test
    public void shouldInitWithArray() {
        AbstractArrayMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }
    
    @Test
    public void testPut() {
        AbstractArrayMap<Integer,String> map = create();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");
        
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }
    
    @Test
    public void testPutDifferentOrderOfInsertion() {
        AbstractArrayMap<Integer,String> map = create();
        map.put(3, "three");
        map.put(2, "two");
        map.put(1, "one");
        
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }
    
    @Test
    public void testPutExistingKey() {
        AbstractArrayMap<Integer,String> map = create();
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
    public void testRemoveFirst() {
        AbstractArrayMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        map.remove(1);
        
        assertEquals(2, map.size());
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }

    @Test
    public void testRemoveMiddle() {
        AbstractArrayMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        map.remove(2);

        assertEquals(2, map.size());
        assertEquals("one", map.get(1));
        assertEquals("three", map.get(3));
    }

    @Test
    public void testRemoveLast() {
        AbstractArrayMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        map.remove(3);

        assertEquals(2, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
    }

    @Test
    public void testRemoveNotPresent() {
        AbstractArrayMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertNull(map.remove(4));
    }

    @Test
    public void testToArray() {
        Object[] objects = new Object[] {1, "one", 2, "two", 3, "three"};
        AbstractArrayMap<Integer,String> map = create(objects);
        Object[] array = map.toArray();
        Assertions.assertArrayEquals(objects, array);
    }

    @Test
    public void testClear() {
        AbstractArrayMap<Integer,String> map = create(2, "two", 3, "three",1, "one");
        map.clear();
        assertEquals(0, map.size());
    }    
}
