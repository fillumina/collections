package com.fillumina.collections;

import com.fillumina.collections.KeyOnlyMap.KeyOnlyEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A container for its key set, which is a very usable set implementation.
 * 
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class KeyOnlyMap<T>
        extends AbstractSimpleMap<T,Boolean,KeyOnlyEntry<T>,KeyOnlyMap<T>> {
    
    public static class KeyOnlyEntry<T> implements Map.Entry<T,Boolean> {
        private final T value;

        public KeyOnlyEntry(T value) {
            this.value = value;
        }
        
        @Override
        public T getKey() {
            return value;
        }

        @Override
        public Boolean getValue() {
            return true;
        }

        @Override
        public Boolean setValue(Boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.value);
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
            final KeyOnlyEntry<?> other = (KeyOnlyEntry<?>) obj;
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }
    }

    public static <T> Set<T> createSet() {
        return new KeyOnlyMap<T>().keySet();
    }

    public static <T> Set<T> createSet(int size) {
        return new KeyOnlyMap<T>(size).keySet();
    }

    public static <T> Set<T> createSet(Collection<T> coll) {
        final Set<T> set = new KeyOnlyMap<T>(coll.size()).keySet();
        set.addAll(coll);
        return set;
    }

    public static <T> Set<T> createSet(T... array) {
        final Set<T> set = new KeyOnlyMap<T>().keySet();
        set.addAll(Arrays.asList(array));
        return set;
    }

    public static <T> Set<T> createSet(
            AbstractSimpleMap<T,Boolean,KeyOnlyEntry<T>,KeyOnlyMap<T>>.KeySet set) {
        return new KeyOnlyMap<T>(set.getMap()).keySet();
    }
    
    public KeyOnlyMap() {
        super();
    }

    public KeyOnlyMap(int initialSize) {
        super(initialSize);
    }

    public KeyOnlyMap(Map<T, Boolean> map) {
        super(map);
    }

    public KeyOnlyMap(
            AbstractSimpleMap<T, Boolean, KeyOnlyEntry<T>, KeyOnlyMap<T>> map) {
        super(map);
    }

    public KeyOnlyMap(InternalState<KeyOnlyEntry<T>> internalState) {
        super(internalState);
    }

    @Override
    protected KeyOnlyEntry<T> createEntry(T k, Boolean v) {
        return new KeyOnlyEntry<>(k);
    }

    @Override
    protected AbstractSimpleMap<T, Boolean, KeyOnlyEntry<T>, KeyOnlyMap<T>> createMap(
            int size) {
        return new KeyOnlyMap<>(size);
    }

    @Override
    protected Boolean innerPut(T key, Boolean value) {
        return super.innerPut(key, true);
    }
    
}
