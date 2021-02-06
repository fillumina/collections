/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fillumina.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class CounterTest {
    
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
    public void testDecrement() {
        Counter c = new Counter(100);
        
        assertEquals(100, c.get());
        
        c.decrement();
        
        assertEquals(99, c.get());
        
        c.decrement();
        
        assertEquals(98, c.get());
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
    
}
