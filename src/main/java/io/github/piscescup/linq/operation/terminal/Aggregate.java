package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.interfaces.exfunction.BinFunction;
import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Provides static aggregation (fold/reduce) operations for {@link Enumerable} sequences.
 *
 * <p>This class supplies LINQ-style {@code aggregate} methods that reduce a sequence
 * into a single result value by iteratively applying an accumulator function.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Aggregate {

    private Aggregate() {
        throw new UnsupportedOperationException(
            "No instance of " + Aggregate.class.getName() + " for you!"
        );
    }

    /**
     * Aggregates the elements of a sequence using an initial identity value,
     * an accumulator function, and a final result selector.
     *
     * <p>The accumulator function is applied sequentially to each element
     * in the source sequence, starting with the provided identity value.
     * After all elements have been processed, the accumulated result
     * is transformed by the result selector before being returned.
     *
     * <h3>Example</h3>
     *
     * <pre>{@code
     * Enumerable<Integer> numbers = Enumerable.of(1, 2, 3, 4);
     *
     * String result = Aggregate.aggregate(
     *     numbers,
     *     0,
     *     (acc, x) -> acc + x,
     *     sum -> "Total = " + sum
     * );
     *
     * // result: "Total = 10"
     * }</pre>
     *
     * @param source          the source sequence to aggregate
     * @param identity        the initial accumulator value
     * @param aggregator      the function used to accumulate each element
     * @param resultSelector  transforms the final accumulator value
     *                        into the desired result type
     * @param <T> element type of the source sequence
     * @param <A> intermediate accumulator type
     * @param <R> final result type
     * @return the aggregated result after applying the result selector
     *
     * @throws NullPointerException if {@code source}, {@code identity},
     *         {@code aggregator}, or {@code resultSelector} is {@code null}
     */
    public static <T, A, R> R aggregate(
        Enumerable<T> source, A identity,
        BinFunction<? super A, ? super T, ? extends A> aggregator,
        Function<? super A, ? extends R> resultSelector
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(identity);
        NullCheck.requireNonNull(aggregator);
        NullCheck.requireNonNull(resultSelector);

        A accumulate = identity;
        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                accumulate = aggregator
                    .apply(accumulate, enumerator.current());
            }
        }

        return resultSelector.apply(accumulate);
    }


    /**
     * Aggregates the elements of a sequence using an initial seed value
     * and an accumulator function.
     *
     * <p>The accumulator function is applied sequentially to each element
     * in the source sequence, starting from the specified seed.
     * The final accumulated value is returned directly.
     *
     * <h3>Example</h3>
     *
     * <pre>{@code
     * Enumerable<Integer> numbers = Enumerable.of(1, 2, 3, 4);
     *
     * int sum = Aggregate.aggregate(
     *     numbers,
     *     0,
     *     (acc, x) -> acc + x
     * );
     *
     * // sum: 10
     * }</pre>
     *
     * @param source     the source sequence to aggregate
     * @param seed       the initial accumulator value
     * @param aggregator the function used to accumulate each element
     * @param <T> element type of the source sequence
     * @param <R> accumulator and result type
     * @return the final accumulated value
     *
     * @throws NullPointerException if {@code source}, {@code seed},
     *         or {@code aggregator} is {@code null}
     */
    public static <T, R> R aggregate(
        Enumerable<T> source, R seed,
        BinFunction<? super R, ? super T, ? extends R> aggregator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(seed);
        NullCheck.requireNonNull(aggregator);

        R accumulate = seed;
        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                accumulate = aggregator
                    .apply(accumulate, enumerator.current());
            }
        }

        return accumulate;
    }


    /**
     * Aggregates the elements of a sequence using the first element
     * as the initial accumulator value.
     *
     * <p>The aggregation starts with the first element in the sequence.
     * The accumulator function is then applied sequentially
     * to the remaining elements.
     *
     * <h3>Example</h3>
     *
     * <pre>{@code
     * Enumerable<Integer> numbers = Enumerable.of(1, 2, 3, 4);
     *
     * int product = Aggregate.aggregate(
     *     numbers,
     *     (a, b) -> a * b
     * );
     *
     * // product: 24
     * }</pre>
     *
     * @param source     the source sequence to aggregate
     * @param aggregator the function used to combine two elements
     * @param <T> element type of the source sequence
     * @return the aggregated result
     *
     * @throws NullPointerException if {@code source} or {@code aggregator} is {@code null}
     * @throws NoSuchElementException if the source sequence is empty
     */
    public static <T> T aggregate(
        Enumerable<T> source,
        BinFunction<? super T, ? super T, ? extends T> aggregator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(aggregator);

        try (Enumerator<T> enumerator = source.enumerator()) {
            if (!enumerator.moveNext()) {
                throw new NoSuchElementException();
            }

            T accumulate = enumerator.current();
            while (enumerator.moveNext()) {
                accumulate = aggregator
                    .apply(accumulate, enumerator.current());
            }

            return accumulate;
        }
    }
}