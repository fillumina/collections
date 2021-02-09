package com.fillumina.collections;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A matrix implementation to help manages squared arrays of data. It can be considered a sort
 * of multi association map. It is also a quite compact (but slow) map representation.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class Matrix<K,V> {

    private static final int COLUMN_SEPARATION = 2;

    public static class Immutable<K,V> extends Matrix<K,V> {

        public Immutable() {
        }

        public Immutable(K[] keys, int rows) {
            super(keys, rows);
        }

        public Immutable(Map<K, V> map) {
            super(map);
        }

        protected Immutable(K[] keys, V[][] array) {
            super(keys, array);
        }

        public Immutable(int x, int y) {
            super(x, y);
        }

        public Immutable(Matrix copy) {
            super(copy);
        }

        @Override
        protected void readOnlyCheck() {
            throw new UnsupportedOperationException("read only");
        }

        @Override
        protected void resizeCheck() {
            throw new UnsupportedOperationException("read only");
        }
    }

    public static class Builder<K,V> {

        private K[] keys;
        private List<V[]> list = new ArrayList<>();
        private int rowLength = -1;

        public Builder<K,V> keys(K... values) {
            this.keys = values;
            return this;
        }

        public Builder<K,V> row(V... values) {
            list.add(values);
            if (rowLength == -1) {
                rowLength = values.length;
            } else if (rowLength != values.length) {
                throw new IllegalArgumentException("lines must all have the same element number");
            }
            return this;
        }

        public Matrix<K,V> build() {
            V[][] array = (V[][]) new Object[list.size()][];
            for (int i = 0, l = list.size(); i < l; i++) {
                array[i] = list.get(i);
            }
            return new Matrix<>(keys, array);
        }

        public Immutable<K,V> buildImmutable() {
            V[][] array = (V[][]) new Object[list.size()][];
            for (int i = 0, l = list.size(); i < l; i++) {
                array[i] = list.get(i);
            }
            return new Immutable<>(keys, array);
        }
    }

    private class RowCursor implements Iterator<Entry<K,V>>, Entry<K,V> {
        private final int row;
        private int col = -1;
        
        public RowCursor(int row) {
            this.row = row;
        }
        
        @Override
        public boolean hasNext() {
            return col < keys.size();
        }

        @Override
        public Entry<K, V> next() {
            col++;
            return this;
        }

        @Override
        public K getKey() {
            return keys.get(col);
        }

        @Override
        public V getValue() {
            return get(row, col);
        }

        @Override
        public V setValue(V value) { throw new UnsupportedOperationException("read only."); }
    }
    
    public static <K,V> Builder<K,V> builder() {
        return new Builder<>();
    }

    private ImmutableList<K> keys;
    
    // using T[][] interferes with Kryo
    private Object[][] matrix;

    public Matrix() {
        keys = null;
        matrix = null;
    }

    public Matrix(ImmutableList<K> keys) {
        this.keys = keys;
    }

    public Matrix(K... keys) {
        this.keys = ImmutableList.of(keys);
    }

    protected Matrix(K[] keys, V[][] array) {
        this.keys = ImmutableList.of(keys);
        this.matrix = array;
    }

    protected Matrix(K[] keys, int rows) {
        this.keys = ImmutableList.of(keys);
        matrix = (V[][]) new Object[keys.length][rows];
    }

    protected Matrix(ImmutableList<K> keys, V[][] array) {
        this.keys = keys;
        this.matrix = array;
    }

    protected Matrix(ImmutableList<K> keys, int rows) {
        this.keys = keys;
        matrix = (V[][]) new Object[keys.size()][rows];
    }

    /**
     * Sizes the array with predefined length
     */
    public Matrix(int cols, int rows) {
        keys = null;
        matrix = (V[][]) new Object[cols][rows];
    }

    /** Transforms a map in a mono-dimensional matrix where K are keys and V are values. */
    public Matrix(Map<K,V> map) {
        keys = ImmutableList.of((K[]) map.keySet().toArray());
        matrix = (V[][]) new Object[1][];
        matrix[0] = map.values().toArray();
    }
    
    public Matrix(Matrix<K,V> copy) {
        keys = copy.keys;
        if (copy.matrix == null) {
            matrix = null;
        } else {
            matrix = (V[][]) new Object[copy.matrix.length][];
            for (int i = 0, li = matrix.length; i < li; i++) {
                matrix[i] = (V[]) copy.matrix[i].clone();
            }
        }
    }

    protected Matrix(V[][] array) {
        this((K[])null, array);
    }

    protected void resizeCheck() {
    }

    protected void readOnlyCheck() {
    }

    public Matrix set(int col, int row, V value) {
        readOnlyCheck();
        if (matrix == null) {
            matrix = (V[][]) new Object[col + 1][row + 1];
        } else if (matrix[0].length <= row || matrix.length <= col) {
            resize(col, row);
        }
        matrix[col][row] = value;
        return this;
    }

    private void resize(int col, int row) {
        resizeCheck();
        final int xsize = Math.max(col + 1, matrix.length);
        final int ysize = Math.max(row + 1, matrix[0].length);
        Object[][] newMatrix = new Object[xsize][ysize];
        copy(newMatrix, matrix);
        matrix = newMatrix;
    }

    public V get(int row, int col) {
        return (V) matrix[row][col];
    }
    
    public List<K> getKeys() {
        return keys;
    }
    
    public Map<K,V> getRowMap(int row) {
        return new AbstractMap<K,V>() {
            @Override public Set<Map.Entry<K, V>> entrySet() {
                return new AbstractSet<Entry<K,V>>() {
                    @Override public Iterator<Map.Entry<K, V>> iterator() {
                        return new RowCursor(row);
                    }

                    @Override public int size() {
                        return keys == null ? 0 : keys.size();
                    }
                };
            }
        };
    }

    public void forEachElement(Consumer<V> consumer) {
        for (int i = 0, li = matrix.length; i < li; i++) {
            for (int j = 0, lj = matrix[0].length; j < lj; j++) {
                consumer.accept((V) matrix[i][j]);
            }
        }
    }

    private void copy(Object[][] newMatrix, Object[][] matrix) {
        for (int i = 0, li = matrix.length; i < li; i++) {
            for (int j = 0, lj = matrix[0].length; j < lj; j++) {
                newMatrix[i][j] = matrix[i][j];
            }
        }
    }

    public void insertColumnAtIndex(int index) {
        int length = matrix.length;
        V[][] newmtx = (V[][]) new Object[length + 1][];
        System.arraycopy(matrix, 0, newmtx, 0, index);
        System.arraycopy(matrix, index, newmtx, index + 1, length - index);
        newmtx[index] = (V[]) new Object[matrix[0].length];
        matrix = newmtx;
    }

    public void insertRowAtIndex(int index) {
        final int length = matrix[0].length;
        for (int i = 0, l = matrix.length; i < l; i++) {
            V[] array = (V[]) new Object[length + 1];
            System.arraycopy(matrix[i], 0, array, 0, index);
            System.arraycopy(matrix[i], index, array, index + 1, length - index);
            matrix[i] = array;
        }
    }

    public void removeColumnAtIndex(int index) {
        int length = matrix.length;
        V[][] newmtx = (V[][]) new Object[length - 1][];
        if (index > 0) {
            System.arraycopy(matrix, 0, newmtx, 0, index);
        }
        if (index < length) {
            System.arraycopy(matrix, index + 1, newmtx, index, length - index - 1);
        }
        matrix = newmtx;
    }

    public void removeRowAtIndex(int index) {
        final int length = matrix[0].length;
        for (int i = 0, l = matrix.length; i < l; i++) {
            V[] array = (V[]) new Object[length - 1];
            if (index > 0) {
                System.arraycopy(matrix[i], 0, array, 0, index);
            }
            if (index < length - 1) {
                System.arraycopy(matrix[i], index + 1, array, index, length - index - 1);
            }
            matrix[i] = array;
        }
    }

    /**
     * X
     */
    public int rowSize() {
        return matrix.length;
    }

    /**
     * Y
     */
    public int colSize() {
        return matrix[0].length;
    }

    /**
     * @return a read only list backed by the matrix.
     */
    public List<V> column(int x) {
        return new AbstractList<V>() {
            @Override
            public V get(int index) {
                return (V) matrix[x][index];
            }

            @Override
            public int size() {
                return matrix[x].length;
            }
        };
    }

    /**
     * @return a read only list backed by the matrix.
     */
    public List<V> row(int y) {
        return new AbstractList<V>() {
            @Override
            public V get(int index) {
                return (V) matrix[index][y];
            }

            @Override
            public int size() {
                return matrix.length;
            }
        };
    }

    public Immutable<K,V> immutable() {
        return new Immutable<>(this);
    }

    @Override
    public Matrix<K,V> clone() {
        return new Matrix<>(this);
    }
    
    @Override
    public String toString() {
        return toString(keys);
    }

    public String toString(List<?> headers) {
        StringBuilder buf = new StringBuilder();
        writeTo(headers, buf::append);
        return buf.toString();
    }

    public void writeTo(List<?> headers, Consumer<String> consumer) {
        int[] sizes = new int[matrix.length];
        if (!headers.isEmpty()) {
            for (int i = Math.min(headers.size(), matrix.length) - 1; i >= 0; i--) {
                sizes[i] = Objects.toString(headers.get(i)).length();
            }
        }

        String[][] table = new String[matrix.length][matrix[0].length];
        for (int i = 0, li = matrix.length; i < li; i++) {
            for (int j = 0, lj = matrix[0].length; j < lj; j++) {
                final String str = Objects.toString(matrix[i][j], "");
                table[i][j] = str;
                int length = str.length();
                if (sizes[i] < length) {
                    sizes[i] = length;
                }
            }
        }

        Map<Integer, String> spaces = new HashMap<>();

        if (!headers.isEmpty()) {
            int totalSize = 0;
            for (int i = 0, l = headers.size(); i < l; i++) {
                String str = Objects.toString(headers.get(i));
                int align = sizes[i] - str.length();
                String separator = getSeparator(spaces, align);
                consumer.accept(str + separator);
                totalSize += sizes[i];
            }
            consumer.accept(System.lineSeparator());
            char[] line = new char[totalSize + (sizes.length - 1) * COLUMN_SEPARATION];
            Arrays.fill(line, '-');
            consumer.accept(new String(line));
            consumer.accept(System.lineSeparator());
        }

        for (int j = 0, lj = table[0].length; j < lj; j++) {
            for (int i = 0, li = table.length; i < li; i++) {
                String str = table[i][j];
                int align = sizes[i] - str.length();
                String separator = getSeparator(spaces, align);
                consumer.accept(str + separator);
            }
            consumer.accept(System.lineSeparator());
        }
    }

    private String getSeparator(Map<Integer, String> spaces, int align) {
        String separator = spaces.get(align);
        if (separator == null) {
            separator = createSpaces(align);
            spaces.put(align, separator);
        }
        return separator;
    }

    private String createSpaces(int size) {
        final char[] chars = new char[size + COLUMN_SEPARATION];
        Arrays.fill(chars, ' ');
        String separator = new String(chars);
        return separator;
    }

}
