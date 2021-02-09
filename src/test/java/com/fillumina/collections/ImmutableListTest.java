package com.fillumina.collections;

import java.util.Iterator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableListTest {

    @Test
    public void testCreate_GenericType() {
        List<String> immutable = ImmutableList.create("one", "two", "three");
        assertEquals(List.of("one", "two", "three"), immutable);
    }

    @Test
    public void testCreate_List() {
        List<String> list = List.of("one", "two", "three");
        List<String> immutable = ImmutableList.create(list);
        assertEquals(list, immutable);
    }

    @Test
    public void testGet() {
        List<String> immutable = ImmutableList.create("one", "two", "three");

        assertEquals("one", immutable.get(0));
        assertEquals("two", immutable.get(1));
        assertEquals("three", immutable.get(2));
    }

    @Test
    public void testSize() {
        List<String> immutable = ImmutableList.create("one", "two", "three");
        assertEquals(3, immutable.size());
    }

    @Test
    public void shouldCreateEmpty() {
        List<String> empty1 = ImmutableList.create();
        List<String> empty2 = ImmutableList.create();

        assertEquals(0, empty1.size());
        assertTrue(empty1.isEmpty());

        // same object
        assertTrue(empty1 == empty2);
    }

    @Test
    public void shouldNotBeModifiable() {
        List<String> immutable = ImmutableList.create("one", "two", "three");

        assertThrows(UnsupportedOperationException.class, () -> immutable.clear());
        assertThrows(UnsupportedOperationException.class, () -> immutable.add("four"));
        assertThrows(UnsupportedOperationException.class, () -> immutable.remove(1));
        assertThrows(UnsupportedOperationException.class,
                () -> {
            Iterator<String> it = immutable.iterator();
            it.next();
            it.remove();
        });
    }
    
    @Test
    public void shouldReturnAnAlreadyImmutableListIfPassed() {
        List<String> immutable = ImmutableList.create("one", "two", "three");
        List<String> other = ImmutableList.create(immutable);
        
        assertTrue(immutable == other);
    }
}
