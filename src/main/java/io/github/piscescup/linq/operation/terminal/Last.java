package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Provides LINQ-style {@code last} retrieval operations for {@link Enumerable}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Enumerable<Integer> numbers = ...
 *
 * int last = Last.last(numbers);
 *
 * int lastEven = Last.last(numbers, n -> n % 2 == 0);
 *
 * int fallback = Last.lastOrDefault(numbers, -1);
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Last {

    /**
     * Private constructor to prevent instantiation.
     *
     * @throws UnsupportedOperationException always
     */
    private Last() {
        throw new UnsupportedOperationException(
            "No instance of " + Last.class.getName() + " for you!"
        );
    }

    /**
     * Returns the last element of the sequence.
     *
     * <p>This method iterates through the entire sequence and
     * returns the final element encountered.
     *
     * <p>Time complexity: O(n)
     *
     * @param <T>    the element type
     * @param source the source sequence
     * @return the last element in the sequence
     *
     * @throws NullPointerException   if {@code source} is {@code null}
     * @throws NoSuchElementException if the sequence is empty
     */
    public static <T> T last(Enumerable<T> source) {
        NullCheck.requireNonNull(source);

        try (Enumerator<T> enumerator = source.enumerator()) {
            if (!enumerator.moveNext())
                throw new NoSuchElementException();

            T result;
            do {
                result = enumerator.current();
            } while (enumerator.moveNext());

            return result;
        }
    }

    /**
     * Returns the last element in the sequence that satisfies the given predicate.
     *
     * <p>The sequence is fully enumerated, and the last matching element
     * is returned.
     *
     * <p>Time complexity: O(n)
     *
     * @param <T>       the element type
     * @param source    the source sequence
     * @param predicate the condition to test
     * @return the last matching element
     *
     * @throws NullPointerException   if {@code source} or {@code predicate} is {@code null}
     * @throws NoSuchElementException if no matching element is found
     */
    public static <T> T last(
        Enumerable<T> source,
        Predicate<? super T> predicate
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);

        boolean found = false;
        T result = null;

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                T current = enumerator.current();
                if (predicate.test(current)) {
                    found = true;
                    result = current;
                }
            }
        }

        if (!found)
            throw new NoSuchElementException();

        return result;
    }

    /**
     * Returns the last element of the sequence,
     * or {@code defaultValue} if the sequence is empty.
     *
     * <p>Time complexity: O(n)
     *
     * @param <T>          the element type
     * @param source       the source sequence
     * @param defaultValue the value to return if the sequence is empty
     * @return the last element, or {@code defaultValue} if the sequence is empty
     *
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public static <T> T lastOrDefault(
        Enumerable<T> source,
        T defaultValue
    ) {
        NullCheck.requireNonNull(source);

        try (Enumerator<T> enumerator = source.enumerator()) {
            if (!enumerator.moveNext())
                return defaultValue;

            T result;
            do {
                result = enumerator.current();
            } while (enumerator.moveNext());

            return result;
        }
    }

    /**
     * Returns the last element in the sequence that satisfies the given predicate,
     * or {@code defaultValue} if no such element exists.
     *
     * <p>Time complexity: O(n)
     *
     * @param <T>          the element type
     * @param source       the source sequence
     * @param predicate    the condition to test
     * @param defaultValue the value to return if no matching element is found
     * @return the last matching element, or {@code defaultValue}
     *
     * @throws NullPointerException if {@code source} or {@code predicate} is {@code null}
     */
    public static <T> T lastOrDefault(
        Enumerable<T> source,
        Predicate<? super T> predicate,
        T defaultValue
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);

        boolean found = false;
        T result = defaultValue;

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                T current = enumerator.current();
                if (predicate.test(current)) {
                    found = true;
                    result = current;
                }
            }
        }

        return found ? result : defaultValue;
    }
}