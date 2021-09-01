package com.fillumina.collections;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    @SuppressWarnings("unchecked")
    ArrayMap<Integer,String> create(Object... o) {
        return new ArrayMap<>(o);
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
    public void shouldAssertEntries() {
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

        Map.Entry<Integer,String> e1 = map.getEntryAtIndex(0);
        assertEquals("one", e1.getValue());
        assertEquals(1, e1.getKey());

        Map.Entry<Integer,String> e2 = map.getEntryAtIndex(1);
        assertEquals("two", e2.getValue());
        assertEquals(2, e2.getKey());

        Map.Entry<Integer,String> e3 = map.getEntryAtIndex(2);
        assertEquals("three", e3.getValue());
        assertEquals(3, e3.getKey());
    }

    @Test
    public void shouldContainsEntry() {
        ArrayMap<Integer,String> map = create(1, "one", 2, "two", 3, "three");

        assertTrue(map.containsEntry(1, "one"));
        assertTrue(map.containsEntry(2, "two"));
        assertTrue(map.containsEntry(3, "three"));

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
    public void testReadOnlyBaseClass() {
        Map<Integer,String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");

        BaseArrayMap<Integer,String> map = new ImmutableArrayMap<>(m);

        assertThrows(UnsupportedOperationException.class,
            () -> map.put(4, "four"));

        assertThrows(UnsupportedOperationException.class,
            () -> map.remove(1, "one"));

        assertThrows(UnsupportedOperationException.class,
            () -> map.remove(2, "two"));

        assertThrows(UnsupportedOperationException.class,
            () -> map.remove(3, "three"));

        assertThrows(UnsupportedOperationException.class,
            () -> map.clear());
    }

    @Test
    public void testReadOnlyEntrySet() {
        Map<Integer,String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");

        BaseArrayMap<Integer,String> map = new ImmutableArrayMap<>(m);

        assertThrows(UnsupportedOperationException.class,
            () -> map.entrySet().add(new SimpleEntry<>(5, "five")));

        assertThrows(UnsupportedOperationException.class,
            () -> map.entrySet().clear());

        assertThrows(UnsupportedOperationException.class,
            () -> map.entrySet().remove(new SimpleEntry<>(1, "one")));

        assertThrows(UnsupportedOperationException.class,
            () -> map.entrySet().remove(new SimpleEntry<>(2, "two")));

        assertThrows(UnsupportedOperationException.class,
            () -> map.entrySet().remove(new SimpleEntry<>(3, "three")));
    }

    @Test
    public void testReadOnlyKeySet() {
        Map<Integer,String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");

        BaseArrayMap<Integer,String> map = new ImmutableArrayMap<>(m);

        assertThrows(UnsupportedOperationException.class,
            () -> map.keySet().add(5));

        assertThrows(UnsupportedOperationException.class,
            () -> map.keySet().clear());

        assertThrows(UnsupportedOperationException.class,
            () -> map.keySet().remove(1));

        assertThrows(UnsupportedOperationException.class,
            () -> map.keySet().remove(2));

        assertThrows(UnsupportedOperationException.class,
            () -> map.keySet().remove(3));
    }

    @Test
    public void testReadOnlyKeyValueCollection() {
        Map<Integer,String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");

        BaseArrayMap<Integer,String> map = new ImmutableArrayMap<>(m);

        assertThrows(UnsupportedOperationException.class,
            () -> map.values().add("five"));

        assertThrows(UnsupportedOperationException.class,
            () -> map.values().clear());

        assertThrows(UnsupportedOperationException.class,
            () -> map.values().remove("one"));

        assertThrows(UnsupportedOperationException.class,
            () -> map.values().remove("two"));

        assertThrows(UnsupportedOperationException.class,
            () -> map.values().remove("three"));
    }

    @Test
    public void testBuilder3Entries() {
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
    public void testBuilder1Entry() {
        ArrayMap<Integer,String> map = ArrayMap.<Integer,String>builder()
            .put(1, "one")
            .build();

        assertEquals(1, map.size());
        assertEquals("one", map.get(1));
    }

    @Test
    public void testBuilderNoEntry() {
        ArrayMap<Integer,String> map = ArrayMap.<Integer,String>builder()
            .build();

        assertTrue(map.isEmpty());
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

    @Test
    public void shouldBeEqualToASimilarMap() {
        Map<Integer,String> hashMap = new HashMap<>();
        hashMap.put(1, "one");
        hashMap.put(2, "two");
        hashMap.put(3, "three");

        ArrayMap<Integer,String> arrayMap = new ArrayMap<>(hashMap);

        assertEquals(hashMap, arrayMap);
        assertEquals(hashMap.hashCode(), arrayMap.hashCode());
    }

    @Test
    public void shouldConstructFromCollection() {
        List<String> list = new ArrayList<>();
        list.add("k1");
        list.add("v1");
        list.add("k2");
        list.add("v2");
        list.add("k3");
        list.add("v3");

        ArrayMap<String,String> arrayMap = new ArrayMap<>(list);
        arrayMap.assertEntry("k1", "v1");
        arrayMap.assertEntry("k2", "v2");
        arrayMap.assertEntry("k3", "v3");
    }

    @Test
    public void shouldConstructFromIterable() {
        List<String> list = new ArrayList<>();
        list.add("k1");
        list.add("v1");
        list.add("k2");
        list.add("v2");
        list.add("k3");
        list.add("v3");

        ArrayMap<String,String> arrayMap = new ArrayMap<>((Iterable)list);
        arrayMap.assertEntry("k1", "v1");
        arrayMap.assertEntry("k2", "v2");
        arrayMap.assertEntry("k3", "v3");
    }

}
