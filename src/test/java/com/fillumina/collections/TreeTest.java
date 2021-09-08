/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.fillumina.collections;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class TreeTest {
    private static final String VALUE_ROOT = "root";
    private static final String VALUE_CHILD = "child";
    private static final String KEY_CHILD = "key_child";
    private static final String KEY_NEPHEW_2 = "key_nephew_2";
    private static final String KEY_NEPHEW_1 = "key_nephew_1";
    private static final String VALUE_NEPHEW_2 = "nephew_2";
    private static final String VALUE_NEPHEW_1 = "nephew_1";
    public static final String TRANSFORMED = "__TRANSFORMED";

    private static Tree<String> ROOT;
    private static Tree<String> CHILD;
    private static Tree<String> NEPHEW1;
    private static Tree<String> NEPHEW2;

    @BeforeAll
    public static void init() {
        NEPHEW1 = new Tree<>(null, null, VALUE_NEPHEW_1);
        NEPHEW2 = new Tree<>(null, null, VALUE_NEPHEW_2);

        Map<Object,Tree<String>> nephewMap = new HashMap<>();
        nephewMap.put(KEY_NEPHEW_1, NEPHEW1);
        nephewMap.put(KEY_NEPHEW_2, NEPHEW2);
        CHILD = new Tree<>(null, nephewMap, VALUE_CHILD);

        Map<Object,Tree<String>> childrenMap = new HashMap<>();
        childrenMap.put(KEY_CHILD, CHILD);
        ROOT = new Tree<>(null, childrenMap, VALUE_ROOT);

    }

    @Test
    public void testGetValue() {
        Tree<String> tree = new Tree<>(null, null, "value");
        assertEquals("value", tree.getValue());

        assertEquals(VALUE_CHILD, CHILD.getValue());
        assertEquals(VALUE_ROOT, ROOT.getValue());
        assertEquals(VALUE_NEPHEW_1, NEPHEW1.getValue());
        assertEquals(VALUE_NEPHEW_2, NEPHEW2.getValue());
    }

    @Test
    public void testIsRoot() {
        assertTrue(ROOT.isRoot());
        assertFalse(CHILD.isRoot());
        assertFalse(NEPHEW1.isRoot());
    }

    @Test
    public void testIsLeaf() {
        assertFalse(ROOT.isLeaf());
        assertFalse(CHILD.isLeaf());
        assertTrue(NEPHEW1.isLeaf());
    }

    @Test
    public void testGetRoot() {
        assertEquals(ROOT, CHILD.getRoot());
        assertEquals(ROOT, NEPHEW1.getRoot());
    }

    @Test
    public void testGetDepth() {
        assertEquals(0, ROOT.getDepth());
        assertEquals(1, CHILD.getDepth());
        assertEquals(2, NEPHEW1.getDepth());
    }

    @Test
    public void testGetParent() {
        assertEquals(ROOT, CHILD.getParent());
        assertEquals(CHILD, NEPHEW1.getParent());
    }

    @Test
    public void testGetKeyList() {
        List<Object> list = Arrays.asList("one", "two");
        Tree<String> tree = new Tree<>(list, null, "child");
        assertEquals(list, tree.getKeyList());
    }

    @Test
    public void testGetChildren() {
        assertEquals(Utils.mapOf(KEY_CHILD,CHILD),
                ROOT.getChildren());
        assertEquals(Utils.mapOf(KEY_NEPHEW_1, NEPHEW1, KEY_NEPHEW_2, NEPHEW2),
                CHILD.getChildren());
    }

    @Test
    public void testGet() {
        assertEquals(CHILD, ROOT.get(KEY_CHILD));
        assertEquals(NEPHEW1, CHILD.get(KEY_NEPHEW_1));
    }

    @Test
    public void testClone() {
        Tree<String> cloned = ROOT.clone();
        cloned.getChildren().clear();
        assertTrue(cloned.getChildren().isEmpty());
        assertFalse(ROOT.getChildren().isEmpty());
    }

    @Test
    public void testToMultiLevelMap_0args() {
        Map<?,?> rootMap = ROOT.toMultiLevelMap(v -> v + TRANSFORMED);
        Map<?,?> childMap = (Map<?,?>) rootMap.get(KEY_CHILD);
        assertEquals(VALUE_NEPHEW_1 + TRANSFORMED, childMap.get(KEY_NEPHEW_1));
        assertEquals(VALUE_NEPHEW_2 + TRANSFORMED, childMap.get(KEY_NEPHEW_2));
    }

    @Test
    public void testToMultiLevelMap_Function() {
        Map<?,?> rootMap = ROOT.toMultiLevelMap();
        Map<?,?> childMap = (Map<?,?>) rootMap.get(KEY_CHILD);
        assertEquals(VALUE_NEPHEW_1, childMap.get(KEY_NEPHEW_1));
        assertEquals(VALUE_NEPHEW_2, childMap.get(KEY_NEPHEW_2));
    }

    @Test
    public void testVisitValues() {
        Set<String> values = new HashSet<>();

        ROOT.visitValues(values::add);

        assertTrue(values.containsAll(
                Arrays.asList(VALUE_ROOT, VALUE_CHILD, VALUE_NEPHEW_1, VALUE_NEPHEW_2)));
    }

    @Test
    public void testReplaceTree() {
    }

    @Test
    public void testFlatToLevel() {
    }

    @Test
    public void testGetLeavesMap() {
    }

    @Test
    public void testVisitLeaves() {
    }

    @Test
    public void testPruneLeaves() {
    }

    @Test
    public void testPruneBranches() {
    }

}
