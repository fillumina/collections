package com.fillumina.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public abstract class AbstractSetTest extends GenericSetTest {

    protected abstract <T> Set<T> createSet();
    @SuppressWarnings("unchecked")
    protected abstract <T> Set<T> createSet(T... array);
    protected abstract <T> Set<T> createSet(Collection<T> coll);

    @Test
    public void shouldSkipDuplicates() {
        Set<Integer> set = createSet(1, 2, 2, 3, 2, 4);
        assertEquals(4, set.size());
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

}
