package com.fillumina.collections;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
public class BiMapTest {

    @Test
    public void testImmutableStaticCreator() {
        BiMap<String, Integer> biMap = BiMap.<String, Integer>immutable(
                "one", 1, "two", 2, "three", 3);

        assertEquals(3, biMap.size());
        assertEquals(1, biMap.get("one"));
        assertEquals(2, biMap.get("two"));
        assertEquals(3, biMap.get("three"));

        assertEquals(3, biMap.inverse().size());
        assertEquals("one", biMap.inverse().get(1));
        assertEquals("two", biMap.inverse().get(2));
        assertEquals("three", biMap.inverse().get(3));
    }

    @Test
    public void testBuilder() {
        BiMap<String, Integer> biMap = BiMap.<String, Integer>builder()
                .put("one", 1)
                .put("two", 2)
                .put("three", 3)
                .build();

        assertEquals(3, biMap.size());
        assertEquals(1, biMap.get("one"));
        assertEquals(2, biMap.get("two"));
        assertEquals(3, biMap.get("three"));

        assertEquals(3, biMap.inverse().size());
        assertEquals("one", biMap.inverse().get(1));
        assertEquals("two", biMap.inverse().get(2));
        assertEquals("three", biMap.inverse().get(3));
    }

    @Test
    public void testImmutableBuilder() {
        BiMap<String, Integer> biMap = BiMap.<String, Integer>immutableBuilder()
                .put("one", 1)
                .put("two", 2)
                .put("three", 3)
                .build();

        assertThrows(UnsupportedOperationException.class, 
                () -> biMap.put("four", 4));

        assertThrows(UnsupportedOperationException.class, 
                () -> biMap.inverse().put(4, "four"));
        
        assertEquals(3, biMap.size());
        assertEquals(1, biMap.get("one"));
        assertEquals(2, biMap.get("two"));
        assertEquals(3, biMap.get("three"));

        assertEquals(3, biMap.inverse().size());
        assertEquals("one", biMap.inverse().get(1));
        assertEquals("two", biMap.inverse().get(2));
        assertEquals("three", biMap.inverse().get(3));
    }

    @Test
    public void shouldCloneArray() {
        int[] array = new int[]{1, 2, 3};
        int[] clone = array.clone();
        array[0] = 0;
        assertEquals(1, clone[0]);
    }

    @Test
    public void shouldGetAnImmutableView() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        BiMap<String, Integer> view = biMap.immutableView();

        biMap.put("one", 11);

        assertEquals(3, view.size());
        assertEquals(11, view.get("one")); // <-- the view changes too
        assertEquals(2, view.get("two"));
        assertEquals(3, view.get("three"));

        assertEquals(3, view.inverse().size());
        assertEquals("one", view.inverse().get(11)); // <-- the view changes too
        assertEquals("two", view.inverse().get(2));
        assertEquals("three", view.inverse().get(3));

