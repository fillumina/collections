package com.fillumina.collections;

/**
 * An integer counter that can be used inside (non concurrent) lambdas. Use {@link AtomicInteger} if 
 * the lambda is called concurrently (as with {@link java.util.stream.Stream#parallel() } streams).
 * 
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class Counter {
    private int value;

    public Counter() {
    }
    
    public Counter(int value) {
        this.value = value;
    }
    
    public void increment() {
        value++;
    }
    
    public void decrement() {
        value--;
    }
    
    public void incrementBy(int delta) {
        value += delta;
    }
    
    public void decrementBy(int delta) {
        value -= delta;
    }
    
    public int get() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + this.value;
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Counter other = (Counter) obj;
        if (this.value != other.value) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
