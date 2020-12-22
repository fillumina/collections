package com.fillumina.collections;

import com.fillumina.collections.MultiMap.Tree;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MultiMapTest {

    @Test
    public void shouldAddValues() {
        MultiMap<Integer> mmap = new MultiMap<>();
        
        mmap.add(1, "alpha", 12);
        mmap.add(2, "alpha", 13);
        
        Set<Integer> alphaSet = mmap.get("alpha");
        
        assertEquals(2, alphaSet.size());
        assertTrue(alphaSet.contains(1));
        assertTrue(alphaSet.contains(2));
        
        Set<Integer> alphaNumberSet = mmap.get("alpha", 12);
        
        assertEquals(1, alphaNumberSet.size());
        assertTrue(alphaNumberSet.contains(1));
    }

    @Test
    public void shouldRemoveValues() {
        MultiMap<Integer> mmap = new MultiMap<>();
        
        mmap.add(1, "alpha", 12);
        mmap.add(2, "alpha", 13);
        
        mmap.remove("alpha", 12);
        
        Set<Integer> alphaSet = mmap.get("alpha");

        assertEquals(1, alphaSet.size());
        assertFalse(alphaSet.contains(1));
        assertTrue(alphaSet.contains(2));
    }
    
    @Test
    public void shouldCreateTree() {
        MultiMap<String> mmap = new MultiMap<>();
        
        for (String c : List.of("IT", "FR", "ES")) {
            for (String b : List.of("one", "two", "three")) {
                for (String d : List.of("alfa", "beta")) {
                    for (int i=0; i<5; i++) {
                        String value = "" + c + ":" + b + ":" + d + ":" + i;
                        mmap.add(value, c, b, d);
                    }
                }
            }
        }
        
        Tree<String> tree = mmap.treeFromIndexes(0, 1,2);
        
        System.out.println("END");
    }
    
    @Test
    public void shouldSelectFromEqualElements() {
        MultiMap<String> mmap = new MultiMap<>();
        
        mmap.add("one", 'a', 1);
        mmap.add("one", 'a', 2);
        mmap.add("one", 'b', 1);
        mmap.add("one", 'b', 2);
        
        assertEquals(1, mmap.size());
        
        assertEquals(Set.of("one"), mmap.get('a',1));
        assertEquals(Set.of("one"), mmap.get('a',2));
        assertEquals(Set.of("one"), mmap.get('b',1));
        assertEquals(Set.of("one"), mmap.get('b',2));
    }
    
    @Test
    public void shoulRetainSet() {
        Set<Integer> a = Set.of(1, 2, 3);
        Set<Integer> b = Set.of(2, 3, 4);
        Set<Integer> result = new HashSet<>(a);
        result.retainAll(b);
        assertEquals(Set.of(2, 3), result);
    }
    
    @Test
    public void shouldDifferentiate() {
        MultiMap<String> mmap = new MultiMap<>();
        
        mmap.add("one", 'a', 1);
        mmap.add("one", 'a', 2);
        mmap.add("one", 'b', 3);
        
        assertEquals(1, mmap.size());
        
        assertNotEquals(Set.of("one"), mmap.get('a',3));
    }
}
