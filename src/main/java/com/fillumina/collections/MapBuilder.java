package com.fillumina.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MapBuilder<M extends Map<K, V>, K, V> {
    
    private final List<Object> list = new ArrayList<>();
    private final Function<List<Object>, M> creator;

    public MapBuilder(Function<List<Object>, M> creator) {
        this.creator = creator;
    }

    public MapBuilder<M, K, V> put(K key, V value) {
        list.add(key);
        list.add(value);
        return this;
    }

    public M build() {
        return creator.apply(list);
    }
    
}
