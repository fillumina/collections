package com.fillumina.collections;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A {@link java.util.List} implementation backed by an array with the aim of using as little space
 * as possible.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class SmallList<T> extends AbstractList<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private T[] array;

    public SmallList() {
    }

    @SuppressWarnings("unchecked")
    public SmallList(T... elements) {
        this.array = elements;
    }

    @SuppressWarnings("unchecked")
    public SmallList(Collection<? extends T> elements) {
        if (elements != null && !elements.isEmpty()) {
            array = (T[]) elements.toArray();
        }
    }

    public SmallList(SmallList<? extends T> smallList) {
        if (smallList.array != null) {
            this.array = smallList.array;
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
        if (array == null) {
            throw new IndexOutOfBoundsException("empty set, index=" + index);
        } else {
            try {
                return ((T[]) array)[index];
            } catch (IndexOutOfBoundsException ex) {
                throw new IndexOutOfBoundsException("empty set, index=" + index);
            }
        }
    }

    @Override
    public void sort(Comparator<? super T> comparator) {
        readOnlyCheck();
        if (array != null) {
            Arrays.sort(array, comparator);
        }
    }

    /**
     * <b>In place</b> sorting. Don't use {@link java.util.Collections#sort(java.util.List) }
     * which is much slower than this method.
     */
    public void sort() {
        readOnlyCheck();
        if (array != null) {
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
        if (array == null) {
            array = (T[]) new Object[]{e};
            return true;
        } else {
            T[] next = (T[]) new Object[array.length + 1];
            System.arraycopy(array, 0, next, 0, array.length);
            next[array.length] = e;
            array = next;
            return true;
        }
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
        if (e == null || array == null) {
            return false;
        }
        if (array.getClass().isArray()) {
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
                    array = na;
                    return true;
                }
            }
        } else if (equals(array, (T) e)) {
            array = null;
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public T removeAtIndex(int i) {
        readOnlyCheck();
        T oldValue = array[i];
        int l = array.length;
        T[] na = (T[]) new Object[l - 1];
        if (i > 0) {
            System.arraycopy(array, 0, na, 0, i);
        }
        if (i < l - 1) {
            System.arraycopy(array, i + 1, na, i, l - i - 1);
        }
        array = na;
        return oldValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (array == null) {
            return false;
        } else if (equals(o, (T) array)) {
            // ok there is a catch here but I choose not to care
            return true;
        } else if (array.getClass().isArray()) {
            for (T t : ((T[]) array)) {
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
        if (array == null) {
            // the set is empty
            return EmptyIterator.empty();
        }
        if (array.getClass().isArray()) {
            return new Iterator<T>() {
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return pos < ((T[]) array).length;
                }

                @Override
                public T next() {
                    T t;
                    try {
                        t = ((T[]) array)[pos];
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        throw new NoSuchElementException();
                    }
                    pos++;
                    return t;
                }

                @Override
                public void remove() {
                    pos--;
                    SmallList.this.removeAtIndex(pos);
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
                    return (T) array;
                }

                @Override
                public void remove() {
                    SmallList.this.clear();
                }
            };
        }
    }

    @Override
    public void clear() {
        readOnlyCheck();
        array = null;
    }

    @Override
    public int size() {
        if (array == null) {
            return 0;
        } else if (array.getClass().isArray()) {
            return ((T[]) array).length;
        }
        return 1;
    }

    @Override
    public int hashCode() {
        if (array == null) {
            return 0;
        } else if (array.getClass().isArray()) {
            return Arrays.deepHashCode((T[]) array);
        }
        return Objects.hashCode(array);
    }

    // equals() is imported from AbstractCollection
    @Override
    public String toString() {
        if (array == null) {
            return "null";
        } else if (array.getClass().isArray()) {
            return Arrays.toString((T[]) array);
        }
        return "[" + array.toString() + "]";
    }

    /**
     * @return immutable clone
     */
    public ImmutableSmallList<T> immutable() {
        return new ImmutableSmallList<>(this);
    }
}
