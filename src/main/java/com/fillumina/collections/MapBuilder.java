package com.fillumina.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A class useful to build maps that uses a temporary list to store entries.
 *
 * @param M the map to create
 * @param K the key
 * @param V the value
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MapBuilder<M extends Map<K, V>, K, V> {

    private final List<Object> list = new ArrayList<>();
    private final Function<List<Object>, M> creator;

    public MapBuilder(Function<List<Object>, M> creator) {
        this.creator = creator;
    }

    /** fluent method to add entries to the map. */
    public MapBuilder<M, K, V> put(K key, V value) {
        list.add(key);
        list.add(value);
        return this;
    }

    public M build() {
        return creator.apply(list);
    }

}
