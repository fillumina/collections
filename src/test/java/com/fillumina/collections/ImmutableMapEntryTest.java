package com.fillumina.collections;

import java.util.Map;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class ImmutableMapEntryTest extends GenericMapEntryTest {

    @Override
    protected <K, V> Map.Entry<K, V> create(K k, V v) {
        return new ImmutableMapEntry<>(k, v);
    }

    @Override
    protected boolean isReadOnly() {
        return true;
    }

}
