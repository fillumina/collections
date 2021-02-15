package com.fillumina.collections;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        Matrix<Integer, String> mtx = new Matrix<Integer, String>(ImmutableLinkedHashSet.of(1, 2))
                .set(0, 0, "one")
                .set(0, 1, "two two")
                .set(0, 2, "three")
                .set(1, 0, "four four four")
                .set(1, 1, "")
                .set(1, 2, "six");
        System.out.println(mtx.toString());
    }

    @Test
    public void shouldUseBuilder() {
        Matrix<Void, String> mtx = Matrix.<Void, String>rowBuilder()
                .row("one", "four")
                .row("two", "five")
                .row("three", "six")
                .build();

        assertEquals("one", mtx.get(0, 0));
        assertEquals("two", mtx.get(1, 0));
        assertEquals("three", mtx.get(2, 0));
        assertEquals("four", mtx.get(0, 1));
        assertEquals("five", mtx.get(1, 1));
        assertEquals("six", mtx.get(2, 1));

        mtx.set(0, 0, "first");
        assertEquals("first", mtx.get(0, 0));
    }

    @Test
    public void shouldBuildImmutable() {
        Matrix<Void, String> mtx = Matrix.<Void, String>rowBuilder()
                .row("one", "four")
                .row("two", "five")
                .row("three", "six")
                .buildImmutable();

        assertEquals("one", mtx.get(0, 0));
        assertEquals("two", mtx.get(1, 0));
        assertEquals("three", mtx.get(2, 0));
        assertEquals("four", mtx.get(0, 1));
        assertEquals("five", mtx.get(1, 1));
        assertEquals("six", mtx.get(2, 1));

        assertThrows(UnsupportedOperationException.class,
                () -> mtx.set(0, 0, "first"));
    }

    @Test
    public void shouldCreateMatrix() {
        Matrix<Void, Integer> mtx = new Matrix<>();
    }

    @Test
    public void shouldSetElements() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        assertEquals(1, mtx.get(0, 0));
        assertEquals(2, mtx.get(0, 1));
        assertEquals(3, mtx.get(1, 0));
        assertEquals(4, mtx.get(1, 1));
    }

    @Test
    public void shouldPresizeMatrix() {
        Matrix<Void, Integer> mtx = new Matrix<Void, Integer>(2, 2) {
            @Override
            protected void resizeCheck() {
                throw new AssertionError();
            }
        };

        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        assertEquals(1, mtx.get(0, 0));
        assertEquals(2, mtx.get(0, 1));
        assertEquals(3, mtx.get(1, 0));
        assertEquals(4, mtx.get(1, 1));
    }

    @Test
    public void shouldSetExistingValue() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        assertEquals(2, mtx.get(0, 1));

        mtx.set(0, 1, 22);

        assertEquals(22, mtx.get(0, 1));
    }

    @Test
    public void shouldReturnSize() {
        Matrix<Void, Integer> mtx = new Matrix<>(7, 3);
        assertEquals(3, mtx.rowSize());
        assertEquals(7, mtx.colSize());
    }

    @Test
    public void shouldBuildByColumn() {
        Matrix<String, Integer> mtx = Matrix.<String, Integer>columnBuilder()
                .col("A", 11, 21)
                .col("B", 12, 22)
                .col("C", 13, 23)
                .build();

        assertEquals(2, mtx.rowSize());
        assertEquals(3, mtx.colSize());

        assertEquals(12, mtx.get(0, 1));
        assertEquals(22, mtx.get(1, 1));
        assertEquals(13, mtx.get(0, 2));
        assertEquals(23, mtx.get(1, 2));
    }

    @Test
    public void shouldGerRowsAndColumns() {
        Matrix<String, Integer> mtx = Matrix.<String, Integer>rowBuilder()
                .keys("A", "B", "C")
                .row(11, 12, 13)
                .row(21, 22, 23)
                .build();

        assertEquals(2, mtx.rowSize());
        assertEquals(3, mtx.colSize());

        assertEquals(12, mtx.get(0, 1));
        assertEquals(22, mtx.get(1, 1));
        assertEquals(13, mtx.get(0, 2));
        assertEquals(23, mtx.get(1, 2));

        assertEquals(List.of(11, 12, 13), mtx.getRowAsList(0));
        assertEquals(List.of(21, 22, 23), mtx.getRowAsList(1));

        assertEquals(List.of(11, 21), mtx.getColumnAsList(0));
        assertEquals(List.of(12, 22), mtx.getColumnAsList(1));
        assertEquals(List.of(13, 23), mtx.getColumnAsList(2));

        Map<String, Integer> map = mtx.getRowMap(0);
        assertEquals(11, map.get("A"));
        assertEquals(12, map.get("B"));
        assertEquals(13, map.get("C"));
        
        assertEquals(13, mtx.get(0, 2));
        mtx.set(0, 2, 666);
        assertEquals(666, mtx.get(0, 2));
    }

    @Test
    public void shouldReturnRowList() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        List<Integer> col = mtx.getColumnAsList(1);

        assertEquals(2, col.get(0));
        assertEquals(4, col.get(1));
    }

    @Test
    public void shouldReturnColumnList() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        List<Integer> row = mtx.getRowAsList(1);

        assertEquals(3, row.get(0));
        assertEquals(4, row.get(1));
    }

    @Test
    public void shouldReturnImmutable() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        Matrix<Void, Integer> immutable = mtx.immutable();

        assertThrows(UnsupportedOperationException.class,
                () -> immutable.set(0, 3, 666));
    }

    @Test
    public void shouldInsertColumnAtIndex() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        mtx.insertColumnAtIndex(0);

        assertEquals(null, mtx.get(0, 0));
        assertEquals(null, mtx.get(0, 1));
    }

    @Test
    public void shouldInsertRowAtIndex() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        mtx.insertRowAtIndex(0);

        assertEquals(null, mtx.get(0, 0));
        assertEquals(null, mtx.get(1, 0));
    }

    @Test
    public void shouldRemoveColumnAtIndex0() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        mtx.removeColumnAtIndex(0);

        assertEquals(3, mtx.get(0, 0));
        assertEquals(4, mtx.get(0, 1));
    }

    @Test
    public void shouldRemoveColumnAtIndex1() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        mtx.removeColumnAtIndex(1);

        assertEquals(1, mtx.get(0, 0));
        assertEquals(2, mtx.get(0, 1));
    }

    @Test
    public void shouldRemoveRowAtIndex0() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        mtx.removeRowAtIndex(0);

        assertEquals(2, mtx.get(0, 0));
        assertEquals(4, mtx.get(1, 0));
    }

    @Test
    public void shouldRemoveRowAtIndex1() {
        Matrix<Void, Integer> mtx = new Matrix<>();
        mtx.set(0, 0, 1);
        mtx.set(0, 1, 2);
        mtx.set(1, 0, 3);
        mtx.set(1, 1, 4);

        mtx.removeRowAtIndex(1);

        assertEquals(1, mtx.get(0, 0));
        assertEquals(3, mtx.get(1, 0));
    }

    @Test
    public void shouldReturnAMap() {
        Matrix<Integer, String> mtx = Matrix.<Integer, String>rowBuilder()
                .keys(1, 2, 3)
                .row("one", "two", "three")
                .row("uno", "due", "tre")
                .row("une", "deux", "trois")
                .buildImmutable();

        Set<Integer> keys = mtx.getKeys();
        assertEquals(Set.of(1, 2, 3), keys);
        assertThrows(UnsupportedOperationException.class, () -> keys.add(4));
        assertThrows(UnsupportedOperationException.class, () -> keys.clear());

        Map<Integer, String> map1 = mtx.getRowMap(0);

        assertEquals(3, map1.size());
        assertEquals("one", map1.get(1));
        assertEquals("two", map1.get(2));
        assertEquals("three", map1.get(3));

        Map<Integer, String> map2 = mtx.getRowMap(1);

        assertEquals(3, map2.size());
        assertEquals("uno", map2.get(1));
        assertEquals("due", map2.get(2));
        assertEquals("tre", map2.get(3));

        Map<Integer, String> map3 = mtx.getRowMap(2);

        assertEquals(3, map3.size());
        assertEquals("une", map3.get(1));
        assertEquals("deux", map3.get(2));
        assertEquals("trois", map3.get(3));
    }

    @Test
    public void shouldConstructFromAMap() {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(3, "three");

        Matrix<Integer, String> mtx = new Matrix<>(map);

        assertEquals(3, mtx.colSize());
        assertEquals(1, mtx.rowSize());

        assertEquals("one", mtx.get(0, 0));
        assertEquals("two", mtx.get(0, 1));
        assertEquals("three", mtx.get(0, 2));

        Map<Integer, String> rowMap = mtx.getRowMap(0);

        assertEquals("one", rowMap.get(1));
        assertEquals("two", rowMap.get(2));
        assertEquals("three", rowMap.get(3));
    }

    @Test
    public void shouldGetTranslations() {
        Matrix<String, String> mtx = Matrix.<String, String>rowBuilder()
                .keys("IT", "EN", "FR")
                .row("uno", "one", "une")
                .row("due", "two", "deux")
                .row("tre", "three", "trois")
                .buildImmutable();

        assertEquals("une", mtx.getTranslation("IT", "FR", "uno"));
        assertEquals("two", mtx.getTranslation("IT", "EN", "due"));
        assertEquals("trois", mtx.getTranslation("EN", "FR", "three"));
    }

    @Test
    public void shouldContainTheGivenPair() {
        Matrix<String, String> mtx = Matrix.<String, String>rowBuilder()
                .keys("IT", "EN", "FR")
                .row("uno", "one", "une")
                .row("due", "two", "deux")
                .row("tre", "three", "trois")
                .buildImmutable();
        
        assertEquals(0, mtx.rowIndexOf("FR", "une"));
        assertEquals(1, mtx.rowIndexOf("IT", "due"));
        assertEquals(2, mtx.rowIndexOf("EN", "three"));
    }    

    enum MyEnum { A, B, C};

    @Test
    public void shouldChangeKey() {
        
        Matrix<String, String> mtx = Matrix.<String, String>rowBuilder()
                .keys("IT", "EN", "FR")
                .row("uno", "one", "une")
                .row("due", "two", "deux")
                .row("tre", "three", "trois")
                .buildImmutable();
        
        ImmutableLinkedHashSet<MyEnum> newKeys = ImmutableLinkedHashSet.of(MyEnum.values());
        Matrix<MyEnum, String> newMtx = new Matrix<>(newKeys, mtx);
        
        assertEquals("two", newMtx.getByKey(MyEnum.B, 1));
    }
    
    @Test
    public void shouldGetColumnIterator() {
        
        Matrix<String, String> mtx = Matrix.<String, String>rowBuilder()
                .keys("IT", "EN", "FR")
                .row("uno", "one", "une")
                .row("due", "two", "deux")
                .row("tre", "three", "trois")
                .buildImmutable();

        Iterator<String> it = mtx.getColumnIteratorByKey("EN");
        assertTrue(it.hasNext());
        assertEquals("one", it.next());
        assertTrue(it.hasNext());
        assertEquals("two", it.next());
        assertTrue(it.hasNext());
        assertEquals("three", it.next());
        assertFalse(it.hasNext());
    }
}
