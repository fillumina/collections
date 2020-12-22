package com.fillumina.collections;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class KeyOnlyMapTest extends AbstractSetTest {

    @Override
    protected <T> Set<T> createSet() {
        return KeyOnlyMap.createSet();
    }

    @Override
    protected <T> Set<T> createSet(T... array) {
        return KeyOnlyMap.createSet(array);
    }

    @Override
    protected <T> Set<T> createSet(Collection<T> coll) {
        return KeyOnlyMap.createSet(coll);
    }
    
}
