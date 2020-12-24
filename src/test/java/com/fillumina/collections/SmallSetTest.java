package com.fillumina.collections;

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
public class SmallSetTest extends AbstractSetTest {

    @Override
    protected <T> Set<T> createSet() {
        return new SmallSet<>();
    }

    @Override
    protected <T> Set<T> createSet(T... array) {
        return new SmallSet<>(array);
    }

    @Override
    protected <T> Set<T> createSet(Collection<T> coll) {
        return new SmallSet<>(coll);
    }
    
    @Test
    public void shouldGetElementsByIndex() {
        SmallSet<Integer> set = new SmallSet<>(List.of(3, 2, 1));
        assertEquals(3, set.get(0));
        assertEquals(2, set.get(1));
        assertEquals(1, set.get(2));
    }
 
    @Test
    public void shouldBeSortable() {
        SmallSet<Integer> set = new SmallSet<>(List.of(3, 2, 1));
        set.sort();
        assertEquals(1, set.get(0));
        assertEquals(2, set.get(1));
        assertEquals(3, set.get(2));
    }
    
    @Test
    public void shouldRemoveAtIndexFirst() {
        SmallSet<Integer> set = new SmallSet<>(List.of(1, 2, 3));
        set.removeAtIndex(0);
        assertFalse(set.contains(1));
        assertEquals(2, set.size());
    }
    
    @Test
    public void shouldRemoveAtIndexMiddle() {
        SmallSet<Integer> set = new SmallSet<>(List.of(1, 2, 3));
        set.removeAtIndex(1);
        assertFalse(set.contains(2));
        assertEquals(2, set.size());
    }
    
    @Test
    public void shouldRemoveAtIndexLast() {
        SmallSet<Integer> set = new SmallSet<>(List.of(1, 2, 3));
        set.removeAtIndex(2);
        assertFalse(set.contains(3));
        assertEquals(2, set.size());
    }
    
    @Test
    public void shouldRemoveByIteratorFirst() {
        SmallSet<Integer> set = new SmallSet<>(List.of(1, 2, 3));
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
        SmallSet<Integer> set = new SmallSet<>(List.of(1, 2, 3));
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
        SmallSet<Integer> set = new SmallSet<>(List.of(1, 2, 3));
        Iterator<Integer> it = set.iterator();
        assertEquals(1, it.next());
        assertEquals(2, it.next());
        assertEquals(3, it.next());
        it.remove();
        assertFalse(set.contains(3));
        assertEquals(2, set.size());
        assertFalse(it.hasNext());
    }
    
    @Test
    public void shouldUseCloneConstructorForArray() {
        SmallSet<String> smallSet = new SmallSet<>(
                "one", "two", "three");
        SmallSet<String> clone = new SmallSet<>(smallSet);
        assertEquals("one", clone.get(0));
        assertEquals("two", clone.get(1));
        assertEquals("three", clone.get(2));
        
        clone.add("four");
        assertTrue(clone.contains("four"));
        assertFalse(smallSet.contains("four"));
    }
    
    @Test
    public void shouldUseCloneConstructorForSingleObject() {
        SmallSet<Integer> smallSet = new SmallSet<>(1);
        SmallSet<Integer> clone = new SmallSet<>(smallSet);
        assertEquals(1, clone.get(0));
        
        clone.add(2);
        assertTrue(clone.contains(2));
        assertFalse(smallSet.contains(2));
    }
}
