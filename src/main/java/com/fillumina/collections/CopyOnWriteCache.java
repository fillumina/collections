package com.fillumina.collections;

import com.fillumina.collections.CopyOnWriteCache.CowInternalState;
import com.fillumina.collections.CopyOnWriteCache.LinkedEntry;
import java.util.AbstractList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * It's a concurrent cache of fixed size: whenever a new item is added the least accessed one gets
 * removed. Its elements cannot be removed.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class CopyOnWriteCache<K,V>
        extends AbstractEntryMap<K, V, LinkedEntry<K, V>, CopyOnWriteCache<K, V>, CowInternalState<K,V>> {

    public static class LinkedEntry<K,V> extends SimpleEntry<K,V> {
        private LinkedEntry<K,V> head, tail;

        public LinkedEntry(K key, V value) {
            super(key, value);
        }

        public LinkedEntry(
                Entry<? extends K, ? extends V> entry) {
            super(entry);
        }

        @Override
        public synchronized V setValue(V value) {
            return super.setValue(value);
        }
    }

    public static class CowInternalState<K,V> extends InternalState<LinkedEntry<K,V>> {
        LinkedEntry<K,V> head, tail;

        public CowInternalState() {
        }

        public CowInternalState(CowInternalState<K, V> other) {
            super(other);
            this.head = other.head;
            this.tail = other.tail;
        }
    }


    public CopyOnWriteCache() {
        super();
    }

    /**
     * @param maxSize max number of cached elements before dropping the least used one
     *                (actually is maxSize + 1)
     */
    public CopyOnWriteCache(int maxSize) {
        super(maxSize);
    }

    public CopyOnWriteCache(Map<? extends K, ? extends V> map) {
        super(map);
    }

    public CopyOnWriteCache(
            AbstractEntryMap<? extends K, ? extends V, ? extends LinkedEntry<K, V>, ? extends CopyOnWriteCache<K, V>, ? extends CowInternalState<K,V>> map) {
        super(map);
    }

    public CopyOnWriteCache(InternalState<LinkedEntry<K, V>> internalState) {
        super(internalState);
    }

    public CopyOnWriteCache(List<?> list) {
        super(list);
    }

    public CopyOnWriteCache(Object... array) {
        super(array);
    }

    @Override
    protected CowInternalState<K,V> createNewInternalState() {
        return new CowInternalState<>();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    protected void readOnlyCheck() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected LinkedEntry<K, V> createEntry(K k, V v, CowInternalState<K,V> internalState) {
        CowInternalState<K,V> iState = (CowInternalState<K,V>) internalState;
        final LinkedEntry<K, V> entry = new LinkedEntry<>(k, v);
        if (iState.tail == null) {
            iState.tail = entry;
        }
        if (iState.head != null) {
            iState.head.head = entry;
        }
        entry.tail = iState.head;
        iState.head = entry;
        return entry;
    }

    @Override
    protected AbstractEntryMap<K, V, LinkedEntry<K, V>, CopyOnWriteCache<K, V>, CowInternalState<K,V>> createMap(
            int size) {
        return new CopyOnWriteCache<>(size);
    }

    @Override
    protected synchronized void setInternalState(CowInternalState<K,V> otherState) {
        super.setInternalState(otherState);
    }

    @Override
    protected CowInternalState<K,V> getInternalStateClone() {
        return new CowInternalState<>(getInternalState());
    }

    @Override
    protected CowInternalState<K,V> getInternalState() {
        return (CowInternalState<K,V>) super.getInternalState();
    }

    @Override
    public LinkedEntry<K, V> getEntry(Object key) {
        CowInternalState<K,V> internalState = getInternalState();
        final LinkedEntry<K, V> entry = super.innerGetEntry(key, internalState);
        if (entry != null) {
            internalState.tail = entry.head;
            if (entry.head != null) {
                entry.head.tail = entry.tail;
            }
            if (entry.tail != null) {
                entry.tail.head = entry.head;
            }
            entry.head = null;
            entry.tail = internalState.head;
            internalState.head.head = entry;
            internalState.head = entry;
        }
        return entry;
    }

    @Override
    public synchronized V put(K key, V value) {
        return innerPut(key, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void resize(int newSize, CowInternalState internalState) {
        // removes the least used entry
        LinkedEntry<K, V> toRemove = internalState.tail;
        Holder<LinkedEntry<K, V>> removedEntry = new Holder<>();
        innerRemove(toRemove.getKey(), internalState, removedEntry);
        if (removedEntry.isPresent()) {
            internalState.tail = toRemove.head;
            if (toRemove.head != null) {
                toRemove.head.tail = null;
            }
        }
    }

    public List<LinkedEntry<K, V>> getOrderedEntryList() {
        return new AbstractList<LinkedEntry<K, V>>() {
            @Override
            public LinkedEntry<K, V> get(int index) {
                ListIterator<LinkedEntry<K, V>> it = listIterator();
                for (int i=0; i<index-1; i++) {
                    it.next();
                }
                return it.next();
            }

            @Override
            public int size() {
                return CopyOnWriteCache.this.size();
            }

            @Override
            public Iterator<LinkedEntry<K, V>> iterator() {
                return listIterator();
            }

            @Override
            public ListIterator<LinkedEntry<K, V>> listIterator() {
                final CowInternalState<K,V> internalState = getInternalState();
                return new ListIterator<LinkedEntry<K, V>>() {
                    int index = -1;
                    LinkedEntry<K, V> current = internalState.head;

                    @Override
                    public boolean hasNext() {
                        return current != null && current.tail != null;
                    }

                    @Override
                    public LinkedEntry<K, V> next() {
                        if (index != -1) {
                            current = current.tail;
                        }
                        index++;
                        return current;
                    }

                    @Override
                    public boolean hasPrevious() {
                        return index >= 0 && current != null && current.head != null;
                    }

                    @Override
                    public LinkedEntry<K, V> previous() {
                        current = current.head;
                        index--;
                        return current;
                    }

                    @Override
                    public int nextIndex() {
                        return index + 1;
                    }

                    @Override
                    public int previousIndex() {
                        return index - 1;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void set(LinkedEntry<K, V> e) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void add(LinkedEntry<K, V> e) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                };
            }

        };
    }
}
