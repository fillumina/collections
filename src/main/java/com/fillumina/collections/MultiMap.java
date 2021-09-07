package com.fillumina.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Stores data in an non normalized way by indexing each value with multiple keys. The data can then
 * be queried by selecting the keys of interest in the right index. It is possible to generate trees
 * out of the data by specifying the index that will compose the levels of the tree. The
 * tree can then be manipulated and maps can be extracted by flattening it.
 *
 * @param T the <i>value</i> type (keys are always objects)
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MultiMap<T>
        extends AbstractEntryMap<List<Object>, T, Entry<List<Object>, T>, MultiMap<T>> {

    @SuppressWarnings("unchecked")
    public static final Tree<?> EMPTY_TREE =
            new Tree<>(null, Collections.EMPTY_MAP, Collections.EMPTY_LIST);

    /**
     * Represents a node in a tree (recursively defining the tree).
     */
    public static class Tree<T> {

        /**
         * Recursively generate the tree. The root has parent null.
         */
        private Tree<T> parent;

        /**
         * The list of keys of this node (should be the path of parents).
         */
        private final List<Object> keyList;

        /**
         * Children trees indexex by key (only in non leaf nodes).
         */
        private final Map<Object, Tree<T>> children;

        /**
         * The value of this node (only in leaves).
         */
        private final T value;

        public Tree(T value, Map<Object, Tree<T>> children, List<Object> keyList) {
            this.value = value;
            this.children = children;
            this.keyList = keyList;
            if (children != null) {
                // set children.parent to self
                this.children.values().forEach(t -> t.parent = this);
            }
        }

        /**
         * Clone the entire structure into a map of maps.
         */
        @SuppressWarnings("unchecked")
        public Map<?, ?> toMap() {
            return (Map<?, ?>) replaceMap((Function<T, Object>) Function.identity());
        }

        /**
         * Clone the entire structure into a map of maps transforming its leaf values.
         */
        public Map<?, ?> toMap(Function<T, Object> valueTransformer) {
            return (Map<?, ?>) replaceMap(valueTransformer);
        }

        /**
         * Returns a map of maps for all the levels of the tree except the last one that will be
         * substituted by plain Objects.
         *
         * @param valueTransformer modify the value
         * @return might return a value or another map
         */
        private Object replaceMap(Function<T, Object> valueTransformer) {
            if (children == null) {
                return valueTransformer.apply(value);
            } else {
                Map<?, ?> newChildren = children.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue().replaceMap(valueTransformer)));
                return newChildren;
            }
        }

        /**
         * Clone the entire structure changing its leave values only.
         */
        public <R> Tree<R> replaceTree(Function<T, R> valueTransformer) {
            Map<Object, Tree<R>> m = new HashMap<>();
            if (children == null) {
                return new Tree<>(valueTransformer.apply(value), null, keyList);
            } else {
                Map<Object, Tree<R>> newChildren = children.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey,
                                e -> e.getValue().replaceTree(valueTransformer)));
                return new Tree<>(null, newChildren, keyList);
            }
        }

        /**
         * Compress the tree to the given level and than convert to a map.
         */
        public Map<?, ?> flatToLevel(int level) {
            return (Map<?, ?>) mapAtLevel(Collections.emptyList(), level);
        }

        private Object mapAtLevel(List<Object> klist, int level) {
            if (level > 0) {
                Map<Object, Object> map = new HashMap<>();
                List<Object> listOfKeys = addItemToList(klist, null);
                int pos = klist.size();
                children.forEach((k, c) -> {
                    listOfKeys.set(pos, k);
                    map.putAll((Map<?, ?>) c.mapAtLevel(listOfKeys, level - 1));
                });
                return map;
            }
            // level 0: compacts the rest in a single level
            if (children == null) {
                return value; // no need to compact: we are at the leaf level
            } else {
                Map<?, ?> newChildren = children.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> addItemToList(klist, e.getKey()),
                                e -> valueOrMap(e.getValue())));
                return newChildren;
            }
        }

        private static Object valueOrMap(Object obj) {
            if (obj instanceof Tree) {
                final Tree tree = (Tree) obj;
                if (tree.isLeaf()) {
                    return tree.getValue();
                }
                return (tree).toMap();
            }
            return obj;
        }

        /**
         * Returns a mapping between all keys -> values. It's equal to {@code getFlatMap(maxLevel)}.
         *
         * @return
         */
        public Map<List<Object>, T> getLeavesMap() {
            Map<List<Object>, T> map = new HashMap<>();
            visitLeaves(t -> map.put(t.getKeyList(), t.getValue()));
            return map;
        }

        public void visitLeaves(Consumer<Tree<T>> leafConsumer) {
            if (children != null) {
                children.values().forEach(t -> t.visitLeaves(leafConsumer));
            } else {
                leafConsumer.accept(this);
            }
        }

        public void visitValues(Consumer<T> leafConsumer) {
            if (children != null) {
                for (Tree<T> child : children.values()) {
                    child.visitValues(leafConsumer);
                }
            }
            if (value != null) {
                leafConsumer.accept(value);
            }
        }

        @Override
        public Tree<T> clone() {
            Map<Object, Tree<T>> m = new HashMap<>();
            if (children == null) {
                return new Tree<>(value, null, keyList);
            }
            children.forEach((o, t) -> m.put(o, t.clone()));
            return new Tree<>(value, m, keyList);
        }

        /**
         * Removes leaves and branches passing the predicate test.
         */
        public boolean pruneLeaves(Predicate<T> leavesRemovePredicate) {
            if (isLeaf()) {
                if (value != null) {
                    return leavesRemovePredicate.test(value);
                } else {
                    return true;
                }
            } else {
                Iterator<Entry<Object, Tree<T>>> it = children.entrySet().iterator();
                while (it.hasNext()) {
                    if (it.next().getValue().pruneLeaves(leavesRemovePredicate)) {
                        it.remove();
                    }
                }
                return children.isEmpty();
            }
        }

        /**
         * Removes branches passing the predicate test
         */
        public void pruneBranches(Predicate<Object> branchRemovePredicate) {
            if (!isLeaf()) {
                Iterator<Entry<Object, Tree<T>>> it = children.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Object, Tree<T>> e = it.next();
                    Object key = e.getKey();
                    if (branchRemovePredicate.test(key)) {
                        it.remove();
                    } else {
                        final Tree<T> t = e.getValue();
                        t.pruneBranches(branchRemovePredicate);
                    }
                }
            }
        }

        public boolean isRoot() {
            return parent == null;
        }

        public boolean isLeaf() {
            return children == null || children.size() == 0;
        }

        public Tree<T> getRoot() {
            Tree<T> current = this;
            while (current.parent != null) {
                current = current.parent;
            }
            return current;
        }

        public Tree<T> getParent() {
            return parent;
        }

        public List<Object> getKeyList() {
            return keyList;
        }

        public String getKeyListAsString() {
            return getKeyListAsString(":");
        }

        public String getKeyListAsString(String delimiter) {
            if (keyList == null || keyList.isEmpty()) {
                return null;
            }
            return keyList.stream()
                    .map(o -> Objects.toString(o))
                    .collect(Collectors.joining(delimiter));
        }

        public Map<Object, Tree<T>> getChildren() {
            return children;
        }

        /**
         * Return the tree pointed by the given key.
         *
         * @param key
         * @return
         */
        public Tree<T> get(Object key) {
            return children.get(key);
        }

        /**
         * Returns a map with the given keys each pointing to its child tree.
         *
         * @param keys
         * @return a map of (key -> tree)
         */
        public Map<Object, Tree<T>> mget(Object... keys) {
            if (keys.length == 1) {
                return Collections.singletonMap(keys[0], children.get(keys[0]));
            }
            Map<Object, Tree<T>> map = new HashMap<>();
            for (Object k : keys) {
                if (map.containsKey(k)) {
                    map.put(k, children.get(k));
                }
            }
            return map;
        }

        /**
         * Available only on leaves, otherwise null.
         */
        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("(");
            if (keyList != null) {
                buf.append(keyList.toString());
            }
            if (keyList != null && value != null) {
                buf.append(" => ");
            }
            if (value != null) {
                buf.append(value.toString());
            }
            buf.append(")");
            return buf.toString();
        }
    }

    // The List items are the indexes of key sets mapping to values:
    // each index have a set of keys each of which point to a set of values.
    //            index    key      set of values
    private final List<Map<Object, Set<Entry<List<Object>, T>>>> mapList =
            new ArrayList<>();

    private int keySize = -1;

    // helper to be able to easily change internal set type
    private static <T> Set<Entry<List<Object>, T>> createNewSet() {
        return new HashSet<>();
    }

    // helper to be able to easily change internal set type
    private static <T> Set<Entry<List<Object>, T>> createNewSet(
            Collection<Entry<List<Object>, T>> coll) {
        return new HashSet<>(coll);
    }

    public MultiMap() {
        super();
    }

    public MultiMap(int initialSize) {
        super(initialSize);
    }

    @Override
    protected Entry<List<Object>, T> createEntry(List<Object> k, T v) {
        return new ImmutableMapEntry<>(k, v);
    }

    @Override
    protected AbstractEntryMap<List<Object>, T, Entry<List<Object>, T>, MultiMap<T>>
            createMap(int size) {
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
    public Set<T> getSetAtIndex(int index, Object key) {
        Set<Entry<List<Object>, T>> set = getEntrySetAtIndex(index, key);
        if (set == null) {
            return Collections.<T>emptySet();
        }
        return set.stream().map(e -> e.getValue()).collect(Collectors.toSet());
    }

    private Set<Entry<List<Object>, T>> getEntrySetAtIndex(int index, Object key)
            throws IndexOutOfBoundsException {
        Map<Object, Set<Entry<List<Object>, T>>> map = getEntryMapAtIndex(index);
        return map == null ? null : map.get(key);
    }

    /**
     * Gets the map between keys and values in the index.
     *
     * @param index
     * @return the map of (key,value) from the given index
     */
    public Map<Object, Set<T>> getMapAtIndex(int index) {
        Map<Object, Set<Entry<List<Object>, T>>> map = getEntryMapAtIndex(index);
        if (map == null) {
            return Collections.<Object, Set<T>>emptyMap();
        }
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().stream()
                                .map(n -> n.getValue()).collect(Collectors.toSet())));
    }

    private Map<Object, Set<Entry<List<Object>, T>>> getEntryMapAtIndex(int index) throws
            IndexOutOfBoundsException {
        if (mapList == null || mapList.isEmpty() || index >= mapList.size() || index < 0) {
            throw new IndexOutOfBoundsException("index= " + index + ", multiMap size= " +
                    ((mapList == null) ? 0 : mapList.size()));
        }
        final Map<Object, Set<Entry<List<Object>, T>>> map = mapList.get(index);
        return map;
    }

    /**
     * @param keys the keys ordered by index (order is important)
     * @return a value associated to all passed keys.
     */
    public T getAny(Collection<Object> keys) {
        Set<T> set = getAll(keys);
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.iterator().next();
    }

    /**
     * @param keys the keys ordered by index (order is important)
     * @return the values associated to all the passed keys.
     */
    public Set<T> getAll(Collection<Object> keys) {
        return getAll(keys.toArray());
    }

    /**
     * @param keys the keys ordered by index (order is important)
     * @return a value associated to all passed keys.
     */
    public T getAny(Object... keys) {
        Set<T> set = getAll(keys);
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
    public Set<T> getAll(Object... keys) {
        Set<Entry<List<Object>, T>> result = null;
        for (int i = 0, l = keys.length; i < l; i++) {
            Object k = keys[i];
            if (k != null) {
                Set<Entry<List<Object>, T>> set = getEntrySetAtIndex(i, k);
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
    public synchronized boolean addSynchronized(T value, Object... keys) {
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
    public boolean add(T value, Object... keys) {
        List<Object> keylist = Arrays.asList(keys);
        if (keySize == -1) {
            keySize = keylist.size();
        } else if (keySize != keylist.size()) {
            throw new IllegalArgumentException(
                    "expected key of size " + keySize + ", was " + keylist.size());
        }
        Entry<List<Object>, T> entry = createEntry(keylist, value);
        super.putEntry(entry);

        checkIndexesAndAddIfNeeded(keys.length);
        boolean added = false;
        for (int i = 0, l = keys.length; i < l; i++) {
            Object k = keys[i];
            Set<Entry<List<Object>, T>> set = mapList
                    .get(i)
                    .computeIfAbsent(k, key -> createNewSet());
            added |= set.add(entry);
        }
        return added;
    }

    @Override
    public T put(List<Object> keys, T value) {
        add(value, keys);
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
    public T remove(Object key) {
        throw new UnsupportedOperationException("remove not supported");
    }

    /**
     * NOT SUPPORTED
     */
    @Override
    protected void removeIndex(int idx) {
        throw new UnsupportedOperationException("remove not supported");
    }

    /**
     *
     * @param index
     * @return the set of keys in the given index
     * @throw IndexOutOfBoundsException
     */
    public Set<Object> getKeySetAtIndex(int index) {
        return mapList.get(index).keySet();
    }

    /**
     * @return a tree where each level is assigned to the index in the given position.<br>
     * i.e. {@code createTreeFromIndexes(1, 0, 2)} uses the index 1 for the first level, 0 at the
     * second and 2 at the third.
     */
    public Tree<T> createTreeFromIndexes(int... indexes) {
        Tree<T> root = createTree(Collections.emptyList(), null, indexes, 0, null);
        return root;
    }

    @SuppressWarnings("unchecked")
    private Tree<T> createTree(
            List<Object> keys,
            Object key,
            int[] indexes,
            int pos,
            Set<Entry<List<Object>, T>> selection) {

        final List<Object> keyList = addKeyToKeys(keys, key);
        final Set<Entry<List<Object>, T>> currentSelection =
                createCurrentSelection(key, indexes, pos, selection);

        if (pos < indexes.length) {
            // not a leaf so creates child trees
            Set<Object> keySet = getKeySetAtIndex(indexes[pos]);
            if (keySet.isEmpty()) {
                // no keys
                return (Tree<T>) EMPTY_TREE;

            } else {
                // creates children trees
                Map<Object, Tree<T>> map = new HashMap<>(keySet.size()); // cannot use TreeMap
                final int indexPosition = pos + 1;
                keySet.parallelStream().forEach(k -> {
                    final Tree<T> t = createTree(keyList, k,
                            indexes, indexPosition, currentSelection);
                    synchronized (map) {
                        map.put(k, t);
                    }
                });
                return new Tree<>(null, map, null);
            }

        } else {
            // it's a leaf
            T value = currentSelection.iterator().next().getValue();
            if (currentSelection.size() > 1) {
                throw new IllegalArgumentException("wrong number of indexes");
            }
            return new Tree<>(value, null, keyList);
        }
    }

    private static List<Object> addKeyToKeys(List<Object> keys, Object key) {
        List<Object> keyList;
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

    private static List<Object> addItemToList(List<Object> list, Object item) {
        List<Object> l = new ArrayList<>(list.size() + 1);
        l.addAll(list);
        l.add(item);
        return l;
    }

    private Set<Entry<List<Object>, T>> createCurrentSelection(
            Object key,
            int[] indexes,
            int pos,
            Set<Entry<List<Object>, T>> selection) {

        final Set<Entry<List<Object>, T>> currentSelection;
        if (key != null) {
            Set<Entry<List<Object>, T>> keySelection = getEntrySetAtIndex(indexes[pos - 1], key);
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
