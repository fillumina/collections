package com.fillumina.collections;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Useful inside lambdas instead of using {@link java.util.concurrent.atomic.AtomicReference}.
 *
 * @author Francesco Illuminati <fillumina@gmail.com>
 */
public class Holder<T> {
    private static final Object EMPTY = new Object();

    @SuppressWarnings("unchecked")
    private T value = (T) EMPTY;

    public Holder() {
    }

    public Holder(T value) {
        this.value = value;
    }

    public void onNullSet(T value) {
        if (isNull() || isEmpty()) {
            this.value = value;
        }
    }

    public void ifEmptySet(T value) {
        if (isEmpty()) {
            this.value = value;
        }
    }

    public boolean isEmpty() {
        return value == EMPTY;
    }

    public boolean isNull() {
        return value == null;
    }

    public T get() {
        if (isEmpty()) {
            return null;
        }
        return value;
    }

    public void set(T value) {
        this.value = value;
    }


    /**
     * If a value is present, returns the value, otherwise returns
     * {@code other}.
     *
     * @param other the value to be returned, if no value is present.
     *        May be {@code null}.
     * @return the value, if present, otherwise {@code other}
     */
    public T orElse(T other) {
        return value != null && value != EMPTY ? value : other;
    }

    /**
     * If a value is present, returns the value, otherwise returns the result
     * produced by the supplying function.
     *
     * @param supplier the supplying function that produces a value to be returned
     * @return the value, if present, otherwise the result produced by the
     *         supplying function
     * @throws NullPointerException if no value is present and the supplying
     *         function is {@code null}
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        return value != null && value != EMPTY ? value : supplier.get();
    }

    /**
     * If a value is present, returns the value, otherwise throws
     * {@code NoSuchElementException}.
     *
     * @return the non-{@code null} value described by this {@code Optional}
     * @throws NoSuchElementException if no value is present
     * @since 10
     */
    public T orElseThrow() {
        if (value == null || value == EMPTY) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * If a value is present, returns the value, otherwise throws an exception
     * produced by the exception supplying function.
     *
     * @apiNote
     * A method reference to the exception constructor with an empty argument
     * list can be used as the supplier. For example,
     * {@code IllegalStateException::new}
     *
     * @param <X> Type of the exception to be thrown
     * @param exceptionSupplier the supplying function that produces an
     *        exception to be thrown
     * @return the value, if present
     * @throws X if no value is present
     * @throws NullPointerException if no value is present and the exception
     *          supplying function is {@code null}
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null && value != EMPTY) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (value == null) {
            return obj == null;
        }
        return value.equals(obj);
    }


    @Override
    public String toString() {
        return Objects.toString(value);
    }
}
