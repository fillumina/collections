package com.fillumina.collections;

import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class VieweableMapTest {
    
    @Test
    public void shouldCloneBeAnotherObject() {
        VieweableMap<String,String> map = new VieweableMap<String,String>()
                .add("1", "a")
                .add("2", "b")
                .add("3", "c");
        
        assertTrue(map.containsKey("1"));       
        assertTrue(map.containsKey("2"));       
        assertTrue(map.containsKey("3"));

        VieweableMap<String,String> clone = map.clone();
        
        map.put("4", "d");
        
        assertEquals("d", map.get("4"));
        assertNull(clone.get("4"));
    }

    @Test
    public void shouldGetImmutableMapFromViewableMap() {
        VieweableMap<String,String> map = new VieweableMap<>();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");

        assertEquals(3, map.size());
        assertTrue(map.containsKey("1"));       
        assertTrue(map.containsKey("2"));       
        assertTrue(map.containsKey("3"));

        Map.Entry<String,String> entry = map.getEntry("1");
        assertThrows(UnsupportedOperationException.class, 
                () -> entry.setValue("aa") );
        
        
        ImmutableTableMap<String,String> roMap = map.immutable();
        
        assertEquals(3, roMap.size());
        assertTrue(roMap.containsKey("1"));       
        assertTrue(roMap.containsKey("2"));       
        assertTrue(roMap.containsKey("3"));
        
        assertThrows(UnsupportedOperationException.class, 
                () -> roMap.remove("1") );

        assertThrows(UnsupportedOperationException.class, 
                () -> roMap.put("4", "d") );

        assertThrows(UnsupportedOperationException.class, 
                () -> roMap.clear() );

        map.put("4", "d");
        assertFalse(roMap.containsKey("4"));
    }

    @Test
    public void shouldGetUnmodifiableMapFromViewableMap() {
        VieweableMap<String,String> map = new VieweableMap<>();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");

        assertEquals(3, map.size());
        assertTrue(map.containsKey("1"));       
        assertTrue(map.containsKey("2"));       
        assertTrue(map.containsKey("3"));

        Map.Entry<String,String> entry = map.getEntry("1");
        assertThrows(UnsupportedOperationException.class, 
                () -> entry.setValue("aa") );
        
        
        UnmodifiableTableMap<String,String> roMap = map.unmodifiable();
        
        assertEquals(3, roMap.size());
        assertTrue(roMap.containsKey("1"));       
        assertTrue(roMap.containsKey("2"));       
        assertTrue(roMap.containsKey("3"));
        
        assertThrows(UnsupportedOperationException.class, 
                () -> roMap.remove("1") );

        assertThrows(UnsupportedOperationException.class, 
                () -> roMap.put("4", "d") );

        assertThrows(UnsupportedOperationException.class, 
                () -> roMap.clear() );

        map.put("4", "d");
        assertTrue(roMap.containsKey("4"));
    }
    
}
