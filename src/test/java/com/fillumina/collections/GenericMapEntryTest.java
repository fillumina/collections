package com.fillumina.collections;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import org.junit.jupiter.api.Test;

/**
 * Map entries <b>must be interchangeable</b> so they must have the same implementation for
 * equals() and hashCode().
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class GenericMapEntryTest {

    protected <K,V> Entry<K,V> create(K k, V v) {
        return new HashMap.SimpleEntry<>(k, v);
    }

    protected boolean isReadOnly() {
        return false;
    }

    @Test
    public void testGetKey() {
        assertEquals(1, create(1, "one").getKey());
        assertEquals("one", create("one", 1).getKey());
    }

    @Test
    public void testGetValue() {
        assertEquals("one", create(1, "one").getValue());
        assertEquals(1, create("one", 1).getValue());
    }

    @Test
    public void testSetValue() {
        if (!isReadOnly()) {
            Entry<Integer,String> entry = create(1, "one");
            assertEquals("one", entry.getValue());

            entry.setValue("other");
            assertEquals("other", entry.getValue());
        }
    }

    @Test
    public void testHashCode() {
        assertEquals(innerHashCode("one", 1), create("one", 1).hashCode());
        assertEquals(innerHashCode(1, "one"), create(1, "one").hashCode());
        assertEquals(create("one", 1).hashCode(), create(1, "one").hashCode());
    }

    @Test
    public void shouldHashCodeBeInvertibleBetweenKeyAndValue() {
        assertEquals(innerHashCode("one", 1), innerHashCode(1, "one"));
        assertEquals(innerHashCode("one", "two"), innerHashCode("two", "one"));
    }

    /**
     * this is the ONLY allowed algorithm.
     * @see HashMap.SimpleEntry#hashCode()
     */
    public static <K, V> int innerHashCode(K key, V value) {
        return Objects.hashCode(key) ^ Objects.hashCode(value);
    }

    @Test
    public void testEquals() {
        assertEquals(create(1, "one"), create(1, "one"));
        assertEquals(new HashMap.SimpleEntry<>(1, "one"), create(1, "one"));
        assertEquals(create("one", 1), create("one", 1));
        assertEquals(new HashMap.SimpleEntry<>("one", 1), create("one", 1));

        assertNotEquals(create(1, "one"), create("one", 1));
        assertNotEquals(new HashMap.SimpleEntry<>(1, "one"), create("one", 1));
    }

    @Test
    public void testToString() {
        assertEquals("1=one", create(1, "one").toString());
        assertEquals("one=1", create("one", 1).toString());
        assertEquals("alpha=beta", create("alpha", "beta").toString());
    }

}
