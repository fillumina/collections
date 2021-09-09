package com.fillumina.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
        extends AbstractEntryMap<List<K>, Tree<K,V>, Tree<K,V>, Tree<K,V>>
        implements Map.Entry<List<K>,Tree<K,V>> {

    @SuppressWarnings("unchecked")
    public static final Tree<?,?> EMPTY_TREE = new Tree<>((Object)null, null);

    @SuppressWarnings("unchecked")
    public static <K,V> Tree<K,V> emptyTree() {
        return (Tree<K,V>) EMPTY_TREE;
    }

    private static class KeyList<T> extends SmallList<T> {

        public KeyList() {
        }

        public KeyList(
                Collection<? extends T> elements) {
            super(elements);
        }

        /**
         * If there is only one item returns the hash code of that item.
         */
        @Override
        public int hashCode() {
            if (size() == 1) {
                return Objects.hashCode(get(0));
            }
            return super.hashCode();
        }

        /**
         * It allows to match an object with the first item of the list if the list has only
         * one item.
         */
        @Override
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (size() == 1) {
                return Objects.equals(get(0), o);
            }
            return super.equals(o);
        }
    }

    /**
     * The root has parent is null.
     */
    private Tree<K,V> parent;

    private final List<K> keyList;

    /**
     * The leaf value of this node.
     */
    private V leafValue;

    public Tree(List<K> keyList, V leafValue, int initialSize) {
        super(initialSize);
        this.keyList = new KeyList<>(keyList);
        this.leafValue = leafValue;
    }

    /** Creates a root. */
    public Tree() {
        this.parent = null;
        this.keyList = null;
    }

    /** Creates a node. **/
    @SuppressWarnings("unchecked")
    public Tree(K k) {
        this.keyList = new ImmutableSmallList<>(k);
    }

    /** Creates a node. **/
    public Tree(List<K> k) {
        this.keyList = new ImmutableSmallList<>(k);
    }

    /** Creates a leaf. */
    @SuppressWarnings("unchecked")
    public Tree(K k, V v) {
        this.keyList = new ImmutableSmallList<>(k);
        this.leafValue = v;
    }

    /** Creates a leaf. */
    public Tree(List<K> k, V v) {
        this.keyList = new ImmutableSmallList<>(k);
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
            if (current == null) {
                return;
            }
            if (current == tree) {
                throw new IllegalArgumentException("the parent circularly points to tree");
            }
            current = current.parent;
        }
    }

    @Override
    protected Tree<K,V> createEntry(List<K> key, Tree<K,V> value) {
        final Tree<K, V> tree = new Tree<>(key, value.leafValue, value.size());
        value.forEach((k,v) -> tree.putEntry(createEntry(k,v).withParent(tree)));
        return tree;
    }

    @Override
    protected Tree<K,V> createMap(int size) {
        return new Tree<>(this.keyList, this.leafValue, size)
                .withParent(this.parent);
    }

    public Tree<K, V> addTree(Tree<K, V> entry) {
        return super.putEntry(entry.withParent(this));
    }

    @Override
    public Tree<K, V> getEntry(Object key) {
        if (key instanceof List) {
            return super.getEntry(key);
        } else {
            return super.getEntry(Collections.singletonList(key));
        }
    }

    public Tree<K, V> put(K key, Tree<K, V> value) {
        return put(Collections.singletonList(key), value);
    }


    @Override
    public Tree<K, V> put(List<K> key, Tree<K, V> value) {
        if (value.getParent() == null && (key == null || key.equals(value.getKey()))) {
            return super.putEntry(value.withParent(this));
        }
        Tree<K,V> clone = value.cloneWithKey(key);
        return super.putEntry(clone.withParent(this));
    }

    @Override
    public void putAll(Map<? extends List<K>, ? extends Tree<K, V>> m) {
        m.forEach((k,v) -> put(k, v));
    }



    /** Adds the passed tree to the new one, if it is already part of another tree it's cloned. */
    @Override
    protected Tree<K, V> putEntry(Tree<K, V> entry) {
        if (entry.getParent() == null) {
            return super.putEntry(entry.withParent(this));
        }
        Tree<K,V> clone = entry.clone();
        return super.putEntry(clone.withParent(this));
    }



    @Override
    public List<K> getKey() {
        return keyList;
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
            // insert in reversed order
            List<K> clist = current.keyList;
            if (clist != null && !clist.isEmpty()) {
                for (int i=clist.size() - 1; i>=0; i--) {
                    list.add(clist.get(i));
                }
            }
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
            return new Tree<>(keyList, valueTransformer.apply(leafValue));
        } else {
            Tree<K,W> tree = new Tree<>(keyList);
            forEach((k,v) -> tree.put(k, v.cloneReplacingValues(valueTransformer)));
            return tree;
        }
    }

    /**
     * Compress the tree from the given level on and than convert to a map.
     * @param level to compact
     */
    public Tree<K,V> flatFromLevel(int level) {
        if (isLeaf()) {
            return new Tree<>(getKeyList(), getLeafValue());
        } else if (getDepth() >= level) {
            // compact here
            Tree<K,V> tree = new Tree<>(getKey(), getLeafValue());
            forEach((k,v ) -> {
                if (v.isLeaf()) {
                    List<K> kl = v.getKeyList();
                    kl = kl.subList(level, kl.size());
                    Tree<K,V> leaf = new Tree<>(kl, v.getLeafValue());
                    tree.putEntry(leaf);
                } else {
                    tree.putAll(v.flatFromLevel(level));
                }
            });
            return tree;
        } else {
            // clone here
            Tree<K,V> tree = new Tree<>(keyList, leafValue);
            forEach((k,v) -> tree.putEntry(v.flatFromLevel(level)));
            return tree;
        }
    }

    public Tree<K,V> flatToLevel(int level) {
        if (level > 0) {
            // clone here
            Tree<K,V> tree = new Tree<>();
            int newLevel = level - 1;
            forEach((k,v) -> tree.putAll(v.flatToLevel(newLevel)));
            return tree;
        } else {
            // compact here
            Tree<K,V> tree = new Tree<>();
            forEach((k,v) -> {
                final List<K> kl = v.getKeyList();
                tree.put(kl, v);
            });
            return tree;
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

    public Tree<K,V> cloneWithKey(List<K> keyList) {
        if (isLeaf()) {
            return new Tree<>(keyList, leafValue);
        }
        Tree<K,V> tree = new Tree<>(keyList, leafValue, size());
        forEach((o, t) -> tree.putEntry(t.clone().withParent(tree)));
        return tree;
    }

    @Override
    public Tree<K,V> clone() {
        if (isLeaf()) {
            return new Tree<>(keyList, leafValue);
        }
        Tree<K,V> tree = new Tree<>(keyList, leafValue, size());
        forEach((o, t) -> tree.putEntry(t.withParent(tree)));
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
            Iterator<Map.Entry<List<K>, Tree<K,V>>> it = entrySet().iterator();
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
    public void pruneBranches(Predicate<List<K>> branchRemovePredicate) {
        if (!isLeaf()) {
            Iterator<Map.Entry<List<K>, Tree<K,V>>> it = entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<List<K>, Tree<K,V>> e = it.next();
                List<K> key = e.getKey();
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
        hash = 37 * hash + Objects.hashCode(this.keyList);
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
        if (!Objects.equals(this.keyList, other.keyList)) {
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
        }
        if (!isEmpty()) {
            buf.append('\n');
            forEach((k,v) -> buf.append(v.toString(tab + 1)));
        }
        return buf.toString();
    }

    private static String createTabs(int tab) {
        if (tab == 0) {
            return "";
        }
        char[] array = new char[tab * 4];
        Arrays.fill(array, ' ');
        return new String(array);
    }
}
