package com.fillumina.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Stores data in an non normalized way by indexing each value with multiple keys (position is
 * important). The data can then be queried by selecting the keys of interest in the right index.
 *
 * @param V the <i>value</i> type (keys are always objects)
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MultiMap<K,V>
        extends AbstractEntryMap<List<K>, V, Entry<List<K>, V>, MultiMap<K,V>> {

    // The List items are the indexes of key sets mapping to values:
    // each index have a set of keys each of which point to a set of values.
    //            index    key      set of values
    private final List<Map<K, Set<Entry<List<K>, V>>>> mapList = new ArrayList<>();

    private int keySize = -1;

    /** helper to be able to easily change internal set type */
    protected Set<Entry<List<K>, V>> createNewSet() {
        return new HashSet<>();
    }

    /** helper to be able to easily change internal set type */
    protected Set<Entry<List<K>, V>> createNewSet(Collection<Entry<List<K>, V>> coll) {
        return new HashSet<>(coll);
    }

    public MultiMap() {
        super();
    }

    public MultiMap(int initialSize) {
        super(initialSize);
    }

    @Override
    protected Entry<List<K>, V> createEntry(List<K> k, V v) {
        return new ImmutableMapEntry<>(k, v);
    }

    @Override
    protected MultiMap<K,V> createMap(int size) {
        return new MultiMap<>(size);
    }

    @Override
    public void clear() {
        super.clear();
        mapList.forEach(m -> m.clear());
        mapList.clear();
    }

    /**
     * Gets the set of values pointed by the key in the index.
     *
     * @param index
     * @param key
     * @return the set of values pointed by the given key in the given index
     */
    @SuppressWarnings("unchecked")
    public Set<V> getSetAtIndex(int index, K key) {
        Set<Entry<List<K>, V>> set = getEntrySetAtIndex(index, key);
        if (set == null) {
            return Collections.<V>emptySet();
        }
        return set.stream().map(e -> e.getValue()).collect(Collectors.toSet());
    }

    private Set<Entry<List<K>, V>> getEntrySetAtIndex(int index, Object key)
            throws IndexOutOfBoundsException {
        Map<K, Set<Entry<List<K>, V>>> map = getEntryMapAtIndex(index);
        return map == null ? null : map.get(key);
    }

    /**
     * Gets the map between keys and values in the index.
     *
     * @param index
     * @return the map of (key,value) from the given index
     */
    public Map<K, Set<V>> getMapAtIndex(int index) {
        Map<K, Set<Entry<List<K>, V>>> map = getEntryMapAtIndex(index);
        if (map == null) {
            return Collections.<K, Set<V>>emptyMap();
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream()
                                .map(n -> n.getValue()).collect(Collectors.toSet())));
    }

    private Map<K, Set<Entry<List<K>, V>>> getEntryMapAtIndex(int index) throws
            IndexOutOfBoundsException {
        if (mapList == null || mapList.isEmpty() || index >= mapList.size() || index < 0) {
            throw new IndexOutOfBoundsException("index= " + index + ", multiMap size= " +
                    ((mapList == null) ? 0 : mapList.size()));
        }
        final Map<K, Set<Entry<List<K>, V>>> map = mapList.get(index);
        return map;
    }

    /**
     * @param keys the keys ordered by index (order is important)
     * @return a value associated to all passed keys.
     */
    public V getAny(Collection<K> keys) {
        Set<V> set = getAll(keys);
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.iterator().next();
    }

    /**
     * @param keys the keys ordered by index (order is important)
     * @return the values associated to all the passed keys.
     */
    public Set<V> getAll(Collection<K> keys) {
        return getAll(keys.toArray());
    }

    /**
     * @param keys the keys ordered by index (order is important)
     * @return a value associated to all passed keys.
     */
    public V getAny(Object... keys) {
        Set<V> set = getAll(keys);
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.iterator().next();
    }

    /**
     * Get the value associated to the passed keys. A null key means all the values in that index.
     *
     * @param keys the keys ordered by index (order is important)
     * @return the values associated to all passed keys.
     */
    public Set<V> getAll(Object... keys) {
        Set<Entry<List<K>, V>> result = null;
        for (int i = 0, l = keys.length; i < l; i++) {
            Object k = keys[i];
            if (k != null) {
                Set<Entry<List<K>, V>> set = getEntrySetAtIndex(i, k);
                if (set != null) {
                    if (result == null) {
                        result = createNewSet(set);
                    } else {
                        result.retainAll(set);
                    }
                } else {
                    return null;
                }
            }
        }
        return result == null
                ? null
                : result.stream().map(s -> s.getValue()).collect(Collectors.toSet());
    }

    /**
     * Adds a value associated with keys. The position of the key represents the index. This method
     * is synchronized and can be used in parallel streams. Note that this class is not otherwise
     * thread safe.
     *
     * @param value the value to add
     * @param keys the keys ordered by index (order is important)
     * @return the old value
     */
    @SuppressWarnings("unchecked")
    public synchronized boolean addSynchronized(V value, K... keys) {
        return add(value, keys);
    }

    /**
     * Adds a value associated with keys. The position of the key represents the index.
     *
     * @param value the value to add
     * @param keys the keys ordered by index (order is important)
     * @return the old value
     */
    @SuppressWarnings("unchecked")
    public boolean add(V value, K... keys) {
        List<K> keylist = Arrays.asList(keys);
        if (keySize == -1) {
            keySize = keylist.size();
        } else if (keySize != keylist.size()) {
            throw new IllegalArgumentException(
                    "expected key of size " + keySize + ", was " + keylist.size());
        }
        Entry<List<K>, V> entry = createEntry(keylist, value);
        super.putEntry(entry);

        checkIndexesAndAddIfNeeded(keys.length);
        boolean added = false;
        for (int i = 0, l = keys.length; i < l; i++) {
            K k = keys[i];
            Set<Entry<List<K>, V>> set = mapList
                    .get(i)
                    .computeIfAbsent(k, key -> createNewSet());
            added |= set.add(entry);
        }
        return added;
    }

    @Override
    public V put(List<K> keys, V value) {
        @SuppressWarnings("unchecked")
        final K[] arrayKey = (K[]) keys.toArray();
        add(value, arrayKey);
        return null; // changes many associations each in a different way
    }

    /**
     * Checks if there are enough maps to contain all the indexes and if there aren't create the new
     * ones.
     *
     * @param size how many maps there should be
     */
    private void checkIndexesAndAddIfNeeded(int size) {
        if (size > mapList.size()) {
            synchronized (mapList) {
                // double check in the thread safe code
                int innerMissing = size - mapList.size();
                if (innerMissing > 0) {
                    for (int i = 0; i < innerMissing; i++) {
                        mapList.add(new ConcurrentHashMap<>());
                    }
                }
            }
        }
    }

    /**
     * NOT SUPPORTED
     */
    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     * NOT SUPPORTED
     */
    @Override
    protected void removeIndex(int idx) {
        throw new UnsupportedOperationException("not supported");
    }

    /**
     *
     * @param index
     * @return the set of keys in the given index
     * @throw IndexOutOfBoundsException
     */
    public Set<K> getKeySetAtIndex(int index) {
        return mapList.get(index).keySet();
    }

    /**
     * @return a tree where each level is assigned to the index in the given position.<br>
     * i.e. {@code createTreeFromIndexes(1, 0, 2)} uses the index 1 for the first level, 0 at the
     * second and 2 at the third.
     */
    public Tree<Object,V> createTreeFromIndexes(int... indexes) {
        Tree<Object,V> root = createTree(/*Collections.emptyList(),*/ null, indexes, 0, null);
        return root;
    }

    @SuppressWarnings("unchecked")
    private Tree<Object,V> createTree(
            Object key,
            int[] indexes,
            int pos,
            Set<Entry<List<K>, V>> selection) {

        final Set<Entry<List<K>, V>> currentSelection =
                createCurrentSelection(key, indexes, pos, selection);

        if (pos < indexes.length) {
            // not a leaf so creates child trees
            Set<K> keySet = getKeySetAtIndex(indexes[pos]);
            if (keySet.isEmpty()) {
                // no keys
                return Tree.<Object,V>emptyTree();

            } else {
                // creates children trees
                Tree<Object, V> tree = new Tree<>(key); // cannot use TreeMap
                final int indexPosition = pos + 1;
                keySet.stream().forEach(k -> {
                    final Tree<Object,V> t = createTree(
                            k, indexes, indexPosition, currentSelection);
                    synchronized (tree) {
                        tree.addTree(t);
                    }
                });
                return tree;
            }

        } else {
            // it's a leaf
            V value = currentSelection.iterator().next().getValue();
            if (currentSelection.size() > 1) {
                throw new IllegalArgumentException("wrong number of indexes");
            }
            return new Tree<>(key, value);
        }
    }

    private static <K> List<K> addKeyToKeys(List<K> keys, K key) {
        List<K> keyList;
        if (key != null) {
            if (keys == null) {
                keyList = Collections.singletonList(key);
            } else {
                keyList = addItemToList(keys, key);
            }
        } else {
            keyList = keys;
        }
        return keyList;
    }

    private static <K> List<K> addItemToList(List<K> list, K item) {
        List<K> l = new ArrayList<>(list.size() + 1);
        l.addAll(list);
        l.add(item);
        return l;
    }

    private Set<Entry<List<K>, V>> createCurrentSelection(
            Object key,
            int[] indexes,
            int pos,
            Set<Entry<List<K>, V>> selection) {

        final Set<Entry<List<K>, V>> currentSelection;
        if (key != null) {
            Set<Entry<List<K>, V>> keySelection = getEntrySetAtIndex(indexes[pos - 1], key);
            if (keySelection.isEmpty()) {
                currentSelection = createNewSet(selection);
            } else {
                // intersect the new keySelection with the previous one
                currentSelection = createNewSet(keySelection);
                if (selection != null && !selection.isEmpty()) {
                    currentSelection.retainAll(selection);
                }
            }
            if (currentSelection.isEmpty()) {
                return null;
            }
        } else {
            return null;
        }
        return currentSelection;
    }

}
