/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fillumina.collections;

import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class CounterTest {
    
    @Test
    public void usage() {
        Counter counter = new Counter();
        Stream.iterate(0, i -> i<3, i -> i+1).forEach(i -> counter.increment());
        assertEquals(3, counter.get());
    }
    
    @Test
    public void testIncrement() {
        Counter c = new Counter();
        assertEquals(0, c.get());
        c.increment();
        assertEquals(1, c.get());
        c.increment();
        assertEquals(2, c.get());
    }

    @Test
    public void testIncrementBy() {
        Counter c = new Counter(12);
        c.incrementBy(4);
        assertEquals(16, c.get());
    }
    
    @Test
    public void testDecrement() {
        Counter c = new Counter(100);
        assertEquals(100, c.get());
        c.decrement();
        assertEquals(99, c.get());
        c.decrement();
        assertEquals(98, c.get());
    }

    @Test
    public void testDecrementBy() {
        Counter c = new Counter(12);
        c.decrementBy(4);
        assertEquals(8, c.get());
    }

    @Test
    public void testEqualsValue() {
        Counter c = new Counter(55);
        assertTrue(c.equals(55));
        assertFalse(c.equals(22));
    }
    
    @Test
    public void testGet() {
        Counter c = new Counter(45);
        assertEquals(45, c.get());
    }

    @Test
    public void testToString() {
        Counter c = new Counter(66);
        assertEquals("66", c.toString());
    }
    
    @Test
    public void testIsZero() {
        Counter c = new Counter();
        assertTrue(c.isZero());
        c.increment();
        assertFalse(c.isZero());
        c.decrement();
        assertTrue(c.isZero());
    }
}
