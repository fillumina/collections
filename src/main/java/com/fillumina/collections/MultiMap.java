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
 * Multi map simulates a DB allowing for many keys, indexes and complex
 * searches. It provides a way to easily navigate and manipulate complex 
 * structures.
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

        /** Clone the entire structure to map. */
        public Map<?,?> toMap() {
            return (Map<?, ?>) replaceMap(
                    (Function<T, Object>) Function.identity());
        }

        /** Clone the entire structure to map changing its leave values. */
        public Map<?,?> toMap(Function<T,Object> transformer) {
            return (Map<?, ?>) replaceMap(transformer);
        }

        private Object replaceMap(Function<T,Object> transformer) {
            if (children == null) {
                return value;
            } else {
                Map<?,?> newChildren = children.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey(),
                                e -> e.getValue().replaceMap(transformer)));
                return newChildren;
            }
        }
        
        /** Clone the entire structure changing its leave values only. */
        public <R> Tree<R> replaceTree(Function<T,R> transformer) {
            Map<Object,Tree<R>> m = new HashMap<>();
            if (children == null) {
                return new Tree<>(transformer.apply(value), 
                        null, keyList);
            } else {
                Map<Object,Tree<R>> newChildren = children.entrySet().stream()
                        .collect(Collectors.toMap(Entry::getKey,
                                e -> e.getValue().replaceTree(transformer)));
                return new Tree<>(null, newChildren, keyList);
            }
        }
        
        /** 
         * Compress the tree to the given level and than convert to map. 
         * {@code flatToLevel(0)} returns a similar map than {@link toMap()} 
         * but keys are of type {@code List<Object>} instead of {@code String}.
         */
        public Map<?,?> flatToLevel(int level) {
            return (Map<?,?>) mapAtLevel(List.of(), level);
        }

        private Object mapAtLevel(List<Object> klist, int level) {
            if (level > 0) {
                Map<Object,Object> map = new HashMap<>();
                List<Object> listOfKeys = createList(klist, null);
                int pos = klist.size();
                children.forEach((k,c) -> {
                    listOfKeys.set(pos, k);
                    map.putAll((Map<?,?>)
                            c.mapAtLevel(listOfKeys, level - 1));
                });
                return map;
            }
            if (children == null) {
                return value;
            } else {
                Map<?,?> newChildren = children.entrySet().stream()
                        .collect(Collectors.toMap(
                                e -> createList(klist, e.getKey()),
                                e -> e.getValue().toMap()));
                return newChildren;
            }
        }
        
        private List<Object> createList(List<Object> list, Object elem) {
            List<Object> l = new ArrayList<>(list.size() + 1);
            l.addAll(list);
            l.add(elem);
            return l;
        }
        
        public Map<List<Object>, T> getLeavesMap() {
            Map<List<Object>,T> map = new HashMap<>();
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
            Map<Object,Tree<T>> m = new HashMap<>();
            if (children == null) {
                return new Tree<>(value, null, keyList);
            }
            children.forEach((o,t) -> m.put(o, t.clone()));
            return new Tree<>(value, m, keyList);
        }
        
        /** Removes leaves and branches passing the predicate test */
        public boolean pruneLeaves(Predicate<T> predicateOnValue) {
            if (isLeaf()) {
                if (value != null) {
                    return predicateOnValue.test(value);
                } else {
                    return true;
                }
            } else {
                Iterator<Entry<Object,Tree<T>>> it = 
                        children.entrySet().iterator();
                while(it.hasNext()) {
                    Entry<Object,Tree<T>> e = it.next();
                    if (e.getValue().pruneLeaves(predicateOnValue)) {
                        it.remove();
                    }
                }
                return children.isEmpty();
            }
        }
        
        /** Removes branches passing the predicate test */
        public void pruneBranches(Predicate<Object> predicateOnKey) {
            if (!isLeaf()) {
                Iterator<Entry<Object,Tree<T>>> it = 
                        children.entrySet().iterator();
                while(it.hasNext()) {
                    Entry<Object,Tree<T>> e = it.next();
                    Object key = e.getKey();
                    if (predicateOnKey.test(key)) {
                        it.remove();
                    } else {
                        final Tree<T> t = e.getValue();
                        t.pruneBranches(predicateOnKey);
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

        public T getValue() {
            return value;
        }
    }

    private static class Container<T> {
        private final Object[] params;
        private final T value;
        private final int hashcode;

        public Container(Object[] params, T value) {
            this.params = params;
            this.value = value;
            this.hashcode = Arrays.deepHashCode(params);
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
            return Arrays.deepEquals(this.params, other.params);
        }
    }
    
    private final Map<Object, Set<Container<T>>> map = new ConcurrentHashMap<>();
    private final List<Set<Object>> positionKeyList = new ArrayList<>();

    private static <T> Set<Container<T>> createNewSet() {
        return new SetWrapper<>();
    }
    
    private static <T> Set<Container<T>> createNewSet(
            Collection<Container<T>> coll) {
        return new SetWrapper<>(coll);
    }
    
    public void clear() {
        map.clear();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return values().size();
    }

    public Set<T> get(Collection<Object> keys) {
        return get(keys.toArray());
    }

    public Set<T> get(Object... keys) {
        Set<Container<T>> result = null;
        for (Object k : keys) {
            Set<Container<T>> set = map.get(k);
            if (set != null) {
                if (result == null) {
                    result = createNewSet(set);
                } else {
                    result.retainAll(set);
                }
            }
        }
        return result.stream()
                .map(s -> s.value).collect(Collectors.toSet());
    }

    public boolean add(T value, Object... keys) {
        recordKeyPositions(keys);
        Container container = new Container(keys, value);
        boolean added = false;
        for (Object k : keys) {
            Set<Container<T>> set = 
                    map.computeIfAbsent(k, key -> createNewSet());
            synchronized (set) {
                added |= set.add(container);
            }
        }
        return added;
    }

    public boolean remove(Object... keys) {
        boolean removed = false;
        Container<T> element = new Container<>(keys, null);
        for (Object key : keys) {
            Set<Container<T>> set = map.get(key);
            if (set != null) {
                removed |= set.remove(element);
            }
        }
        return removed;
    }

    public Set<T> values() {
        return map.values().stream()
                .flatMap(s -> s.stream())
                .map(c -> c.value)
                .collect(Collectors.toSet());
    }

    public Set<Object> keySet(Object... keys) {
        return map.keySet();
    }

    public Set<Object> keysAtIndex(int index) {
        return positionKeyList.get(index);
    }

    private final static Object NULL_KEY = new Object();
    /** It's not advisable to use more than 3 indexes */
    public Tree<T> treeFromIndexes(int... indexes) {
        Tree<T> root = createTree(List.of(), NULL_KEY, indexes, 0, 
                null);
        return root;
    }

    private Tree<T> createTree(
            List<Object> keys, Object key, 
            int[] indexes, int pos,
            Set<Container<T>> selection) {
        
        List<Object> keyList = new ArrayList<>(keys.size() + 1);
        keyList.addAll(keys);
        Set<Container<T>> currentSelection = null;
        if (key != NULL_KEY) {
            keyList.add(key);
            Set<Container<T>> keySelection = map.get(key);
            if (selection != null) {
                currentSelection = createNewSet(selection);
                currentSelection.retainAll(keySelection);
            } else {
                currentSelection = createNewSet(keySelection);
            }
            if (currentSelection == null || currentSelection.isEmpty()) {
                return null;
            }
        }
        final Set<Container<T>> currSelection = currentSelection;
        final boolean notLeaf = pos < indexes.length;
        if (notLeaf) {
            Set<Object> keySet = keysAtIndex(indexes[pos]);
            if (keySet.isEmpty()) {
                return null;
            }
            // cannot use TreeMap on unknown types
            Map<Object, Tree<T>> map = new HashMap<>(keySet.size());
            
            keySet.parallelStream().forEach(k -> {
                
                final Tree<T> t = createTree(keyList, k,
                        indexes, pos + 1, currSelection);
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
            if (currentSelection == null || currSelection.isEmpty()) {
                return null;
            }
            Set<T> set = currSelection.stream()
                    .map(c -> c.value)
                    .collect(Collectors.toSet());
            
            T value = currentSelection.iterator().next().value;
            
            if (set.size() > 1) {
                throw new AssertionError();
            }
            
            return new Tree<>(value, null, keyList);
        }
    }

    private void recordKeyPositions(Object[] keys) {
        for (int i = 0; i < keys.length; i++) {
            Set<Object> keySet;
            if (i >= positionKeyList.size()) {
                keySet = new HashSet<>();
                positionKeyList.add(keySet);
            } else {
                keySet = positionKeyList.get(i);
            }
            keySet.add(keys[i]);
        }
    }
}
