package com.fillumina.collections;

import java.util.AbstractCollection;
import java.util.AbstractMap.SimpleEntry;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A {@link java.util.Map} implementation that offers various interesting
 * characteristics over standard {@link java.util.HashMap}:
 * <ul>
 * <li>It allows to specify a {@link Map.Entry} implementation
 * <li>It has {@link #putEntry(java.util.Map.Entry) } and 
 *     {@link #getEntry(Object) } methods
 * <li>It's extremely fast to clone (much faster than {@link java.util.HashMap})
 * allowing for a quite efficient CopyOnWriteMap implementation (which is a
 * great solution for rarely updating caches - faster than ConcurrentHashMap for
 * this scenario).
 * <li>It's average performances are O(1) for all operations (add, get, remove)
 * but it's O(n) for the worst case scenario (frequent keys collisions)
 * ({@link java.util.HashMap} manages it better with O(1) for insertion and O(n)
 * for extraction and removal on worst case scenario).
 * <li>Space efficient and because of the use of arrays it takes advantage of
 * memory locality, caching and read ahead
 * <li>read-only derived class available
 * <li>read-only view
 * <li>clone constructors
 * <li>Can be copied to other implementations of the same class very quickly
 * <li>Has a {@link #forEach(java.util.function.Consumer) } for entries
 * <li>Its keySet() can add elements (with null value)
 * </ul>
 *
 * @param <K> map key
 * @param <V> map value
 * @param <E> entry type, needed for {@link #getEntry(Object) }
 * @param <M> map type, needed for fluent methods like
 * {@link #add(Object, Object)}
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public abstract class AbstractEntryMap<K, V, E extends Entry<K, V>, M extends Map<K, V>>
        implements Map<K, V> {

    private static final int INITIAL_SIZE = 8;

    public static Iterator<?> NULL_ITERATOR = new Iterator<Object>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
    };

    public static Entry<?, ?> NULL_ENTRY =
            new SimpleImmutableEntry<>(null, null);

    /**
     * Full {@link java.util.Map} conform implementation. Useful for its very
     * fast {@link #clone() } operation.
     */
    public static class SimpleMap<K, V>
            extends AbstractEntryMap<K, V, Entry<K, V>, SimpleMap<K, V>> {

        public SimpleMap() {
            super();
        }

        public SimpleMap(int initialSize) {
            super(initialSize);
        }

        public SimpleMap(Map<K, V> map) {
            super(map);
        }

        /**
         * Copy constructor.
         */
        public SimpleMap(AbstractEntryMap<K, V, Entry<K, V>, SimpleMap<K, V>> map) {
            super(map);
        }

        @Override
        protected SimpleEntry<K, V> createEntry(K k, V v) {
            return new SimpleEntry<>(k, v);
        }

        @Override
        protected SimpleMap<K, V> createMap(int size) {
            return new SimpleMap<>(size);
        }

        /**
         * Very fast cloning of the map.
         */
        @Override
        public SimpleMap<K, V> clone() {
            return new SimpleMap<K, V>(this);
        }
    }

    /**
     * Full {@link java.util.Map} conform implementation. Useful for its very
     * fast {@link #clone() } operation. Differently from {@link SimpleMap} it allows
     * to add a {@link Map.Entry}.
     */
    public static class EntryMap<K, V>
            extends AbstractEntryMap<K, V, Entry<K, V>, SimpleMap<K, V>> {

        public EntryMap() {
            super();
        }

        public EntryMap(int initialSize) {
            super(initialSize);
        }

        public EntryMap(Map<K, V> map) {
            super(map);
        }

        /**
         * Copy constructor.
         */
        public EntryMap(AbstractEntryMap<K, V, Entry<K, V>, SimpleMap<K, V>> map) {
            super(map);
        }

        @Override
        public Entry<K, V> putEntry(Entry<K, V> entry) {
            return super.putEntry(entry);
        }
        
        @Override
        protected SimpleEntry<K, V> createEntry(K k, V v) {
            return new SimpleEntry<>(k, v);
        }

        @Override
        protected EntryMap<K, V> createMap(int size) {
            return new EntryMap<>(size);
        }

        /**
         * Very fast cloning of the map.
         */
        @Override
        public EntryMap<K, V> clone() {
            return new EntryMap<K, V>(this);
        }
    }

    /**
     * It's a {@link java.util.Map} implementation with the following features:
     * <ul>
     * <li>Differently to {@link SimpleMap} entries are read-only (cannot use
     * {@link Map.Entry#setValue(java.lang.Object)}). The map can be changed by
     * other usual methods.
     * <li>A read-only view of the map is available via {@link #getReadOnlyView() }
     * </ul>
     * This is a base for other types of maps, not really useful by itself.
     *
     * @param <K>
     * @param <V>
     */
    public static class VieweableMap<K, V>
            extends AbstractEntryMap<K, V, SimpleImmutableEntry<K, V>, VieweableMap<K, V>> {

        private ReadOnlyMap<K, V> readOnlyView;

        public VieweableMap() {
            super();
        }

        public VieweableMap(int initialSize) {
            super(initialSize);
        }

        public VieweableMap(Map<K, V> map) {
            super(map);
        }

        /**
         * Copy constructor.
         */
        public VieweableMap(
                AbstractEntryMap<K, V, SimpleImmutableEntry<K, V>, ?> map) {
            super(map);
        }

        protected VieweableMap(
                InternalState<SimpleImmutableEntry<K, V>> internalState) {
            super(internalState);
        }

        @Override
        protected SimpleImmutableEntry<K, V> createEntry(K k, V v) {
            return new SimpleImmutableEntry<>(k, v);
        }

        @Override
        protected VieweableMap<K, V> createMap(
                int size) {
            return new VieweableMap<>(size);
        }

        /**
         * @return a read-only view of this map. Every change to this map
         * reflects to the view. Concurrent access might result in unexpected
         * results.
         */
        public ReadOnlyMap<K, V> getReadOnlyView() {
            if (readOnlyView != null) {
                return readOnlyView;
            }
            return readOnlyView = new ReadOnlyMap<K, V>(getInternalState());
        }

        /**
         * Very fast cloning of the map.
         */
        @Override
        public VieweableMap<K, V> clone() {
            return new VieweableMap<K, V>(this);
        }
    }

    /**
     * It's a read only implementation of {@link java.util.Map}. It can be built
     * via the provided builder or by using one of its constructors.
     *
     * @param <K>
     * @param <V>
     */
    public static class ReadOnlyMap<K, V> extends VieweableMap<K, V> {

        public static <K, V> VieweableMap<K, V> builder() {
            return new VieweableMap<>();
        }

        /**
         * Classic {@code java.util} style copy constructor.
         */
        public ReadOnlyMap(Map<K, V> map) {
            super(map);
        }

        /**
         * Fast copy constructor.
         */
        public ReadOnlyMap(
                AbstractEntryMap<K, V, SimpleImmutableEntry<K, V>, ?> map) {
            super(map);
        }

        /**
         * Used for views.
         */
        protected ReadOnlyMap(
                InternalState<SimpleImmutableEntry<K, V>> internalState) {
            super(internalState);
        }

        @Override
        public void readOnlyCheck() {
            throw new UnsupportedOperationException("read only");
        }

        /**
         * Just returns this.
         */
        @Override
        public ReadOnlyMap<K, V> getReadOnlyView() {
            return this;
        }

        /**
         * Very fast read-only clone of the map (not a view anymore).
         */
        @Override
        public ReadOnlyMap<K, V> clone() {
            return new ReadOnlyMap<K, V>(this);
        }
    }

    public static class InternalState<E> {
        private E[] array;
        private int size = 0;
        private int mask;
    }

    private Set<Entry<K, V>> set;
    private InternalState<E> state;
    private int collisionCounter;

    public AbstractEntryMap() {
        super();
        state = new InternalState<>();
    }

    public AbstractEntryMap(int initialSize) {
        this();
        // by using a power of 2 as a size the expensive module operation
        // can be substituted by a very cheap bit masking.
        state.array = (E[]) new Entry[nextPowerOf2(initialSize << 1)];
        state.mask = state.array.length - 1;
    }

    // https://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2
    static int nextPowerOf2(int v) {
        v--;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v++;
        return v;
    }

    /**
     * Usual default {@code java.util} style copy constructor.
     */
    public AbstractEntryMap(Map<K, V> map) {
        this();
        map.forEach((k, v) -> innerPut(k, v));
    }

    /**
     * Very fast copy constructor.
     */
    public AbstractEntryMap(AbstractEntryMap<K, V, E, M> map) {
        this();
        this.state.array = map.state.array.clone();
        this.state.mask = map.state.mask;
        this.state.size = map.state.size;
    }

    /**
     * View constructor.
     */
    protected AbstractEntryMap(InternalState<E> internalState) {
        this.state = internalState;
    }

    /**
     * {@link #getEntry(java.lang.Object) } might require to create an entry
     * containing null (key=null, value=null). A null object can be used to
     * avoid {@link java.lang.NullPointerException}.
     *
     * @param k the key
     * @param v the value
     * @return the created {@link Map.Entry}.
     */
    protected abstract E createEntry(K k, V v);

    /**
     * Needed for map resize. Don't bother to implement if the map is read only.
     *
     * @param size the size of its internal array (should be at least twice the
     * expected data size)
     * @return a new instance of the map
     */
    protected abstract AbstractEntryMap<K, V, E, M> createMap(int size);

    /**
     * Override to provide a read-only implementation.
     */
    public void readOnlyCheck() {
        // do nothing
    }

    protected InternalState<E> getInternalState() {
        return state;
    }

    public int hash(Object key) {
        if (key == null) {
            return 0;
        }
        int h = key.hashCode();
        // copied from java.util.HashMap
        //return h ^ (h >>> 16);

        // https://stackoverflow.com/questions/9624963/java-simplest-integer-hash
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    public M add(K k, V v) {
        put(k, v);
        return (M) this;
    }

    public M add(Map.Entry<K, V> entry) {
        return add(entry.getKey(), entry.getValue());
    }

    public M addAll(Map<K, V> map) {
        map.forEach((k, v) -> put(k, v));
        return (M) this;
    }

    public int getCollisionCounter() {
        return collisionCounter;
    }

    public boolean containsEntry(K k, V v) {
        return v.equals(get(k));
    }
        
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new SimpleMap<>(this);
    }

    @Override
    public void clear() {
        readOnlyCheck();
        state.array = null;
        state.mask = 0;
        state.size = 0;
    }

    @Override
    public int size() {
        return state.size;
    }

    @Override
    public boolean isEmpty() {
        return state.size == 0;
    }

    protected E putEntry(E entry) {
        readOnlyCheck();
        resizeCheck();
        return innerPutEntry(entry);
    }

    protected E innerPutEntry(E entry) {
        K key = entry.getKey();
        int idx = hash(key) & state.mask;
        E e;
        while ((e = state.array[idx]) != null) {
            collisionCounter++;
            if (Objects.equals(key, e.getKey())) {
                state.array[idx] = entry;
                return e;
            }
            idx = (idx + 1) & state.mask;
        }
        state.array[idx] = entry;
        state.size++;
        return null;
    }

    @Override
    public V put(K key, V value) {
        readOnlyCheck();
        return innerPut(key, value);
    }

    /**
     * This put operation bypass {@link #readOnlyCheck()} check.
     */
    protected V innerPut(K key, V value) {
        resizeCheck();
        int idx = hash(key) & state.mask;
        E e;
        while ((e = state.array[idx]) != null) {
            collisionCounter++;
            if (Objects.equals(key, e.getKey())) {
                V old = e.getValue();
                try {
                    e.setValue(value);
                } catch (UnsupportedOperationException ex) {
                    state.array[idx] = createEntry(key, value);
                }
                return old;
            }
            idx = (idx + 1) & state.mask;
        }
        state.array[idx] = createEntry(key, value);
        state.size++;
        return null;
    }

    private void resizeCheck() {
        if (state.array == null || state.array.length == 0) {
            state.array = (E[]) new Entry[INITIAL_SIZE];
            state.mask = state.array.length - 1;
        } else if (state.size > (state.array.length >> 1)) {
            resize(state.array.length << 1);
        }
    }

    protected void resize(int newSize) {
        if (newSize > (state.size << 1)) {
            AbstractEntryMap<K, V, E, M> map =
                    createMap(nextPowerOf2(newSize) >> 1);
            if (!isEmpty()) {
                forEach(e -> map.innerPutEntry(e));
            }
            this.state = map.state;
        }
    }

    private void relocateEntry(E entry) {
        int idx = hash(entry.getKey()) & state.mask;
        while (state.array[idx] != null) {
            idx = (idx + 1) & state.mask;
        }
        state.array[idx] = entry;
    }

    @Override
    public V get(Object key) {
        Entry<K, V> entry = getEntry(key);
        return entry == null ? null : entry.getValue();
    }

    public V getOrCreate(K key, Supplier<V> creator) {
        V value = get(key);
        if (value == null) {
            value = creator.get();
            put(key, value);
        }
        return value;
    }
    
    public E getEntry(Object key) {
        if (state.array == null || key == null) {
            return createEntry(null, null);
        }
        int hc = hash(key);
        int idx = hc & state.mask;
        do {
            E e = state.array[idx];
            if (e == null) {
                return null; //createEntry(null, null);
            }
            if (hc == hash(e.getKey()) &&
                    Objects.equals(key, e.getKey())) {
                return e;
            }
            idx = (idx + 1) & state.mask;
        } while (true);
    }

    protected boolean removeEntry(E entry) {
        return remove(entry.getKey()) != null;
    }

    @Override
    public boolean containsKey(Object key) {
        // default AbstractMap implementation is very inefficient
        return getEntry(key) != null;
    }

    @Override
    public V remove(Object key) {
        readOnlyCheck();
        if (state.array == null || key == null) {
            return null;
        }
        int hc = hash(key);
        int idx = hc & state.mask;
        do {
            E e = state.array[idx];
            if (e == null) {
                return null;
            }
            K ekey = e.getKey();
            if (hc == hash(ekey)) {
                if (Objects.equals(ekey, key)) {
                    V result = e.getValue();
                    removeIndex(idx);
                    return result;
                }
            }
            idx = (idx + 1) & state.mask;
        } while (true);
    }

    private void removeIndex(int idx) {
        state.array[idx] = null;
        // relocate following entries until null
        do {
            idx = (idx + 1) & state.mask;
            E e = state.array[idx];
            if (e == null) {
                break;
            }
            state.array[idx] = null;
            relocateEntry(e);
        } while (true);
        state.size--;
    }
    
    public boolean removeAll(Collection<K> coll) {
        boolean removed = false;
        for (K k : coll) {
            removed |= (remove(k) != null);
        }
        return removed;
    }

    public boolean retainAll(Collection<K> coll) {
        AbstractEntryMap<K,V,E,M> tmap = createMap(size());
        for (K k : coll) {
            E e = getEntry(k);
            if (e != null) {
                tmap.putEntry(e);
            }
        }
        this.state = tmap.state;
        return !tmap.isEmpty();
    }
    
    public void forEach(Consumer<E> consumer) {
        if (isEmpty()) {
            return;
        }
        for (E e : state.array) {
            if (e != null) {
                consumer.accept(e);
            }
        }
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (set != null) {
            return set;
        }
        return set = new AbstractSet<Entry<K, V>>() {

            @Override
            public int size() {
                return AbstractEntryMap.this.size();
            }

            @Override
            public void clear() {
                AbstractEntryMap.this.clear();
            }

            @Override
            public boolean isEmpty() {
                return AbstractEntryMap.this.isEmpty();
            }

            @Override
            public boolean remove(Object o) {
                return AbstractEntryMap.this.removeEntry(((E) o));
            }

            @Override
            public boolean add(Entry<K, V> e) {
                return AbstractEntryMap.this.putEntry((E) e) == null;
            }

            @Override
            public boolean contains(Object o) {
                return AbstractEntryMap.this.getEntry(o) != null;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                if (state.size == 0) {
                    return (Iterator<Entry<K, V>>) NULL_ITERATOR;
                }
                int i = 0;
                while (state.array[i] == null) {
                    i++;
                };
                final int firstNonNullItem = i;
                return new Iterator<Entry<K, V>>() {
                    int idx = firstNonNullItem;
                    int currentIdx = firstNonNullItem;
                    Entry<K, V> current;

                    @Override
                    public boolean hasNext() {
                        return idx < state.array.length &&
                                state.array[idx] != null;
                    }

                    @Override
                    public Entry<K, V> next() {
                        if (idx == state.array.length) {
                            throw new NoSuchElementException();
                        }
                        current = state.array[idx];
                        currentIdx = idx;
                        goToNextNonNullItem();
                        return current;
                    }

                    @Override
                    public void remove() {
                        readOnlyCheck();
                        AbstractEntryMap.this.removeIndex(currentIdx);
                        idx = Math.max(0, currentIdx - 1);
                        goToNextNonNullItem();
                    }

                    private void goToNextNonNullItem() {
                        do {
                            idx++;
                        } while (idx < state.array.length &&
                                state.array[idx] == null);
                    }
                };

            }
        };
    }

    // COPIED FROM AbstractMap<K,V>
    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation iterates over {@code entrySet()} searching
     * for an entry with the specified value. If such an entry is found,
     * {@code true} is returned. If the iteration terminates without finding
     * such an entry, {@code false} is returned. Note that this implementation
     * requires linear time in the size of the map.
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean containsValue(Object value) {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (value == null) {
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                if (e.getValue() == null) {
                    return true;
                }
            }
        } else {
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                if (value.equals(e.getValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    // Bulk Operations
    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation iterates over the specified map's
     * {@code entrySet()} collection, and calls this map's {@code put} operation
     * once for each entry returned by the iteration.
     *
     * <p>
     * Note that this implementation throws an
     * {@code UnsupportedOperationException} if this map does not support the
     * {@code put} operation and the specified map is nonempty.
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    // Views
    /**
     * Each of these fields are initialized to contain an instance of the
     * appropriate view the first time this view is requested. The views are
     * stateless, so there's no reason to create more than one of each.
     *
     * <p>
     * Since there is no synchronization performed while accessing these fields,
     * it is expected that java.util.Map view classes using these fields have no
     * non-final fields (or any fields at all except for outer-this). Adhering
     * to this rule would make the races on these fields benign.
     *
     * <p>
     * It is also imperative that implementations read the field only once, as
     * in:
     *
     * <pre> {@code
     * public Set<K> keySet() {
     *   Set<K> ks = keySet;  // single racy read
     *   if (ks == null) {
     *     ks = new KeySet();
     *     keySet = ks;
     *   }
     *   return ks;
     * }
     *}</pre>
     */
    transient Set<K> keySet;
    transient Collection<V> values;

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation returns a set that subclasses
     * {@link AbstractSet}. The subclass's iterator method returns a "wrapper
     * object" over this map's {@code entrySet()} iterator. The {@code size}
     * method delegates to this map's {@code size} method and the
     * {@code contains} method delegates to this map's {@code containsKey}
     * method.
     *
     * <p>
     * The set is created the first time this method is called, and returned in
     * response to all subsequent calls. No synchronization is performed, so
     * there is a slight chance that multiple calls to this method will not all
     * return the same set.
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec This implementation returns a collection that subclasses {@link
     * AbstractCollection}. The subclass's iterator method returns a "wrapper
     * object" over this map's {@code entrySet()} iterator. The {@code size}
     * method delegates to this map's {@code size} method and the
     * {@code contains} method delegates to this map's {@code containsValue}
     * method.
     *
     * <p>
     * The collection is created the first time this method is called, and
     * returned in response to all subsequent calls. No synchronization is
     * performed, so there is a slight chance that multiple calls to this method
     * will not all return the same collection.
     */
    public Collection<V> values() {
        Collection<V> vals = values;
        if (vals == null) {
            vals = new AbstractCollection<V>() {
                public Iterator<V> iterator() {
                    return new Iterator<V>() {
                        private Iterator<Entry<K, V>> i = entrySet().iterator();

                        public boolean hasNext() {
                            return i.hasNext();
                        }

                        public V next() {
                            return i.next().getValue();
                        }

                        public void remove() {
                            i.remove();
                        }
                    };
                }

                public int size() {
                    return AbstractEntryMap.this.size();
                }

                public boolean isEmpty() {
                    return AbstractEntryMap.this.isEmpty();
                }

                public void clear() {
                    AbstractEntryMap.this.clear();
                }

                public boolean contains(Object v) {
                    return AbstractEntryMap.this.containsValue(v);
                }
            };
            values = vals;
        }
        return vals;
    }

    // Comparison and hashing
    /**
     * Compares the specified object with this map for equality. Returns
     * {@code true} if the given object is also a map and the two maps represent
     * the same mappings. More formally, two maps {@code m1} and {@code m2}
     * represent the same mappings if
     * {@code m1.entrySet().equals(m2.entrySet())}. This ensures that the
     * {@code equals} method works properly across different implementations of
     * the {@code Map} interface.
     *
     * @implSpec This implementation first checks if the specified object is
     * this map; if so it returns {@code true}. Then, it checks if the specified
     * object is a map whose size is identical to the size of this map; if not,
     * it returns {@code false}. If so, it iterates over this map's
     * {@code entrySet} collection, and checks that the specified map contains
     * each mapping that this map contains. If the specified map fails to
     * contain such a mapping, {@code false} is returned. If the iteration
     * completes, {@code true} is returned.
     *
     * @param o object to be compared for equality with this map
     * @return {@code true} if the specified object is equal to this map
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof Map)) {
            return false;
        }
        Map<?, ?> m = (Map<?, ?>) o;
        if (m.size() != size()) {
            return false;
        }

        try {
            for (Entry<K, V> e : entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key))) {
                        return false;
                    }
                } else {
                    if (!value.equals(m.get(key))) {
                        return false;
                    }
                }
            }
        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }

    /**
     * Returns the hash code value for this map. The hash code of a map is
     * defined to be the sum of the hash codes of each entry in the map's
     * {@code entrySet()} view. This ensures that {@code m1.equals(m2)} implies
     * that {@code m1.hashCode()==m2.hashCode()} for any two maps {@code m1} and
     * {@code m2}, as required by the general contract of
     * {@link Object#hashCode}.
     *
     * @implSpec This implementation iterates over {@code entrySet()}, calling
     * {@link Map.Entry#hashCode hashCode()} on each element (entry) in the set,
     * and adding up the results.
     *
     * @return the hash code value for this map
     * @see Map.Entry#hashCode()
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    @Override
    public int hashCode() {
        int h = 0;
        for (Entry<K, V> entry : entrySet()) {
            h += entry.hashCode();
        }
        return h;
    }

    /**
     * Returns a string representation of this map. The string representation
     * consists of a list of key-value mappings in the order returned by the
     * map's {@code entrySet} view's iterator, enclosed in braces
     * ({@code "{}"}). Adjacent mappings are separated by the characters
     * {@code ", "} (comma and space). Each key-value mapping is rendered as the
     * key followed by an equals sign ({@code "="}) followed by the associated
     * value. Keys and values are converted to strings as by
     * {@link String#valueOf(Object)}.
     *
     * @return a string representation of this map
     */
    @Override
    public String toString() {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (!i.hasNext()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (;;) {
            Entry<K, V> e = i.next();
            K key = e.getKey();
            V value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',').append(' ');
        }
    }

    protected class KeySet extends AbstractSet<K> {

        public KeySet() {
        }

        public KeySet(Collection<K> set) {
            addAll(set);
        }

        protected AbstractEntryMap<K,V,E,M> getMap() {
            return AbstractEntryMap.this;
        }
        
        @Override
        public boolean removeAll(Collection<?> c) {
            return AbstractEntryMap.this.removeAll((Collection<K>)c);
        }
        
        @Override
        public boolean retainAll(Collection<?> c) {
            return AbstractEntryMap.this.retainAll((Collection<K>)c);
        }

        @Override
        public boolean addAll(Collection<? extends K> c) {
            return super.addAll(c);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return super.containsAll(c);
        }

        @Override
        public boolean remove(Object key) {
            return AbstractEntryMap.this.remove((K) key) != null;
        }

        @Override
        public boolean add(K e) {
            return AbstractEntryMap.this.put(e, null) == null;
        }

        @Override
        public int size() {
            return AbstractEntryMap.this.size();
        }

        @Override
        public boolean isEmpty() {
            return AbstractEntryMap.this.isEmpty();
        }

        @Override
        public void clear() {
            AbstractEntryMap.this.clear();
        }

        @Override
        public boolean contains(Object k) {
            return AbstractEntryMap.this.containsKey(k);
        }

        @Override
        public Iterator<K> iterator() {
            return new Iterator<K>() {
                private final Iterator<Entry<K, V>> i = entrySet().iterator();

                @Override
                public boolean hasNext() {
                    return i.hasNext();
                }

                @Override
                public K next() {
                    final Entry<K, V> next = i.next();
                    return next.getKey();
                }

                @Override
                public void remove() {
                    i.remove();
                }
            };
        }

    }

}
