package com.fillumina.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class GenericListTest extends GenericCollectionTest {
    public static final int MASS_SIZE = 1000;

    /** Override */
    protected <T extends Comparable<T>> List<T> create(Collection<T> collection) {
        return new ArrayList<>(collection);
    }

    /** Override */
    protected boolean isReadOnly() {
        return false;
    }

    @SuppressWarnings("unchecked")
    private <T extends Comparable<T>> List<T> create(T... array) {
        final List<T> coll = create(Arrays.asList(array));
        return coll;
    }

    @Test
    public void shouldGetElements() {
        List<Integer> list = create(0, 1, 2, 3, 4);
        for (int i: list) {
            assertEquals(i, list.get(i));
        }
    }

    @Test
    public void shouldGetIndexOfElements() {
        List<Integer> list = create(0, 1, 2, 3, 4);
        for (int i: list) {
            assertEquals(i, list.indexOf(i));
        }
    }

    @Test
    public void shouldRemoveElementWithoutImpactingOrderFirst() {
        if (isReadOnly()) {
            return;
        }
        List<Integer> list = create(666, 0, 1, 2, 3, 4);
        list.remove(Integer.valueOf(666));
        for (int i: list) {
            assertEquals(i, list.indexOf(i));
        }
    }

    @Test
    public void shouldRemoveElementWithoutImpactingOrderMiddle() {
        if (isReadOnly()) {
            return;
        }
        List<Integer> list = create(0, 1, 2, 666, 3, 4);
        list.remove(Integer.valueOf(666));
        for (int i: list) {
            assertEquals(i, list.indexOf(i));
        }
    }

    @Test
    public void shouldRemoveElementWithoutImpactingOrderLast() {
        if (isReadOnly()) {
            return;
        }
        List<Integer> list = create(0, 1, 2, 3, 4, 666);
        list.remove(Integer.valueOf(666));
        for (int i: list) {
            assertEquals(i, list.indexOf(i));
        }
    }

}
