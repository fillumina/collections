package com.fillumina.collections;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class HolderTest {
    
    @Test
    public void usage() {
        Holder<Integer> h = new Holder<>();
        
        Stream.of(1, 7, 8, 2).forEach(i -> h.set(i));
        
        assertEquals(2, h.get());
    }
    
    @Test
    public void testSetIfNull() {
        Holder<Integer> h = new Holder<>();

        h.onNullSet(34);
        assertEquals(34, h.get());

        h.onNullSet(72);
        assertEquals(34, h.get());
    }

    @Test
    public void testIsEmpty() {
        Holder<Integer> h = new Holder<>();
        assertTrue(h.isEmpty());
        
        h.set(null);
        assertFalse(h.isEmpty());
        assertTrue(h.isNull());
    }

    @Test
    public void testIsNullFromConstructor() {
        Holder<Integer> h = new Holder<>(null);
        assertTrue(h.isNull());
    }

    @Test
    public void testIsNull() {
        Holder<Integer> h = new Holder<>();
        h.set(null);
        assertTrue(h.isNull());
    }

    @Test
    public void testGetValue() {
        Holder<Integer> h = new Holder<>();
        h.set(12);
        assertEquals(12, h.get());
    }

    @Test
    public void testSetValue() {
        Holder<Integer> h = new Holder<>();
        h.set(4);
        h.set(7);
        assertEquals(7, h.get());
    }

    @Test
    public void testOrElse() {
        Holder<Integer> h = new Holder<>();
        
        assertEquals(21, h.orElse(21));
        
        h.set(12);
        assertEquals(12, h.orElse(21));
    }

    @Test
    public void testOrElseGet() {
        Holder<Integer> h = new Holder<>();
        
        assertEquals(21, h.orElseGet(() -> 21));
        
        h.set(12);
        assertEquals(12, h.orElseGet(() -> 21));
    }

    @Test
    public void testOrElseThrow_0args() {
        Holder<Integer> h = new Holder<>();
        
        assertThrows(NoSuchElementException.class, () -> h.orElseThrow());
    }

    @Test
    public void testOrElseThrow_Supplier() throws Exception {
        Holder<Integer> h = new Holder<>();
        
        assertThrows(IllegalArgumentException.class, () -> 
                h.orElseThrow(() -> new IllegalArgumentException()));
    }

    @Test
    public void testHashCode() {
        final String string = "hello";
        assertEquals(string.hashCode(), new Holder<>(string).hashCode());
        
        final Integer value = Integer.valueOf(123);
        assertEquals(value.hashCode(), new Holder<>(value).hashCode());
    }

    @Test
    public void testEquals() {
        final String string = "hello";
        assertTrue(new Holder<>(string).equals(string));
    }

    @Test
    public void testToString() {
        final Object value = new Date();
        assertEquals(value.toString(), new Holder<>(value).toString());
    }
    
}
