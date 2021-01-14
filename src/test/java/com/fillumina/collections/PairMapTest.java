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
public class PairMapTest extends AbstractArrayMapTestHelper {

    @Override
    PairMap<Integer,String> create(Object... o) {
        return new PairMap<Integer,String>(o);
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
    public void testSortByValues() {
        PairMap<Integer,String> map = create(1, "c", 2, "a", 3, "b");
        map.sortByValues((a,b) -> a.compareTo(b));
        
        assertEquals(2, map.getKeyAtIndex(0));
        assertEquals("a", map.getValueAtIndex(0));
        
        assertEquals(3, map.getKeyAtIndex(1));
        assertEquals("b", map.getValueAtIndex(1));
        
        assertEquals(1, map.getKeyAtIndex(2));
        assertEquals("c", map.getValueAtIndex(2));
    }
    
    
    @Test
    public void testGetValueAtIndex() {
        PairMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        
        assertEquals("one", map.getValueAtIndex(0));
        assertEquals("two", map.getValueAtIndex(1));
        assertEquals("three", map.getValueAtIndex(2));
    }

    @Test
    public void testKeyAtIndex() {
        PairMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        
        assertEquals(1, map.getKeyAtIndex(0));
        assertEquals(2, map.getKeyAtIndex(1));
        assertEquals(3, map.getKeyAtIndex(2));
    }

    @Test
    public void testEntryAtIndex() {
        PairMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        
        Map.Entry<Integer,String> e = map.getEntryAtIndex(1);
        assertEquals("two", e.getValue());
        assertEquals(2, e.getKey());
    }

    @Test
    public void testSortByKeys() {
        PairMap<Integer,String> map = create(2, "two", 3, "three",1, "one");
        map.sortByKeys((a,b) -> a.compareTo(b));
        
        assertEquals(1, map.getKeyAtIndex(0));
        assertEquals("one", map.getValueAtIndex(0));
        
        assertEquals(2, map.getKeyAtIndex(1));
        assertEquals("two", map.getValueAtIndex(1));
        
        assertEquals(3, map.getKeyAtIndex(2));
        assertEquals("three", map.getValueAtIndex(2));
    }
    
    @Test
    public void testReadOnlyCheck() {
        AbstractArrayMap<Integer,String> map = new PairMap.Immutable<>(
            Map.of(1, "one", 2, "two", 3, "three"));
        
        assertThrows(UnsupportedOperationException.class,
            () -> map.put(4, "four"));
    }
    
    @Test
    public void testBuilder() {
        PairMap<Integer,String> map = PairMap.<Integer,String>builder()
            .put(1, "one")
            .put(2, "two")
            .put(3, "three")
            .build();
        
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }
    
    @Test
    public void testImmutableBuilder() {
        PairMap<Integer,String> map = PairMap.Immutable.<Integer,String>builder()
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
        PairMap<Integer,String> map = PairMap.<Integer,String>empty();

        assertTrue(map.isEmpty());
        
        assertThrows(UnsupportedOperationException.class, 
            () -> map.put(4, "four"));
    }
        
    @Test
    public void shouldGetImmutableMap() {
        PairMap<Integer,String> map = new PairMap<>();
        map.put(3, "three");
        map.put(1, "one");
        map.put(2, "two");

        PairMap<Integer,String> immutable = map.immutable();
        
        assertFalse(immutable == map);
        
        PairMap<Integer,String> anotherImmutable = immutable.immutable();
        
        assertTrue(anotherImmutable == immutable);
    }
}
