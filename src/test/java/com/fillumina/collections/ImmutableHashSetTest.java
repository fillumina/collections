package com.fillumina.collections;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableHashSetTest {
    
    @Test
    public void testEmpty() {
        ImmutableHashSet<Integer> set = ImmutableHashSet.empty();
        assertTrue(set.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> set.add(4));
    }

    @Test
    public void testOf_GenericType() {
        ImmutableHashSet<Integer> set = ImmutableHashSet.of(1, 2, 3);
        assertEquals(1, set.get(0));
        assertEquals(2, set.get(1));
        assertEquals(3, set.get(2));

        assertThrows(UnsupportedOperationException.class, () -> set.add(4));
        assertThrows(UnsupportedOperationException.class, () -> set.clear());
        assertThrows(UnsupportedOperationException.class, () -> {
            Iterator<Integer> it = set.iterator();
            it.next();
            it.remove();
        });
    }

    @Test
    public void testOf_Collection() {
        ImmutableHashSet<Integer> set = ImmutableHashSet.of(List.of(1, 2, 3));
        assertEquals(1, set.get(0));
        assertEquals(2, set.get(1));
        assertEquals(3, set.get(2));
    }

    @Test
    public void testIndexOf() {
        ImmutableHashSet<Integer> set = ImmutableHashSet.of(1, 2, 3);
        assertEquals(0, set.indexOf(1));
        assertEquals(1, set.indexOf(2));
        assertEquals(2, set.indexOf(3));
    }

    @Test
    public void testSize() {
        assertEquals(0, ImmutableHashSet.of().size());
        assertEquals(1, ImmutableHashSet.of(1).size());
        assertEquals(2, ImmutableHashSet.of(1, 2).size());
        assertEquals(3, ImmutableHashSet.of(1, 2, 3).size());
    }


    @Test
    public void testContains() {
        ImmutableHashSet<Integer> set = ImmutableHashSet.of(1, 2, 3);
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
        assertFalse(set.contains(4));
    }

    @Test
    public void testIterator() {
        ImmutableHashSet<Integer> set = ImmutableHashSet.of(1, 2, 3);
        Iterator<Integer> it = set.iterator();
        assertEquals(1, it.next());
        assertEquals(2, it.next());
        assertEquals(3, it.next());
    }

    @Test
    public void testToArray_0args() {
        ImmutableHashSet<Integer> set = ImmutableHashSet.of(1, 2, 3);
        Number[] array = set.toArray(new Number[3]);
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);
        assertEquals(3, array[2]);
    }

    @Test
    public void testToArray_GenericType() {
        ImmutableHashSet<Integer> set = ImmutableHashSet.of(1, 2, 3);
        Object[] array = set.toArray();
        assertEquals(1, array[0]);
        assertEquals(2, array[1]);
        assertEquals(3, array[2]);
    }

    @Test
    public void testDuplicates() {
        ImmutableHashSet<Integer> set = ImmutableHashSet.of(1, 2, 2);
        assertEquals(2, set.size());
    }
    
    @Test
    public void testContainsAll() {
        assertTrue(ImmutableHashSet.of(1, 2, 3)
                        .containsAll(List.of(1, 2, 3)) );

        assertFalse(ImmutableHashSet.of(1, 2, 3)
                        .containsAll(List.of(1, 5, 3)) );
    }

    @Test
    public void testAdd() {
        assertThrows(UnsupportedOperationException.class,
                () -> ImmutableHashSet.of(1, 2, 3)
                        .add(4));
    }

    @Test
    public void testRemove() {
        assertThrows(UnsupportedOperationException.class,
                () -> ImmutableHashSet.of(1, 2, 3)
                        .remove(1));
    }

    @Test
    public void testAddAll() {
        assertThrows(UnsupportedOperationException.class,
                () -> ImmutableHashSet.of(1, 2, 3)
                        .addAll(List.of(4, 5, 6)));
    }

    @Test
    public void testRetainAll() {
        assertThrows(UnsupportedOperationException.class,
                () -> ImmutableHashSet.of(1, 2, 3)
                        .retainAll(List.of(1, 2, 6)));
    }

    @Test
    public void testRemoveAll() {
        assertThrows(UnsupportedOperationException.class,
                () -> ImmutableHashSet.of(1, 2, 3)
                        .removeAll(List.of(1, 5, 3)));
    }

    @Test
    public void testClear() {
        assertThrows(UnsupportedOperationException.class,
                () -> ImmutableHashSet.of(1, 2, 3)
                        .clear());
    }

    @Test
    public void testEquals() {
        assertEquals(Set.of(1, 2, 3), ImmutableHashSet.of(1, 2, 3));
    }
    
}
