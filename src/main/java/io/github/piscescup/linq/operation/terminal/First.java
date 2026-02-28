package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Provides LINQ-style {@code first} retrieval operations for {@link Enumerable}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Enumerable<Integer> numbers = ...
 *
 * int first = First.first(numbers);
 *
 * int firstEven = First.first(numbers, n -> n % 2 == 0);
 *
 * int fallback = First.firstOrDefault(numbers, -1);
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class First {

    /**
     * Private constructor to prevent instantiation.
     *
     * @throws UnsupportedOperationException always
     */
    private First() {
        throw new UnsupportedOperationException(
            "No instance of " + First.class.getName() + " for you!"
        );
    }

    /**
     * Returns the first element of the sequence.
     *
     * <p>Enumeration stops immediately after the first element is found.
     *
     * <p>Time complexity: O(1) in best case; O(n) in worst case (if empty).
     *
     * @param <T>    the element type
     * @param source the source sequence
     * @return the first element
     *
     * @throws NullPointerException  if {@code source} is {@code null}
     * @throws NoSuchElementException if the sequence is empty
     */
    public static <T> T first(Enumerable<T> source) {
        NullCheck.requireNonNull(source);

        try (Enumerator<T> enumerator = source.enumerator()) {
            if (enumerator.moveNext())
                return enumerator.current();
        }

        throw new NoSuchElementException();
    }

    /**
     * Returns the first element in the sequence that satisfies the predicate.
     *
     * <p>Enumeration stops immediately after a matching element is found.
     *
     * <p>Time complexity: O(k), where k is the index of the first match;
     * O(n) if no match exists.
     *
     * @param <T>       the element type
     * @param source    the source sequence
     * @param predicate the condition to test
     * @return the first matching element
     *
     * @throws NullPointerException  if {@code source} or {@code predicate} is {@code null}
     * @throws NoSuchElementException if no matching element is found
     */
    public static <T> T first(
        Enumerable<T> source,
        Predicate<? super T> predicate
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                T current = enumerator.current();
                if (predicate.test(current))
                    return current;
            }
        }

        throw new NoSuchElementException();
    }

    /**
     * Returns the first element of the sequence,
     * or {@code defaultValue} if the sequence is empty.
     *
     * @param <T>          the element type
     * @param source       the source sequence
     * @param defaultValue the value to return if empty
     * @return the first element, or {@code defaultValue} if empty
     *
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public static <T> T firstOrDefault(
        Enumerable<T> source,
        T defaultValue
    ) {
        NullCheck.requireNonNull(source);

        try (Enumerator<T> enumerator = source.enumerator()) {
            if (enumerator.moveNext())
                return enumerator.current();
        }

        return defaultValue;
    }

    /**
     * Returns the first element that satisfies the predicate,
     * or {@code defaultValue} if no such element exists.
     *
     * @param <T>          the element type
     * @param source       the source sequence
     * @param predicate    the condition to test
     * @param defaultValue the value to return if no match is found
     * @return the first matching element, or {@code defaultValue}
     *
     * @throws NullPointerException if {@code source} or {@code predicate} is {@code null}
     */
    public static <T> T firstOrDefault(
        Enumerable<T> source,
        Predicate<? super T> predicate,
        T defaultValue
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                T current = enumerator.current();
                if (predicate.test(current))
                    return current;
            }
        }

        return defaultValue;
    }
}