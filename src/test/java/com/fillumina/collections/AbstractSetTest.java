package com.fillumina.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public abstract class AbstractSetTest {

    protected abstract <T> Set<T> createSet();
    protected abstract <T> Set<T> createSet(T... array);
    protected abstract <T> Set<T> createSet(Collection<T> coll);


    @Test
    public void shouldSkipDuplicates() {
        Set<Integer> set = createSet(1, 2, 2, 3, 2, 4);
        assertEquals(4, set.size());
    }

    @Test
    public void shouldBeEmpty() {
        assertTrue(createSet().isEmpty());
    }

    @Test
    public void shouldHaveZeroSize() {
        assertEquals(0, createSet().size());
    }

    @Test
    public void shouldAddElement() {
        Set<Integer> set = createSet();
        assertTrue(set.add(1));
    }

    @Test
    public void shouldNotBeEmpty() {
        Set<Integer> set = createSet();
        set.add(1);
        assertFalse(set.isEmpty());
    }

    @Test
    public void shouldHaveSizeOne() {
        Set<Integer> set = createSet();
        set.add(1);
        assertEquals(1, set.size());
    }

    @Test
    public void shouldAddTwoElements() {
        Set<Integer> set = createSet();
        assertTrue(set.add(1));
        assertTrue(set.add(2));
    }

    @Test
    public void shouldSizeBeTwo() {
        Set<Integer> set = createSet();
        set.add(1);
        set.add(2);

        assertEquals(2, set.size());
    }

    @Test
    public void shouldContainTwoElements() {
        Set<Integer> set = createSet();
        set.add(1);
        set.add(2);

        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
    }

    @Test
    public void shouldNotAddTheSameElementTwice() {
        Set<Integer> set = createSet();
        assertTrue(set.add(1));
        assertFalse(set.add(1));

        assertEquals(1, set.size());
    }

    @Test
    public void shouldCostructASetFromOneElement() {
        Set<Integer> set = createSet(1);
        assertEquals(1, set.size());
        assertTrue(set.contains(1));
    }

    @Test
    public void shouldCostructASetFromArray() {
        Set<Integer> set = createSet(1, 2, 3);
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
    }

    @Test
    public void shouldCostructASetFromCollection() {
        Set<Integer> set = createSet(Arrays.asList(1, 2, 3));
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
    }

    @Test
    public void shoudClearTheSet() {
        Set<Integer> set = createSet(Arrays.asList(1, 2, 3));
        set.clear();
        assertTrue(set.isEmpty());
    }

    @Test
    public void shouldRemoveFirstElement() {
        Set<Integer> set = createSet(Arrays.asList(1, 2, 3));
        set.remove(1);
        assertFalse(set.contains(1));
    }

    @Test
    public void shouldRemoveMiddleElement() {
        Set<Integer> set = createSet(Arrays.asList(1, 2, 3));
        set.remove(2);
        assertFalse(set.contains(2));
    }

    @Test
    public void shouldRemoveLastElement() {
        Set<Integer> set = createSet(Arrays.asList(1, 2, 3));
        set.remove(3);
        assertFalse(set.contains(3));
    }

    @Test
    public void shouldRemoveByIteratorFirst() {
        Set<Integer> set = createSet(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = set.iterator();
        assertEquals(1, it.next());
        it.remove();
        assertFalse(set.contains(1));
        assertEquals(2, set.size());
        assertTrue(it.hasNext());
        assertEquals(2, it.next());
    }

    @Test
    public void shouldRemoveByIteratorMiddle() {
        Set<Integer> set = createSet(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = set.iterator();
        assertEquals(1, it.next());
        assertEquals(2, it.next());
        it.remove();
        assertFalse(set.contains(2));
        assertEquals(2, set.size());
        assertTrue(it.hasNext());
        assertEquals(3, it.next());
    }

    @Test
    public void shouldRemoveByIteratorLast() {
        Set<Integer> set = createSet(Arrays.asList(1, 2, 3));
        Iterator<Integer> it = set.iterator();
        assertEquals(1, it.next());
        assertEquals(2, it.next());
        assertEquals(3, it.next());
        it.remove();
        assertFalse(set.contains(3));
        assertEquals(2, set.size());
        assertFalse(it.hasNext());
    }

}
