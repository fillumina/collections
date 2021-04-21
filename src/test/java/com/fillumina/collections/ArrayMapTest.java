package com.fillumina.collections;

import java.util.HashMap;
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
public class ArrayMapTest extends AbstractArrayMapTestHelper {

    @Override
    ArrayMap<Integer,String> create(Object... o) {
        return new ArrayMap<Integer,String>(o);
    }

    @Test
    public void shouldInitWithMap() {
        Map<Integer,String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");

        ArrayMap<Integer,String> map = new ArrayMap<>(m);
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }

    @Test
    public void shouldAssertContainsEntry() {
        Map<Integer,String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");

        ArrayMap<Integer,String> map = new ArrayMap<>(m);
        map.assertEntry(1, "one");
        map.assertEntry(2, "two");
        map.assertEntry(3, "three");

        assertThrows(AssertionError.class, () -> map.assertEntry(4, "four"));
    }

    @Test
    public void shouldAssertSize() {
        Map<Integer,String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");

        ArrayMap<Integer,String> map = new ArrayMap<>(m);
        map.assertSize(3);

        assertThrows(AssertionError.class, () -> map.assertSize(4));
    }

    @Test
    public void testSortByValues() {
        ArrayMap<Integer,String> map = create(1, "c", 2, "a", 3, "b");
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
        ArrayMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");

        assertEquals("one", map.getValueAtIndex(0));
        assertEquals("two", map.getValueAtIndex(1));
        assertEquals("three", map.getValueAtIndex(2));
    }

    @Test
    public void testKeyAtIndex() {
        ArrayMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");

        assertEquals(1, map.getKeyAtIndex(0));
        assertEquals(2, map.getKeyAtIndex(1));
        assertEquals(3, map.getKeyAtIndex(2));
    }

    @Test
    public void testEntryAtIndex() {
        ArrayMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");

        Map.Entry<Integer,String> e = map.getEntryAtIndex(1);
        assertEquals("two", e.getValue());
        assertEquals(2, e.getKey());
    }

    @Test
    public void shouldContainsEntry() {
        ArrayMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");

        assertTrue(map.containsEntry(1, "one"));
        assertFalse(map.containsEntry(1, "other"));
    }

    @Test
    public void testSortByKeys() {
        ArrayMap<Integer,String> map = create(2, "two", 3, "three",1, "one");
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
        Map<Integer,String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");

        AbstractArrayMap<Integer,String> map = new ImmutableArrayMap<>(m);

        assertThrows(UnsupportedOperationException.class,
            () -> map.put(4, "four"));
    }

    @Test
    public void testBuilder() {
        ArrayMap<Integer,String> map = ArrayMap.<Integer,String>builder()
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
        ArrayMap<Integer,String> map = ImmutableArrayMap.<Integer,String>builder()
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
        ArrayMap<Integer,String> map = ArrayMap.<Integer,String>empty();

        assertTrue(map.isEmpty());

        assertThrows(UnsupportedOperationException.class,
            () -> map.put(4, "four"));
    }

    @Test
    public void shouldGetImmutableMap() {
        ArrayMap<Integer,String> map = new ArrayMap<>();
        map.put(3, "three");
        map.put(1, "one");
        map.put(2, "two");

        ArrayMap<Integer,String> immutable = map.immutable();

        assertFalse(immutable == map);

        ArrayMap<Integer,String> anotherImmutable = immutable.immutable();

        assertTrue(anotherImmutable == immutable);
    }

    @Test
    public void shouldNotPutTheSameKeyTwice() {
        ArrayMap<Integer,String> map = new ArrayMap<>();
        map.put(1, "one");
        map.put(1, "another one");
        map.put(1, "third one");

        assertEquals(1, map.size());
    }

    @Test
    public void toStringTest() {
        ArrayMap<Integer,String> map = new ArrayMap<>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");

        assertEquals("{1=one, 2=two, 3=three}", map.toString());
    }
}
