package com.fillumina.collections;

import com.fillumina.collections.MultiMap.Tree;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MultiMapTest {

    @Test
    public void usageExample() {
        MultiMap<Double> mmap = new MultiMap<>();

        //       value      key_1          key_2
        //       time (s)   runner name    race type
        mmap.add(55.3, "Maria Stella", "400 mt");
        mmap.add(52.9, "Gisella Masi", "400 mt");

        mmap.add(23.3, "Maria Stella", "200 mt");
        mmap.add(21.9, "Gisella Masi", "200 mt");

        mmap.add(12.3, "Maria Stella", "100 mt");
        mmap.add(10.9, "Gisella Masi", "100 mt");

        //                                             key_1=*   key_2='100 mt'
        final Set<Double> avg100mtValuesSet = mmap.getAll(null, "100 mt");
        double avg100mt = avg100mtValuesSet.stream().mapToDouble(d -> d).average().getAsDouble();
        assertEquals((12.3 + 10.9)/2, avg100mt);

        // uses the canonical order to create the tree: first runner, then race
        Tree<Double> byRunner = mmap.createTreeFromIndexes(0, 1);
        final Tree<Double> mariaTree = byRunner.get("Maria Stella");
        Map<String,Double> runnerTimings =  (Map<String,Double>) mariaTree.toMap();
        assertEquals(3, runnerTimings.size());
        assertEquals(12.3, runnerTimings.get("100 mt"));
        assertEquals(23.3, runnerTimings.get("200 mt"));
        assertEquals(55.3, runnerTimings.get("400 mt"));

        // uses the reverse order to create the tree: first race, then runner
        Tree<Double> byRace = mmap.createTreeFromIndexes(1, 0);

        assertEquals(new HashSet<>(Arrays.asList("100 mt", "200 mt", "400 mt")),
                byRace.getChildren().keySet());

        final Tree<Double> race200mtTree = byRace.get("200 mt");
        Map<String,Double> raceTimings = (Map<String,Double>) race200mtTree.toMap();
        assertEquals(2, raceTimings.size());
        assertEquals(21.9, raceTimings.get("Gisella Masi"));
        assertEquals(23.3, raceTimings.get("Maria Stella"));
    }



    private static final Set<String> A_SET = Utils.setOf("IT", "FR", "ES");
    private static final Set<Integer> B_SET = Utils.setOf(1, 2, 3);
    private static final Set<Character> C_SET = Utils.setOf('a', 'b');

    private static MultiMap<String> MMAP;
    private static Tree<String> TREE;

    public static void main(String[] args) {
        init();
        System.out.println(MMAP.toString());
    }

    @BeforeAll
    public static void init() {
        MMAP = new MultiMap<>();

        // loads a tree
        for (String a : A_SET) {
            for (Integer b : B_SET) {
                for (Character c : C_SET) {
                    // each branch of this tree structure has its own leaf value
                    String value = "" + a + ":" + b + ":" + c;
                    MMAP.add(value, a, b, c);
                }
            }
        }

        // creates a tree with the same structure order of the inserted one
        TREE = MMAP.createTreeFromIndexes(0, 1, 2);
    }

    @Test
    public void shouldAddValues() {
        MultiMap<Integer> mmap = new MultiMap<>();

        mmap.add(1, "alpha", 12);
        mmap.add(2, "alpha", 13);

        Set<Integer> alphaSet = mmap.getAll("alpha");

        assertEquals(2, alphaSet.size());
        assertTrue(alphaSet.contains(1));
        assertTrue(alphaSet.contains(2));

        Set<Integer> alphaNumberSet = mmap.getAll("alpha", 12);

        assertEquals(1, alphaNumberSet.size());
        assertTrue(alphaNumberSet.contains(1));

        Set<Integer> values = mmap.getAll(null, 13);
        assertEquals(Utils.setOf(2), values);
    }

    @Test
    public void shouldCreateTree() {
        checkTree(TREE, 0, 1, 2);
    }

    @Test
    public void shouldCreateTreeWithDifferentIndexOrder() {
        Tree<String> otherTree = MMAP.createTreeFromIndexes(1, 2, 0);
        checkTree(otherTree, 1, 2, 0);
    }

    @Test
    public void shouldCreateTreeWithAnotherDifferentIndexOrder() {
        Tree<String> otherTree = MMAP.createTreeFromIndexes(2, 0, 1);
        checkTree(otherTree, 2, 0, 1);
    }

    @Test
    public void shouldCloneTree() {
        final Tree<String> clone = TREE.clone();
        assertFalse(clone == TREE);
        checkTree(clone, 0, 1, 2);
    }

    public void checkTree(Tree<String> tree, int...indexes) {
        List<Set<?>> orderedSets = Arrays.asList(A_SET, B_SET, C_SET);
        List<Set<?>> sets = Arrays.asList(
                orderedSets.get(indexes[0]),
                orderedSets.get(indexes[1]),
                orderedSets.get(indexes[2]));
        Map<Integer,Integer> idxMap = new HashMap<>(indexes.length);
        for (int i=0; i<indexes.length; i++) {
            idxMap.put(indexes[i], i);
        }

        assertTrue(tree.isRoot());
        assertEquals(sets.get(0),
                tree.getChildren().keySet());

        tree.getChildren().forEach((x, t1) -> {
            assertEquals(sets.get(1),
                    t1.getChildren().keySet());
            assertFalse(t1.isLeaf());
            assertFalse(t1.isRoot());
            assertEquals(tree, t1.getParent());
            assertEquals(tree, t1.getRoot());

            t1.getChildren().forEach((y, t2) -> {
                assertFalse(t2.isLeaf());
                assertFalse(t2.isRoot());
                assertEquals(t1, t2.getParent());
                assertEquals(tree, t2.getRoot());
                assertEquals(sets.get(2),
                        t2.getChildren().keySet());

                t2.getChildren().forEach((z, t3) -> {
                    assertTrue(t3.isLeaf());
                    assertFalse(t3.isRoot());
                    assertEquals(t2, t3.getParent());
                    assertEquals(tree, t3.getRoot());

                    List<?> l = Arrays.asList(x, y, z);
                    String value = "" + l.get(idxMap.get(0)) +
                            ":" + l.get(idxMap.get(1)) +
                            ":" + l.get(idxMap.get(2));

                    assertEquals(value, t3.getValue());

                    List<Object> keyList = t3.getKeyList();
                    assertEquals(x, keyList.get(0));
                    assertEquals(y, keyList.get(1));
                    assertEquals(z, keyList.get(2));

                    String keyValue = "" + x + ":" + y + ":" + z;
                    assertEquals(keyValue,
                            t3.getKeyListAsString(":"));
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
                leavesMap.get(Arrays.asList("FR", 3, 'a')));
    }

    @Test
    public void shouldSelectFromEqualElements() {
        MultiMap<String> mmap = new MultiMap<>();

        mmap.add("one", 'a', 1);
        mmap.add("one", 'a', 2);
        mmap.add("one", 'b', 1);
        mmap.add("one", 'b', 2);

        assertEquals(4, mmap.size());

        assertEquals(Utils.setOf("one"), mmap.getAll('a', 1));
        assertEquals(Utils.setOf("one"), mmap.getAll('a', 2));
        assertEquals(Utils.setOf("one"), mmap.getAll('b', 1));
        assertEquals(Utils.setOf("one"), mmap.getAll('b', 2));
    }

    @Test
    public void shouldGetFromPartialKey() {
        MultiMap<String> mmap = new MultiMap<>();

        mmap.add("one", 'a', 1);
        mmap.add("two", 'a', 2);
        mmap.add("three", 'b', 1);
        mmap.add("four", 'b', 2);

        assertEquals(4, mmap.size());

        assertEquals(Utils.setOf("one", "two"), mmap.getAll('a'));
        assertEquals(Utils.setOf("three", "four"), mmap.getAll('b'));

        // use null as placeholder (key position is important)
        assertEquals(Utils.setOf("one", "three"), mmap.getAll(null, 1));
        assertEquals(Utils.setOf("two", "four"), mmap.getAll(null, 2));
    }

    @Test
    public void shoulRetainSet() {
        Set<Integer> a = Utils.setOf(1, 2, 3);
        Set<Integer> b = Utils.setOf(2, 3, 4);
        Set<Integer> result = new HashSet<>(a);
        result.retainAll(b);
        assertEquals(Utils.setOf(2, 3), result);
    }

    @Test
    public void shouldDifferentiate() {
        MultiMap<String> mmap = new MultiMap<>();

        mmap.add("one", 'a', 1);
        mmap.add("one", 'a', 2);
        mmap.add("one", 'b', 3);

        assertEquals(3, mmap.size());
        assertEquals(1, new HashSet<>(mmap.values()).size());

        assertEquals(mmap.getAll('a', 2), mmap.getAll('a', 1));

        assertEquals(Utils.setOf("one"), mmap.getAll('a', 1));
        assertEquals(Utils.setOf("one"), mmap.getAll('a', 2));

        assertTrue(mmap.getAll('b', 1).isEmpty());
        assertTrue(mmap.getAll('a', 3).isEmpty());
    }

    @Test
    public void shouldConsiderKeyOrder() {
        MultiMap<String> mmap = new MultiMap<>();

        mmap.add("one", 'a', 1);
        mmap.add("two", 1, 'a');

        assertEquals(2, mmap.size());

        assertEquals(Utils.setOf("one"), mmap.getAll('a', 1));
        assertEquals(Utils.setOf("two"), mmap.getAll(1, 'a'));
    }

    @Test
    public void shouldReturnOnlySavedValues() {
        MultiMap<String> mmap = new MultiMap<>();

        mmap.add("one", 'a', 1);
        mmap.add("two", 'a', 2);

        assertEquals(2, mmap.size());

        assertEquals(Utils.setOf("one"), mmap.getAll('a', 1));
        assertEquals(Utils.setOf("two"), mmap.getAll('a', 2));

        assertEquals(Utils.setOf("one", "two"), mmap.getAll('a'));

        assertNull(mmap.getAll('a', 3));
        assertNull(mmap.getAll('b', 1));
    }

    @Test
    public void shouldGetAny() {
        MultiMap<String> mmap = new MultiMap<>();
        mmap.add("alpha", 0, 1, 2);
        mmap.add("beta", 0, 1, 3);
        mmap.add("gamma", 0, 4, 3);

        assertEquals("alpha", mmap.getAny(0, 1, 2));
        assertEquals("beta", mmap.getAny(0, 1, 3));
        assertEquals("gamma", mmap.getAny(0, 4, 3));
    }

    @Test
    public void shouldGetAll() {
        MultiMap<String> mmap = new MultiMap<>();
        mmap.add("alpha", 0, 1, 2);
        mmap.add("beta", 0, 1, 3);
        mmap.add("gamma", 0, 4, 3);

        assertContains(mmap.getAll(0, null, null),
                "alpha", "beta", "gamma");

        assertContains(mmap.getAll(null, 1, null),
                "alpha", "beta");

        assertContains(mmap.getAll(0, 1, null),
                "alpha", "beta");

        assertContains(mmap.getAll(0, 1, 3),
                "beta");

        assertContains(mmap.getAll(null, null, 3),
                "beta", "gamma");

        assertContains(mmap.getAll(null, 4, null),
                "gamma");
    }

    @Test
    public void shouldGetMapAtIndex() {
        MultiMap<String> mmap = new MultiMap<>();
        mmap.add("alpha", 0, 1, 2);
        mmap.add("beta", 1, 1, 3);
        mmap.add("gamma", 0, 4, 3);

        Map<Object, Set<String>> map0 = mmap.getMapAtIndex(0);
        assertContains(map0.get(0), "alpha", "gamma");
        assertContains(map0.get(1), "beta");
        assertNull(map0.get(2));
        assertNull(map0.get(3));
        assertNull(map0.get(4));

        Map<Object, Set<String>> map1 = mmap.getMapAtIndex(1);
        assertContains(map1.get(1), "alpha", "beta");
        assertContains(map1.get(4), "gamma");
        assertNull(map1.get(2));
        assertNull(map1.get(3));
        assertNull(map1.get(0));

        Map<Object, Set<String>> map2 = mmap.getMapAtIndex(2);
        assertContains(map2.get(3), "gamma", "beta");
        assertContains(map2.get(2), "alpha");
        assertNull(map2.get(0));
        assertNull(map2.get(1));
        assertNull(map2.get(4));
    }

    @Test
    public void shouldGetSetAtIndex() {
        MultiMap<String> mmap = new MultiMap<>();
        mmap.add("alpha", 0, 1, 2);
        mmap.add("beta", 1, 1, 3);
        mmap.add("gamma", 0, 4, 3);

        assertContains(mmap.getSetAtIndex(0, 0), "alpha", "gamma");
        assertContains(mmap.getSetAtIndex(0, 1), "beta");

        assertContains(mmap.getSetAtIndex(1, 1), "alpha", "beta");
        assertContains(mmap.getSetAtIndex(1, 4), "gamma");

        assertContains(mmap.getSetAtIndex(2, 3), "gamma", "beta");
        assertContains(mmap.getSetAtIndex(2, 2), "alpha");
    }

    @Test
    public void shouldGet() {
        MultiMap<String> mmap = new MultiMap<>();
        mmap.add("alpha", 0, 1, 2);
        mmap.add("beta", 0, 1, 3);
        mmap.add("gamma", 0, 4, 3);

        assertEquals("alpha", mmap.get(Arrays.asList(0, 1, 2)));
        assertEquals("beta", mmap.get(Arrays.asList(0, 1, 3)));
        assertEquals("gamma", mmap.get(Arrays.asList(0, 4, 3)));
    }

    @Test
    public void shouldClear() {
        MultiMap<String> mmap = new MultiMap<>();
        mmap.add("alpha", 0, 1, 2);
        mmap.add("beta", 0, 1, 3);
        mmap.add("gamma", 0, 4, 3);

        assertEquals(3, mmap.size());
        assertFalse(mmap.isEmpty());

        mmap.clear();

        assertEquals(0, mmap.size());
        assertTrue(mmap.isEmpty());
    }

    @Test
    public void shouldGetKeysAtIndex() {
        MultiMap<String> mmap = new MultiMap<>();
        mmap.add("alpha", 0, 1, 2);
        mmap.add("beta", 0, 1, 3);
        mmap.add("gamma", 0, 4, 3);

        assertContains(mmap.getKeySetAtIndex(0), 0);
        assertContains(mmap.getKeySetAtIndex(1), 1, 4);
        assertContains(mmap.getKeySetAtIndex(2), 2, 3);
    }

    private <T> void assertContains(Set<T> set, T... values) {
        List<T> valueList = Arrays.asList(values);
        assertEquals(set.size(), valueList.size());
        assertTrue(set.containsAll(valueList));
        assertTrue(valueList.containsAll(set));
    }
}
