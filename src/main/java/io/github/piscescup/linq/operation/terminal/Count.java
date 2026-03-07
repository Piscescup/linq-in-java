package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.function.Predicate;

/**
 * Provides LINQ-style {@code count} aggregation operations for {@link Enumerable}.
 *
 * <p>This class is stateless and cannot be instantiated.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Enumerable<Integer> numbers = ...
 *
 * long total = Count.count(numbers);
 *
 * long evenCount = Count.count(numbers, n -> n % 2 == 0);
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Count {

    private Count() {
        throw new UnsupportedOperationException(
            "No instance of " + Count.class.getName() + " for you!"
        );
    }

    /**
     * Counts the number of elements in the specified sequence.
     *
     * <p>This method iterates through the entire sequence and returns
     * the total number of elements.
     *
     * @param <T>    the element type
     * @param source the source sequence
     * @return the number of elements in the sequence
     *
     * @throws NullPointerException if {@code source} is {@code null}
     * @throws ArithmeticException  if the element count exceeds {@link Long#MAX_VALUE}
     */
    public static <T> long count(Enumerable<T> source) {
        NullCheck.requireNonNull(source);

        long count = 0;
        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                count = Math.addExact(count, 1);
            }
        }
        return count;
    }

    /**
     * Counts the number of elements in the specified sequence
     * that satisfy the given predicate.
     *
     * <p>This method iterates through the entire sequence and increments
     * the counter only when {@code predicate.test(element)} returns {@code true}.
     *
     *
     * @param <T>       the element type
     * @param source    the source sequence
     * @param predicate the condition to test for each element
     * @return the number of matching elements
     *
     * @throws NullPointerException if {@code source} or {@code predicate} is {@code null}
     * @throws ArithmeticException  if the matching count exceeds {@link Long#MAX_VALUE}
     */
    public static <T> long count(
        Enumerable<T> source,
        Predicate<? super T> predicate
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);

        long count = 0;
        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                if (predicate.test(enumerator.current())) {
                    count = Math.addExact(count, 1);
                }
            }
        }
        return count;
    }
}