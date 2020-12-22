package com.fillumina.collections;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SetWrapperTest extends AbstractSetTest {
    
    @Override
    protected <T> Set<T> createSet() {
        return new SetWrapper<>();
    }

    @Override
    protected <T> Set<T> createSet(T... array) {
        return new SetWrapper<>(array);
    }

    @Override
    protected <T> Set<T> createSet(Collection<T> coll) {
        return new SetWrapper<>(coll);
    }
    
}