        assertThrows(UnsupportedOperationException.class,
                () -> view.put("wrong", 666));

    }

    @Test
    public void shouldUseCopyConstructor() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        BiMap<String, Integer> copy = new BiMap<>(biMap);

        biMap.put("one", 11);

        assertEquals(3, copy.size());
        assertEquals(1, copy.get("one"));
        assertEquals(2, copy.get("two"));
        assertEquals(3, copy.get("three"));

        assertEquals(3, copy.inverse().size());
        assertEquals("one", copy.inverse().get(1));
        assertEquals("two", copy.inverse().get(2));
        assertEquals("three", copy.inverse().get(3));
    }

    @Test
    public void shouldClone() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        BiMap<String, Integer> clone = biMap.clone();

        biMap.put("one", 11);

        assertEquals(3, clone.size());
        assertEquals(1, clone.get("one"));
        assertEquals(2, clone.get("two"));
        assertEquals(3, clone.get("three"));

        assertEquals(3, clone.inverse().size());
        assertEquals("one", clone.inverse().get(1));
        assertEquals("two", clone.inverse().get(2));
        assertEquals("three", clone.inverse().get(3));
    }

    @Test
    public void shouldGetImmutableClone() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        BiMap<String, Integer> immutable = biMap.immutableClone();

        biMap.put("one", 11);

        assertEquals(3, immutable.size());
        assertEquals(1, immutable.get("one"));
        assertEquals(2, immutable.get("two"));
        assertEquals(3, immutable.get("three"));

        assertEquals(3, immutable.inverse().size());
        assertEquals("one", immutable.inverse().get(1));
        assertEquals("two", immutable.inverse().get(2));
        assertEquals("three", immutable.inverse().get(3));

        assertThrows(UnsupportedOperationException.class,
                () -> immutable.put("wrong", 666));
    }

    @Test
    public void shouldPutItems() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        assertEquals(3, biMap.size());
        assertEquals(1, biMap.get("one"));
        assertEquals(2, biMap.get("two"));
        assertEquals(3, biMap.get("three"));

        assertEquals(3, biMap.inverse().size());
        assertEquals("one", biMap.inverse().get(1));
        assertEquals("two", biMap.inverse().get(2));
        assertEquals("three", biMap.inverse().get(3));
    }

    @Test
    public void shouldReplaceRelation() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("one", 11);

        assertEquals(1, biMap.size());
        assertEquals(1, biMap.inverse().size());
        assertTrue(biMap.containsEntry("one", 11));
    }

    @Test
    public void shouldReplaceInverseRelation() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("other", 1);

        assertEquals(1, biMap.size());
        assertEquals(1, biMap.inverse().size());
        assertTrue(biMap.containsEntry("other", 1));
    }

    @Test
    public void shouldInversePutItems() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.inverse().put(1, "one");
        biMap.inverse().put(2, "two");
        biMap.inverse().put(3, "three");

        assertEquals(3, biMap.size());
        assertEquals(1, biMap.get("one"));
        assertEquals(2, biMap.get("two"));
        assertEquals(3, biMap.get("three"));

        assertEquals(3, biMap.inverse().size());
        assertEquals("one", biMap.inverse().get(1));
        assertEquals("two", biMap.inverse().get(2));
        assertEquals("three", biMap.inverse().get(3));
    }

    @Test
    public void shuouldChangeBothParts() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("A", 1);
        biMap.put("B", 1);
        biMap.put("A", 2);

        assertEquals(2, biMap.size());
        assertEquals(2, biMap.get("A"));
        assertEquals(1, biMap.get("B"));

        assertEquals(2, biMap.inverse().size());
        assertEquals("A", biMap.inverse().get(2));
        assertEquals("B", biMap.inverse().get(1));
    }

    public void shouldKeySetAndValuesBeingReciprocal() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        assertTrue(biMap.keySet().containsAll(
                biMap.inverse().values()));
        assertTrue(biMap.inverse().values().containsAll(
                biMap.keySet()));

        assertTrue(biMap.values().containsAll(
                biMap.inverse().keySet()));
        assertTrue(biMap.inverse().keySet().containsAll(
                biMap.values()));
    }

    @Test
    public void shouldRemoveKey() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("A", 1);
        biMap.put("B", 1);
        biMap.put("A", 2);

        biMap.remove("A");
        assertEquals(1, biMap.size());
        assertNull(biMap.get("A"));
        assertNull(biMap.inverse().get(2));
    }

    @Test
    public void shouldRemoveKeyInverse() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("A", 1);
        biMap.put("B", 1);
        biMap.put("A", 2);

        biMap.inverse().remove(2);
        assertEquals(1, biMap.size());
        assertNull(biMap.get("A"));
        assertNull(biMap.inverse().get(2));
    }

    @Test
    public void shouldClear() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("A", 1);
        biMap.put("B", 1);
        biMap.put("A", 2);

        biMap.clear();
        assertTrue(biMap.isEmpty());
        assertTrue(biMap.inverse().isEmpty());
    }

    @Test
    public void shouldClearInverse() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("A", 1);
        biMap.put("B", 1);
        biMap.put("A", 2);

        biMap.inverse().clear();
        assertTrue(biMap.isEmpty());
        assertTrue(biMap.inverse().isEmpty());
    }

    @Test
    public void shouldContainsValue() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        assertTrue(biMap.inverse().containsValue("one"));
        assertTrue(biMap.inverse().containsValue("two"));
        assertTrue(biMap.inverse().containsValue("three"));

        assertTrue(biMap.containsValue(1));
        assertTrue(biMap.containsValue(2));
        assertTrue(biMap.containsValue(3));
    }

    @Test
    public void shouldContainsValueInverse() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.inverse().put(1, "one");
        biMap.inverse().put(2, "two");
        biMap.inverse().put(3, "three");

        assertTrue(biMap.inverse().containsValue("one"));
        assertTrue(biMap.inverse().containsValue("two"));
        assertTrue(biMap.inverse().containsValue("three"));

        assertTrue(biMap.containsValue(1));
        assertTrue(biMap.containsValue(2));
        assertTrue(biMap.containsValue(3));
    }

    @Test
    public void shouldInvertInverted() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        assertEquals(1, biMap.inverse().inverse().get("one"));
    }

    @Test
    public void shouldRemoveFromValueCollection() {
        BiMap<String, Integer> biMap = new BiMap<>();
        biMap.put("one", 1);
        biMap.put("two", 2);
        biMap.put("three", 3);

        Set<Integer> values = biMap.values();
        values.remove(2);

        assertFalse(biMap.containsKey("two"));
    }

    @Test
    public void shouldInitWithExistingMap() {
        BiMap<String, Integer> biMap = new BiMap<>(
                Map.of("one", 1, "two", 2, "three", 3));

        assertEquals(3, biMap.size());
        assertEquals(1, biMap.get("one"));
        assertEquals(2, biMap.get("two"));
        assertEquals(3, biMap.get("three"));
    }

    @Test
    public void shouldRemoveTheInverseMappingIfExisting() {
        BiMap<String, Integer> biMap = new BiMap<>();

        biMap.put("one", 1);
        biMap.put("two", 1);

        assertEquals(1, biMap.size());
        assertEquals(1, biMap.inverse().size());

        assertEquals(1, biMap.get("two"));
        assertEquals("two", biMap.inverse().get(1));

    }

    @Test
    public void shouldInitWithExistingUncorrectMap() {
        BiMap<String, Integer> biMap = new BiMap<>(
                Map.of("one", 1, "two", 1, "three", 3));

        assertEquals(2, biMap.size());
        assertEquals(3, biMap.get("three"));
    }

    @Test
    public void shouldImmutableBeInitializedWithMap() {
        BiMap<String, Integer> biMap = BiMap.immutable(
                Map.of("one", 1, "two", 2, "three", 3));

        assertEquals(3, biMap.size());
        assertEquals("one", biMap.get(1));
        assertEquals("two", biMap.get(2));
        assertEquals("three", biMap.get(3));

        assertEquals(3, biMap.inverse().size());
        assertEquals(1, biMap.inverse().get("one"));
        assertEquals(2, biMap.inverse().get("two"));
        assertEquals(3, biMap.inverse().get("three"));
    }

    @Test
    public void shouldBeImmutable() {
        BiMap<String, Integer> biMap = BiMap.immutable(
                Map.of("one", 1, "two", 2, "three", 3));

        assertThrows(UnsupportedOperationException.class,
                () -> biMap.put("four", 4));

        assertThrows(UnsupportedOperationException.class,
                () -> biMap.inverse().put(4, "four"));

        assertThrows(UnsupportedOperationException.class,
                () -> biMap.remove("two"));

        assertThrows(UnsupportedOperationException.class,
                () -> biMap.inverse().remove(2));

        assertThrows(UnsupportedOperationException.class,
                () -> {
            final Iterator<Integer> it = biMap.values().iterator();
            it.next();
            it.remove();
        });

        assertThrows(UnsupportedOperationException.class,
                () -> {
            final Iterator<String> it = biMap.keySet().iterator();
            it.next();
            it.remove();
        });

        assertThrows(UnsupportedOperationException.class,
                () -> {
            final Iterator<String> it = biMap.inverse().values().iterator();
            it.next();
            it.remove();
        });

        assertThrows(UnsupportedOperationException.class,
                () -> {
            final Iterator<Integer> it = biMap.inverse().keySet().iterator();
            it.next();
            it.remove();
        });

    }
}
