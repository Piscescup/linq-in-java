package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
/**
 * Provides LINQ-style minimum aggregation operations.
 *
 * <p>This utility class contains static methods for computing the minimum
 * value of elements in an {@link Enumerable} sequence, either by:
 * <ul>
 *     <li>Projecting elements to primitive values</li>
 *     <li>Using a {@link Comparator}</li>
 *     <li>Extracting a comparable key</li>
 * </ul>
 *
 * <p>All methods perform a single-pass iteration over the source sequence.
 *
 * <p>If the source sequence is empty, a {@link NoSuchElementException}
 * is thrown.
 *
 * <p>This class is not instantiable.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Min {

    private Min() {
        throw new UnsupportedOperationException(
            "No instance of " + Min.class.getName() + " for you!"
        );
    }

    /**
     * Returns the minimum {@code int} value produced by applying the given mapping
     * function to each element of the source sequence.
     *
     * @param source      the source sequence
     * @param intMapping  function used to project elements to {@code int}
     * @param <T>         element type
     *
     * @return the minimum projected {@code int} value
     *
     * @throws NullPointerException   if {@code source} or {@code intMapping} is null
     * @throws NoSuchElementException if the source sequence is empty
     */
    public static <T> int min(
        Enumerable<T> source,
        ToIntFunction<? super T> intMapping
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(intMapping);

        try (Enumerator<T> enumerator = source.enumerator()) {
            if (!enumerator.moveNext())
                throw new NoSuchElementException("Sequence contains no elements");

            int result = intMapping.applyAsInt(enumerator.current());

            while (enumerator.moveNext()) {
                int value = intMapping.applyAsInt(enumerator.current());
                if (value < result)
                    result = value;
            }

            return result;
        }
    }

    /**
     * Returns the minimum {@code long} value produced by applying the given mapping
     * function to each element of the source sequence.
     *
     * @param source       the source sequence
     * @param longMapping  function used to project elements to {@code long}
     * @param <T>          element type
     *
     * @return the minimum projected {@code long} value
     *
     * @throws NullPointerException   if {@code source} or {@code longMapping} is null
     * @throws NoSuchElementException if the source sequence is empty
     */
    public static <T> long min(
        Enumerable<T> source,
        ToLongFunction<? super T> longMapping
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(longMapping);

        try (Enumerator<T> enumerator = source.enumerator()) {
            if (!enumerator.moveNext())
                throw new NoSuchElementException("Sequence contains no elements");

            long result = longMapping.applyAsLong(enumerator.current());

            while (enumerator.moveNext()) {
                long value = longMapping.applyAsLong(enumerator.current());
                if (value < result)
                    result = value;
            }

            return result;
        }
    }

    /**
     * Returns the minimum {@code double} value produced by applying the given mapping
     * function to each element of the source sequence.
     *
     * @param source         the source sequence
     * @param doubleMapping  function used to project elements to {@code double}
     * @param <T>            element type
     *
     * @return the minimum projected {@code double} value
     *
     * @throws NullPointerException   if {@code source} or {@code doubleMapping} is null
     * @throws NoSuchElementException if the source sequence is empty
     */
    public static <T> double min(
        Enumerable<T> source,
        ToDoubleFunction<? super T> doubleMapping
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(doubleMapping);

        try (Enumerator<T> enumerator = source.enumerator()) {
            if (!enumerator.moveNext())
                throw new NoSuchElementException("Sequence contains no elements");

            double result = doubleMapping.applyAsDouble(enumerator.current());

            while (enumerator.moveNext()) {
                double value = doubleMapping.applyAsDouble(enumerator.current());
                if (value < result)
                    result = value;
            }

            return result;
        }
    }

    /* ============================================================ */
    /* Object min                                                   */
    /* ============================================================ */

    /**
     * Returns the minimum element according to the specified comparator.
     *
     * @param source      the source sequence
     * @param comparator  comparator used to compare elements
     * @param <T>         element type
     *
     * @return the minimum element
     *
     * @throws NullPointerException   if {@code source} or {@code comparator} is null
     * @throws NoSuchElementException if the source sequence is empty
     */
    public static <T> T minBy(
        Enumerable<T> source,
        Comparator<? super T> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(comparator);

        try (Enumerator<T> enumerator = source.enumerator()) {
            if (!enumerator.moveNext())
                throw new NoSuchElementException("Sequence contains no elements");

            T result = enumerator.current();

            while (enumerator.moveNext()) {
                T value = enumerator.current();
                if (comparator.compare(value, result) < 0)
                    result = value;
            }

            return result;
        }
    }

    /**
     * Returns the element whose extracted key is minimal according to natural ordering.
     *
     * @param source        the source sequence
     * @param keyExtractor  function used to extract a comparable key
     * @param <T>           element type
     * @param <K>           key type (must implement {@link Comparable})
     *
     * @return the element whose key is minimal
     *
     * @throws NullPointerException   if {@code source} or {@code keyExtractor} is null
     * @throws NoSuchElementException if the source sequence is empty
     */
    public static <T, K extends Comparable<? super K>> T minBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);

        return minBy(source, keyExtractor, Comparator.naturalOrder());
    }

    /**
     * Returns the element whose extracted key is minimal according to the specified comparator.
     *
     * @param source        the source sequence
     * @param keyExtractor  function used to extract a key
     * @param comparator    comparator used to compare keys
     * @param <T>           element type
     * @param <K>           key type
     *
     * @return the element whose key is minimal
     *
     * @throws NullPointerException   if any argument is null
     * @throws NoSuchElementException if the source sequence is empty
     */
    public static <T, K> T minBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(comparator);

        try (Enumerator<T> enumerator = source.enumerator()) {
            if (!enumerator.moveNext())
                throw new NoSuchElementException("Sequence contains no elements");

            T result = enumerator.current();
            K minKey = keyExtractor.apply(result);

            while (enumerator.moveNext()) {
                T value = enumerator.current();
                K key = keyExtractor.apply(value);

                if (comparator.compare(key, minKey) < 0) {
                    result = value;
                    minKey = key;
                }
            }

            return result;
        }
    }
}