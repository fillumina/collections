package com.fillumina.collections;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * It's a bi-dimensional array with the columns mapped by a set of immutable keys. It is possible to
 * retrieve the value of a cell by using coordinates or by indicating its key and the row number and
 * it's possible to "translate" from one cell to another on the same row with a different key. Key
 * access is quite fast being based on an hash set making the mapping performances an O(1). Cell
 * access performances are also O(1) with the exception of searching of values which is linear.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class Matrix<K, V> {

    private static final int COLUMN_SEPARATION = 2;

    public static class Immutable<K, V> extends Matrix<K, V> {

        public Immutable() {
        }

        public Immutable(ImmutableHashSet<K> keys, V[][] array) {
            super(keys, array);
        }

        public Immutable(ImmutableHashSet<K> keys, int rows) {
            super(keys, rows);
        }

        /**
         * Keys are the column headers
         */
        public Immutable(K[] keys, int rows) {
            super(keys, rows);
        }

        public Immutable(Map<K, V> map) {
            super(map);
        }

        /**
         * Keys are the column headers
         */
        protected Immutable(K[] keys, V[][] array) {
            super(keys, array);
        }

        /**
         * Keys are the column headers
         */
        public Immutable(int rows, int cols) {
            super(rows, cols);
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

    public static class RowBuilder<K, V> {

        private K[] keys;
        private List<V[]> rows = new ArrayList<>();
        private int rowLength = -1;

        public RowBuilder<K, V> keys(Collection<? extends K> keys) {
            return keys((K[]) keys.toArray());
        }

        public RowBuilder<K, V> keys(K... values) {
            this.keys = values;
            return this;
        }

        public RowBuilder<K, V> row(Collection<? extends V> values) {
            return row((V[]) values.toArray());
        }

        public RowBuilder<K, V> row(V... values) {
            rows.add(values);
            if (rowLength == -1) {
                rowLength = values.length;
            } else if (rowLength != values.length) {
                throw new IllegalArgumentException("lines must all have the same element number");
            }
            return this;
        }

        public Matrix<K, V> build() {
            V[][] array = (V[][]) new Object[rows.size()][];
            for (int i = 0, l = rows.size(); i < l; i++) {
                array[i] = rows.get(i);
            }
            return new Matrix<>(keys, array);
        }

        public Immutable<K, V> buildImmutable() {
            V[][] array = (V[][]) new Object[rows.size()][];
            for (int i = 0, l = rows.size(); i < l; i++) {
                array[i] = rows.get(i);
            }
            return new Immutable<>(keys, array);
        }
    }

    public static class ColBuilder<K, V> {

        private List<K> keys = new ArrayList<>();
        private List<V[]> columns = new ArrayList<>();
        private int colLength = -1;

        public ColBuilder<K, V> col(K key, Collection<? extends V> values) {
            return col(key, (V[]) values.toArray());
        }

        public ColBuilder<K, V> col(K key, V... values) {
            this.keys.add(key);
            this.columns.add(values);
            if (colLength < values.length) {
                colLength = values.length;
            }
            return this;
        }

        public Matrix<K, V> build() {
            final int rows = columns.size();
            V[][] array = (V[][]) new Object[colLength][rows];
            for (int i = 0, il = rows; i < il; i++) {
                for (int j = 0; j < colLength; j++) {
                    array[j][i] = columns.get(i)[j];
                }
            }
            return new Matrix<>(ImmutableHashSet.of(keys), array);
        }

        public Immutable<K, V> buildImmutable() {
            V[][] array = (V[][]) new Object[columns.size()][];
            for (int i = 0, l = columns.size(); i < l; i++) {
                array[i] = columns.get(i);
            }
            return new Immutable<>(ImmutableHashSet.of(keys), array);
        }
    }

    private class RowCursor implements Iterator<Entry<K, V>>, Entry<K, V> {

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
        public V setValue(V value) {
            readOnlyCheck();
            V old = get(row, col);
            set(row, col, value);
            return old;
        }
    }

    public static <K, V> RowBuilder<K, V> rowBuilder() {
        return new RowBuilder<>();
    }

    public static <K, V> ColBuilder<K, V> columnBuilder() {
        return new ColBuilder<>();
    }

    private ImmutableHashSet<K> keys;

    // using T[][] interferes with Kryo
    private Object[][] matrix;

    public Matrix() {
        keys = null;
        matrix = null;
    }

    public Matrix(ImmutableHashSet<K> keys) {
        this.keys = keys;
    }

    public Matrix(K... keys) {
        this.keys = ImmutableHashSet.<K>of(keys);
    }

    protected Matrix(K[] keys, V[][] array) {
        this.keys = ImmutableHashSet.<K>of(keys);
        this.matrix = array;
    }

    protected Matrix(K[] keys, int rows) {
        this.keys = ImmutableHashSet.of(keys);
        matrix = (V[][]) new Object[keys.length][rows];
    }

    protected Matrix(ImmutableHashSet<K> keys, V[][] array) {
        this.keys = keys;
        this.matrix = array;
    }

    public Matrix(ImmutableHashSet<K> keys, int rows) {
        this.keys = keys;
        matrix = (V[][]) new Object[keys.size()][rows];
    }

    /**
     * Sizes the array with predefined length
     */
    public Matrix(int rows, int cols) {
        keys = null;
        matrix = (V[][]) new Object[cols][rows];
    }

    /**
     * Transforms a map in a mono-dimensional matrix where K are keys and V are values.
     */
    public Matrix(Map<K, V> map) {
        keys = ImmutableHashSet.of((K[]) map.keySet().toArray());
        matrix = (V[][]) new Object[1][];
        matrix[0] = map.values().toArray();
    }

    public Matrix(ImmutableHashSet<K> keys, Matrix<?, ? extends V> copy) {
        this.keys = keys;
        this.matrix = copyFromMatrix(copy);
    }

    public Matrix(Matrix<? extends K, ? extends V> copy) {
        this.keys = (ImmutableHashSet<K>) copy.keys;
        this.matrix = copyFromMatrix(copy);
    }

    private static <V> V[][] copyFromMatrix(Matrix<?, ? extends V> copy) {
        if (copy.matrix == null) {
            return null;
        } else {
            V[][] objArray = (V[][]) new Object[copy.matrix.length][];
            for (int i = 0, li = objArray.length; i < li; i++) {
                objArray[i] = (V[]) copy.matrix[i].clone();
            }
            return objArray;
        }
    }

    protected Matrix(V[][] array) {
        this((K[]) null, array);
    }

    protected void resizeCheck() {
    }

    protected void readOnlyCheck() {
    }

    public Matrix<K, V> set(int row, int col, V value) {
        readOnlyCheck();
        if (matrix == null) {
            matrix = (V[][]) new Object[row + 1][col + 1];
        } else if (matrix[0].length <= col || matrix.length <= row) {
            resize(row, col);
        }
        matrix[row][col] = value;
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

    public V getByKey(K key, int rowIndex) {
        int col = keys.indexOf(key);
        return (V) matrix[rowIndex][col];
    }

    public Set<K> getKeys() {
        return keys;
    }

    public Map<K, V> getRowMap(int row) {
        return new AbstractMap<K, V>() {
            @Override
            public Set<Map.Entry<K, V>> entrySet() {
                return new AbstractSet<Entry<K, V>>() {
                    @Override
                    public Iterator<Map.Entry<K, V>> iterator() {
                        return new RowCursor(row);
                    }

                    @Override
                    public int size() {
                        return keys == null ? 0 : keys.size();
                    }
                };
            }
        };
    }

    /**
     * @return the row index at which the given pair of key, value is found.
     */
    public int rowIndexOf(K key, V value) {
        int col = keys.indexOf(key);
        return rowIndexOf(col, value);
    }

    /**
     * @return true if the given pair of key, value is found.
     */
    public boolean contains(K key, V value) {
        int col = keys.indexOf(key);
        return rowIndexOf(col, value) != -1;
    }

    public Matrix<K, V> assertContains(K key, V value) {
        if (!contains(key, value)) {
            throw new AssertionError("key=" + key + " => value=" + value + " not found");
        }
        return this;
    }

    public V getTranslation(K srcKey, K dstKey, V srcValue) {
        int col = keys.indexOf(dstKey);
        int row = rowIndexOf(srcKey, srcValue);
        return get(row, col);
    }

    public List<V> getList(K key) {
        int srcColIdx = keys.indexOf(key);
        return getColAsList(srcColIdx);
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
    public List<V> getRowAsList(int row) {
        return new AbstractList<V>() {
            @Override
            public V get(int index) {
                return (V) matrix[row][index];
            }

            @Override
            public int size() {
                return matrix[row].length;
            }
        };
    }

    /**
     * @return a read only list backed by the matrix.
     */
    public List<V> getColAsList(int col) {
        return new AbstractList<V>() {
            @Override
            public V get(int index) {
                return (V) matrix[index][col];
            }

            @Override
            public int size() {
                return matrix.length;
            }
        };
    }

    public int rowIndexOf(int col, V value) {
        for (int i = matrix.length - 1; i >= 0; i--) {
            if (Objects.equals(value, matrix[i][col])) {
                return i;
            }
        }
        return -1;
    }

    public Immutable<K, V> immutable() {
        if (this instanceof Immutable) {
            return (Immutable<K, V>) this;
        }
        return new Immutable<>(this);
    }

    @Override
    public Matrix<K, V> clone() {
        return new Matrix<>(this);
    }

    @Override
    public String toString() {
        return toString(keys);
    }

    public String toString(Collection<?> headers) {
        StringBuilder buf = new StringBuilder();
        writeTo(ImmutableList.of(headers), buf::append);
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
