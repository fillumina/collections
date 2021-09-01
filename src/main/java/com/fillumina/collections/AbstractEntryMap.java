package com.fillumina.collections;

import java.util.AbstractCollection;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A very extendable hash-map {@link java.util.Map} implementation.
 * <ul>
 * <li>allows to specify a customized {@link Map.Entry} implementation
 * <li>has a {@code public} {@code forEach(Consumer<Entry>)} and
 * <i>{@code protected}</i> {@link #putEntry(java.util.Map.Entry) } and {@link #getEntry(Object) }
 * and can add entries with its {@code keySet}.
 * <li>It's performances are O(1) for all operations (add, get, remove) but it's O(n) for the worst
 * case scenario (colliding keys) ({@link java.util.HashMap} has O(1) for insertion and O(n) for
 * extraction and removal on worst case scenario).
 * </ul>
 *
 * @param <K> map key
 * @param <V> map value
 * @param <E> entry type, needed for {@link #getEntry(Object) }
 * @param <M> map type, needed for fluent methods like {@link #add(Object, Object)}
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public abstract class AbstractEntryMap<K, V, E extends Entry<K, V>, M extends Map<K, V>>
        implements Map<K, V> {

    private static final int INITIAL_SIZE = 8;

    protected static Entry<?, ?> NULL_ENTRY = new SimpleImmutableEntry<>(null, null);

    /**
     * This is the status that is actually passed to the immutable implementation.
     * @param <E>
     */
    public static class InternalState<E> {

        private E[] array;
        private int size = 0;
        private int mask;
    }

    private Set<Entry<K, V>> entrySet;
    private InternalState<E> state;

    public AbstractEntryMap() {
        super();
        state = new InternalState<>();
    }

    @SuppressWarnings("unchecked")
    public AbstractEntryMap(int initialSize) {
        this();
        // by using a power of 2 as a size the expensive module operation
        // can be substituted by a very cheap bit masking.
        final int size = nextPowerOf2(initialSize) << 1;
        state.array = (E[]) new Entry[size];
        state.mask = size - 1;
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
     * Copy constructor.
     */
    public AbstractEntryMap(Map<? extends K, ? extends V> map) {
        this();
        map.forEach((k, v) -> innerPut(k, v));
    }

    /**
     * Copy constructor.
     */
    @SuppressWarnings("unchecked")
    public AbstractEntryMap(
            AbstractEntryMap<? extends K, ? extends V, ? extends E, ? extends M> map) {
        this();
        E[] otherArray = map.state.array;
        E[] array = null;
        if (otherArray != null) {
            array = (E[]) new Entry[otherArray.length];
            for (int i = 0, l = otherArray.length; i < l; i++) {
                if (otherArray[i] != null) {
                    E e = otherArray[i];
                    array[i] = createEntry(e.getKey(), e.getValue());
                }
            }
        }
        this.state.array = array;
        this.state.mask = map.state.mask;
        this.state.size = map.state.size;
    }

    /**
     * View constructor. Be careful that using this constructor will give access to the same
     * state to both classes.
     */
    protected AbstractEntryMap(InternalState<E> internalState) {
        this.state = internalState;
    }

    protected AbstractEntryMap(List<?> list) {
        this(list.toArray());
    }

    @SuppressWarnings("unchecked")
    protected AbstractEntryMap(Object... array) {
        this();
        for (int i=0,l=array.length; i<l; i+=2) {
            K k = (K) array[i];
            V v = (V) array[i+1];
            // skip read-only check
            innerPut(k,v);
        }
    }

    protected abstract E createEntry(K k, V v);

    /**
     * Needed for map resize. Don't bother to implement if the map is read only.
     */
    protected abstract AbstractEntryMap<K, V, E, M> createMap(int size);

    /**
     * Override to provide a read-only implementation.
     */
    protected void readOnlyCheck() {
        // do nothing
    }

    protected boolean isKeyEqualsToEntry(Object key, E e) {
        return Objects.equals(key, e.getKey());
    }

    protected InternalState<E> getInternalState() {
        return state;
    }

    protected int hash(Object key) {
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

    // fluent interface methods
    @SuppressWarnings("unchecked")
    public M add(K k, V v) {
        put(k, v);
        return (M) this;
    }

    public M add(Map.Entry<K, V> entry) {
        return add(entry.getKey(), entry.getValue());
    }

    @SuppressWarnings("unchecked")
    public M addAll(Map<K, V> map) {
        map.forEach((k, v) -> put(k, v));
        return (M) this;
    }

    public boolean containsEntry(K k, V v) {
        E entry = getEntry(k);
        if (entry == null) {
            return false;
        }
        return Objects.equals(entry.getValue(), v);
    }

    public AbstractEntryMap<K, V, E, M> assertEntry(K k, V v) throws AssertionError {
        if (!containsEntry(k, v)) {
            throw new AssertionError("entry not present: key=" + k + " => value=" + v);
        }
        return this;
    }

    public AbstractEntryMap<K, V, E, M> assertSize(int size) throws AssertionError {
        if (size != size()) {
            throw new AssertionError("expected size=" + size + " but was " + size());
        }
        return this;
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
        return size() == 0;
    }

    protected E putEntry(E entry) {
        readOnlyCheck();
        resizeCheck();
        return innerPutEntry(entry);
    }

    /**
     * Substitutes the existing entry with the same key if exists.
     */
    protected E innerPutEntry(E entry) {
        // risizeCheck() must have been called already
        K key = entry.getKey();
        int idx = hash(key) & state.mask;
        E e;
        while ((e = state.array[idx]) != null) {
            if (isKeyEqualsToEntry(key, e)) {
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
            if (isKeyEqualsToEntry(key, e)) {
                V old = e.getValue();
                try {
                    e.setValue(value);
                } catch (UnsupportedOperationException ex) {
                    // some Entry implementation doesn't allow setting values, creates a new entry
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

    @SuppressWarnings("unchecked")
    private void resizeCheck() {
        if (state.array == null || state.array.length == 0) {
            state.array = (E[]) new Entry[INITIAL_SIZE];
            state.mask = state.array.length - 1;
        } else if (state.size > (state.array.length >> 1)) {
            resize(state.array.length << 1);
        }
    }

    protected void resize(int newSize) {
        AbstractEntryMap<K, V, E, M> map = createMap(nextPowerOf2(newSize) >> 1);
        forEach(e -> map.innerPutEntry(e));
        this.state = map.state;
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

    public V getOrCreate(K key, V value) {
        V v = get(key);
        if (v == null) {
            v = value;
            put(key, v);
        }
        return v;
    }

    public E getEntry(Object key) {
        if (state.array == null || key == null) {
            return null;
        }
        int hc = hash(key);
        int idx = hc & state.mask;
        do {
            E e = state.array[idx];
            if (e == null) {
                return null; //createEntry(null, null);
            }
            if (hc == hash(e.getKey()) && isKeyEqualsToEntry(key, e)) {
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

    protected void removeIndex(int idx) {
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
        AbstractEntryMap<K, V, E, M> tmap = createMap(size());
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
        if (entrySet != null) {
            return entrySet;
        }
        return entrySet = new AbstractSet<Entry<K, V>>() {

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
            @SuppressWarnings("unchecked")
            public boolean remove(Object o) {
                return AbstractEntryMap.this.removeEntry(((E) o));
            }

            @Override
            @SuppressWarnings("unchecked")
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
                    return EmptyIterator.empty();
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
     * @implSpec This implementation iterates over {@code entrySet()} searching for an entry with
     * the specified value. If such an entry is found, {@code true} is returned. If the iteration
     * terminates without finding such an entry, {@code false} is returned. Note that this
     * implementation requires linear time in the size of the map.
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    public boolean containsValue(Object value) {
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (value == null) {
            for (E e : state.array) {
                if (e != null && e.getValue() == null) {
                    return true;
                }
            }
        } else {
            for (E e : state.array) {
                if (e != null && value.equals(e.getValue() == null)) {
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
     * @implSpec This implementation iterates over the specified map's {@code entrySet()}
     * collection, and calls this map's {@code put} operation once for each entry returned by the
     * iteration.
     *
     * <p>
     * Note that this implementation throws an {@code UnsupportedOperationException} if this map
     * does not support the {@code put} operation and the specified map is nonempty.
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
     * Each of these fields are initialized to contain an instance of the appropriate view the first
     * time this view is requested. The views are stateless, so there's no reason to create more
     * than one of each.
     *
     * <p>
     * Since there is no synchronization performed while accessing these fields, it is expected that
     * java.util.Map view classes using these fields have no non-final fields (or any fields at all
     * except for outer-this). Adhering to this rule would make the races on these fields benign.
     *
     * <p>
     * It is also imperative that implementations read the field only once, as in:
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
     * @implSpec This implementation returns a set that subclasses {@link AbstractSet}. The
     * subclass's iterator method returns a "wrapper object" over this map's {@code entrySet()}
     * iterator. The {@code size} method delegates to this map's {@code size} method and the
     * {@code contains} method delegates to this map's {@code containsKey} method.
     *
     * <p>
     * The set is created the first time this method is called, and returned in response to all
     * subsequent calls. No synchronization is performed, so there is a slight chance that multiple
     * calls to this method will not all return the same set.
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
     * AbstractCollection}. The subclass's iterator method returns a "wrapper object" over this
     * map's {@code entrySet()} iterator. The {@code size} method delegates to this map's
     * {@code size} method and the {@code contains} method delegates to this map's
     * {@code containsValue} method.
     *
     * <p>
     * The collection is created the first time this method is called, and returned in response to
     * all subsequent calls. No synchronization is performed, so there is a slight chance that
     * multiple calls to this method will not all return the same collection.
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
     * Compares the specified object with this map for equality. Returns {@code true} if the given
     * object is also a map and the two maps represent the same mappings. More formally, two maps
     * {@code m1} and {@code m2} represent the same mappings if
     * {@code m1.entrySet().equals(m2.entrySet())}. This ensures that the {@code equals} method
     * works properly across different implementations of the {@code Map} interface.
     *
     * @implSpec This implementation first checks if the specified object is this map; if so it
     * returns {@code true}. Then, it checks if the specified object is a map whose size is
     * identical to the size of this map; if not, it returns {@code false}. If so, it iterates over
     * this map's {@code entrySet} collection, and checks that the specified map contains each
     * mapping that this map contains. If the specified map fails to contain such a mapping,
     * {@code false} is returned. If the iteration completes, {@code true} is returned.
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
     * Returns the hash code value for this map. The hash code of a map is defined to be the sum of
     * the hash codes of each entry in the map's {@code entrySet()} view. This ensures that
     * {@code m1.equals(m2)} implies that {@code m1.hashCode()==m2.hashCode()} for any two maps
     * {@code m1} and {@code m2}, as required by the general contract of {@link Object#hashCode}.
     *
     * @implSpec This implementation iterates over {@code entrySet()}, calling
     * {@link Map.Entry#hashCode hashCode()} on each element (entry) in the set, and adding up the
     * results.
     *
     * @return the hash code value for this map
     * @see Map.Entry#hashCode()
     * @see Object#equals(Object)
     * @see Set#equals(Object)
     */
    @Override
    public int hashCode() {
        int h = 0;
        for (E e : state.array) {
            if (e != null) {
                h += e.hashCode();
            }
        }
        return h;
    }

    /**
     * Returns a string representation of this map. The string representation consists of a list of
     * key-value mappings in the order returned by the map's {@code entrySet} view's iterator,
     * enclosed in braces ({@code "{}"}). Adjacent mappings are separated by the characters
     * {@code ", "} (comma and space). Each key-value mapping is rendered as the key followed by an
     * equals sign ({@code "="}) followed by the associated value. Keys and values are converted to
     * strings as by {@link String#valueOf(Object)}.
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

        protected AbstractEntryMap<K, V, E, M> getMap() {
            return AbstractEntryMap.this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean removeAll(Collection<?> c) {
            return AbstractEntryMap.this.removeAll((Collection<K>) c);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean retainAll(Collection<?> c) {
            return AbstractEntryMap.this.retainAll((Collection<K>) c);
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
        @SuppressWarnings("unchecked")
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
