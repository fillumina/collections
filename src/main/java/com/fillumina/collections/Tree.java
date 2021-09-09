package com.fillumina.collections;

import java.util.ArrayList;
import java.util.Arrays;
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
// TODO use an interface and a factory to mask the horrible getValue() returning itself
// TODO use Tree as a return type for flat operations (inner operation)
public class Tree<K,V> // it's a Map AND an Entry with value as itself
        //                        K        V       E          M
        extends AbstractEntryMap<K, Tree<K,V>, Tree<K,V>, Tree<K,V>>
        implements Map.Entry<K,Tree<K,V>> {

    @SuppressWarnings("unchecked")
    public static final Tree<?,?> EMPTY_TREE = new Tree<>(null, null);

    @SuppressWarnings("unchecked")
    public static <K,V> Tree<K,V> emptyTree() {
        return (Tree<K,V>) EMPTY_TREE;
    }

    /**
     * The root has parent is null.
     */
    private Tree<K,V> parent;

    private final K key;

    /**
     * The leaf value of this node.
     */
    private V leafValue;

    /** Creates a node with its children defined into a map (perform defensive shallow copy). */
    public Tree(K key, Map<K, Tree<K,V>> children) {
        this.key = key;
        if (children != null && !children.isEmpty()) {
            addAll(children);
            values().forEach(t -> t.parent = this);
        }
    }

    /** Creates a root. */
    public Tree() {
        this.parent = null;
        this.key = null;
    }

    /** Creates a node. **/
    public Tree(K k) {
        this.key = k;
    }

    /** Creates a leaf. */
    public Tree(K k, V v) {
        this.key = k;
        this.leafValue = v;
    }

    /** Set the parent. */
    public Tree<K,V> withParent(Tree<K,V> parent) throws IllegalArgumentException {
        circularityCheck(this, parent);
        this.parent = parent;
        return this;
    }

    static <K,V> void circularityCheck(Tree<K,V> tree, Tree<K,V> parent)
            throws IllegalArgumentException {
        Tree<K,V> current = parent;
        for (int i=0; i<10; i++) {
            if (current == tree) {
                throw new IllegalArgumentException("the parent circularly points to tree");
            }
            current = current.parent;
            if (current == null) {
                return;
            }
        }
    }

    @Override
    protected Tree<K,V> createEntry(K k, Tree<K,V> v) {
        return v;
    }

    @Override
    protected Tree<K,V> createMap(int size) {
        return new Tree<>();
    }

    public Tree<K, V> addTree(Tree<K, V> entry) {
        return super.putEntry(entry.withParent(this));
    }

    @Override
    public Tree<K, V> put(K key, Tree<K, V> value) {
        Objects.requireNonNull(value, "value must be not null");
        return super.put(key, value.withParent(this));
    }

    @Override
    public K getKey() {
        return key;
    }

    /**
     * WARNING: this will return the same tree (self)!
     * @see #getLeafValue()
     * @see #getLeafValue(java.lang.Object)
     */
    @Override
    public Tree<K,V> getValue() {
        return this; // <<---- that's the trick! *********************************
    }

    @Override
    public Tree<K,V> setValue(Tree<K,V> value) {
        // it makes no sense
        throw new UnsupportedOperationException("not supported");
    }

    public V getLeafValue(K key) {
        return get(key).getLeafValue();
    }

    public V getLeafValue() {
        return leafValue;
    }

    public V setLeafValue(V value) {
        V oldValue = this.leafValue;
        this.leafValue = value;
        return oldValue;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return isEmpty();
    }

    public Tree<K,V> getRoot() {
        Tree<K,V> current = this;
        while (current.parent != null) {
            current = current.parent;
        }
        return current;
    }

    public int getDepth() {
        int depth = 0;
        Tree<K,V> current = this;
        while (current.parent != null) {
            current = current.parent;
            depth++;
        }
        return depth;
    }

    public Tree<K,V> getParent() {
        return parent;
    }

    public List<K> getKeyList() {
        List<K> list = new ArrayList<>();
        Tree<K,V> current = this;
        while (current.parent != null) {
            list.add(current.key);
            current = current.parent;
        }
        Collections.reverse(list);
        return list;
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

    /**
     * Clone the entire structure changing its leave values only.
     */
    public <W> Tree<K,W> cloneReplacingValues(Function<V, W> valueTransformer) {
        if (isLeaf()) {
            return new Tree<>(key, valueTransformer.apply(leafValue));
        } else {
            Tree<K,W> tree = new Tree<>(key);
            forEach((k,v) -> tree.put(k, v.cloneReplacingValues(valueTransformer)));
            return tree;
        }
    }

    /**
     * Compress the tree from the given level on and than convert to a map.
     * @param level to compact
     */
    public Map<?,?> flatFromLevel(int level) {
        return (Map<?,?>) flatFromLevelObj(level);
    }

    private Object flatFromLevelObj(int level) {
        if (isLeaf()) {
            return leafValue;
        } else if (level > getDepth()) {
            Map<Object,Object> map = new HashMap<>();
            forEach((k,v) -> map.putAll((Map<?,?>)v.flatFromLevelObj(level)));
            return map;
        } else {
            Map<Object,Object> map = new HashMap<>();
            forEach((k,v) -> {
                Object key = (level == getDepth()) ? v.getKeyList() : v.getKey();
                map.put(key, v.flatFromLevelObj(level));
            });
            return map;
        }
    }

    public Map<List<K>,Tree<K,V>> flatMapToLevel(int level) {
        return flatToLevel(level, Function.identity());
    }

    /**
     * Compress the tree to the given level and than convert to a map.
     * @param level to compact
     */
    @SuppressWarnings("unchecked")
    public <L> Map<L,Tree<K,V>> flatToLevel(int level, Function<List<K>,L> keyListTransformer) {
        if (level > 0) {
            Map<L,Tree<K,V>> map = new HashMap<>();
            int newLevel = level - 1;
            forEach((k,v) -> map.putAll((Map<L,Tree<K,V>>)v.flatMapToLevel(newLevel)));
            return map;
        } else {
            Map<L,Tree<K,V>> map = new HashMap<>();
            forEach((k,v) -> map.put(keyListTransformer.apply(v.getKeyList()), v));
            return map;
        }
    }

    /**
     * Returns a mapping between all keys -> values. It's functionally equals and more efficient
     * than {@code getFlatMap(maxLevel)}.
     *
     * @return A single level Map with key list as keys and leaves as values.
     */
    public Map<List<K>, V> toFlatMap() {
        return toFlatMap(Function.identity());
    }

    /**
     * Returns a mapping between all keys -> values. It's functionally equals and more efficient
     * than {@code getFlatMap(maxLevel)}.
     *
     * @param keyListTransformer transforms the key list
     * @return A single level Map with key list as keys and leaves as values.
     */
    public <L> Map<L, V> toFlatMap(Function<List<K>, L> keyListTransformer) {
        Map<L, V> map = new HashMap<>();
        visitLeaves(t ->
                map.put(keyListTransformer.apply(t.getKeyList()), t.getLeafValue()));
        return map;
    }

    public void visitLeaves(Consumer<Tree<K,V>> leafConsumer) {
        if (!isEmpty()) {
            values().forEach(t -> t.visitLeaves(leafConsumer));
        } else {
            leafConsumer.accept(this);
        }
    }

    public void visitValues(Consumer<V> leafConsumer) {
        if (!isEmpty()) {
            forEach((k,v) -> v.visitValues(leafConsumer));
        }
        if (leafValue != null) {
            leafConsumer.accept(leafValue);
        }
    }

    @Override
    public Tree<K,V> clone() {
        if (isLeaf()) {
            return new Tree<>(key, leafValue);
        }
        Tree<K,V> tree = new Tree<>(key);
        forEach((o, t) -> tree.put(o, t.clone()));
        return tree;
    }

    /**
     * Removes leaves and branches passing the predicate test.
     */
    public boolean pruneLeaves(Predicate<V> leavesRemovePredicate) {
        if (isLeaf()) {
            if (leafValue != null) {
                return leavesRemovePredicate.test(leafValue);
            } else {
                return true;
            }
        } else {
            Iterator<Map.Entry<K, Tree<K,V>>> it = entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue().pruneLeaves(leavesRemovePredicate)) {
                    it.remove();
                }
            }
            return isEmpty();
        }
    }

    /**
     * Removes branches passing the predicate test
     */
    public void pruneBranches(Predicate<K> branchRemovePredicate) {
        if (!isLeaf()) {
            Iterator<Map.Entry<K, Tree<K,V>>> it = entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<K, Tree<K,V>> e = it.next();
                K key = e.getKey();
                if (branchRemovePredicate.test(key)) {
                    it.remove();
                } else {
                    final Tree<K,V> t = e.getValue();
                    t.pruneBranches(branchRemovePredicate);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.key);
        hash = 37 * hash + Objects.hashCode(this.leafValue);
        return hash;
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
        final Tree<?,?> other = (Tree<?,?>) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        if (!Objects.equals(this.leafValue, other.leafValue)) {
            return false;
        }
        return true;
    }

    /** Avoid IDE Debugger default map representations. */
    public String toActualString(int tab) {
        return toString();
    }

    @Override
    public String toString() {
        return toString(0);
    }

    public String toString(int tab) {
        String tabs = createTabs(tab);
        StringBuilder buf = new StringBuilder();
        buf.append(tabs).append(getKey());
        if (isLeaf()) {
            buf.append(" => ").append(getLeafValue()).append("\n");
        } else {
            buf.append(" :\n");
            forEach((k,v) -> buf.append(v.toString(tab + 1)));
        }
        return buf.toString();
    }

    private String createTabs(int tab) {
        if (tab == 0) {
            return "";
        }
        char[] array = new char[tab];
        Arrays.fill(array, '\t');
        return new String(array);
    }
}
