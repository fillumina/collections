package com.fillumina.collections;

import com.fillumina.collections.MultiMap.Tree;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
        final Set<String> aSet = Set.of("IT", "FR", "ES");
        final Set<Integer> bSet = Set.of(1, 2, 3);
        final Set<Character> cSet = Set.of('a', 'b', 'c');

        for (String a : aSet) {
            for (Integer b : bSet) {
                for (Character c : cSet) {
                    String value = "" + a + ":" + b + ":" + c;
                    mmap.add(value, a, b, c);
                }
            }
        }

        Tree<String> tree = mmap.treeFromIndexes(0, 1, 2);
        assertTrue(tree.isRoot());
        assertEquals(aSet, tree.getChildren().keySet());

        tree.getChildren().forEach((a, t1) -> {
            assertEquals(bSet, t1.getChildren().keySet());
            assertFalse(t1.isLeaf());
            assertFalse(t1.isRoot());
            assertEquals(tree, t1.getParent());
            assertEquals(tree, t1.getRoot());
            
            t1.getChildren().forEach((b, t2) -> {
                assertFalse(t2.isLeaf());
                assertFalse(t2.isRoot());
                assertEquals(t1, t2.getParent());
                assertEquals(tree, t2.getRoot());
                assertEquals(cSet, t2.getChildren().keySet());

                t2.getChildren().forEach((c, t3) -> {
                    assertTrue(t3.isLeaf());
                    assertFalse(t3.isRoot());
                    assertEquals(t2, t3.getParent());
                    assertEquals(tree, t3.getRoot());
                    
                    String value = "" + a + ":" + b + ":" + c;
                    assertEquals(value, t3.getValue());
                    
                    List<Object> keyList = t3.getKeyList();
                    assertEquals(a, keyList.get(0));
                    assertEquals(b, keyList.get(1));
                    assertEquals(c, keyList.get(2));
                    
                    assertEquals(value, t3.getKeyListAsString(":"));
                });
            });
        });
        
        Set<String> set = new HashSet<>();
        tree.visitValues(set::add);
        assertEquals(aSet.size() * bSet.size() * cSet.size(), set.size());
        
        tree.pruneLeaves(s -> s.contains(":b"));
        tree.getChildren().values().forEach(t1 ->
            t1.getChildren().values().forEach(t2 ->
                assertEquals(cSet.size() - 1, t2.getChildren().size())));
        
        tree.pruneBranches(o -> o.equals('c'));
        tree.getChildren().values().forEach(t1 ->
            assertNull(t1.getChildren().get('c')));
        
        Map<?,?> treeMap = tree.toMap();
        treeMap.forEach((a, m1) -> {
            assertTrue(aSet.contains(a));
            ((Map<?,?>)m1).forEach((b, m2) -> {
                assertTrue(bSet.contains(b));
                ((Map<?,?>)m2).forEach((c, v) -> {
                    assertTrue(cSet.contains(c));
                    String value = "" + a + ":" + b + ":" + c;
                    assertEquals(v, value);
                });
            });
        });
        
        Tree<String> changedTreeMap = tree.replaceTree(s -> s + "-changed");
        changedTreeMap.visitLeaves(t -> {
            if (!t.getValue().endsWith("-changed")) {
                throw new AssertionError();
            }
        });
        changedTreeMap.visitValues(s -> {
            if (!s.endsWith("-changed")) {
                throw new AssertionError();
            }
        });
       
        Map<?,?> flatMap0 = tree.flatToLevel(0);
        flatMap0.forEach((a, m1) -> {
            ((Map<?,?>)m1).forEach((b, m2) -> {
                ((Map<?,?>)m2).forEach((c, v) -> {
                    String value = "" + ((List<?>)a).get(0) + ":" + b + ":" + c;
                    assertEquals(v, value);
                });
            });
        });
        
        Map<?,?> flatMap1 = tree.flatToLevel(1);
        flatMap1.forEach((keyList, m12) -> {
            ((Map<?,?>)m12).forEach((c, v) -> {
                List<?> kl = (List<?>) keyList;
                String value = "" + kl.get(0) + ":" + kl.get(1) + ":" + c;
                assertEquals(v, value);
            });
        });
        
        Map<List<Object>,?> leavesMap = tree.getLeavesMap();
        assertEquals(9, leavesMap.size());
        assertEquals("FR:3:a", 
                leavesMap.get(List.of("FR", 3, 'a')));
    }

    @Test
    public void shouldSelectFromEqualElements() {
        MultiMap<String> mmap = new MultiMap<>();

        mmap.add("one", 'a', 1);
        mmap.add("one", 'a', 2);
        mmap.add("one", 'b', 1);
        mmap.add("one", 'b', 2);

        assertEquals(1, mmap.size());

        assertEquals(Set.of("one"), mmap.get('a', 1));
        assertEquals(Set.of("one"), mmap.get('a', 2));
        assertEquals(Set.of("one"), mmap.get('b', 1));
        assertEquals(Set.of("one"), mmap.get('b', 2));
    }

    @Test
    public void shouldGetFromPartialKey() {
        MultiMap<String> mmap = new MultiMap<>();

        mmap.add("one", 'a', 1);
        mmap.add("two", 'a', 2);
        mmap.add("three", 'b', 1);
        mmap.add("four", 'b', 2);

        assertEquals(4, mmap.size());

        assertEquals(Set.of("one", "two"), mmap.get('a'));
        assertEquals(Set.of("three", "four"), mmap.get('b'));
        assertEquals(Set.of("one", "three"), mmap.get(1));
        assertEquals(Set.of("two", "four"), mmap.get(2));
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

        assertNotEquals(Set.of("one"), mmap.get('a', 3));
    }
}
