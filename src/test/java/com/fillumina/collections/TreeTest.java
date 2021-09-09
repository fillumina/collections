/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.fillumina.collections;

import java.util.Arrays;
import java.util.HashSet;
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

    private static Tree<String,String> ROOT;
    private static Tree<String,String> CHILD;
    private static Tree<String,String> NEPHEW1;
    private static Tree<String,String> NEPHEW2;

    @BeforeAll
    public static void init() {
        NEPHEW1 = new Tree<>(KEY_NEPHEW_1,VALUE_NEPHEW_1);
        NEPHEW2 = new Tree<>(KEY_NEPHEW_2,VALUE_NEPHEW_2);

        CHILD = new Tree<>(KEY_CHILD, VALUE_CHILD);
        CHILD.put(KEY_NEPHEW_1, NEPHEW1);
        CHILD.put(KEY_NEPHEW_2, NEPHEW2);

        ROOT = new Tree<>(null, VALUE_ROOT);
        ROOT.putEntry(CHILD);

        assertEquals(ROOT, CHILD.getParent());
    }

    @Test
    public void testGetLeafValue() {
        Tree<String,String> tree = new Tree<>("key", "value");
        assertEquals("value", tree.getNodeValue());

        assertEquals(VALUE_CHILD, CHILD.getNodeValue());
        assertEquals(VALUE_ROOT, ROOT.getNodeValue());
        assertEquals(VALUE_NEPHEW_1, NEPHEW1.getNodeValue());
        assertEquals(VALUE_NEPHEW_2, NEPHEW2.getNodeValue());
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
    public void testGetChildren() {
        assertEquals(Utils.mapOf(KEY_CHILD,CHILD),
                ROOT);
        assertEquals(Utils.mapOf(KEY_NEPHEW_1, NEPHEW1, KEY_NEPHEW_2, NEPHEW2),
                CHILD);
    }

    @Test
    public void testGet() {
        assertEquals(CHILD, ROOT.get(Arrays.asList(KEY_CHILD)));
        assertEquals(NEPHEW1, CHILD.get(Arrays.asList(KEY_NEPHEW_1)));
    }

    @Test
    public void testGetSingleValue() {
        assertEquals(CHILD, ROOT.get(KEY_CHILD));
        assertEquals(NEPHEW1, CHILD.get(KEY_NEPHEW_1));
    }

    @Test
    public void testClone() {
        Tree<String,String> cloned = ROOT.clone();
        cloned.clear();
        assertTrue(cloned.isEmpty());
        assertFalse(ROOT.isEmpty());
    }

    @Test
    public void testVisitValues() {
        Set<String> values = new HashSet<>();

        ROOT.visitValues(values::add);

        assertTrue(values.containsAll(
                Arrays.asList(VALUE_ROOT, VALUE_CHILD, VALUE_NEPHEW_1, VALUE_NEPHEW_2)));
    }

//    @Test
//    public void testReplaceTree() {
//    }
//
//    @Test
//    public void testFlatToLevel() {
//    }
//
//    @Test
//    public void testGetLeavesMap() {
//    }
//
//    @Test
//    public void testVisitLeaves() {
//    }
//
//    @Test
//    public void testPruneLeaves() {
//    }
//
//    @Test
//    public void testPruneBranches() {
//    }

}
