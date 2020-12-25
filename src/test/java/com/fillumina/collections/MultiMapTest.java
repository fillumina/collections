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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MultiMapTest {
    private static final Set<String> A_SET = Set.of("IT", "FR", "ES");
    private static final Set<Integer> B_SET = Set.of(1, 2, 3);
    private static final Set<Character> C_SET = Set.of('a', 'b');

    private static Tree<String> TREE;
    
    @BeforeAll
    public static void init() {
        MultiMap<String> mmap = new MultiMap<>();
        for (String a : A_SET) {
            for (Integer b : B_SET) {
                for (Character c : C_SET) {
                    String value = "" + a + ":" + b + ":" + c;
                    mmap.add(value, a, b, c);
                }
            }
        }
        TREE = mmap.treeFromIndexes(0, 1, 2);
    }
    
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
        checkTree(TREE);
    }
    
    @Test
    public void shouldCloneTree() {
        final Tree<String> clone = TREE.clone();
        assertFalse(clone == TREE);
        checkTree(clone);
    }

    public void checkTree(Tree<String> tree) {
        assertTrue(tree.isRoot());
        assertEquals(A_SET, tree.getChildren().keySet());

        tree.getChildren().forEach((a, t1) -> {
            assertEquals(B_SET, t1.getChildren().keySet());
            assertFalse(t1.isLeaf());
            assertFalse(t1.isRoot());
            assertEquals(tree, t1.getParent());
            assertEquals(tree, t1.getRoot());
            
            t1.getChildren().forEach((b, t2) -> {
                assertFalse(t2.isLeaf());
                assertFalse(t2.isRoot());
                assertEquals(t1, t2.getParent());
                assertEquals(tree, t2.getRoot());
                assertEquals(C_SET, t2.getChildren().keySet());

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
    }
    
    @Test
    public void shouldVisitValues() {
        Set<String> set = new HashSet<>();
        TREE.visitValues(set::add);
        assertEquals(A_SET.size() * B_SET.size() * C_SET.size(), set.size());
    }
    
    @Test
    public void shouldPruneLeaves() {
        Tree<String> clone = TREE.clone();
        clone.pruneLeaves(s -> s.contains(":b"));
        clone.getChildren().values().forEach(t1 ->
            t1.getChildren().values().forEach(t2 ->
                assertEquals(C_SET.size() - 1, t2.getChildren().size())));
    }
    
    @Test
    public void shouldPruneBrances() {
        Tree<String> clone = TREE.clone();
        clone.pruneBranches(o -> o.equals('c'));
        clone.getChildren().values().forEach(t1 ->
            assertNull(t1.getChildren().get('c')));
    }
    
    @Test
    public void shouldGetMap() {
        Map<?,?> treeMap = TREE.toMap();
        treeMap.forEach((a, m1) -> {
            assertTrue(A_SET.contains(a));
            ((Map<?,?>)m1).forEach((b, m2) -> {
                assertTrue(B_SET.contains(b));
                ((Map<?,?>)m2).forEach((c, v) -> {
                    assertTrue(C_SET.contains(c));
                    String value = "" + a + ":" + b + ":" + c;
                    assertEquals(v, value);
                });
            });
        });
    }
    
    @Test
    public void shouldReplaceTree() {
        Tree<String> changedTreeMap = TREE.replaceTree(s -> s + "-changed");
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
    }
    
    @Test
    public void shouldFlatToLevel0() {
        Map<?,?> flatMap0 = TREE.flatToLevel(0);
        flatMap0.forEach((a, m1) -> {
            ((Map<?,?>)m1).forEach((b, m2) -> {
                ((Map<?,?>)m2).forEach((c, v) -> {
                    String value = "" + ((List<?>)a).get(0) + ":" + b + ":" + c;
                    assertEquals(v, value);
                });
            });
        });
    }
        
    @Test
    public void shouldFlatToLevel1() {
        Map<?,?> flatMap1 = TREE.flatToLevel(1);
        flatMap1.forEach((keyList, m12) -> {
            ((Map<?,?>)m12).forEach((c, v) -> {
                List<?> kl = (List<?>) keyList;
                String value = "" + kl.get(0) + ":" + kl.get(1) + ":" + c;
                assertEquals(v, value);
            });
        });
    }
    
    @Test
    public void shouldGetLeavesMap() {
        Map<List<Object>,?> leavesMap = TREE.getLeavesMap();
        assertEquals(A_SET.size() * B_SET.size() * C_SET.size(), 
                leavesMap.size());
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
