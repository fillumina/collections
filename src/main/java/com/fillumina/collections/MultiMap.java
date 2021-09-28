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
 * @param K the <i>key</i> type
 * @param V the <i>value</i> type
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MultiMap<K,V>
        extends AbstractEntryMap<List<K>, V, Entry<List<K>, V>, MultiMap<K,V>> {

    // The List items are the indexes of key sets mapping to valueSet:
    // each index have a set of keys each of which point to a set of valueSet.
    // The entry is needed because in the selection it is important to know which keys
    // points to a specific value.
    //            index    key      set of valueSet
    private final List<Map<K, Set<Entry<List<K>, V>>>> mapList;

    public MultiMap() {
        super();
        this.mapList = new ArrayList<>();
    }

    public MultiMap(int initialSize) {
        super(initialSize);
        this.mapList = new ArrayList<>(initialSize);
    }

    @Override
    protected Entry<List<K>, V> createEntry(List<K> k, V v) {
        return new ImmutableMapEntry<>(k, v);
    }

    @Override
    protected MultiMap<K,V> createMap(int size) {
        return new MultiMap<>(size);
    }

    /** helper to be able to easily change internal set type */
    protected Set<Entry<List<K>, V>> createNewSet() {
        return new HashSet<>();
    }

    /** helper to be able to easily change internal set type */
    protected Set<Entry<List<K>, V>> createNewSet(Collection<Entry<List<K>, V>> coll) {
        return new HashSet<>(coll);
    }

    @Override
    public void clear() {
        super.clear();
        mapList.forEach(m -> m.clear());
        mapList.clear();
    }

    /**
     * Gets the set of valueSet pointed by the key in the index.
     *
     * @param index the position of the index
     * @param key   the requested key
     * @return the set of valueSet pointed by the given key in the given index or null
     */
    @SuppressWarnings("unchecked")
    public Set<V> setAtIndex(int index, K key) {
        Set<Entry<List<K>, V>> set = getEntrySetAtIndex(index, key);
        if (set == null) {
            return Collections.<V>emptySet();
        }
        return set.stream().map(e -> e.getValue()).collect(Collectors.toSet());
    }

    private Set<Entry<List<K>, V>> getEntrySetAtIndex(int index, Object key) {
        try {
            Map<K, Set<Entry<List<K>, V>>> map = mapList.get(index);
            return map == null ? null : map.get(key);
        } catch (IndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Gets the map between keys and valueSet in the index.
     *
     * @param index
     * @return the map of (key,value) from the given index
     */
    public Map<K, Set<V>> mapAtIndex(int index) {
        Map<K, Set<Entry<List<K>, V>>> map = mapList.get(index);
        if (map == null) {
            return Collections.<K, Set<V>>emptyMap();
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream()
                                .map(s -> s.getValue()).collect(Collectors.toSet())));
    }

    /** @return a new independent set (not a view!) of all the entries in the multi map. */
    public Set<Entry<List<K>,V>> entrySet() {
        return mapList.stream()
                .flatMap(m -> m.values().stream())
                .flatMap(s -> s.stream())
                .collect(Collectors.toSet());
    }

    /** @return a new independent set (not a view!) of all the values in the multi map. */
    public Set<V> valueSet() {
        return mapList.stream()
                .flatMap(m -> m.values().stream())
                .flatMap(s -> s.stream())
                .map(e -> e.getValue())
                .collect(Collectors.toSet());
    }

    /**
     * @param keys the keys ordered by index (order is important)
     * @return a value associated to all passed keys.
     */
    public V getAny(List<K> keys) {
        Set<V> set = getAll(keys);
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.iterator().next();
    }

    /**
     * @param keys the keys ordered by index (order is important)
     * @return the valueSet associated to all the passed keys.
     */
    public Set<V> getAll(List<K> keys) {
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
     * @return the valueSet associated to all passed keys.
     */
    public Set<V> getAll(Object... keys) {
        Set<Entry<List<K>, V>> result = null;
        for (int index = 0, l = keys.length; index < l; index++) {
            Object key = keys[index];
            if (key != null) {
                Set<Entry<List<K>, V>> set = getEntrySetAtIndex(index, key);
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
        return result == null ? null
                : result.stream().map(s -> s.getValue()).collect(Collectors.toSet());
    }

    @Override
    public V put(List<K> keys, V value) {
        @SuppressWarnings("unchecked")
        final K[] arrayKey = (K[]) keys.toArray();
        add(value, arrayKey);
        return null; // changes many associations each in a different way
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
     * Overwriting a previous entry is forbidden and results in an exception.
     *
     * @param value the value to add
     * @param keys the keys ordered by index (order is important)
     * @return the old value
     * @throws IllegalStateException if a value has been overwritten
     */
    @SuppressWarnings("unchecked")
    public boolean add(V value, K... keys) {
        checkIndexesAndAddIfNeeded(keys.length);

        List<K> keylist = Arrays.asList(keys);
        Entry<List<K>, V> entry = createEntry(keylist, value);
        Entry<List<K>, V> old = super.putEntry(entry);
        if (old != null) {
            throw new IllegalStateException("cannot overwrite: " + old + " with value: " + value);
        }

        boolean added = false;
        for (int index = 0, l = keys.length; index < l; index++) {
            K key = keys[index];
            Set<Entry<List<K>, V>> set = mapList
                    .get(index)
                    .computeIfAbsent(key, k -> createNewSet());
            added |= set.add(entry);
        }
        return added;
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
     *
     * @param index
     * @return the set of keys in the given index
     * @throw IndexOutOfBoundsException
     */
    public Set<K> getKeySetAtIndex(int index) {
        return mapList.get(index).keySet();
    }

    /**
     * Creates a {@link Tree} out of the multi-map.
     *
     * @return the created tree
     */
    public Tree<K,V> createTree() {
        int[] indexes = new int[10];
        for (int i=0; i<indexes.length; i++) {
            indexes[i] = i;
        }
        Tree<K,V> root = createTree(null, null, indexes, 0);
        return root;
    }

    /**
     * Note that node values are assigned by key path in multi-map but are owned by nodes in trees
     * so if the indexed are mixed then the tree might have different values assigned to nodes.
     *
     * @return a tree where each level is assigned to the index in the given position.<br>
     * i.e. {@code createTreeFromIndexes(1, 0, 2)} uses the index 1 for the first level, 0 at the
     * second and 2 at the third.
     */
    public Tree<K,V> createTreeFromIndexes(int... indexes) {
        Tree<K,V> root = createTree(null, null, indexes, 0);
        return root;
    }

    /**
     *
     * @param key       it's the key of the child tree to create
     * @param indexes   the index mapping
     * @param pos       the actual position in the index mapping
     * @param selection the values of this subtree
     * @return
     */
    @SuppressWarnings("unchecked")
    private Tree<K,V> createTree(
            Tree<K,V> parent,
            K key,
            int[] indexes,
            int pos) {

        V value = null;
        if (parent != null) {
            value = getValueFromKeysPath(parent, key, indexes);
        }

        Set<K> keySet;
        if (pos < indexes.length && indexes[pos] < mapList.size()) {
            keySet = getKeySetAtIndex(indexes[pos]);
        } else {
            return createLeaf(key, value);
        }

        if (keySet.isEmpty()) {
            return createLeaf(key, value);

        } else {
            final int indexPosition = pos + 1;
            final Tree<K, V> tree = new Tree<>(key, value).withParent(parent);
            keySet.forEach(k -> {
                Tree<K,V> t = createTree(tree, k, indexes, indexPosition);
                if (t != null) {
                    tree.addTree(t);
                }
            });
            return tree;
        }
    }

    private V getValueFromKeysPath(Tree<K, V> parent, K key, int[] indexes) {
        List<K> parentKeyList = parent.getKeyList();
        List<K> list = new ArrayList<>(parentKeyList.size() + 1);
        list.addAll(parentKeyList);
        list.add(key);
        @SuppressWarnings("unchecked")
        K[] array = (K[]) new Object[list.size()];
        for (int i=0; i<array.length; i++) {
            try {
                array[indexes[i]] = list.get(i);
            } catch (IndexOutOfBoundsException e) {
                return null;
            }
        }
        return get(Arrays.asList(array));
    }

    private Tree<K, V> createLeaf(K key, V value) {
        if (value == null) {
            return null;
        }
        return new Tree<>(key, value);
    }

    /**
     *
     * @param key       the index key
     * @param indexes   a mapping of indexes
     * @param pos       the position on the mapping
     * @param selection all the values selected up to now
     * @return          all the values in common between the passed selection and the values
     *                  pointed by the indexed key
     */
    private Set<Entry<List<K>, V>> createCurrentSelection(
            Object key,
            int[] indexes,
            int pos,
            Set<Entry<List<K>, V>> selection) {

        if (key != null) {
            final int index = indexes[pos - 1];
            Set<Entry<List<K>, V>> keySelection = getEntrySetAtIndex(index, key);
            if (keySelection.isEmpty()) {
                return null;
            } else if (selection == null) {
                return keySelection;
            } else {
                // intersect the new keySelection with the previous one
                Set<Entry<List<K>, V>> currentSelection = createNewSet(keySelection);
                if (selection != null && !selection.isEmpty()) {
                    currentSelection.retainAll(selection);
                }
                return currentSelection;
            }
        } else {
            return null;
        }
    }

}
