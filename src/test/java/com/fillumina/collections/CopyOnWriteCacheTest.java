package com.fillumina.collections;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class CopyOnWriteCacheTest extends GenericMapTest {

    /** Override */
    @SuppressWarnings("unchecked")
    protected <K extends Comparable<K>,V extends Comparable<V>> Map<K,V> create(Map<K,V> m) {
        return new CopyOnWriteCache<>(m);
    }

    /** Override */
    protected boolean isReadOnly() {
        return true;
    }

    @Test
    public void shouldRemoveLeastInsertedElement() {
        CopyOnWriteCache<Integer,String> map = new CopyOnWriteCache<>(2);

        map.put(1, "1");
        map.put(2, "2");
        map.put(3, "3");
        map.put(4, "4");

        // 4,3,2
        printOrderList(map);

        assertFalse(map.containsKey(1));

        assertTrue(map.containsKey(2));
        assertTrue(map.containsKey(3));
        assertTrue(map.containsKey(4));

        map.put(5, "5");

        // 5,4,3
        printOrderList(map);

        assertFalse(map.containsKey(2));

        assertTrue(map.containsKey(3));
        assertTrue(map.containsKey(4));
        assertTrue(map.containsKey(5));
    }

    @Test
    public void shouldRemoveLeastUsedElement() {
        CopyOnWriteCache<Integer,String> map = new CopyOnWriteCache<>(2);

        map.put(1, "1");
        map.put(2, "2");
        map.put(3, "3");

        // 3,2,1
        printOrderList(map);
        assertEquals("1", map.get(1));


        // 1,3,2
        printOrderList(map);
        assertEquals("2", map.get(2));

        // 2,1,3
        printOrderList(map);
        map.put(4, "4");


        // 4,2,1
        printOrderList(map);

        assertFalse(map.containsKey(3));

        assertTrue(map.containsKey(4));
        assertTrue(map.containsKey(2));
        assertTrue(map.containsKey(1));
    }

    private void printOrderList(CopyOnWriteCache<Integer, String> map) {
        final List<CopyOnWriteCache.LinkedEntry<Integer, String>> list = map.getOrderedEntryList();
        if (false) {
            System.out.println(
                    "h=" + map.getInternalState().head +
                    ", t=" + map.getInternalState().tail +
                    " list=" + list.toString());
        }
    }
}
