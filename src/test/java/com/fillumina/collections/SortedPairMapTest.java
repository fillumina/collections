package com.fillumina.collections;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SortedPairMapTest extends AbstractArrayMapTestHelper {
    
    @Override
    SortedPairMap<Integer,String> create(Object... o) {
        return new SortedPairMap<Integer,String>(o);
    }
    
    @Test
    public void shouldMapIsSorted() {
        AbstractArrayMap<Integer,String> map = new SortedPairMap<>();
        map.put(3, "three");
        map.put(2, "two");
        map.put(1, "one");
        
        Object[] array = map.toArray();
        assertEquals(6, array.length);
        assertEquals(1, array[0]);
        assertEquals("one", array[1]);
        assertEquals(2, array[2]);
        assertEquals("two", array[3]);
        assertEquals(3, array[4]);
        assertEquals("three", array[5]);
    }
    
    @Test
    public void testPutInMiddle() {
        AbstractArrayMap<Integer,String> map = create();
        map.put(1, "one");
        map.put(2, "two");
        map.put(4, "four");
        map.put(5, "five");
        
        map.put(3, "three");
        
        assertEquals(5, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
        assertEquals("four", map.get(4));
        assertEquals("five", map.get(5));
    }
    
    @Test
    public void testPutAtBeginning() {
        AbstractArrayMap<Integer,String> map = create();
        map.put(2, "two");
        map.put(3, "three");
        map.put(4, "four");
        map.put(5, "five");
        
        map.put(1, "one");
        
        assertEquals(5, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
        assertEquals("four", map.get(4));
        assertEquals("five", map.get(5));
    }
    
    @Test
    public void testPutAtBeginningThreeItems() {
        AbstractArrayMap<Integer,String> map = create();
        map.put(2, "two");
        map.put(3, "three");
        
        map.put(1, "one");
        
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }
    
    @Test
    public void testPutAfterBeginning() {
        AbstractArrayMap<Integer,String> map = create();
        map.put(1, "one");
        map.put(3, "three");
        map.put(4, "four");
        map.put(5, "five");
        
        map.put(2, "two");
        
        assertEquals(5, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
        assertEquals("four", map.get(4));
        assertEquals("five", map.get(5));
    }
    
    @Test
    public void testPutButLast() {
        AbstractArrayMap<Integer,String> map = create();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");
        map.put(5, "five");
        
        map.put(4, "four");
        
        assertEquals(5, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
        assertEquals("four", map.get(4));
        assertEquals("five", map.get(5));
    }
    
    @Test
    public void testPutReversed() {
        AbstractArrayMap<Integer,String> map = create();
        map.put(5, "five");
        map.put(4, "four");
        map.put(3, "three");
        map.put(2, "two");
        map.put(1, "one");
        
        assertEquals(5, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
        assertEquals("four", map.get(4));
        assertEquals("five", map.get(5));
    }
    
    @Test
    public void testReadOnlyCheck() {
        AbstractArrayMap<Integer,String> map = new SortedPairMap.Immutable<>(
            Map.of(1, "one", 2, "two", 3, "three"));
        
        assertThrows(UnsupportedOperationException.class,
            () -> map.put(4, "four"));
    }
    
    @Test
    public void testBuilder() {
        SortedPairMap<Integer,String> map = SortedPairMap.<Integer,String>builder()
            .put(3, "three")
            .put(1, "one")
            .put(2, "two")
            .build();
        
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }
    
    @Test
    public void testImmutableBuilder() {
        SortedPairMap<Integer,String> map = SortedPairMap.Immutable.<Integer,String>builder()
            .put(1, "one")
            .put(2, "two")
            .put(3, "three")
            .build();
        
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
        
        assertThrows(UnsupportedOperationException.class, 
            () -> map.put(4, "four"));
    }
    
    @Test
    public void shouldEmptyMapBeImmutable() {
        SortedPairMap<Integer,String> map = SortedPairMap.<Integer,String>empty();

        assertTrue(map.isEmpty());
        
        assertThrows(UnsupportedOperationException.class, 
            () -> map.put(4, "four"));
    }
    
    @Test
    public void shouldGetImmutableMap() {
        SortedPairMap<Integer,String> map = new SortedPairMap<>();
        map.put(3, "three");
        map.put(1, "one");
        map.put(2, "two");

        SortedPairMap<Integer,String> immutable = map.immutable();
        
        assertFalse(immutable == map);
        
        SortedPairMap<Integer,String> anotherImmutable = immutable.immutable();
        
        assertTrue(anotherImmutable == immutable);
    }
}
