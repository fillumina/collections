package com.fillumina.collections;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class Matrix<T> {
    
    public static class Immutable<T> extends Matrix<T> {

        public Immutable() {
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
    
    private T[][] matrix;

    public Matrix() {
    }

    /** Sizes the array with predefined length */
    public Matrix(int x, int y) {
        matrix = (T[][]) new Object[x][y];
    }

    public Matrix(Matrix copy) {
        matrix = (T[][]) new Object[copy.matrix.length][];
        for (int i=0,li=matrix.length; i<li; i++) {
            matrix[i] = (T[]) copy.matrix[i].clone();
        }
    }
    
    protected void resizeCheck() {
    }
    
    protected void readOnlyCheck() {
    }
    
    public Matrix set(int x, int y, T value) {
        readOnlyCheck();
        if (matrix == null) {
            matrix = (T[][]) new Object[x + 1][y + 1];
        } else if (matrix[0].length <= y || matrix.length <= x) {
            resize(x, y);
        }
        matrix[x][y] = value;
        return this;
    }

    private void resize(int x, int y) {
        resizeCheck();
        final int xsize = Math.max(x + 1, matrix.length);
        final int ysize = Math.max(y + 1, matrix[0].length);
        T[][] newMatrix = (T[][]) new Object[xsize][ysize];
        copy(newMatrix, matrix);
        matrix = newMatrix;
    }

    public T get(int x, int y) {
        return matrix[x][y];
    }
    
    public void forEachElement(Consumer<T> consumer) {
        for (int i=0,li=matrix.length; i<li; i++) {
            for (int j=0,lj=matrix[0].length; j<lj; j++) {
                consumer.accept(matrix[i][j]);
            }
        }
    }

    private void copy(T[][] newMatrix, T[][] matrix) {
        for (int i=0,li=matrix.length; i<li; i++) {
            for (int j=0,lj=matrix[0].length; j<lj; j++) {
                newMatrix[i][j] = matrix[i][j];
            }
        }
    }

    public void insertColumnAtIndex(int index) {
        int length = matrix.length;
        T[][] newmtx = (T[][]) new Object[length + 1][];
        System.arraycopy(matrix, 0, newmtx, 0, index);
        System.arraycopy(matrix, index, newmtx, index + 1, length - index);
        newmtx[index] = (T[]) new Object[matrix[0].length];
        matrix = newmtx;
    }

    public void insertRowAtIndex(int index) {
        final int length = matrix[0].length;
        for (int i=0,l=matrix.length; i<l ; i++) {
            T[] array = (T[]) new Object[length + 1];
            System.arraycopy(matrix[i], 0, array, 0, index);
            System.arraycopy(matrix[i], index, array, index + 1, length - index);
            matrix[i] = array;
        }
    }

    public void removeColumnAtIndex(int index) {
        int length = matrix.length;
        T[][] newmtx = (T[][]) new Object[length - 1][];
        if (index > 0)
            System.arraycopy(matrix, 0, newmtx, 0, index);
        if (index < length)
            System.arraycopy(matrix, index + 1, newmtx, index, length - index - 1);
        matrix = newmtx;
    }

    public void removeRowAtIndex(int index) {
        final int length = matrix[0].length;
        for (int i=0,l=matrix.length; i<l ; i++) {
            T[] array = (T[]) new Object[length - 1];
            if (index > 0)
                System.arraycopy(matrix[i], 0, array, 0, index);
            if (index < length - 1)
                System.arraycopy(matrix[i], index + 1, array, index, length - index - 1);
            matrix[i] = array;
        }
    }
    
    /** X */
    public int rowSize() {
        return matrix.length;
    }

    /** Y */
    public int colSize() {
        return matrix[0].length;
    }
    
    /** @return a read only list backed by the matrix. */
    public List<T> column(int x) {
        return new AbstractList<T>() {
            @Override
            public T get(int index) {
                return matrix[x][index];
            }

            @Override
            public int size() {
                return matrix[x].length;
            }
        };
    }
    
    /** @return a read only list backed by the matrix. */
    public List<T> row(int y) {
        return new AbstractList<T>() {
            @Override
            public T get(int index) {
                return matrix[index][y];
            }

            @Override
            public int size() {
                return matrix.length;
            }
        };
    }
    
    public Immutable<T> immutable() {
        return new Immutable<>(this);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        writeTo(buf::append);
        return buf.toString();
    }

    public void writeTo(Consumer<String> consumer) {
        int[] sizes = new int[matrix.length];
        String[][] table = new String[matrix.length][matrix[0].length];
        for (int i=0,li=matrix.length; i<li; i++) {
            for (int j=0,lj=matrix[0].length; j<lj; j++) {
                final String str = Objects.toString(matrix[i][j], "");
                table[i][j] = str;
                int length = str.length();
                if (sizes[j] < length) {
                    sizes[j] = length;
                }
            }
        }
        
        Map<Integer,String> spaces = new HashMap<>();
        for (int i=0,li=table.length; i<li; i++) {
            for (int j=0,lj=table[0].length; j<lj; j++) {
                String str = table[j][i];
                int align = sizes[j] - str.length();
                String separator = spaces.get(align);
                if (separator == null) {
                    final char[] chars = new char[align + 2];
                    Arrays.fill(chars, ' ');
                    separator = new String(chars);
                    spaces.put(align, separator);
                }
                consumer.accept(str + separator);
            }
            consumer.accept(System.lineSeparator());
        }
    }
    
}
