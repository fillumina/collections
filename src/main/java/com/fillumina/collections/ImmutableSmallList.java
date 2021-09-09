package com.fillumina.collections;

import java.util.Collection;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableSmallList<T> extends SmallList<T> {

    public ImmutableSmallList() {
        super();
    }

    @SuppressWarnings("unchecked")
    public ImmutableSmallList(T... elements) {
        super(elements);
    }

    public ImmutableSmallList(Collection<? extends T> elements) {
        super(elements);
    }

    public ImmutableSmallList(SmallList<? extends T> smallSet) {
        super(smallSet);
    }

    @Override
    protected void readOnlyCheck() {
        throw new UnsupportedOperationException("read only");
    }

}
