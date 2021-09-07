package com.fillumina.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 * @see GenericCollectionTest
 * @see GenericSetTest
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class GenericMapTest {
    public static final int MASS_SIZE = 1000;

    /** Override */
    @SuppressWarnings("unchecked")
    protected <K extends Comparable<K>,V extends Comparable<V>> Map<K,V> create(Map<K,V> m) {
        return new HashMap<>(m);
    }

    /** Override */
    protected boolean isReadOnly() {
        return false;
    }

    @SuppressWarnings("unchecked")
    private <K extends Comparable<K>,V extends Comparable<V>> Map<K,V> create(Object ... o) {
        Map<K,V> m = new HashMap<>();
        for (int i=0; i<o.length; i+=2) {
            m.put((K)o[i], (V)o[i+1]);
        }
        final Map<K, V> map = create(m);
        return map;
    }

    @Test
    public void testMassInsertionAndRemoval() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer, String> map = create();
        List<Integer> list = new ArrayList<>();

        for (int i=0; i<MASS_SIZE; i++) {
            list.add(i);
        }
        assertEquals(MASS_SIZE, list.size());

        Collections.shuffle(list);
        int size = 1;
        for (Integer i : list) {
            final String prev = map.put(i, ""+i);
            assertNull(prev);
            assertEquals(size, map.size());
            size++;
        }
        assertEquals(MASS_SIZE, map.size());

        Collections.shuffle(list);
        for (int key : list) {
            assertEquals("" + key, map.get(key));
            assertTrue(map.containsKey(key));
            assertTrue(map.containsValue(""+key));
        }

        Collections.shuffle(list);
        int expectedSize = MASS_SIZE;
        for (int key : list) {
            expectedSize--;
            final String v = map.remove(key);
            if (v == null) {
                fail("found null value for key: " + key + ", size=" + expectedSize);
            }
            assertEquals(expectedSize, map.size());
        }

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    public void testGetOrDefault() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");

        assertEquals("one", map.getOrDefault(1, "other"));
        assertEquals("two", map.getOrDefault(2, "other"));
        assertEquals("three", map.getOrDefault(3, "other"));

        assertEquals("other", map.getOrDefault(4, "other"));
    }

    @Test
    public void testForEach() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        Map<Integer,String> m = new HashMap<>();
        map.forEach((k,v) -> m.put(k,v));
        assertEquals(m, map);
    }

    @Test
    public void testForEachEmptyMap() {
        Map<Integer,String> map = create();
        map.forEach((k,v) -> fail("shouldn't be executed"));
    }

    @Test
    public void testReplaceAll() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        map.replaceAll((k,v) -> v + "-" + k);

        assertEquals(3, map.size());
        assertEquals("one-1", map.get(1));
        assertEquals("two-2", map.get(2));
        assertEquals("three-3", map.get(3));
    }

    @Test
    public void testPutIfAbsent() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        map.putIfAbsent(1, "other");
        assertEquals("one", map.get(1));
        assertFalse(map.containsKey(4));
        map.putIfAbsent(4, "four");
        assertEquals("four", map.get(4));
    }

    @Test
    public void testRemoveKeyValue() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        assertFalse(map.remove(2, "something"));
        assertEquals(3, map.size());

        assertTrue(map.remove(2, "two"));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(2));
    }

    @Test
    public void testMerge() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        map.merge(1, "other", (v1,v2) -> v1 + "-" + v2);
        assertEquals("one-other", map.get(1));
        assertEquals(3, map.size());

        map.merge(4, "other", (v1,v2) -> v1 + "-" + v2);
        assertEquals("other", map.get(4));
        assertEquals(4, map.size());
    }

    @Test
    public void testHashCode() {
        assertEquals(create().hashCode(),
                create().hashCode());

        assertEquals(create(1, "one").hashCode(),
                create(1, "one").hashCode());

        assertEquals(create(1, "one", 2, "two").hashCode(),
                create(1, "one", 2, "two").hashCode());

        assertEquals(create(1, "one", 2, "two", 3, "three").hashCode(),
                create(1, "one", 2, "two", 3, "three").hashCode());

        assertNotEquals(create(1, "one").hashCode(),
                create(1, "one", 2, "two", 3, "three").hashCode());

        assertNotEquals(create(1, "one").hashCode(),
                create(1, "one", 2, "two").hashCode());

        assertNotEquals(create(1, "one").hashCode(),
                create(2, "two").hashCode());
    }

    @Test
    public void testHashCodeDifferentOrder() {

        assertEquals(create(1, "one", 2, "two").hashCode(),
                create(2, "two", 1, "one").hashCode());

        assertEquals(create(3, "three",1, "one", 2, "two").hashCode(),
                create(2, "two", 1, "one", 3, "three").hashCode());
    }

    @Test
    public void testEqualsEtherogeneous() {
        Map<Integer,String> hmap = new HashMap<>();
        hmap.put(1, "one");
        hmap.put(2, "two");
        hmap.put(3, "three");

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");

        assertTrue(hmap.equals(map));
        assertTrue(map.equals(hmap));
    }

    @Test
    public void testEqualsEtherogeneousEmpty() {
        Map<Integer,String> hmap = new HashMap<>();
        Map<Integer,String> map = create();

        assertTrue(hmap.equals(map));
        assertTrue(map.equals(hmap));
    }

    @Test
    public void testEqualsHomogeneous() {
        Map<Integer,String> map1 = create(1, "one", 2, "two", 3, "three");
        Map<Integer,String> map2 = create(1, "one", 2, "two", 3, "three");

        assertTrue(map1.equals(map2));
        assertTrue(map2.equals(map1));
    }

    @Test
    public void testEqualsHomogeneousEmpty() {
        Map<Integer,String> map1 = create();
        Map<Integer,String> map2 = create();

        assertTrue(map1.equals(map2));
        assertTrue(map2.equals(map1));
    }

    @Test
    public void testClear() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());
        assertFalse(map.isEmpty());
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testClearEmptyMap() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
        map.clear();
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    public void testPutAll() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");
        assertEquals(3, m.size());

        Map<Integer,String> map = create(4, "four", 5, "five");
        assertEquals(2, map.size());
        map.putAll(m);
        assertFalse(map.isEmpty());
        assertEquals(3 + 2, map.size());
        assertTrue(map.containsKey(1));
        assertTrue(map.containsValue("one"));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsValue("two"));
        assertTrue(map.containsKey(3));
        assertTrue(map.containsValue("three"));
        assertTrue(map.containsKey(4));
        assertTrue(map.containsValue("four"));
        assertTrue(map.containsKey(5));
        assertTrue(map.containsValue("five"));

    }

    @Test
    public void testPutAllEmptyMap() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> m = new HashMap<>();
        m.put(1, "one");
        m.put(2, "two");
        m.put(3, "three");
        assertEquals(3, m.size());

        Map<Integer,String> map = create();
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        map.putAll(m);
        assertFalse(map.isEmpty());
        assertEquals(3, map.size());
        assertTrue(map.containsKey(1));
        assertTrue(map.containsValue("one"));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsValue("two"));
        assertTrue(map.containsKey(3));
        assertTrue(map.containsValue("three"));
    }

    @Test
    public void testRemoveFirstObject() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());
        assertEquals("one", map.remove(1));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue("one"));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsValue("two"));
        assertTrue(map.containsKey(3));
        assertTrue(map.containsValue("three"));
    }

    @Test
    public void testRemoveMiddleObject() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());
        assertEquals("two", map.remove(2));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(2));
        assertFalse(map.containsValue("two"));
        assertTrue(map.containsKey(1));
        assertTrue(map.containsValue("one"));
        assertTrue(map.containsKey(3));
        assertTrue(map.containsValue("three"));
    }

    @Test
    public void testRemoveLastObject() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());
        assertEquals("three", map.remove(3));
        assertEquals(2, map.size());
        assertFalse(map.containsKey(3));
        assertFalse(map.containsValue("three"));
        assertTrue(map.containsKey(1));
        assertTrue(map.containsValue("one"));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsValue("two"));
    }

    @Test
    public void testRemoveUnexistentObject() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());
        assertNull(map.remove(4));
        assertEquals(3, map.size());
        assertTrue(map.containsKey(1));
        assertTrue(map.containsValue("one"));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsValue("two"));
        assertTrue(map.containsKey(3));
        assertTrue(map.containsValue("three"));
    }

    @Test
    public void testPut() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create();
        assertTrue(map.isEmpty());

        map.put(1, "one");
        assertFalse(map.isEmpty());
        assertEquals(1, map.size());
        assertEquals("one", map.get(1));

        map.put(2, "two");
        assertFalse(map.isEmpty());
        assertEquals(2, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));

        map.put(3, "three");
        assertFalse(map.isEmpty());
        assertEquals(3, map.size());
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
    }

    @Test
    public void testGet() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals("one", map.get(1));
        assertEquals("two", map.get(2));
        assertEquals("three", map.get(3));
        assertNull(map.get(4));
    }

    @Test
    public void testContainsKey() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertTrue(map.containsKey(1));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsKey(3));
        assertFalse(map.containsKey(4));
    }

    @Test
    public void testContainsValue() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertTrue(map.containsValue("one"));
        assertTrue(map.containsValue("two"));
        assertTrue(map.containsValue("three"));
        assertFalse(map.containsValue("four"));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(create().isEmpty());
        assertFalse(create(1, "one").isEmpty());
        assertFalse(create(1, "one", 2, "two").isEmpty());
        assertFalse(create(1, "one", 2, "two", 3, "three").isEmpty());
    }

    @Test
    public void testSize() {
        assertEquals(0, create().size());
        assertEquals(1, create(1, "one").size());
        assertEquals(2, create(1, "one", 2, "two").size());
        assertEquals(3, create(1, "one", 2, "two", 3, "three").size());
    }

    /** Use {@link GenericCollectionTest} */
    @Test
    public void testValuesEmptyMap() {
        Map<Integer,String> map = create();
        assertTrue(map.isEmpty());

        Collection<String> values = map.values();
        assertTrue(values.isEmpty());
    }

    @Test
    public void testValuesSameContent() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Collection<String> values = map.values();
        assertEquals(3, values.size());
        assertTrue(values.contains("one"));
        assertTrue(values.contains("two"));
        assertTrue(values.contains("three"));
    }

    @Test
    public void testValuesClear() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Collection<String> values = map.values();
        assertEquals(3, values.size());

        values.clear();
        assertEquals(0, values.size());
        assertTrue(values.isEmpty());
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    public void shouldNotAddToValues() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Collection<String> values = map.values();
        assertThrows(UnsupportedOperationException.class, () -> values.add("four"));
    }

    @Test
    public void testValuesRemoveFirstEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Collection<String> values = map.values();
        values.remove("one");

        assertEquals(2, map.size());
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue("one"));
    }

    @Test
    public void testValuesRemoveMiddleEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Collection<String> values = map.values();
        values.remove("two");

        assertEquals(2, map.size());
        assertFalse(map.containsKey(2));
        assertFalse(map.containsValue("two"));
    }

    @Test
    public void testValuesRemoveLastEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Collection<String> values = map.values();
        values.remove("three");

        assertEquals(2, map.size());
        assertFalse(map.containsKey(3));
        assertFalse(map.containsValue("three"));
    }

    @Test
    public void testValuesIteratorRemoveFirstEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Object, Object> reverseMapping =
                Utils.mapOf("one", 1, "two", 2, "three", 3);

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Iterator<String> it = map.values().iterator();
        assertTrue(it.hasNext());
        String v1 = it.next();
        assertNotNull(v1);
        it.remove();

        assertEquals(2, map.size());
        assertFalse(map.containsValue(v1));
        assertFalse(map.containsKey(reverseMapping.get(v1)));
    }

    @Test
    public void testValuesIteratorRemoveMiddleEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Object, Object> reverseMapping =
                Utils.mapOf("one", 1, "two", 2, "three", 3);

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Iterator<String> it = map.values().iterator();
        assertTrue(it.hasNext());
        String v1 = it.next();
        assertNotNull(v1);
        assertTrue(it.hasNext());
        String v2 = it.next();
        assertNotNull(v2);
        it.remove();

        assertEquals(2, map.size());
        assertFalse(map.containsKey(reverseMapping.get(v2)));
        assertFalse(map.containsValue(v2));
    }

    @Test
    public void testValuesIteratorRemoveLastEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Object, Object> reverseMapping =
                Utils.mapOf("one", 1, "two", 2, "three", 3);

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Iterator<String> it = map.values().iterator();
        assertTrue(it.hasNext());
        String v1 = it.next();
        assertNotNull(v1);
        assertTrue(it.hasNext());
        String v2 = it.next();
        assertNotNull(v2);
        assertTrue(it.hasNext());
        String v3 = it.next();
        assertNotNull(v3);
        it.remove();

        assertEquals(2, map.size());
        assertFalse(map.containsKey(reverseMapping.get(v3)));
        assertFalse(map.containsValue(v3));
    }

    /** Use {@link GenericCollectionTest} */
    @Test
    public void testKeySetEmptyMap() {
        Map<Integer,String> map = create();
        assertTrue(map.isEmpty());

        Set<Integer> keys = map.keySet();
        assertTrue(keys.isEmpty());
    }

    @Test
    public void testKeySetSameContent() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Integer> keys = map.keySet();
        assertEquals(3, keys.size());
        assertTrue(keys.contains(1));
        assertTrue(keys.contains(2));
        assertTrue(keys.contains(3));
    }

    @Test
    public void testKeySetClear() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Integer> keys = map.keySet();
        assertEquals(3, keys.size());

        keys.clear();
        assertEquals(0, keys.size());
        assertTrue(keys.isEmpty());
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    public void shouldNotAddToKeySet() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Integer> keys = map.keySet();
        assertThrows(UnsupportedOperationException.class, () -> keys.add(4));
    }

    @Test
    public void testKeySetRemoveFirstEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Integer> keys = map.keySet();
        keys.remove(1);

        assertEquals(2, map.size());
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue("one"));
    }

    @Test
    public void testKeySetRemoveMiddleEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Integer> keys = map.keySet();
        keys.remove(2);

        assertEquals(2, map.size());
        assertFalse(map.containsKey(2));
        assertFalse(map.containsValue("two"));
    }

    @Test
    public void testKeySetRemoveLastEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Integer> keys = map.keySet();
        keys.remove(3);

        assertEquals(2, map.size());
        assertFalse(map.containsKey(3));
        assertFalse(map.containsValue("three"));
    }

    @Test
    public void testKeySetIteratorRemoveFirstEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Iterator<Integer> it = map.keySet().iterator();
        assertTrue(it.hasNext());
        Integer value = it.next();
        assertNotNull(value);
        it.remove();

        assertEquals(2, map.size());
        assertFalse(map.containsKey(value));
    }

    @Test
    public void testKeySetIteratorRemoveMiddleEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Iterator<Integer> it = map.keySet().iterator();
        assertTrue(it.hasNext());
        assertEquals(1, it.next());
        assertTrue(it.hasNext());
        assertEquals(2, it.next());
        it.remove();

        assertEquals(2, map.size());
        assertFalse(map.containsKey(2));
        assertFalse(map.containsValue("two"));
    }

    @Test
    public void testKeySetIteratorRemoveLastEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Iterator<Integer> it = map.keySet().iterator();
        assertTrue(it.hasNext());
        assertEquals(1, it.next());
        assertTrue(it.hasNext());
        assertEquals(2, it.next());
        assertTrue(it.hasNext());
        assertEquals(3, it.next());
        it.remove();

        assertEquals(2, map.size());
        assertFalse(map.containsKey(3));
        assertFalse(map.containsValue("three"));
    }

    /** Use {@link GenericCollectionTest} */
    @Test
    public void testEntrySetEmptyMap() {
        Map<Integer,String> map = create();
        assertTrue(map.isEmpty());

        Set<Entry<Integer,String>> entries = map.entrySet();
        assertTrue(entries.isEmpty());
    }

    @Test
    public void testEntrySetSameContent() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Entry<Integer,String>> entries = map.entrySet();
        assertEquals(3, entries.size());
        assertTrue(entries.contains(new ImmutableMapEntry<>(1, "one")));
        assertTrue(entries.contains(new ImmutableMapEntry<>(2, "two")));
        assertTrue(entries.contains(new ImmutableMapEntry<>(3, "three")));
    }

    @Test
    public void testEntrySetClear() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Entry<Integer,String>> entries = map.entrySet();
        assertEquals(3, entries.size());

        entries.clear();
        assertEquals(0, entries.size());
        assertTrue(entries.isEmpty());
        assertEquals(0, map.size());
        assertTrue(map.isEmpty());
    }

    @Test
    public void shouldNotAddToEntrySet() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Entry<Integer,String>> entries = map.entrySet();
        assertThrows(UnsupportedOperationException.class,
                () -> entries.add(new ImmutableMapEntry<>(4, "four")));
    }

    @Test
    public void testEntrySetRemoveFirstEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Entry<Integer,String>> entries = map.entrySet();
        entries.remove(new ImmutableMapEntry<>(1, "one"));

        assertEquals(2, map.size());
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue("one"));
    }

    @Test
    public void testEntrySetRemoveMiddleEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Entry<Integer,String>> entries = map.entrySet();
        entries.remove(new ImmutableMapEntry<>(2, "two"));

        assertEquals(2, map.size());
        assertFalse(map.containsKey(2));
        assertFalse(map.containsValue("two"));
    }

    @Test
    public void testEntrySetRemoveLastEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Set<Entry<Integer,String>> entries = map.entrySet();
        entries.remove(new ImmutableMapEntry<>(3, "three"));

        assertEquals(2, map.size());
        assertFalse(map.containsKey(3));
        assertFalse(map.containsValue("three"));
    }

    @Test
    public void testEntrySetIteratorRemoveFirstEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Iterator<Entry<Integer,String>> it = map.entrySet().iterator();
        assertTrue(it.hasNext());
        assertEquals(new ImmutableMapEntry<>(1, "one"), it.next());
        it.remove();

        assertEquals(2, map.size());
        assertFalse(map.containsKey(1));
        assertFalse(map.containsValue("one"));
    }

    @Test
    public void testEntrySetIteratorRemoveMiddleEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Iterator<Entry<Integer,String>> it = map.entrySet().iterator();
        assertTrue(it.hasNext());
        // using ImmutableMapEntry to protect against cursors which are mutable entries
        Entry<Integer,String> e1 = new ImmutableMapEntry<>(it.next());
        assertNotNull(e1);
        assertTrue(it.hasNext());
        Entry<Integer,String> e2 = new ImmutableMapEntry<>(it.next());
        assertNotNull(e2);
        it.remove();

        assertEquals(2, map.size());
        assertFalse(map.containsKey(e2.getKey()));
        assertFalse(map.containsValue(e2.getValue()));
    }

    @Test
    public void testEntrySetIteratorReadPastLastEntry() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Iterator<Entry<Integer,String>> it = map.entrySet().iterator();
        assertTrue(it.hasNext());
        assertEquals(new ImmutableMapEntry<>(1, "one"), it.next());
        assertTrue(it.hasNext());
        assertEquals(new ImmutableMapEntry<>(2, "two"), it.next());
        assertTrue(it.hasNext());
        assertEquals(new ImmutableMapEntry<>(3, "three"), it.next());
        assertFalse(it.hasNext());

        assertThrows(NoSuchElementException.class, () -> it.next());
    }

    @Test
    public void testEntrySetIteratorRemoveLastEntry() {
        if (isReadOnly()) {
            return;
        }

        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");
        assertEquals(3, map.size());

        Iterator<Entry<Integer,String>> it = map.entrySet().iterator();
        assertTrue(it.hasNext());
        assertEquals(new ImmutableMapEntry<>(1, "one"), it.next());
        assertTrue(it.hasNext());
        assertEquals(new ImmutableMapEntry<>(2, "two"), it.next());
        assertTrue(it.hasNext());
        assertEquals(new ImmutableMapEntry<>(3, "three"), it.next());
        assertFalse(it.hasNext());

        it.remove();

        assertEquals(2, map.size());
        assertFalse(map.containsKey(3));
        assertFalse(map.containsValue("three"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldGetllEntries() {
        Map<Integer,String> map = create(1, "one", 2, "two", 3, "three");

        Set<Entry<Integer,String>> entries = map.entrySet();

        assertTrue(entries.contains(new ImmutableMapEntry(1, "one")));
        assertTrue(entries.contains(new ImmutableMapEntry(2, "two")));
        assertTrue(entries.contains(new ImmutableMapEntry(3, "three")));
    }
}
