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
import java.util.function.Function;

/**
 * It's a multi value map backed by a 2-dimensional array. It is possible to
 * retrieve the value of a cell by using coordinates or by indicating its key and the row number and
 * it's possible to "translate" from one cell to another on the same row with a different key. Key
 * access and cell access are all O(1). Searching of values is linear.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class Matrix<K, V> {

    private static final int COLUMN_SEPARATION = 2;

    public static final Matrix<?,?> EMPTY = new Immutable<Object,Object>();
    
    public static <K,V> Matrix<K,V> empty() {
        return (Matrix<K, V>) EMPTY;
    }
    
    public static class Immutable<K, V> extends Matrix<K, V> {

        private Immutable() {
        }

        public Immutable(Collection<? extends K> keys, V[][] array) {
            super(keys, array);
        }

        public Immutable(Collection<? extends K> keys, int rows) {
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

        public Immutable(Matrix<? extends K, ? extends V> copy) {
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
            return new Matrix<>(ImmutableLinkedTableSet.of(keys), array);
        }

        public Immutable<K, V> buildImmutable() {
            final int rows = columns.size();
            V[][] array = (V[][]) new Object[colLength][rows];
            for (int i = 0, il = rows; i < il; i++) {
                for (int j = 0; j < colLength; j++) {
                    array[j][i] = columns.get(i)[j];
                }
            }
            return new Immutable<>(ImmutableLinkedTableSet.of(keys), array);
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
            return col + 1 < matrix[row].length;
        }

        @Override
        public Entry<K, V> next() {
            col++;
            return this;
        }

        @Override
        public K getKey() {
            return keys.inverse().get(col);
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

    private class ColumnIterator implements Iterator<V> {
        private final int col;
        private int row = -1;

        public ColumnIterator(int col) {
            this.col = col;
        }

        @Override
        public boolean hasNext() {
            return row + 1 < matrix.length;
        }

        @Override
        public V next() {
            row++;
            return get(row, col);
        }
    }

    public static <K, V> RowBuilder<K, V> rowBuilder() {
        return new RowBuilder<>();
    }

    public static <K, V> ColBuilder<K, V> columnBuilder() {
        return new ColBuilder<>();
    }

    private BiMap<K,Integer> keys;

    // using T[][] interferes with Kryo
    private Object[][] matrix;

    public Matrix() {
        keys = null;
        matrix = null;
    }

    protected Matrix(K[] keys, V[][] array) {
        this.keys = keys == null ? null : createKeys(Arrays.asList(keys));
        this.matrix = array;
    }

    protected Matrix(K[] keys, int rows) {
        this.keys = keys == null ? null : createKeys(Arrays.asList(keys));
        matrix = (V[][]) new Object[keys.length][rows];
    }

    protected Matrix(Collection<? extends K> keys, V[][] array) {
        this.keys = keys == null ? null : createKeys(keys);
        this.matrix = array;
    }

    public Matrix(Collection<? extends K> keys, int rows) {
        this.keys = keys == null ? null : createKeys(keys);
        matrix = (V[][]) new Object[keys.size()][rows];
    }

    private static <K> BiMap<K,Integer> createKeys(Collection<? extends K> collection) {
        BiMap<K,Integer> bimap = new BiMap<>(collection.size());
        int index = 0;
        for (K k : collection) {
            if (!bimap.containsKey(k)) {
                bimap.put(k, index);
                index++;
            }
        }
        return bimap;
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
        keys = createKeys(map.keySet());
        matrix = (V[][]) new Object[1][];
        matrix[0] = map.values().toArray();
    }

    public Matrix(Matrix<? extends K, ? extends V> copy) {
        this.keys = copy.keys == null ? null : (BiMap<K, Integer>) copy.keys.clone();
        this.matrix = cloneMatrixArray(copy);
    }

    private static <V> V[][] cloneMatrixArray(Matrix<?, ? extends V> copy) {
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

    private Matrix(BiMap<K, Integer> keys, Object[][] matrix) {
        this.keys = keys;
        this.matrix = matrix;
    }
    
    /**
     * Substitutes the passed keys to the ones present in copy.
     * @param keys  new set of keys (the order is important)
     * @param copy  the source of the matrix data
     */
    public <T> Matrix<T,V> changeKeys(Function<K,T> transformer) {
        BiMap<T,Integer> biMap = new BiMap<>();
        if (keys != null && !keys.isEmpty()) {
            keys.forEach((k,v) -> biMap.put(transformer.apply(k), v));
        }
        V[][] m = cloneMatrixArray(this);
        return new Matrix<T,V>(biMap, m);
    }

    protected void resizeCheck() {
    }

    protected void readOnlyCheck() {
    }

    public K getKeyAtColumn(int column) {
        return keys.inverse().get(column);
    }
    
    public Matrix<K,V> addKeys(K... keys) {
        for (K k : keys) {
            addKey(k);
        }
        return this;
    }
    
    public Matrix<K,V> addKey(K key) {
        setKeyAtColumn(key, keys == null ? 0 : keys.size());
        return this;
    }
    
    /** @return the old key */
    public K setKeyAtColumn(K key, int col) {
        readOnlyCheck();
        if (keys == null) {
            keys = new BiMap<>();
        } else if (keys.containsKey(key)) {
            throw new IllegalArgumentException("cannot update keys");
        }
        return keys.inverse().put(col, key);
    }
    
    public Matrix<K, V> addColumn(K key, V... column) {
        return addColumn(key, Arrays.asList(column));
    }

    public Matrix<K, V> addColumn(K key, Collection<? extends V> column) {
        readOnlyCheck();
        if (keys == null) {
            keys = new BiMap<>();
        }
        Integer col = keys.get(key);
        if (col == null) {
            col = keys.size();
            keys.put(key, col);
        }
        insertColumnAtIndex(col);
        int row = 0;
        for (V v : column) {
            set(row, col, v);
            row++;
        }
        return this;
    }
    
    public Matrix<K, V> removeColumn(K key) {
        readOnlyCheck();
        Integer col = keys.get(key);
        if (col == null) {
            throw new IllegalArgumentException("key '" + key + "' not found");
        }
        matrixRemoveColumnAtIndex(col);
        return this;
    }
    
    public Matrix<K, V> removeColumnAtIndex(int index) {
        readOnlyCheck();
        if (keys != null) {
            BiMap<K,Integer> newkeys = new BiMap<>();
            for (int i=0,l=keys.size()-1; i<l; i++) {
                newkeys.inverse().put(i, 
                        i < index ? keys.inverse().get(i) : keys.inverse().get(i-1));
            }
            keys = newkeys;
        }
        matrixRemoveColumnAtIndex(index);
        return this;
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
        int col = keys.get(key);
        return (V) matrix[rowIndex][col];
    }

    public Set<K> getKeys() {
        return keys == null ? Set.of() : keys.immutableView().keySet();
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
        int col = keys.get(key);
        return rowIndexOf(col, value);
    }

    /**
     * @return true if the given pair of key, value is found.
     */
    public boolean contains(K key, V value) {
        return rowIndexOf(key, value) != -1;
    }

    public Matrix<K, V> assertContains(K key, V value) {
        if (!contains(key, value)) {
            throw new AssertionError("key=" + key + " => value=" + value + " not found");
        }
        return this;
    }

    public V getRelationValue(K srcKey, K dstKey, V srcValue) {
        int col = keys.get(dstKey);
        int row = rowIndexOf(srcKey, srcValue);
        return get(row, col);
    }

    public List<V> getColumnAsListByKey(K key) {
        int col = keys.get(key);
        return getColumnAsListByIndex(col);
    }

    public Iterator<V> getColumnIteratorByKey(K key) {
        int col = keys.get(key);
        return new ColumnIterator(col);
    }

    public Iterator<V> getColumnIteratorByIndex(int col) {
        return new ColumnIterator(col);
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

    public void insertRowAtIndex(int index) {
        readOnlyCheck();
        int length = matrix.length;
        V[][] newmtx = (V[][]) new Object[length + 1][];
        System.arraycopy(matrix, 0, newmtx, 0, index);
        System.arraycopy(matrix, index, newmtx, index + 1, length - index);
        newmtx[index] = (V[]) new Object[matrix[0].length];
        matrix = newmtx;
    }

    void insertColumnAtIndex(int index) {
        final int length = matrix[0].length;
        for (int i = 0, l = matrix.length; i < l; i++) {
            V[] array = (V[]) new Object[length + 1];
            System.arraycopy(matrix[i], 0, array, 0, index);
            System.arraycopy(matrix[i], index, array, index + 1, length - index);
            matrix[i] = array;
        }
    }

    public void removeRowAtIndex(int index) {
        readOnlyCheck();
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

    void matrixRemoveColumnAtIndex(int index) {
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

    public boolean isEmpty() {
        return rowSize() == 0 && colSize() == 0 && (keys == null || keys.isEmpty());
    }
    
    /**
     * X
     */
    public int rowSize() {
        return matrix == null ? 0 : matrix.length;
    }

    /**
     * Y
     */
    public int colSize() {
        return matrix == null || matrix[0] == null ? 0 : matrix[0].length;
    }

    /**
     * @return a read only list backed by the matrix.
     */
    public List<V> getRowAsListByIndex(int row) {
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
    public List<V> getColumnAsListByIndex(int col) {
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
        List<String> headers = keys == null ? null : createHeaders();
        return toString(headers);
    }

    private List<String> createHeaders() {
        List<String> headers = new ArrayList<>();
        for (int i=0,l=keys.size(); i<l; i++) {
            K k = keys.inverse().get(i);
            String str = Objects.toString(k);
            headers.add(str);
        }
        return headers;
    }

    public String toString(Collection<?> headers) {
        StringBuilder buf = new StringBuilder();
        writeTo(ImmutableList.of(headers), buf::append);
        return buf.toString();
    }

    public void writeTo(List<?> headers, Consumer<String> consumer) {
        int[] sizes = new int[matrix[0].length];
        if (!headers.isEmpty()) {
            for (int i = Math.min(headers.size(), matrix.length) - 1; i >= 0; i--) {
                sizes[i] = Objects.toString(headers.get(i)).length();
            }
        }

        String[][] table = new String[matrix.length][matrix[0].length];
        for (int row = 0, lrow = matrix.length; row < lrow; row++) {
            for (int col = 0, lcol = matrix[0].length; col < lcol; col++) {
                final String str = Objects.toString(matrix[row][col], "");
                table[row][col] = str;
                int length = str.length();
                if (sizes[col] < length) {
                    sizes[col] = length;
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

        for (int row = 0, lrow = table.length; row < lrow; row++) {
            for (int col = 0, lcol = table[0].length; col < lcol; col++) {
                String str = table[row][col];
                int align = sizes[col] - str.length();
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.keys);
        hash = 43 * hash + Arrays.deepHashCode(this.matrix);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        // it takes into consideration derived classes like Immutable
        if (!obj.getClass().isAssignableFrom(getClass()) &&
                !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Matrix<?, ?> other = (Matrix<?, ?>) obj;
        if (!Objects.equals(this.keys, other.keys)) {
            return false;
        }
        if (!Arrays.deepEquals(this.matrix, other.matrix)) {
            return false;
        }
        return true;
    }
    
}
