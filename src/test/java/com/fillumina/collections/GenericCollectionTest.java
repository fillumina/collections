/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.fillumina.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class GenericCollectionTest {

    /** Override */
    protected <T> Collection<T> create(Collection<T> collection) {
        return new ArrayList<>(collection);
    }

    /** Override */
    protected boolean readOnly() {
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T> Collection<T> create(T... array) {
        return create(Arrays.asList(array));
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
        Iterator<String> it = coll.iterator();
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertTrue(it.hasNext());
        assertEquals("two", it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());
    }

    @Test
    public void testIteratorForThreeElementsCollection() {
        Collection<String> coll = create("one", "two", "three");
        Iterator<String> it = coll.iterator();
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertTrue(it.hasNext());
        assertEquals("two", it.next());
        assertTrue(it.hasNext());
        assertEquals("three", it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, () -> it.next());
    }


    @Test
    public void shouldRemoveByIteratorFirst() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1, 2, 3);
            Iterator<Integer> it = coll.iterator();
            assertEquals(1, it.next());
            it.remove();
            assertFalse(coll.contains(1));
            assertEquals(2, coll.size());
            assertTrue(it.hasNext());
            assertEquals(2, it.next());
        }
    }

    @Test
    public void shouldRemoveByIteratorMiddle() {
        if (!readOnly()) {
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
    }

    @Test
    public void shouldRemoveByIteratorLast() {
        if (!readOnly()) {
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
    }

    @Test
    public void testSize() {
        assertEquals(0, create().size());
        assertEquals(1, create(1).size());
        assertEquals(2, create(1, 2).size());
        assertEquals(3, create(1, 2, 3).size());
    }

    @Test
    public void testClearForEmptyCollection() {
        if (!readOnly()) {
            Collection<Integer> coll = create();
            assertTrue(coll.isEmpty());
            coll.clear();
            assertTrue(coll.isEmpty());
        }
    }

    @Test
    public void testClearForOneElementCollection() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1);
            assertFalse(coll.isEmpty());
            coll.clear();
            assertTrue(coll.isEmpty());
        }
    }

    @Test
    public void testClearForTwoElementsCollection() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1, 2);
            assertFalse(coll.isEmpty());
            coll.clear();
            assertTrue(coll.isEmpty());
        }
    }

    @Test
    public void testRemoveEmptyCollection() {
        if (!readOnly()) {
            Collection<Integer> coll = create();
            assertTrue(coll.isEmpty());
            assertFalse(coll.remove(1));
            assertTrue(coll.isEmpty());
        }
    }

    @Test
    public void testRemoveOneElementCollection() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1);
            assertEquals(1, coll.size());
            assertFalse(coll.remove(5));
            assertEquals(1, coll.size());
            assertTrue(coll.remove(1));
            assertEquals(0, coll.size());
            assertTrue(coll.isEmpty());
        }
    }

    @Test
    public void testRemoveTwoElementsCollection() {
        if (!readOnly()) {
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
    }


    @Test
    public void shouldRemoveFirstElement() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1, 2, 3);
            coll.remove(1);
            assertFalse(coll.contains(1));
        }
    }

    @Test
    public void shouldRemoveMiddleElement() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1, 2, 3);
            coll.remove(2);
            assertFalse(coll.contains(2));
        }
    }

    @Test
    public void shouldRemoveLastElement() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1, 2, 3);
            coll.remove(3);
            assertFalse(coll.contains(3));
        }
    }

    @Test
    public void testRetainAllEmptyCollection() {
        if (!readOnly()) {
            Collection<Integer> coll = create();
            assertTrue(coll.isEmpty());
            assertFalse(coll.retainAll(Arrays.asList(1, 2, 3)));
            assertTrue(coll.isEmpty());
        }
    }

    @Test
    public void testRetainAllOneElementCollectionNoChange() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1);
            assertTrue(coll.contains(1));
            assertFalse(coll.retainAll(Arrays.asList(1, 2, 3)));
            assertTrue(coll.contains(1));
        }
    }

    @Test
    public void testRetainAllOneElementCollectionChange() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1);
            assertTrue(coll.contains(1));
            assertTrue(coll.retainAll(Arrays.asList(2, 3)));
            assertFalse(coll.contains(1));
            assertTrue(coll.isEmpty());
        }
    }

    @Test
    public void testRetainAllThreeElementsCollectionNoChange() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1, 2, 3);
            assertTrue(coll.contains(1));
            assertTrue(coll.contains(2));
            assertTrue(coll.contains(3));
            assertFalse(coll.retainAll(Arrays.asList(1, 2, 3)));
            assertTrue(coll.contains(1));
            assertTrue(coll.contains(2));
            assertTrue(coll.contains(3));
        }
    }

    @Test
    public void testRetainAllThreeElementsCollectionPartialChange() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1, 2, 3);
            assertTrue(coll.contains(1));
            assertTrue(coll.contains(2));
            assertTrue(coll.contains(3));
            assertTrue(coll.retainAll(Arrays.asList(5, 2, 6)));
            assertEquals(1, coll.size());
            assertTrue(coll.contains(2));
        }
    }

    @Test
    public void testRetainAllThreeElementsCollectionChange() {
        if (!readOnly()) {
            Collection<Integer> coll = create(1, 2, 3);
            assertTrue(coll.contains(1));
            assertTrue(coll.retainAll(Arrays.asList(2, 3)));
            assertEquals(2, coll.size());
            assertFalse(coll.contains(1));
            assertTrue(coll.contains(2));
            assertTrue(coll.contains(3));
        }
    }

    @Test
    public void testRemoveAllFourElementsCollection() {
        if (!readOnly()) {
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
    }

    @Test
    public void testRemoveAllThreeElementsCollection() {
        if (!readOnly()) {
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
    }

    @Test
    public void testRemoveAllEmptyCollection() {
        if (!readOnly()) {
            Collection<Integer> coll = create();
            assertTrue(coll.isEmpty());
            assertFalse(coll.removeAll(Arrays.asList()));
            assertTrue(coll.isEmpty());
            assertFalse(coll.removeAll(Arrays.asList(100, 400, 600, 800)));
            assertTrue(coll.isEmpty());
            assertFalse(coll.removeAll(Arrays.asList(1)));
            assertTrue(coll.isEmpty());
        }
    }

    @Test
    public void testAddAllThreeElementsOnNonEmptyCollection() {
        if (!readOnly()) {
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
    }

    @Test
    public void testAddAllNoElementsOnNonEmptyCollection() {
        if (!readOnly()) {
            Collection<String> coll = create("alpha", "beta");
            assertEquals(2, coll.size());
            coll.addAll(Collections.<String>emptyList());
            assertEquals(2, coll.size());
            assertTrue(coll.contains("alpha"));
            assertTrue(coll.contains("beta"));
        }
    }

    @Test
    public void testAddAllThreeElementsOnEmptyCollection() {
        if (!readOnly()) {
            Collection<String> coll = create();
            assertEquals(0, coll.size());
            coll.addAll(Arrays.asList("one", "two", "three"));
            assertEquals(3, coll.size());
            assertTrue(coll.contains("one"));
            assertTrue(coll.contains("two"));
            assertTrue(coll.contains("three"));
        }
    }

    @Test
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
        if (!readOnly()) {

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
    }

    @Test
    public void testToArray_GenericType() {
        assertArrayEquals(new Object[]{},
                create().toArray());

        assertArrayEquals(new Object[]{"one"},
                create("one").toArray());

        assertArrayEquals(new Object[]{"one", "two"},
                create("one", "two").toArray());

        assertArrayEquals(new Object[]{"one", "two", "three"},
                create("one", "two", "three").toArray());
    }

    @Test
    public void testToArray_0args() {
        assertArrayEquals(new String[]{},
                create().toArray(new String[0]));

        assertArrayEquals(new String[]{"one"},
                create("one").toArray(new String[1]));

        assertArrayEquals(new String[]{"one", "two"},
                create("one", "two").toArray(new String[2]));

        assertArrayEquals(new String[]{"one", "two", "three"},
                create("one", "two", "three").toArray(new String[3]));
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
    public void testIsEmpty() {
        assertTrue(create().isEmpty());
        assertFalse(create(1).isEmpty());
        assertFalse(create(1, 2, 3).isEmpty());

        if (!readOnly()) {
            Collection<Integer> coll = create(1, 2, 3);
            assertFalse(coll.isEmpty());
            coll.clear();
            assertTrue(coll.isEmpty());
        }
    }

    @Test
    public void testHashCode() {
        assertEquals(create().hashCode(), create().hashCode());
        assertEquals(create(1).hashCode(), create(1).hashCode());
        assertEquals(create(1,2).hashCode(), create(1, 2).hashCode());
        assertEquals(create(1, 2, 3).hashCode(),
                create(1, 2, 3).hashCode());

        assertNotEquals(create(1).hashCode(), create().hashCode());
        assertNotEquals(create(1).hashCode(), create(1, 2).hashCode());

        Collection<Integer> c1 = create();
        c1.add(1);
        c1.add(2);
        c1.add(3);
        assertEquals(c1.hashCode(), create(1, 2, 3).hashCode());
    }

    @Test
    public void testEquals() {
        assertEquals(create(), create());
        assertEquals(create(1), create(1));
        assertEquals(create(1,2), create(1, 2));
        assertEquals(create(1, 2, 3),
                create(1, 2, 3));

        assertNotEquals(create(1), create());
        assertNotEquals(create(1), create(1, 2));

        Collection<Integer> c1 = create();
        c1.add(1);
        c1.add(2);
        c1.add(3);
        assertEquals(c1, create(1, 2, 3));
    }
}
