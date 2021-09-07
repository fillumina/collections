package com.fillumina.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class GenericSetTest extends GenericCollectionTest {

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> Set<T> createSet(T... array) {
        return create(Arrays.asList(array));
    }

    @Override
    protected <T extends Comparable<T>> Set<T> create(Collection<T> collection) {
        return new HashSet<>(collection);
    }

    @Test
    public void shouldNotAddAnExistingElement() {
        if (isReadOnly()) {
            return;
        }

        Set<Integer> set = createSet(1, 2, 3);
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
        assertFalse(set.add(1));
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
        assertFalse(set.add(2));
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
        assertFalse(set.add(3));
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));

        assertTrue(set.add(4));
        assertEquals(4, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(2));
        assertTrue(set.contains(3));
        assertTrue(set.contains(4));
    }

    @Test
    public void shouldNotAddAnExistingElementOnEmptySet() {
        if (isReadOnly()) {
            return;
        }

        Set<Integer> set = createSet();
        assertEquals(0, set.size());
        assertTrue(set.add(1));
        assertEquals(1, set.size());
        assertTrue(set.contains(1));
        assertFalse(set.add(1));
        assertEquals(1, set.size());
        assertTrue(set.contains(1));

        assertTrue(set.add(4));
        assertEquals(2, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(4));
    }

}
