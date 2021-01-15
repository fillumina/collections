package com.fillumina.collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        
    @Test
    public void shouldForEach() {
        AbstractArrayMap<Integer,String> map = create();
        map.put(3, "three");
        map.put(1, "one");
        map.put(2, "two");

        Map<Integer,String> m = new HashMap<>();
        map.forEach((k,v) -> m.put(k,v));
        
        assertEquals(3, m.size());
        assertEquals("one", m.get(1));
        assertEquals("two", m.get(2));
        assertEquals("three", m.get(3));
    }
    
    @Test
    public void testMutableEntries() {
        AbstractArrayMap<Integer,String> map = create(2, "two", 3, "three",1, "one");

        Iterator<Map.Entry<Integer,String>> it = map.entrySet().iterator();
        Map.Entry<Integer,String> a = it.next();
        Map.Entry<Integer,String> b = it.next();
        Map.Entry<Integer,String> c = it.next();
        
        assertTrue(a == b);
        assertTrue(b == c);
    }
    
    @Test
    public void testParallelStream() {
        Object[] array = new Object[10];
        for (int i=0; i<array.length; i++) {
            if ((i & 1) == 1) {
                array[i] = "" + i;
            } else {
                array[i] = i;
            }
        }
        AbstractArrayMap<Integer,String> map = create(array);

        Map<Integer,String> cmap = new ConcurrentHashMap<>();
        
        map.entrySet().parallelStream().forEach(e -> cmap.put(e.getKey(),e.getValue()));
        
        assertEquals(map.size(), cmap.size());
        assertEquals(map, cmap);
    }
    
    @Test
    public void shouldIterateOverEmptyMap() {
        AbstractArrayMap<Integer,String> map = create();
        
        Iterator<Entry<Integer,String>> it = map.iterator();
        assertFalse(it.hasNext());
    }
    
    @Test
    public void shouldGetSizeForEmptyMap() {
        AbstractArrayMap<Integer,String> map = create();

        assertEquals(0, map.size());
    }
}
