package com.fillumina.collections;

import java.util.Map;
import java.util.Set;

/**
 * A BiMap is a map where value unicity is enforced as well as key's. If an existing value is
 * inserted with a new key the old mapping is removed. The BiMap follows the
 * {@link Map} specification and its keys and values pairs can be inverted by using
 * {@link #inverseMap()}.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class BiMap<K, V> extends TableMap<K, V> {

    public static BiMap<?, ?> EMPTY_MAP = immutable();
    private static final Object[] EMPTY_ARRAY = new Object[0];

    public static <K, V> BiMap<K, V> empty() {
        return (BiMap<K, V>) EMPTY_MAP;
    }

    public static <K, V> BiMap<K, V> immutable(Map<K, V> map) {
        return new BiMap<>(true, map);
    }

    public static <K, V> BiMap<K, V> immutable(Object... array) {
        return new BiMap<>(true, array);
    }

    public static <K, V> MapBuilder<BiMap<K,V>, K,V> builder() {
        return new MapBuilder<>(l -> new BiMap(false, l.toArray()));
    }

    public static <K, V> MapBuilder<BiMap<K,V>, K,V> immutableBuilder() {
        return new MapBuilder<>(l -> new BiMap(true, l.toArray()));
    }

    private final BiMap<V, K> inverseMap;
    private final boolean immutable;

    /** Empty constructor. */
    public BiMap() {
        super();
        this.inverseMap = new BiMap(this, false);
        this.immutable = false;
    }

    private BiMap(BiMap<V, K> inverseMap, boolean immutable) {
        this.inverseMap = inverseMap;
        this.immutable = immutable;
    }

    /** Usual copy constructor from other kind of {@link java.util.Map}. */
    public BiMap(Map<? extends K, ? extends V> map) {
        this();
        map.forEach((k, v) -> put(k, v));
    }

    public BiMap(BiMap<K, V> copy) {
        this(copy, null, false);
    }

    /** @param prepares the map to the expected size (avoid expensive resizing operation). */
    public BiMap(int initialSize) {
        super(initialSize);
        this.inverseMap = new BiMap(this, false, initialSize, EMPTY_ARRAY);
        this.immutable = false;
    }

    /** Creates an immutableClone map (used by static creators). */
    private BiMap(boolean immutable, Map<K, V> map) {
        super(map.size());
        this.inverseMap = new BiMap(this, immutable, map.size(), map);
        this.immutable = immutable;
    }

    private BiMap(boolean immutable, Object... array) {
        super(array.length);
        this.inverseMap = new BiMap(this, immutable, array.length, array);
        this.immutable = immutable;
    }

    private BiMap(BiMap<V, K> inverseMap, boolean immutable, int initialSize, Map<K, V> map) {
        super(initialSize);
        this.inverseMap = inverseMap;
        if (map != null) {
            map.forEach((k, v) -> put(k, v));
        }
        this.immutable = immutable;
    }

    private BiMap(BiMap<V, K> inverseMap, boolean immutable, int initialSize, Object... array) {
        super(initialSize);
        this.inverseMap = inverseMap;
        if (array.length > 0) {
            for (int i = 0; i < array.length; i += 2) {
                put((K) array[i + 1], (V) array[i]);
            }
        }
        this.immutable = immutable;
    }

    /** Clone */
    private BiMap(BiMap<K,V> copy, BiMap<V,K> inverse, boolean immutable) {
        super();
        if (inverse != null) {
            this.inverseMap = inverse;
            copy.forEach((k,v) -> noCheckInnerPut(k, v));
        } else {
            this.inverseMap = new BiMap(copy.inverseMap, this, immutable);
        }
        this.immutable = immutable;
    }

    /** View */
    private BiMap(BiMap<K,V> copy, BiMap<V,K> inverse) {
        super(copy.getInternalState());
        this.immutable = true;
        this.inverseMap = (inverse != null) ?
                inverse :
                new BiMap(copy.inverseMap, this);
    }

    public BiMap<V, K> inverse() {
        return inverseMap;
    }

    public BiMap<K, V> immutableClone() {
        return new BiMap<>(this, null, true);
    }

    public BiMap<K, V> immutableView() {
        if (immutable) {
            return this;
        }
        return new BiMap<>(this, null);
    }

    /** It's a clone of the original biMap. */
    @Override
    public BiMap<K, V> clone() {
        return new BiMap<>(this, null, false);
    }

    @Override
    protected void readOnlyCheck() {
        if (immutable) {
            throw new UnsupportedOperationException("read only instance");
        }
    }

    @Override
    protected V innerPut(K key, V value) {
        readOnlyCheck();
        return noCheckInnerPut(key, value);
    }

    private V noCheckInnerPut(K key, V value) {
        K prevKey = inverseMap.inverseInnerPut(value, key);
        if (prevKey != null) {
            innerRemove(prevKey);
        }
        V prevValue = super.innerPut(key, value);
        if (prevValue != null) {
            inverseMap.innerRemove(prevValue);
        }
        return prevValue;
    }

    private V inverseInnerPut(K key, V value) {
        return super.innerPut(key, value);
    }

    @Override
    public boolean containsValue(Object value) {
        return inverseMap.containsKey(value);
    }

    @Override
    public Set<V> values() {
        return inverseMap.keySet();
    }

    @Override
    public V remove(Object key) {
        V value = super.remove(key);
        inverseMap.innerRemove(value);
        return value;
    }

    private V innerRemove(Object key) {
        return super.remove(key);
    }

    @Override
    public void clear() {
        super.clear();
        inverseMap.innerClear();
    }

    private void innerClear() {
        super.clear();
    }
}
