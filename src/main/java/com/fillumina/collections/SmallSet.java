package com.fillumina.collections;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A Set implementation with the aim of using as little space as possible. Its
 * performances shouldn't be horrible for very few elements having complexity
 * {@code O(n)}. Doesn't accept {@code null}. It maintains insertion order
 * and the n-th elemnt can be get by using {@link #get(int) }. It's sortable by
 * using its own in place {@link #sort()} implementation. It's not thread safe.
 * <p>
 * NOTE: it doesn't work well with Kryo persistor.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SmallSet<T> extends AbstractSet<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final SmallSet<?> EMPTY = new ImmutableSmallSet<Object>();

    // can be either:
    // 1) null
    // 2) a single object
    // 3) an array of objects
    private Object obj;

    public SmallSet() {
    }

    @SuppressWarnings("unchecked")
    public SmallSet(T... elements) {
        if (elements != null) {
            final int length = elements.length;
            switch (length) {
                case 0:
                    // do nothing
                    break;
                case 1:
                    obj = elements[0];
                    break;
                default:
                    Object[] array = new Object[length];
                    int index = 0;
                    ELEM: for (T t : elements) {
                        for (int i=0; i<index; i++) {
                            if (Objects.equals(array[i],t)) {
                                continue ELEM;
                            }
                        }
                        array[index] = t;
                        index++;
                    }
                    if (index < length) {
                        obj = new Object[index];
                        System.arraycopy(array, 0, obj, 0, index);
                    } else {
                        obj = array;
                    }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public SmallSet(Collection<? extends T> elements) {
        this((T[])elements.toArray());
    }

    public SmallSet(SmallSet<? extends T> smallSet) {
        if (smallSet.obj != null) {
            if (smallSet.obj.getClass().isArray()) {
                this.obj = ((Object[])smallSet.obj).clone();
            } else {
                this.obj = smallSet.obj;
            }
        }
    }

    protected void readOnlyCheck() {
        // do nothing
    }

    /**
     * Get the element at the given index.
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (obj == null) {
            throw new IndexOutOfBoundsException("empty set, index=" + index);
        } else if (obj.getClass().isArray()) {
            return ((T[]) obj)[index];
        } else if (index == 0) {
            return (T) obj;
        }
        throw new IndexOutOfBoundsException("empty set, index=" + index);
    }

    @SuppressWarnings("unchecked")
    public int indexOf(T t) {
        if (obj == null) {
            return -1;
        } else if (obj.getClass().isArray()) {
            T[] array = (T[]) obj;
            for (int i=array.length-1; i>=0; i--) {
                if (t.equals(array[i])) {
                    return i;
                }
            }
            return -1;
        } else {
            return obj.equals(t) ? 0 : -1;
        }
    }

    /**
     * In place sorting. Don't use
     * {@link java.util.Collections#sort(java.util.List) }
     * which is much slower than this method.
     */
    @SuppressWarnings("unchecked")
    public void sort(Comparator<T> comparator) {
        readOnlyCheck();
        if (obj != null && obj.getClass().isArray()) {
            T[] array = ((T[]) obj);
            Arrays.sort(array, comparator);
        }
    }

    /**
     * In place sorting. Don't use
     * {@link java.util.Collections#sort(java.util.List) }
     * which is much slower than this method.
     */
    @SuppressWarnings("unchecked")
    public void sort() {
        readOnlyCheck();
        if (obj != null && obj.getClass().isArray()) {
            T[] array = ((T[]) obj);
            Arrays.sort(array);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean add(T e) {
        readOnlyCheck();
        if (e == null) {
            throw new IllegalArgumentException("cannot add null elements");
        }
        if (obj == null) {
            obj = e;
            return true;
        } else if (obj.getClass().isArray()) {
            if (!contains(e)) {
                T[] array = ((T[]) obj);
                T[] next = (T[]) new Object[array.length + 1];
                System.arraycopy(array, 0, next, 0, array.length);
                next[array.length] = e;
                obj = next;
                return true;
            }
        } else if (!equals(obj, e)) {
            Object old = obj;
            obj = new Object[2];
            ((Object[]) obj)[0] = old;
            ((Object[]) obj)[1] = e;
            return true;
        }
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<T> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * It's quite an expensive operation.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object e) {
        readOnlyCheck();
        if (e == null || obj == null) {
            return false;
        }
        if (obj.getClass().isArray()) {
            T[] array = (T[]) obj;
            int l = array.length;
            for (int i = 0; i < l; i++) {
                if (equals(e, array[i])) {
                    T[] na = (T[]) new Object[l - 1];
                    if (i > 0) {
                        System.arraycopy(array, 0, na, 0, i);
                    }
                    if (i < l - 1) {
                        System.arraycopy(array, i + 1, na, i, l - i - 1);
                    }
                    obj = na;
                    return true;
                }
            }
        } else if (equals(obj, (T)e)) {
            obj = null;
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public T removeAtIndex(int i) {
        readOnlyCheck();
        T[] array = (T[]) obj;
        T oldValue = array[i];
        int l = array.length;
        T[] na = (T[]) new Object[l - 1];
        if (i > 0) {
            System.arraycopy(array, 0, na, 0, i);
        }
        if (i < l - 1) {
            System.arraycopy(array, i + 1, na, i, l - i - 1);
        }
        obj = na;
        return oldValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (obj == null) {
            return false;
        } else if (equals(o, (T)obj)) {
            // ok there is a catch here but I choose not to care
            return true;
        } else if (obj.getClass().isArray()) {
            for (T t : ((T[]) obj)) {
                if (equals(o, t)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean equals(Object o, T t) {
        return o.equals(t);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        if (obj == null) {
            // the set is empty
            return EmptyIterator.empty();
        }
        if (obj.getClass().isArray()) {
            return new Iterator<T>() {
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return pos < ((T[]) obj).length;
                }

                @Override
                public T next() {
                    T t;
                    try {
                        t = ((T[]) obj)[pos];
                    } catch (IndexOutOfBoundsException ex) {
                        throw new NoSuchElementException("reading past end");
                    }
                    pos++;
                    return t;
                }

                @Override
                public void remove() {
                    pos--;
                    SmallSet.this.removeAtIndex(pos);
                }
            };
        } else {
            // only 1 item
            return new Iterator<T>() {
                boolean hasNext = true;

                @Override
                public boolean hasNext() {
                    return hasNext;
                }

                @Override
                public T next() {
                    if (!hasNext) {
                        throw new NoSuchElementException();
                    }
                    hasNext = false;
                    return (T) obj;
                }

                @Override
                public void remove() {
                    SmallSet.this.clear();
                }
            };
        }
    }

    @Override
    public void clear() {
        readOnlyCheck();
        obj = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int size() {
        if (obj == null) {
            return 0;
        } else if (obj.getClass().isArray()) {
            return ((T[]) obj).length;
        }
        return 1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int hashCode() {
        if (obj == null) {
            return 0;
        } else if (obj.getClass().isArray()) {
            return Arrays.deepHashCode((T[]) obj);
        }
        return Objects.hashCode(obj);
    }

    // equals() is imported from AbstractCollection
    @Override
    @SuppressWarnings("unchecked")
    public String toString() {
        if (obj == null) {
            return "null";
        } else if (obj.getClass().isArray()) {
            return Arrays.toString((T[]) obj);
        }
        return "[" + obj.toString() + "]";
    }

    /** @return immutable clone */
    public ImmutableSmallSet<T> immutable() {
        return new ImmutableSmallSet<>(this);
    }
}
