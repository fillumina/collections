package com.fillumina.collections;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class CopyOnWriteMapTest extends GenericMapTest {

    /** Override */
    @SuppressWarnings("unchecked")
    protected <K extends Comparable<K>,V extends Comparable<V>> Map<K,V> create(Map<K,V> m) {
        return new CopyOnWriteMap<>(m);
    }

    /** Override */
    protected boolean isReadOnly() {
        return false;
    }

    public void testReplaceAll() {
        // ignore this test b/c entries are not settable
    }

    @Test
    public void shouldReadAndWriteSymultaneously() {
        CopyOnWriteMap<Integer,String> map = new CopyOnWriteMap<>();

        ExecutorService executor = Executors.newCachedThreadPool();
        long time = 5 * 1_000;

        // writer even
        executor.execute(() -> {
            final long start = System.currentTimeMillis();
            final long end = start + time;
            do {
                int i = ThreadLocalRandom.current().nextInt(100);
                if (i % 2 == 0) {
                    map.put(i, "" + i);
                }
            } while(System.currentTimeMillis() < end);
        });

        // writer odd
        executor.execute(() -> {
            final long start = System.currentTimeMillis();
            final long end = start + time;
            do {
                int i = ThreadLocalRandom.current().nextInt(100);
                if (i % 2 == 1) {
                    map.put(i, "" + i);
                }
            } while(System.currentTimeMillis() < end);
        });

        // remover
        executor.execute(() -> {
            final long start = System.currentTimeMillis();
            final long end = start + time * 2;
            do {
                final ThreadLocalRandom rnd = ThreadLocalRandom.current();
                int i = rnd.nextInt(100);
                if (i % 3 != 0) {
                    map.remove(i);
                }
            } while(System.currentTimeMillis() < end);
        });

        // reader
        executor.execute(() -> {
            final long start = System.currentTimeMillis();
            final long end = start + time;
            do {
                for (int i=0; i<100; i++) {
                    map.get(i);
                }
            } while(System.currentTimeMillis() < end);
        });

        // cleaner
        executor.execute(() -> {
            final long start = System.currentTimeMillis();
            final long end = start + time;
            do {
                map.clear();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }
            } while(System.currentTimeMillis() < end);
        });

        // iterator
        executor.execute(() -> {
            final long start = System.currentTimeMillis();
            final long end = start + time;
            do {
                int accumulator = 0;
                Iterator<Integer> it = map.keySet().iterator();
                while (it.hasNext()) {
                    accumulator += it.next();
                }
                if (accumulator == -1) {
                    throw new AssertionError("cannot happen");
                }
            } while(System.currentTimeMillis() < end);
        });


        executor.shutdown();
        while (!executor.isTerminated()) {}

        // System.out.println(map);

        map.forEach((k,v) -> {
            assertEquals(v, "" + k);
            assertTrue(k % 3 == 0);
        });

    }
}
