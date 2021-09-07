package com.fillumina.collections;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableLinkedTableSetGTest extends GenericSetTest {

    @Override
    protected <T extends Comparable<T>> Set<T> create(Collection<T> collection) {
        return new ImmutableLinkedTableSet<>(collection);
    }

    @Override
    protected boolean isReadOnly() {
        return true;
    }

}
