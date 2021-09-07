package com.fillumina.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class GenericCollectionTest {
    public static final int MASS_SIZE = 1000;

    /** Override */
    protected <T extends Comparable<T>> Collection<T> create(Collection<T> collection) {
        return new ArrayList<>(collection);
    }

    /** Override */
    protected boolean isReadOnly() {
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> Collection<T> create(T... array) {
        final Collection<T> coll = create(Arrays.asList(array));
        return coll;
    }

    @Test
    public void testMassInsertionAndRemoval() {
        if (isReadOnly()) {
            return;
        }

        Collection<Integer> coll = create();
        List<Integer> list = new ArrayList<>();

        for (int i=0; i<MASS_SIZE; i++) {
            list.add(i);
            assertTrue(coll.add(i));
            assertEquals(i+1, coll.size());
        }

        assertEquals(MASS_SIZE, coll.size());
        assertEquals(MASS_SIZE, list.size());

        Collections.shuffle(list);
        for (int value : list) {
            assertTrue(coll.contains(value));
        }

        int expectedSize = MASS_SIZE;
        Collections.shuffle(list);
        for (int value : list) {
            expectedSize--;
            assertTrue(coll.remove(value));
            assertEquals(expectedSize, coll.size());
        }

        assertTrue(coll.isEmpty());
        assertEquals(0, coll.size());
    }

    @Test
    public void testIteratorForEmptyCollection() {
        Collection<String> coll = create();
        Iterator<String> it = coll.iterator();
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());
    }

    @Test
    public void testIteratorForOneElementCollection() {
        Collection<String> coll = create("one");
        Iterator<String> it = coll.iterator();
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());
    }

    @Test
    public void testIteratorForTwoElementsCollection() {
        Collection<String> coll = create("one", "two");
        assertEquals(2, coll.size());
        Set<String> set = new HashSet<>();
        Iterator<String> it = coll.iterator();
        assertTrue(it.hasNext());
        String v1 = it.next();
        assertNotNull(v1);
        set.add(v1);
        assertTrue(it.hasNext());
        String v2 = it.next();
        assertNotNull(v2);
        set.add(v2);
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());

        assertEquals(2, coll.size());
        assertEquals(set.size(), coll.size());
        for (String v : coll) {
            assertTrue(set.contains(v));
        }
    }

    /** Iterators aren't guarantee to return ordered data. */
    @Test
    public void testIteratorForThreeElementsCollection() {
        Collection<String> coll = create("one", "two", "three");
        Set<String> set = new HashSet<>();
        Iterator<String> it = coll.iterator();
        assertTrue(it.hasNext());
        String v1 = it.next();
        assertNotNull(v1);
        set.add(v1);
        assertTrue(it.hasNext());
        String v2 = it.next();
        assertNotNull(v2);
        set.add(v2);
        assertTrue(it.hasNext());
        String v3 = it.next();
        assertNotNull(v3);
        set.add(v3);
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());

        assertEquals(3, coll.size());
        assertEquals(set.size(), coll.size());
        for (String v : coll) {
            assertTrue(set.contains(v));
        }
    }

    @Test
    public void shouldRemoveAllElementsByIterating() {
        if (isReadOnly()) {
            return;
        }
        Collection<String> coll = create("one", "two", "three");
        assertEquals(3, coll.size());

        Iterator<String> it = coll.iterator();
        while(it.hasNext()) {
            it.next();
            it.remove();
        }
        assertEquals(0, coll.size());
        assertTrue(coll.isEmpty());
    }

    @Test
    public void shouldRemoveByIteratorFirst() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3);
        Iterator<Integer> it = coll.iterator();
        assertEquals(1, it.next());
        it.remove();
        assertFalse(coll.contains(1));
        assertEquals(2, coll.size());
        assertTrue(it.hasNext());
        assertEquals(2, it.next());
    }

    @Test
    public void shouldRemoveByIteratorMiddle() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3);
        Iterator<Integer> it = coll.iterator();
        assertEquals(1, it.next());
        assertEquals(2, it.next());
        it.remove();
        assertFalse(coll.contains(2));
        assertEquals(2, coll.size());
        assertTrue(it.hasNext());
        assertEquals(3, it.next());
    }

    @Test
    public void shouldRemoveByIteratorLast() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3);
        Iterator<Integer> it = coll.iterator();
        assertEquals(1, it.next());
        assertEquals(2, it.next());
        assertEquals(3, it.next());
        it.remove();
        assertFalse(coll.contains(3));
        assertEquals(2, coll.size());
        assertFalse(it.hasNext());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSize() {
        assertEquals(0, create().size());
        assertEquals(1, create(1).size());
        assertEquals(2, create(1, 2).size());
        assertEquals(3, create(1, 2, 3).size());
    }

    @Test
    public void testClearForEmptyCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create();
        assertTrue(coll.isEmpty());
        coll.clear();
        assertTrue(coll.isEmpty());
    }

    @Test
    public void testClearForOneElementCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1);
        assertFalse(coll.isEmpty());
        coll.clear();
        assertTrue(coll.isEmpty());
    }

    @Test
    public void testClearForTwoElementsCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2);
        assertFalse(coll.isEmpty());
        coll.clear();
        assertTrue(coll.isEmpty());
    }

    @Test
    public void testRemoveEmptyCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create();
        assertTrue(coll.isEmpty());
        assertFalse(coll.remove(1));
        assertTrue(coll.isEmpty());
    }

    @Test
    public void testRemoveOneElementCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1);
        assertEquals(1, coll.size());
        assertFalse(coll.remove(5));
        assertEquals(1, coll.size());
        assertTrue(coll.remove(1));
        assertEquals(0, coll.size());
        assertTrue(coll.isEmpty());
    }

    @Test
    public void testRemoveTwoElementsCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2);
        assertEquals(2, coll.size());
        assertFalse(coll.remove(5));
        assertEquals(2, coll.size());
        assertTrue(coll.remove(1));
        assertEquals(1, coll.size());
        assertFalse(coll.remove(4));
        assertTrue(coll.remove(2));
        assertEquals(0, coll.size());
        assertTrue(coll.isEmpty());
    }


    @Test
    public void shouldRemoveFirstElement() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3);
        coll.remove(1);
        assertFalse(coll.contains(1));
    }

    @Test
    public void shouldRemoveMiddleElement() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3);
        coll.remove(2);
        assertFalse(coll.contains(2));
    }

    @Test
    public void shouldRemoveLastElement() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3);
        coll.remove(3);
        assertFalse(coll.contains(3));
    }

    @Test
    public void testRetainAllEmptyCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create();
        assertTrue(coll.isEmpty());
        assertFalse(coll.retainAll(Arrays.asList(1, 2, 3)));
        assertTrue(coll.isEmpty());
    }

    @Test
    public void testRetainAllOneElementCollectionNoChange() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1);
        assertTrue(coll.contains(1));
        assertFalse(coll.retainAll(Arrays.asList(1, 2, 3)));
        assertTrue(coll.contains(1));
    }

    @Test
    public void testRetainAllOneElementCollectionChange() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1);
        assertTrue(coll.contains(1));
        assertTrue(coll.retainAll(Arrays.asList(2, 3)));
        assertFalse(coll.contains(1));
        assertTrue(coll.isEmpty());
    }

    @Test
    public void testRetainAllThreeElementsCollectionNoChange() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3);
        assertTrue(coll.contains(1));
        assertTrue(coll.contains(2));
        assertTrue(coll.contains(3));
        assertFalse(coll.retainAll(Arrays.asList(1, 2, 3)));
        assertTrue(coll.contains(1));
        assertTrue(coll.contains(2));
        assertTrue(coll.contains(3));
    }

    @Test
    public void testRetainAllThreeElementsCollectionPartialChange() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3);
        assertTrue(coll.contains(1));
        assertTrue(coll.contains(2));
        assertTrue(coll.contains(3));
        assertTrue(coll.retainAll(Arrays.asList(5, 2, 6)));
        assertEquals(1, coll.size());
        assertTrue(coll.contains(2));
    }

    @Test
    public void testRetainAllThreeElementsCollectionChange() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3);
        assertTrue(coll.contains(1));
        assertTrue(coll.retainAll(Arrays.asList(2, 3)));
        assertEquals(2, coll.size());
        assertFalse(coll.contains(1));
        assertTrue(coll.contains(2));
        assertTrue(coll.contains(3));
    }

    @Test
    public void testRemoveAllFourElementsCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3, 4);
        assertEquals(4, coll.size());
        assertTrue(coll.contains(1));
        assertTrue(coll.contains(2));
        assertTrue(coll.contains(3));
        assertTrue(coll.contains(4));
        assertFalse(coll.removeAll(Arrays.asList(100, 400, 600, 800)));
        assertEquals(4, coll.size());
        assertTrue(coll.contains(1));
        assertTrue(coll.contains(2));
        assertTrue(coll.contains(3));
        assertTrue(coll.contains(4));
        assertTrue(coll.removeAll(Arrays.asList(2, 4, 6, 8)));
        assertEquals(2, coll.size());
        assertTrue(coll.contains(1));
        assertTrue(coll.contains(3));
    }

    @Test
    public void testRemoveAllThreeElementsCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create(1, 2, 3);
        assertEquals(3, coll.size());
        assertTrue(coll.contains(1));
        assertTrue(coll.contains(2));
        assertTrue(coll.contains(3));
        assertFalse(coll.removeAll(Arrays.asList(100, 400, 600, 800)));
        assertEquals(3, coll.size());
        assertTrue(coll.contains(1));
        assertTrue(coll.contains(2));
        assertTrue(coll.contains(3));
        assertTrue(coll.removeAll(Arrays.asList(1,2, 3)));
        assertEquals(0, coll.size());
        assertTrue(coll.isEmpty());
    }

    @Test
    public void testRemoveAllEmptyCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<Integer> coll = create();
        assertTrue(coll.isEmpty());
        assertFalse(coll.removeAll(Arrays.asList()));
        assertTrue(coll.isEmpty());
        assertFalse(coll.removeAll(Arrays.asList(100, 400, 600, 800)));
        assertTrue(coll.isEmpty());
        assertFalse(coll.removeAll(Arrays.asList(1)));
        assertTrue(coll.isEmpty());
    }

    @Test
    public void testAddAllThreeElementsOnNonEmptyCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<String> coll = create("alpha", "beta");
        assertEquals(2, coll.size());
        coll.addAll(Arrays.asList("one", "two", "three"));
        assertEquals(5, coll.size());
        assertTrue(coll.contains("alpha"));
        assertTrue(coll.contains("beta"));
        assertTrue(coll.contains("one"));
        assertTrue(coll.contains("two"));
        assertTrue(coll.contains("three"));
    }

    @Test
    public void testAddAllNoElementsOnNonEmptyCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<String> coll = create("alpha", "beta");
        assertEquals(2, coll.size());
        coll.addAll(Collections.<String>emptyList());
        assertEquals(2, coll.size());
        assertTrue(coll.contains("alpha"));
        assertTrue(coll.contains("beta"));
    }

    @Test
    public void testAddAllThreeElementsOnEmptyCollection() {
        if (isReadOnly()) {
            return;
        }
        Collection<String> coll = create();
        assertEquals(0, coll.size());
        coll.addAll(Arrays.asList("one", "two", "three"));
        assertEquals(3, coll.size());
        assertTrue(coll.contains("one"));
        assertTrue(coll.contains("two"));
        assertTrue(coll.contains("three"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testContainsAll() {
        assertTrue(create()
                .containsAll(Arrays.asList()));

        assertFalse(create()
                .containsAll(Arrays.asList(1)));

        assertTrue(create(1)
                .containsAll(Arrays.asList(1)));

        assertFalse(create(1)
                .containsAll(Arrays.asList(2)));

        assertFalse(create(1)
                .containsAll(Arrays.asList(1,2)));

        assertTrue(create(1)
                .containsAll(Arrays.asList(1, 1, 1, 1)));

        assertTrue(create(1, 2)
                .containsAll(Arrays.asList(1, 2)));

        assertFalse(create(1, 2)
                .containsAll(Arrays.asList(1,3)));

        assertFalse(create(1, 2)
                .containsAll(Arrays.asList(1,2,3)));

        assertTrue(create(1, 2, 3)
                .containsAll(Arrays.asList(1, 2, 3)));

    }

    @Test
    public void testAdd() {
        if (isReadOnly()) {
            return;
        }

        Collection<String> coll = create();
        assertEquals(0, coll.size());

        coll.add("one");
        assertEquals(1, coll.size());
        assertTrue(coll.contains("one"));

        coll.add("two");
        assertEquals(2, coll.size());
        assertTrue(coll.contains("one"));
        assertTrue(coll.contains("two"));

        coll.add("three");
        assertEquals(3, coll.size());
        assertTrue(coll.contains("one"));
        assertTrue(coll.contains("two"));
        assertTrue(coll.contains("three"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testToArray_GenericType() {
        assertArrayEquals(new Object[]{},
                create().toArray());

        assertArrayEquals(new Object[]{1}, create(1).toArray());

        Object[] array1 = (Object[]) create(1, 2).toArray();
        assertEquals(2, array1.length);
        Arrays.sort(array1);
        assertArrayEquals(new Object[]{1, 2}, array1);

        Object[] array2 = (Object[]) create(1, 2, 3).toArray();
        assertEquals(3, array2.length);
        Arrays.sort(array2);
        assertArrayEquals(new Object[]{1, 2, 3}, array2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testToArray_0args() {
        assertArrayEquals(new Integer[]{}, create().toArray(new Integer[0]));

        assertArrayEquals(new Integer[]{1}, create(1).toArray(new Integer[1]));

        Integer[] array1 = (Integer[]) create(1, 2).toArray(new Integer[2]);
        assertEquals(2, array1.length);
        Arrays.sort(array1);
        assertArrayEquals(new Integer[]{1, 2}, array1);

        Integer[] array2 = (Integer[]) create(1, 2, 3).toArray(new Integer[3]);
        assertEquals(3, array2.length);
        Arrays.sort(array2);
        assertArrayEquals(new Integer[]{1, 2, 3}, array2);
    }

    @Test
    public void testNoContains() {
        Collection<Integer> coll = create();
        assertFalse(coll.contains(0));
        assertFalse(coll.contains(2));
    }

    @Test
    public void testContains() {
        Collection<Integer> coll = create(1, 2, 3, 4);
        assertFalse(coll.contains(0));
        assertTrue(coll.contains(1));
        assertTrue(coll.contains(2));
        assertTrue(coll.contains(3));
        assertTrue(coll.contains(4));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIsEmpty() {
        assertTrue(create().isEmpty());
        assertFalse(create(1).isEmpty());
        assertFalse(create(1, 2, 3).isEmpty());

        if (!isReadOnly()) {
            Collection<Integer> coll = create(1, 2, 3);
            assertFalse(coll.isEmpty());
            coll.clear();
            assertTrue(coll.isEmpty());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testHashCode() {
        Collection<String> coll = create();
        int emptyCollectionHashCode = coll.hashCode();
        if (emptyCollectionHashCode != 0) {
            return; // it's an inline class that doesn't overwrite Object's hashCode
        }

        assertEquals(0, emptyCollectionHashCode);

        assertEquals(create().hashCode(), create().hashCode());
        assertEquals(create(1).hashCode(), create(1).hashCode());
        assertEquals(create(1,2).hashCode(), create(1, 2).hashCode());
        assertEquals(create(1, 2, 3).hashCode(),
                create(1, 2, 3).hashCode());

        assertNotEquals(create(1).hashCode(), create().hashCode());
        assertNotEquals(create(1).hashCode(), create(1, 2).hashCode());

        if (!isReadOnly()) {
            Collection<Integer> c1 = create();
            c1.add(1);
            c1.add(2);
            c1.add(3);
            assertEquals(c1.hashCode(), create(1, 2, 3).hashCode());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testEquals() {
        if (!create().equals(create())) {
            return; // it's an inline class that doesn't override Object's equals
        }

        assertEquals(create(), create());
        assertEquals(create(1), create(1));
        assertEquals(create(1,2), create(1, 2));
        assertEquals(create(1, 2, 3),
                create(1, 2, 3));

        assertNotEquals(create(1), create());
        assertNotEquals(create(1), create(1, 2));

        if (!isReadOnly()) {
            Collection<Integer> c1 = create();
            c1.add(1);
            c1.add(2);
            c1.add(3);
            assertEquals(c1, create(1, 2, 3));
        }
    }
}
