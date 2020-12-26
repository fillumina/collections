package com.fillumina.collections;

import java.util.Map;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public interface InvertibleBiMap<K, V> extends Map<K, V> {

    InvertibleBiMap<V, K> inverse();
    
}
