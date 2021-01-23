package com.fillumina.collections;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class MatrixTest {

//       0 1 col    
//      +---> 
//    0 |1 4
//    1 |2 5
//    2 |3 6
//  row V
    
    public static void main(String[] args) {
        Matrix<String> mtx = new Matrix<>();
        mtx.set(0,0,"one");
        mtx.set(0,1,"two two");
        mtx.set(0,2,"three");
        mtx.set(1,0,"four four four");
        mtx.set(1,1,"");
        mtx.set(1,2,"six");
        mtx.writeTo(System.out::print, List.of("ONE", "TWO"));
    }
    
    @Test
    public void shouldUseBuilder() {
        Matrix<String> mtx = Matrix.<String>builder()
                .row("one", "four")
                .row("two", "five")
                .row("three", "six")
                .build();
        
        assertEquals("one", mtx.get(0,0));
        assertEquals("two", mtx.get(1,0));
        assertEquals("three", mtx.get(2,0));
        assertEquals("four", mtx.get(0,1));
        assertEquals("five", mtx.get(1,1));
        assertEquals("six", mtx.get(2,1));
        
        mtx.set(0,0, "first");
        assertEquals("first", mtx.get(0,0));
    }
    
    @Test
    public void shouldBuildImmutable() {
        Matrix<String> mtx = Matrix.<String>builder()
                .row("one", "four")
                .row("two", "five")
                .row("three", "six")
                .buildImmutable();
        
        assertEquals("one", mtx.get(0,0));
        assertEquals("two", mtx.get(1,0));
        assertEquals("three", mtx.get(2,0));
        assertEquals("four", mtx.get(0,1));
        assertEquals("five", mtx.get(1,1));
        assertEquals("six", mtx.get(2,1));
        
        assertThrows(UnsupportedOperationException.class, 
                () -> mtx.set(0, 0, "first"));
    }
    
    @Test
    public void shouldCreateMatrix() {
        Matrix<Integer> mtx = new Matrix<>();
    }
    
    @Test
    public void shouldSetElements() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);
        
        assertEquals(1, mtx.get(0, 0));
        assertEquals(2, mtx.get(0, 1));
        assertEquals(3, mtx.get(1, 0));
        assertEquals(4, mtx.get(1, 1));
    }
    
    @Test
    public void shouldPresizeMatrix() {
        Matrix<Integer> mtx = new Matrix<>(2,2) {
            @Override
            protected void resizeCheck() {
                throw new AssertionError();
            }
        };
        
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);
        
        assertEquals(1, mtx.get(0, 0));
        assertEquals(2, mtx.get(0, 1));
        assertEquals(3, mtx.get(1, 0));
        assertEquals(4, mtx.get(1, 1));
    }
    
    @Test
    public void shouldSetExistingValue() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);
        
        assertEquals(2, mtx.get(0, 1));
        
        mtx.set(0, 1, 22);
        
        assertEquals(22, mtx.get(0, 1));
    }
    
    @Test
    public void shouldReturnSize() {
        Matrix<Integer> mtx = new Matrix<>(3, 7);
        assertEquals(3, mtx.rowSize());
        assertEquals(7, mtx.colSize());
    }
    
    @Test
    public void shouldReturnRowList() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);

        List<Integer> col = mtx.row(1);
        
        assertEquals(2, col.get(0));
        assertEquals(4, col.get(1));
    }
    
    @Test
    public void shouldReturnColumnList() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);

        List<Integer> row = mtx.column(1);
        
        assertEquals(3, row.get(0));
        assertEquals(4, row.get(1));
    }
    
    @Test
    public void shouldReturnImmutable() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);

        Matrix<Integer> immutable = mtx.immutable();
        
        assertThrows(UnsupportedOperationException.class,
            () -> immutable.set(0, 3, 666));
    }
    
    @Test
    public void shouldInsertColumnAtIndex() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);
        
        mtx.insertColumnAtIndex(0);
        
        assertEquals(null, mtx.get(0, 0));
        assertEquals(null, mtx.get(0, 1));
    }
    
    @Test
    public void shouldInsertRowAtIndex() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);
        
        mtx.insertRowAtIndex(0);
        
        assertEquals(null, mtx.get(0, 0));
        assertEquals(null, mtx.get(1, 0));
    }
    
    @Test
    public void shouldRemoveColumnAtIndex0() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);
        
        mtx.removeColumnAtIndex(0);
        
        assertEquals(3, mtx.get(0, 0));
        assertEquals(4, mtx.get(0, 1));
    }
    
    @Test
    public void shouldRemoveColumnAtIndex1() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);
        
        mtx.removeColumnAtIndex(1);
        
        assertEquals(1, mtx.get(0, 0));
        assertEquals(2, mtx.get(0, 1));
    }
    
    @Test
    public void shouldRemoveRowAtIndex0() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);
        
        mtx.removeRowAtIndex(0);
        
        assertEquals(2, mtx.get(0, 0));
        assertEquals(4, mtx.get(1, 0));
    }
    
    @Test
    public void shouldRemoveRowAtIndex1() {
        Matrix<Integer> mtx = new Matrix<>();
        mtx.set(0,0,1);
        mtx.set(0,1,2);
        mtx.set(1,0,3);
        mtx.set(1,1,4);
        
        mtx.removeRowAtIndex(1);
        
        assertEquals(1, mtx.get(0, 0));
        assertEquals(3, mtx.get(1, 0));
    }
    
}
