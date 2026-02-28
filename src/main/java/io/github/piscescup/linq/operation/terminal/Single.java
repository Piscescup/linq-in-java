package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;
import io.github.piscescup.utils.Found;

import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * Provides LINQ-style single element aggregation operations.
 *
 * <p>This utility class exposes {@code single} and {@code singleOrDefault} methods that
 * ensure a sequence contains exactly one element (or exactly one element matching a predicate).
 *
 * <p>All methods iterate the source sequence at most once.
 *
 * <p>This class is not instantiable.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Single {

    private Single() {
        throw new UnsupportedOperationException(
            "No instance of " + Single.class.getName() + " for you!"
        );
    }

    /**
     * Returns the only element of the source sequence.
     *
     * @param source the source sequence
     * @param <T>    element type
     * @return the single element in the sequence
     *
     * @throws NullPointerException   if {@code source} is null
     * @throws NoSuchElementException if the sequence contains no elements
     * @throws IllegalStateException  if the sequence contains more than one element
     */
    public static <T> T single(Enumerable<T> source) {
        NullCheck.requireNonNull(source);

        Found<T> r = tryGetSingle(source);
        if (!r.found())
            throw new NoSuchElementException("Sequence contains no elements");
        return r.value();
    }

    /**
     * Returns the only element of the source sequence that satisfies the predicate.
     *
     * @param source    the source sequence
     * @param predicate the predicate used to filter elements
     * @param <T>       element type
     * @return the single matching element
     *
     * @throws NullPointerException   if {@code source} or {@code predicate} is null
     * @throws NoSuchElementException if no element matches the predicate
     * @throws IllegalStateException  if more than one element matches the predicate
     */
    public static <T> T single(Enumerable<T> source, Predicate<? super T> predicate) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);

        Found<T> r = tryGetSingle(source, predicate);
        if (!r.found())
            throw new NoSuchElementException("Sequence contains no matching element");
        return r.value();
    }

    /**
     * Returns the only element of the source sequence, or {@code defaultValue} if the sequence is empty.
     *
     * @param source       the source sequence
     * @param defaultValue the value to return if the sequence is empty
     * @param <T>          element type
     * @return the single element, or {@code defaultValue} if empty
     *
     * @throws NullPointerException  if {@code source} is null
     * @throws IllegalStateException if the sequence contains more than one element
     */
    public static <T> T singleOrDefault(Enumerable<T> source, T defaultValue) {
        NullCheck.requireNonNull(source);

        Found<T> r = tryGetSingle(source);
        return r.found() ? r.value() : defaultValue;
    }

    /**
     * Returns the only element of the source sequence that satisfies the predicate,
     * or {@code defaultValue} if no element matches.
     *
     * @param source       the source sequence
     * @param predicate    the predicate used to filter elements
     * @param defaultValue the value to return if no element matches
     * @param <T>          element type
     * @return the single matching element, or {@code defaultValue} if none matches
     *
     * @throws NullPointerException  if {@code source} or {@code predicate} is null
     * @throws IllegalStateException if more than one element matches the predicate
     */
    public static <T> T singleOrDefault(
        Enumerable<T> source,
        Predicate<? super T> predicate,
        T defaultValue
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);

        Found<T> r = tryGetSingle(source, predicate);
        return r.found() ? r.value() : defaultValue;
    }

    private static <T> Found<T> tryGetSingle(Enumerable<T> source) {
        try (Enumerator<T> enumerator = source.enumerator()) {
            if (!enumerator.moveNext())
                return new Found<>(null, false);

            T value = enumerator.current();

            if (enumerator.moveNext())
                throw new IllegalStateException("Sequence contains more than one element");

            return new Found<>(value, true);
        }
    }

    private static <T> Found<T> tryGetSingle(Enumerable<T> source, Predicate<? super T> predicate) {
        boolean found = false;
        T value = null;

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                T current = enumerator.current();
                if (predicate.test(current)) {
                    if (found)
                        throw new IllegalStateException("Sequence contains more than one matching element");
                    found = true;
                    value = current;
                }
            }
        }

        return new Found<>(value, found);
    }
}