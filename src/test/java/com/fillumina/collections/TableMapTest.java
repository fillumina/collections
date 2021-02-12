package com.fillumina.collections;

import java.util.AbstractMap.SimpleEntry;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class TableMapTest {    
    
    @Test
    public void shouldMaskAsModuleForPowerOfTwoValues() {
        assertEquals(51 % 64, 51 & (64 - 1));
        assertEquals(73 % 32, 73 & (32 - 1));
    }
    
    @Test
    public void shouldSelectTheNextPowerOfTwo() {
        assertEquals(0, AbstractEntryMap.nextPowerOf2(0));
        assertEquals(1, AbstractEntryMap.nextPowerOf2(1));
        assertEquals(2, AbstractEntryMap.nextPowerOf2(2));
        assertEquals(4, AbstractEntryMap.nextPowerOf2(3));
        assertEquals(8, AbstractEntryMap.nextPowerOf2(5));
        assertEquals(8, AbstractEntryMap.nextPowerOf2(6));
        assertEquals(8, AbstractEntryMap.nextPowerOf2(7));
        assertEquals(8, AbstractEntryMap.nextPowerOf2(8));
        assertEquals(16, AbstractEntryMap.nextPowerOf2(9));
        assertEquals(16, AbstractEntryMap.nextPowerOf2(12));
    }
    
    @Test
    public void shouldBeEmptyAtStart() {
        TableMap<String,String> map = new TableMap<>();
        
        assertTrue(map.isEmpty());
    }
    
    @Test
    public void shouldPutAnElement() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "one");
        
        assertFalse(map.isEmpty());
    }
    
    @Test
    public void shouldGetAnElement() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "one");
        
        assertEquals("one", map.get("1"));
    }
    
    @Test
    public void shouldContainsEntry() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "one");
        
        assertTrue(map.containsEntry("1", "one"));
        assertFalse(map.containsEntry("1", "other"));
    }
    
    @Test
    public void shouldIncrementSize() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "one");
        
        assertEquals(1, map.size());
    }
    
    @Test
    public void shouldNotBeEmptyAfterPut() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "one");
        
        assertFalse(map.isEmpty());
    }
    
    @Test
    public void shouldRemoveAnElement() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "one");
        map.remove("1");
        
        assertTrue(map.isEmpty());
    }
    
    @Test
    public void shouldInsertManyElements() {
        TableMap<String,String> map = new TableMap<>();
        
        for (int i=0; i<20; i++) {
            String value = "" + i;
            map.put(value, value);
            assertEquals(i + 1, map.size());
        }
        
        assertEquals(20, map.size());
        
        for (int i=0; i<20; i++) {
            assertTrue(map.containsKey("" + i));
        }
    }
    
    @Test
    public void shouldRemoveManyElements() {
        TableMap<String,String> map = new TableMap<>();
        
        for (int i=0; i<20; i++) {
            String value = "" + i;
            map.put(value, value);
        }
        
        assertEquals(20, map.size());
        
        for (int i=0; i<20; i++) {
            assertEquals("" + i, map.remove("" + i));
            assertEquals(19 - i, map.size());
        }
        
        assertTrue(map.isEmpty());
    }
    
    static class MyEntry extends SimpleEntry<String,String> {

        private final String check;
        
        public MyEntry(String key, String value, String check) {
            super(key, value);
            this.check = check;
        }

        public String getCheck() {
            return check;
        }
    }
    
    @Test
    public void shouldSetValueOnSameKey() {
        TableMap<String,String> map = new TableMap<>();
        MyEntry entry = new MyEntry("1", "one", "mine");
        map.putEntry(entry);
        map.put("1", "uno");
        
        MyEntry myEntry = (MyEntry) map.getEntry("1");
        assertEquals("mine", myEntry.getCheck());
    }
    
    static class MyReadOnlyEntry extends SimpleImmutableEntry<String,String> {

        private final String check;
        
        public MyReadOnlyEntry(String key, String value, String check) {
            super(key, value);
            this.check = check;
        }

        public String getCheck() {
            return check;
        }
    }
    
    @Test
    public void shouldNotSetValueOnSameKeyForReadOnlyEntry() {
        TableMap<String,String> map = new TableMap<>();
        SimpleImmutableEntry<String,String> entry = 
                new SimpleImmutableEntry<>("1", "one");
        
        assertThrows(UnsupportedOperationException.class, () -> {
            entry.setValue("uno");
        });
        
        map.putEntry(entry);
        map.put("1", "uno");
        
        Entry<String,String> newEntry = map.getEntry("1");
        assertNotEquals(entry, newEntry);
    }
    
    @Test
    public void shouldIterate() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");
        
        Iterator<Entry<String,String>> it = map.entrySet().iterator();
        List<String> values = new ArrayList<>();
        while (it.hasNext()) {
            Entry<String,String> e = it.next();
            values.add(e.getValue());
        }
        
        assertEquals(3, values.size());
        assertTrue(values.contains("a"));
        assertTrue(values.contains("b"));
        assertTrue(values.contains("c"));
    }
    
    @Test
    public void shouldRemoveFirstItemIterator() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");
        
        Iterator<Entry<String,String>> it = map.entrySet().iterator();
        Entry<String, String> first = it.next();
        assertNotNull(first);
        
        it.remove();
        
        assertFalse(map.containsKey(first.getKey()));
        
        assertEquals(2, map.size());
        
        List<String> expected = new ArrayList<>(List.of("1", "2", "3"));
        expected.remove(first.getKey());

        List<String> keys = map.entrySet().stream()
                .map(e -> e.getKey())
                .collect(Collectors.toList());
        
        assertTrue(keys.containsAll(expected));
    }
    
    @Test
    public void shouldRemoveMiddleItemIterator() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");
        
        Iterator<Entry<String,String>> it = map.entrySet().iterator();
        assertNotNull(it.next());
        Entry<String, String> middle = it.next();
        assertNotNull(middle);
        
        it.remove();
        
        assertFalse(map.containsKey(middle.getKey()));
        assertEquals(2, map.size());
    }
    
    @Test
    public void shouldRemoveLastItemIterator() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");
        
        Iterator<Entry<String,String>> it = map.entrySet().iterator();
        assertNotNull(it.next());
        assertNotNull(it.next());
        Entry<String, String> last = it.next();
        assertNotNull(last);
        
        Exception exception = assertThrows(
                NoSuchElementException.class, () -> it.next() );
        
        it.remove();
        
        assertFalse(map.containsKey(last.getKey()));
        assertEquals(2, map.size());
    }
    
    @Test
    public void shouldThrowExceptionIfIteratingPastLastItem() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");
        
        Iterator<Entry<String,String>> it = map.entrySet().iterator();
        assertNotNull(it.next());
        assertNotNull(it.next());
        assertNotNull(it.next());
        
        Exception exception = assertThrows(
                NoSuchElementException.class, () -> it.next() );
    }
    
    @Test
    public void shouldInsertSameElementTwice() {
        TableMap<String,String> map = new TableMap<>();
        assertNull(map.put("1", "a"));
        assertEquals(1, map.size());
        
        assertEquals("a", map.put("1", "a"));
        assertEquals(1, map.size());
    }

    static class SameHash {
        private final String value;

        public SameHash(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return 5;
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
            final SameHash other = (SameHash) obj;
            if (!Objects.equals(this.value, other.value)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return value;
        }
    }
    
    @Test
    public void shouldManageHashClashing() {
        TableMap<SameHash,String> map = new TableMap<>();
        
        SameHash one = new SameHash("one");
        SameHash two = new SameHash("two");

        assertEquals(one.hashCode(), two.hashCode());
        
        map.put(one, "one");
        map.put(two, "two");
        
        assertEquals("one", map.get(one));
        assertEquals("two", map.get(two));
        
        map.remove(two);
        assertNull(map.get(two));
        
        map.remove(one);
        assertNull(map.get(one));
        
        assertTrue(map.isEmpty());
    }
    
    @Test
    public void shouldIterateOverEntriesWithForEach() {
        TableMap<String,String> map = new TableMap<>();
        map.put("1", "a");
        map.put("2", "b");
        map.put("3", "c");
        
        List<String> keys = new ArrayList<>();
        map.forEach(e -> keys.add(e.getKey()));
        
        assertTrue(map.keySet().containsAll(keys));
        assertTrue(keys.containsAll(map.keySet()));
    }
    
    @Test
    public void shouldAcceptManyValues() {
        TableMap<String,Integer> map = new TableMap<>();
        for (int i=0; i<1000; i++) {
            String s = "" + i;
            map.put(s, i);
        }
        for (int i=0; i<1000; i++) {
            String s = "" + i;
            assertEquals(i, map.get(s));
        }
    }
    
    @Test
    public void shouldAcceptManyRandomValues() {
        TableMap<String,Integer> map = new TableMap<>();
        
        // create array
        int[] indexes = new int[1000];
        IntStream.range(0, 1000).forEach(i -> indexes[i] = i);

        // shuffle the array
        Random r = ThreadLocalRandom.current();
        for (int i=0; i<1000; i++) {
            int x = r.nextInt(1000);
            int y = r.nextInt(1000);
            int t = indexes[x];
            indexes[x] = indexes[y];
            indexes[y] = t;
        }
        
        // set the elements randomly
        for (int i=0; i<1000; i++) {
            int x = indexes[i];
            String s = "" + x;
            map.put(s, x);
        }
        
        // reads the elements
        for (int i=0; i<1000; i++) {
            String s = "" + i;
            assertEquals(i, map.get(s));
        }
        
        assertEquals(1000, map.size());
    }
    
    @Test
    public void shouldAcceptAndRemoveRandomValues() {
        List<Integer> list = new ArrayList<>(1000);
        IntStream.range(0, 1000).forEach(i -> list.add(i));
        Collections.shuffle(list);

        TableMap<String,Integer> map = new TableMap<>();
        list.forEach( i -> map.put("" + i, i));

        Set<Integer> set = new HashSet<>();
        while (set.size() < 100) {
            set.add(ThreadLocalRandom.current().nextInt(1000));
        }
        set.forEach( i -> assertNotNull(map.remove("" + i), "" + i) );
        
        list.removeAll(set);
        Collections.shuffle(list);
        list.forEach( i -> assertEquals(i, map.get("" + i)));
        assertEquals(900, map.size());
    }
    
    @Test
    public void shouldGetOrCreate() {
        TableMap<Integer,String> map = new TableMap<>();
        assertEquals("one", map.getOrCreate(1, () -> "one"));
        assertEquals("one", map.get(1));
    }
}
