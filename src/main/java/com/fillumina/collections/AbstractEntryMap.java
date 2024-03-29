package com.fillumina.collections;

import com.fillumina.collections.AbstractEntryMap.InternalState;
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
 * <li>allows to specify a customized compliant {@link Map.Entry} implementation (remember to use
 * the same {@link Objects#hashCode()} as defined in {@link java.util.HashMap.Node#hashCode()}).
 * <li>has a public {@code forEach(Consumer<Entry>)} and
 * <i>protected</i> {@link #putEntry(java.util.Map.Entry) } and {@link #getEntry(Object) }.
 * <li>It's performances are O(1) for all operations (add, get, remove) but it's O(n) for the worst
 * case scenario (colliding keys) ({@link java.util.HashMap} has O(1) for insertion and O(n) for
 * extraction and removal on worst case scenario).
 * </ul>
 * To use a dedicated private object to contain each entry can be a waste of space. This class
 * allows to define a customized {@link Map.Entry} that can be returned to clients and used for
 * other purposes.
 *
 * @param <K> map key
 * @param <V> map value
 * @param <E> entry type, needed for {@link #getEntry(Object) }
 * @param <M> map type, needed for fluent methods like {@link #add(Object, Object)}
 * @param <S> internal state type, needed to define a different state
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public abstract class AbstractEntryMap<K, V, E extends Entry<K, V>, M extends Map<K, V>, S extends InternalState<E>>
        implements Map<K, V> {

    // MUST be a power of 2
    private static final int INITIAL_SIZE = 16;

    protected static Entry<?, ?> NULL_ENTRY = new SimpleImmutableEntry<>(null, null);

    /**
     * This is the status that is actually passed to the immutable implementation.
     *
     * @param <E>
     */
    public static class InternalState<E> {

        public E[] array;
        public int size = 0;
        public int mask;

        public InternalState() {}

        public InternalState(InternalState<E> other) {
            if (other != null) {
                if (other.array != null) {
                    this.array = other.array.clone();
                }
                this.size = other.size;
                this.mask = other.mask;
            }
        }
    }

    private volatile S state;
    private volatile Set<Entry<K, V>> entrySet; // cache it only if needed

    public AbstractEntryMap() {
        state = createNewInternalState();
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
        this(map.size());
        map.forEach((k, v) -> innerPut(k, v));
    }

    /**
     * Clone constructor.
     */
    @SuppressWarnings("unchecked")
    public AbstractEntryMap(
            AbstractEntryMap<? extends K, ? extends V, ? extends E, ? extends M, ? extends S> map) {
        this();
        final InternalState<? extends E> otherState = map.state;
        E[] otherArray = otherState.array;
        E[] array = null;
        if (otherArray != null) {
            array = (E[]) new Entry[otherArray.length];
            for (int i = 0, l = otherArray.length; i < l; i++) {
                if (otherArray[i] != null) {
                    E e = otherArray[i];
                    // this way you can copy from maps with different implementations of Entry
                    array[i] = createEntry(e.getKey(), e.getValue(), this.state);
                }
            }
        }
        this.state.array = array;
        this.state.mask = otherState.mask;
        this.state.size = otherState.size;
    }

    /**
     * View constructor. Be careful that using this constructor will give access to the same state
     * to both classes.
     */
    protected AbstractEntryMap(S internalState) {
        this.state = internalState;
    }

    protected AbstractEntryMap(List<?> list) {
        this(list.toArray());
    }

    @SuppressWarnings("unchecked")
    protected AbstractEntryMap(Object... array) {
        this(array.length);
        for (int i = 0, l = array.length; i < l; i += 2) {
            K k = (K) array[i];
            V v = (V) array[i + 1];
            // skip read-only check
            innerPut(k, v);
        }
    }

    /**
     * Implement your own {@link Map.Entry} by overriding this method.
     *
     * @param k
     * @param v
     * @return
     */
    protected abstract E createEntry(K k, V v, S internalState);

    /**
     * Needed for map resize. Don't bother to implement if the map is read only.
     */
    protected abstract AbstractEntryMap<K, V, E, M, S> createMap(int size);

    public boolean isReadOnly() {
        try {
            readOnlyCheck();
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        }
    }

    /**
     * Override to provide a read-only implementation by throwing an
     * {@link UnsupportedOperationException} exception.
     */
    protected void readOnlyCheck() throws UnsupportedOperationException {
        // do nothing
    }

    protected boolean isKeyEqualsToEntry(Object key, E e) {
        if (e == null) {
            return false;
        }
        return Objects.equals(key, e.getKey());
    }

    /** Get internal state */
    protected S getInternalState() {
        return state;
    }

    /** Get internal state clone */
    protected S getInternalStateClone() {
        return state;
    }

    /** Set modified state. */
    protected void setInternalState(S otherState) {
        Objects.requireNonNull(otherState, "state must be not null");
        this.state = otherState;
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

    //****************************
    // fluent interface methods
    //****************************
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

    /**
     * Checks if the entry with given key and value is present.
     */
    public boolean containsEntry(K k, V v) {
        E entry = getEntry(k);
        if (entry == null) {
            return false;
        }
        return Objects.equals(entry.getValue(), v);
    }

    public AbstractEntryMap<K, V, E, M, S> assertEntry(K k, V v) throws AssertionError {
        if (!containsEntry(k, v)) {
            throw new AssertionError("entry not present: key=" + k + " => value=" + v);
        }
        return this;
    }

    public AbstractEntryMap<K, V, E, M, S> assertSize(int size) throws AssertionError {
        if (size != size()) {
            throw new AssertionError("expected size=" + size + " but was " + size());
        }
        return this;
    }

    @Override
    public void clear() {
        readOnlyCheck();
        setInternalState(createNewInternalState());
    }

    @SuppressWarnings("unchecked")
    protected S createNewInternalState() {
        return (S) new InternalState<E>();
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
        return innerPutEntry(entry);
    }

    /**
     * Inserts a new entry. Substitutes the existing entry with the same key if exists.
     */
    protected E innerPutEntry(E entry) {
        final S internalState = getInternalStateClone();
        resizeCheck(internalState);
        E result = innerPutEntry(entry, internalState);
        setInternalState(internalState);
        return result;
    }

    /**
     * Inserts a new entry. Substitutes the existing entry with the same key if exists.
     */
    protected E innerPutEntry(E entry, S internalState) {
        // risizeCheck() must have been called already
        K key = entry.getKey();
        int idx = hash(key) & internalState.mask;
        E e;
        while ((e = internalState.array[idx]) != null) {
            if (isKeyEqualsToEntry(key, e)) {
                internalState.array[idx] = entry;
                setInternalState(internalState);
                return e;
            }
            idx = (idx + 1) & internalState.mask;
        }
        internalState.array[idx] = entry;
        internalState.size++;
        setInternalState(internalState);
        return null;
    }

    @Override
    public V put(K key, V value) {
        readOnlyCheck();
        return innerPut(key, value);
    }

    /**
     * Inserts a key and value possibly reusing an existing entry if it has the same key. This put
     * operation bypass {@link #readOnlyCheck()} check.
     */
    protected V innerPut(K key, V value) {
        final S internalState = getInternalStateClone();
        resizeCheck(internalState);
        int idx = hash(key) & internalState.mask;
        E e;
        while ((e = internalState.array[idx]) != null) {
            if (isKeyEqualsToEntry(key, e)) {
                V old = e.getValue();
                try {
                    e.setValue(value);
                } catch (UnsupportedOperationException ex) {
                    // some Entry implementations doesn't allow setting values, creates a new entry
                    internalState.array[idx] = createEntry(key, value, internalState);
                }
                setInternalState(internalState);
                return old;
            }
            idx = (idx + 1) & internalState.mask;
        }
        internalState.array[idx] = createEntry(key, value, internalState);
        internalState.size++;
        setInternalState(internalState);
        return null;
    }

    @SuppressWarnings("unchecked")
    protected void resizeCheck(S internalState) {
        if (internalState.array == null || internalState.array.length == 0) {
            internalState.array = (E[]) new Entry[INITIAL_SIZE];
            internalState.mask = internalState.array.length - 1;
        } else if (internalState.size > (internalState.array.length >> 1)) {
            // for performance reason always keep half the array empty
            resize(internalState.array.length << 1, internalState);
        }
    }

    protected void resize(int newSize, S internalState) {
        AbstractEntryMap<K, V, E, M, S> map = createMap(nextPowerOf2(newSize) >> 1);
        forEach(e -> map.putEntry(e));
        S mapState = map.getInternalState();
        internalState.array = mapState.array;
        internalState.size = mapState.size;
        internalState.mask = mapState.mask;
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
            put(key, value);
        }
        return value;
    }

    public E getEntry(Object key) {
        final S internalState = getInternalState();
        return innerGetEntry(key, internalState);
    }

    protected E innerGetEntry(Object key, S internalState) {
        if (key == null ||
                internalState.size == 0 ||
                internalState.array == null ||
                internalState.array.length == 0) {
            return null;
        }
        int hc = hash(key);
        int idx = hc & internalState.mask;
        do {
            E e = internalState.array[idx];
            if (e == null) {
                return null; //createEntry(null, null);
            }
            if (hc == hash(e.getKey()) && isKeyEqualsToEntry(key, e)) {
                return e;
            }
            idx = (idx + 1) & internalState.mask;
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
        final S internalState = getInternalStateClone();
        final Holder<E> removedEntry = new Holder<>();
        V oldValue = innerRemove(key, internalState, removedEntry);
        if (removedEntry.isPresent()) {
            setInternalState(internalState);
        }
        return oldValue;
    }

    // TODO remove the need to create an Holder for this
    protected V innerRemove(Object key, S internalState, Holder<E> removedEntry) {
        if (internalState.array == null || key == null) {
            return null;
        }
        int hc = hash(key);
        int idx = hc & internalState.mask;
        do {
            E e = internalState.array[idx];
            if (e == null) {
                return null;
            }
            K ekey = e.getKey();
            if (hc == hash(ekey)) {
                if (Objects.equals(ekey, key)) {
                    V result = e.getValue();
                    removeIndex(idx, internalState);
                    removedEntry.set(e);
                    return result;
                }
            }
            idx = (idx + 1) & internalState.mask;
        } while (true);
    }

    // used by BiMap
    @SuppressWarnings("unckecked")
    protected Entry<K, V> getEntryAtIndex(int idx) {
        final S internalState = getInternalState();
        return (Entry<K, V>) (internalState.array == null ? null : internalState.array[idx]);
    }

    protected void removeIndex(int idx, S internalState) {
        internalState.array[idx] = null;
        // relocate following entries until null
        do {
            idx = (idx + 1) & internalState.mask;
            E e = internalState.array[idx];
            if (e == null) {
                break;
            }
            internalState.array[idx] = null;
            relocateEntry(e, internalState);
        } while (true);
        internalState.size--;
    }

    private void relocateEntry(E entry, S internalState) {
        int idx = hash(entry.getKey()) & internalState.mask;
        while (internalState.array[idx] != null) {
            idx = (idx + 1) & internalState.mask;
        }
        internalState.array[idx] = entry;
    }

    public boolean removeAll(Collection<K> coll) {
        readOnlyCheck();
        final S internalState = getInternalStateClone();
        boolean removed = false;
        for (K k : coll) {
            final Holder<E> removedEntry = new Holder<>();
            innerRemove(k, internalState, removedEntry);
            removed |= removedEntry.isPresent();
        }
        if (removed) {
            setInternalState(internalState);
        }
        return removed;
    }

    public boolean retainAll(Collection<K> coll) {
        readOnlyCheck();
        AbstractEntryMap<K, V, E, M, S> tmap = createMap(size());
        for (K k : coll) {
            E e = getEntry(k);
            if (e != null) {
                tmap.putEntry(e);
            }
        }
        setInternalState(tmap.state);
        return !tmap.isEmpty();
    }

    public void forEach(Consumer<E> consumer) {
        final S internalState = getInternalState();
        if (internalState.size == 0 || internalState.array == null) {
            return;
        }
        final E[] internalStateArray = internalState.array;
        for (E e : internalStateArray) {
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
            public boolean contains(Object o) {
                Entry<K, V> entry = (Entry<K, V>) AbstractEntryMap.this.getEntry(((Entry<K, V>) o)
                        .getKey());
                return Objects.equals(o, entry);
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                final S internalState = getInternalStateClone();
                if (internalState.size == 0) {
                    return EmptyIterator.empty();
                }
                int i = 0;
                for (; i<internalState.array.length; i++) {
                    if (internalState.array[i] != null) {
                        break;
                    }
                }
                final int firstNonNullItem = i;
                return new Iterator<Entry<K, V>>() {
                    S istate = internalState;
                    int idx = firstNonNullItem;
                    int currentIdx = firstNonNullItem;
                    Entry<K, V> current;

                    @Override
                    public boolean hasNext() {
                        return idx < istate.array.length &&
                                istate.array[idx] != null;
                    }

                    @Override
                    public Entry<K, V> next() {
                        if (idx == istate.array.length) {
                            throw new NoSuchElementException();
                        }
                        current = istate.array[idx];
                        currentIdx = idx;
                        goToNextNonNullItem();
                        return current;
                    }

                    @Override
                    public void remove() {
                        readOnlyCheck();
                        // a little slower but plays nicer with extending classes
                        Entry<K, V> entry = getEntryAtIndex(currentIdx);
                        AbstractEntryMap.this.remove(entry.getKey());
                        istate = getInternalState();
                        //AbstractEntryMap.this.removeIndex(currentIdx);
                        idx = Math.max(0, currentIdx - 1);
                        goToNextNonNullItem();
                    }

                    private void goToNextNonNullItem() {
                        do {
                            idx++;
                        } while (idx < istate.array.length &&
                                istate.array[idx] == null);
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
        final S internalState = getInternalState();
        if (internalState.array == null) {
            return false;
        }
        Iterator<Entry<K, V>> i = entrySet().iterator();
        if (value == null) {
            for (E e : internalState.array) {
                if (e != null && e.getValue() == null) {
                    return true;
                }
            }
        } else {
            for (E e : internalState.array) {
                if (e != null && Objects.equals(value, e.getValue())) {
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
    private transient Set<K> keySet;
    private transient Collection<V> values;

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
        if (values != null) {
            return values;
        }
        return values = new AbstractCollection<V>() {
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
        final S internalState = getInternalState();
        if (internalState.array == null || internalState.array.length == 0) {
            return 0;
        }
        int h = 0;
        for (E e : internalState.array) {
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

        protected AbstractEntryMap<K, V, E, M, S> getMap() {
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
        public boolean containsAll(Collection<?> c) {
            return super.containsAll(c);
        }

        @Override
        @SuppressWarnings("unchecked")
        public boolean remove(Object key) {
            return AbstractEntryMap.this.remove((K) key) != null;
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
                private final Iterator<Entry<K, V>> it = entrySet().iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public K next() {
                    final Entry<K, V> next = it.next();
                    if (next != null) {
                        return next.getKey();
                    }
                    return null;
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }

    }

}
