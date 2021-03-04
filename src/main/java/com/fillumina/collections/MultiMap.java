package com.fillumina.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
 * Provides a way to easily navigate, rearrange and manipulate data present in tree structures. The
 * idea is to replicate the functioning of a relational database where every inserted key has its
 * own set of values associated with it and can be queried to create both useful answers or new
 * organization of the same data. In particular a new tree structure can be created with a different
 * hierarchy of indexes. For example it is possible to pass from a hierarchy organized by
 * {@code granpa -> father -> son} to a new one with {@code son -> father -> granpa} or even
 * compress one level to get: {@code granpa -> families (fathers+sons)}.
 * <p>
 * It's performances are not particularly fast but managing potentially a lot of data the accent was
 * on having a compact representation. It should be used as a computational step to extract useful
 * views over unstructured data.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MultiMap<T> {

    public static class Tree<T> {

        private Tree<T> parent;
        private final List<Object> keyList;
        private final Map<Object, Tree<T>> children;
        private final T value;

        public Tree(T value, Map<Object, Tree<T>> children,
                List<Object> keyList) {
            this.value = value;
            this.children = children;
            this.keyList = keyList;
            if (children != null) {
                this.children.values().forEach(t -> t.parent = this);
            }
        }

        /**
         * Clone the entire structure into a map.
         */
        public Map<?, ?> toMap() {
            return (Map<?, ?>) replaceMap((Function<T, Object>) Function.identity());
        }

        /**
         * Clone the entire structure into a map transforming its leave values.
         */
        public Map<?, ?> toMap(Function<T, Object> transformer) {
            return (Map<?, ?>) replaceMap(transformer);
        }

        private Object replaceMap(Function<T, Object> transformer) {
            if (children == null) {
                return value;
            } else {
                Map<?, ?> newChildren = children.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue().replaceMap(transformer)));
                return newChildren;
            }
        }

        /**
         * Clone the entire structure changing its leave values only.
         */
        public <R> Tree<R> replaceTree(Function<T, R> transformer) {
            Map<Object, Tree<R>> m = new HashMap<>();
            if (children == null) {
                return new Tree<>(transformer.apply(value),
                        null, keyList);
            } else {
                Map<Object, Tree<R>> newChildren = children.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey,
                                e -> e.getValue().replaceTree(transformer)));
                return new Tree<>(null, newChildren, keyList);
            }
        }

        /**
         * Compress the tree to the given level and than convert to map. {@code flatToLevel(0)}
         * returns a similar map than {@link toMap()} but keys are of type {@code List<Object>}
         * instead of {@code String}.
         */
        public Map<?, ?> flatToLevel(int level) {
            return (Map<?, ?>) mapAtLevel(List.of(), level);
        }

        private Object mapAtLevel(List<Object> klist, int level) {
            if (level > 0) {
                Map<Object, Object> map = new HashMap<>();
                List<Object> listOfKeys = createList(klist, null);
                int pos = klist.size();
                children.forEach((k, c) -> {
                    listOfKeys.set(pos, k);
                    map.putAll((Map<?, ?>) c.mapAtLevel(listOfKeys, level - 1));
                });
                return map;
            }
            if (children == null) {
                return value;
            } else {
                Map<?, ?> newChildren = children.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> createList(klist, e.getKey()),
                                e -> e.getValue().toMap()));
                return newChildren;
            }
        }

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
         * Removes leaves and branches passing the predicate test
         */
        public boolean pruneLeaves(Predicate<T> leavesRemovePredicate) {
            if (isLeaf()) {
                if (value != null) {
                    return leavesRemovePredicate.test(value);
                } else {
                    return true;
                }
            } else {
                Iterator<Entry<Object, Tree<T>>> it =
                        children.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<Object, Tree<T>> e = it.next();
                    if (e.getValue().pruneLeaves(leavesRemovePredicate)) {
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
                Iterator<Entry<Object, Tree<T>>> it =
                        children.entrySet().iterator();
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

        public Tree<T> get(Object key) {
            return children.get(key);
        }

        public Map<Object, Tree<T>> mget(Object... keys) {
            if (keys.length == 1) {
                return Map.of(keys[0], children.get(keys[0]));
            }
            Map<Object, Tree<T>> map = new HashMap<>();
            for (Object k : keys) {
                map.put(k, children.get(k));
            }
            return map;
        }

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

    /**
     * Contains the value and all the keys it refers to. It's important because we cannot assume all
     * values have a meaningful {@link Object#hashCode()} and
     * {@link Object#equals(java.lang.Object)} implementation.
     */
    private static class Container<T> {

        private final Object[] keys;
        private final T value;
        private final int hashcode;

        public Container(Object[] keys, T value) {
            this.keys = keys;
            this.value = value;
            this.hashcode = Arrays.deepHashCode(keys);
        }

        @Override
        public int hashCode() {
            return hashcode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Container<?> other = (Container<?>) obj;
            return Arrays.deepEquals(this.keys, other.keys);
        }

        @Override
        public String toString() {
            return Arrays.toString(keys) + " => " + value;
        }
    }

    // The keys are organized positionally in a list so that key '1' on the first
    // position of the list is different from key '1' on the second.
    //                      key      set of values
    private final List<Map<Object, Set<Container<T>>>> mapList = new ArrayList<>();

    // helper to be able to change internal set type
    private static <T> Set<Container<T>> createNewSet() {
        return new HashSet<>();
    }

    // helper to be able to change internal set type
    private static <T> Set<Container<T>> createNewSet(Collection<Container<T>> coll) {
        return new HashSet<>(coll);
    }

    public void clear() {
        mapList.forEach(m -> m.clear());
        mapList.clear();
    }

    /**
     * @return true if no <i>values</i> are present.
     */
    public boolean isEmpty() {
        for (Map<Object, Set<Container<T>>> map : mapList) {
            if (!map.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Be careful this is a quite expensive operation.
     *
     * @return the total number of <i>values</i>.
     */
    public int size() {
        return values().size();
    }

    /**
     * @return the values associated with the key at the given index (position).
     */
    private Set<Container<T>> getSetAtIndex(int index, Object key) {
        if (mapList == null || index >= mapList.size() || index < 0) {
            return null;
        }
        final Map<Object, Set<Container<T>>> map = mapList.get(index);
        return map == null ? null : map.get(key);
    }

    /**
     * @return the first value associated to all passed keys.
     */
    public T getFirst(Collection<Object> keys) {
        Set<T> set = get(keys);
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.iterator().next();
    }

    /**
     * @return the values associated to all the passed keys.
     */
    public Set<T> get(Collection<Object> keys) {
        return get(keys.toArray());
    }

    /**
     * @return the first value associated to all passed keys.
     */
    public T getFirst(Object... keys) {
        Set<T> set = get(keys);
        if (set == null || set.isEmpty()) {
            return null;
        }
        return set.iterator().next();
    }

    /**
     * @return the values associated to all passed keys.
     */
    public Set<T> get(Object... keys) {
        Set<Container<T>> result = null;
        for (int i = 0, l = keys.length; i < l; i++) {
            Object k = keys[i];
            if (k != null) {
                Set<Container<T>> set = getSetAtIndex(i, k);
                if (set != null) {
                    if (result == null) {
                        result = createNewSet(set);
                    } else {
                        result.retainAll(set);
                    }
                }
            }
        }
        return result == null ? null : result.stream().map(s -> s.value).collect(Collectors.toSet());
    }
    
    /**
     * Adds a value to all the sets corresponding to the given keys. The position of the key is
     * important and equal keys on different position are considered different.
     * This method can be executed concurrently (i.e. in a parallel stream).
     *
     * @return true if the value has been added (was not already present).
     */
    public boolean add(T value, Object... keys) {
        checkMapListSize(keys.length);
        Container container = new Container(keys, value);
        boolean added = false;
        for (int i = 0, l = keys.length; i < l; i++) {
            Object k = keys[i];
            Set<Container<T>> set = mapList
                    .get(i)
                    .computeIfAbsent(k, key -> createNewSet());
            synchronized (set) {
                added |= set.add(container);
            }
        }
        return added;
    }

    private void checkMapListSize(int size) {
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

    public boolean remove(Object... keys) {
        boolean removed = false;
        Container<T> element = new Container<>(keys, null);
        for (int i = 0, l = keys.length; i < l; i++) {
            Object k = keys[i];
            Set<Container<T>> set = getSetAtIndex(i, k);
            if (set != null) {
                removed |= set.remove(element);
            }
        }
        return removed;
    }

    public Set<T> values() {
        return mapList.stream()
                .flatMap(map -> map.values().stream())
                .flatMap(set -> set.stream())
                .map(container -> container.value)
                .collect(Collectors.toSet());
    }

    public Set<Object> keySet(Object... keys) {
        return mapList.stream()
                .flatMap(map -> map.keySet().stream())
                .collect(Collectors.toSet());
    }

    public Set<Object> keysAtIndex(int index) {
        return mapList.get(index).keySet();
    }

    /**
     * @return a tree from the given positions.
     */
    public Tree<T> treeFromIndexes(int... indexes) {
        Tree<T> root = createTree(List.of(), null, indexes, 0,
                null);
        return root;
    }

    private Tree<T> createTree(
            List<Object> keys, Object key,
            int[] indexes, int pos,
            Set<Container<T>> selection) {

        List<Object> keyList;
        final Set<Container<T>> currentSelection;
        if (key != null) {
            if (keys == null) {
                keyList = List.of(key);
            } else {
                keyList = createList(keys, key);
            }

            Set<Container<T>> keySelection = getSetAtIndex(indexes[pos - 1], key);
            if (keySelection.isEmpty()) {
                currentSelection = createNewSet(selection);
            } else {
                currentSelection = createNewSet(keySelection);
                if (selection != null && !selection.isEmpty()) {
                    currentSelection.retainAll(selection);
                }
            }
            if (currentSelection == null || currentSelection.isEmpty()) {
                return null;
            }
        } else {
            keyList = keys;
            currentSelection = null;
        }

        final boolean isNotLeaf = pos < indexes.length;
        if (isNotLeaf) {
            Set<Object> keySet = keysAtIndex(indexes[pos]);
            if (keySet.isEmpty()) {
                return null;
            }
            // cannot use TreeMap on unknown types
            Map<Object, Tree<T>> map = new HashMap<>(keySet.size());
            int indexPosition = pos + 1;
            keySet.parallelStream().forEach(k -> {

                final Tree<T> t = createTree(keyList, k,
                        indexes, indexPosition, currentSelection);
                if (t != null) {
                    synchronized (map) {
                        map.put(k, t);
                    }
                }
            });

            if (map == null) {
                return null;
            }
            return new Tree<>(null, map, null);
        } else {
            T value = currentSelection.iterator().next().value;

            if (currentSelection.size() > 1) {
                throw new IllegalArgumentException("wrong number of parameters");
            }

            return new Tree<>(value, null, keyList);
        }
    }

    private static List<Object> createList(List<Object> list, Object elem) {
        List<Object> l = new ArrayList<>(list.size() + 1);
        l.addAll(list);
        l.add(elem);
        return l;
    }
}
