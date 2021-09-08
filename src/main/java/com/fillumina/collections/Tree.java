package com.fillumina.collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Represents a node in a tree (recursively defining a tree). The tree is immutable.
 */
// TODO should be heavily modified -> each Tree should be a Map & a Map.Entry at the same time
public class Tree<T> {

    @SuppressWarnings("unchecked")
    public static final Tree<?> EMPTY_TREE =
            new Tree<>(Collections.EMPTY_LIST, Collections.EMPTY_MAP, null);

    @SuppressWarnings("unchecked")
    public static <T> Tree<T> emptyTree() {
        return (Tree<T>) EMPTY_TREE;
    }

    /**
     * The root has parent is null.
     */
    private Tree<T> parent;

    /**
     * The list of keys of this node.
     */
    // TODO it's the path up to root!
    private final List<Object> keyList;

    /**
     * Children trees indexed by key (only in non leaf nodes).
     */
    // TODO could be substituted by keys in the child tree
    private final Map<Object, Tree<T>> children;

    /**
     * The value of this node.
     */
    // TODO ONLY FOR LEAVES!!
    private final T value;

    // TODO warning: no definsive copy of parameters
    public Tree(List<Object> keyList, Map<Object, Tree<T>> children, T value) {
        this.keyList = keyList;
        this.children = children;
        this.value = value;
        if (children != null && !children.isEmpty()) {
            // set children.parent to self
            this.children.values().forEach(t -> t.parent = this);
        }
    }

    public T getValue() {
        return value;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    public Tree<T> getRoot() {
        Tree<T> current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    public int getDepth() {
        int depth = 0;
        Tree<T> current = this;
        while (current.parent != null) {
            current = current.parent;
            depth++;
        }
        return depth;
    }

    public Tree<T> getParent() {
        return parent;
    }

    // TODO warning: passing internal data!
    public List<Object> getKeyList() {
        return keyList;
    }

    public String getKeyListAsString() {
        return getKeyListAsString(":");
    }

    public String getKeyListAsString(String delimiter) {
        List<?> lkeyList = getKeyList();
        if (lkeyList == null || lkeyList.isEmpty()) {
            return null;
        }
        return lkeyList.stream()
                .map(o -> Objects.toString(o)).collect(Collectors.joining(delimiter));
    }

    // TODO warning: passing internal data!
    public Map<Object, Tree<T>> getChildren() {
        return children;
    }

    /**
     * Returns the children tree mapped by the given key.
     *
     * @param key
     * @return the children tree
     */
    public Tree<T> get(Object key) {
        return children.get(key);
    }

    /**
     * Clone the entire structure into a map of maps.
     */
    // TODO basically this is how the Tree should become (this method should't be needed anymore)
    @SuppressWarnings(value = "unchecked")
    public Map<?, ?> toMultiLevelMap() {
        return (Map<?, ?>) replaceMap((Function<T, Object>) Function.identity());
    }

    /**
     * Clone the entire structure into a map of maps transforming its leaf values.
     */
    // TODO basically this is how the Tree should become (this method should't be needed anymore)
    public Map<?, ?> toMultiLevelMap(Function<T, Object> valueTransformer) {
        return (Map<?, ?>) replaceMap(valueTransformer);
    }

    /**
     * Returns a map of maps for all the levels of the tree except the last one that will be
     * substituted by plain Objects.
     *
     * @param valueTransformer modify the value
     * @return might return a value or another map
     */
    // TODO basically this is how the Tree should become (this method should't be needed anymore)
    private Object replaceMap(Function<T, Object> valueTransformer) {
        if (children == null) {
            return valueTransformer.apply(value);
        } else {
            Map<?, ?> newChildren = children.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey(),
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
            return new Tree<>(keyList, null, valueTransformer.apply(value));
        } else {
            Map<Object, Tree<R>> newChildren = children.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().replaceTree(valueTransformer)));
            return new Tree<>(keyList, newChildren, null);
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
                    .collect(Collectors.toMap(e -> addItemToList(klist, e.getKey()),
                            e -> valueOrMap(e.getValue())));
            return newChildren;
        }
    }

    /**
     * Returns a mapping between all keys -> values. It's functionally equals and more efficient
     * than {@code getFlatMap(maxLevel)}.
     *
     * @return A single level Map with key list as keys and leaves as values.
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
        if (value != null) {
            leafConsumer.accept(value);
        }
        if (children != null) {
            for (Tree<T> child : children.values()) {
                child.visitValues(leafConsumer);
            }
        }
    }

    @Override
    public Tree<T> clone() {
        Map<Object, Tree<T>> m = new HashMap<>();
        if (children == null) {
            return new Tree<>(keyList, null, value);
        }
        children.forEach((o, t) -> m.put(o, t.clone()));
        return new Tree<>(keyList, m, value);
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
            Iterator<Map.Entry<Object, Tree<T>>> it = children.entrySet().iterator();
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
            Iterator<Map.Entry<Object, Tree<T>>> it = children.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Object, Tree<T>> e = it.next();
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

    private static List<Object> addItemToList(List<Object> list, Object item) {
        List<Object> l = new ArrayList<>(list.size() + 1);
        l.addAll(list);
        l.add(item);
        return l;
    }

    private static Object valueOrMap(Object obj) {
        if (obj instanceof Tree) {
            final Tree tree = (Tree) obj;
            if (tree.isLeaf()) {
                return tree.getValue();
            }
            return (tree).toMultiLevelMap();
        }
        return obj;
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
