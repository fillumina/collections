package com.fillumina.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SortedArrayMapTest extends AbstractArrayMapTestHelper {

    @Override
    @SuppressWarnings("unchecked")
    SortedArrayMap<Integer,String> create(Object... o) {
        return new SortedArrayMap<>(o);
    }

    @Test
    public void shouldMapIsSorted() {
        BaseArrayMap<Integer,String> map = new SortedArrayMap<>();
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
        BaseArrayMap<Integer,String> map = create();
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
        BaseArrayMap<Integer,String> map = create();
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
        BaseArrayMap<Integer,String> map = create();
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
        BaseArrayMap<Integer,String> map = create();
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
        BaseArrayMap<Integer,String> map = create();
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
        BaseArrayMap<Integer,String> map = create();
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
        BaseArrayMap<Integer,String> map = new ImmutableSortedArrayMap<>(
            Utils.mapOf(1, "one", 2, "two", 3, "three"));

        assertThrows(UnsupportedOperationException.class,
            () -> map.put(4, "four"));
    }

    @Test
    public void testBuilder() {
        SortedArrayMap<Integer,String> map = SortedArrayMap.<Integer,String>builder()
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
        SortedArrayMap<Integer,String> map = ImmutableSortedArrayMap.<Integer,String>builder()
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
        SortedArrayMap<Integer,String> map = SortedArrayMap.<Integer,String>empty();

        assertTrue(map.isEmpty());

        assertThrows(UnsupportedOperationException.class,
            () -> map.put(4, "four"));
    }

    @Test
    public void shouldGetImmutableMap() {
        SortedArrayMap<Integer,String> map = new SortedArrayMap<>();
        map.put(3, "three");
        map.put(1, "one");
        map.put(2, "two");

        SortedArrayMap<Integer,String> immutable = map.immutable();

        assertFalse(immutable == map);

        SortedArrayMap<Integer,String> anotherImmutable = immutable.immutable();

        assertTrue(anotherImmutable == immutable);
    }

}
