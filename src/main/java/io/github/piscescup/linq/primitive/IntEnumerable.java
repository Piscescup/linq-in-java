package io.github.piscescup.linq.primitive;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.Linq;
import io.github.piscescup.linq.operation.terminal.Average;
import io.github.piscescup.linq.operation.terminal.Max;
import io.github.piscescup.linq.operation.terminal.Min;
import io.github.piscescup.linq.operation.terminal.Sum;

import java.util.function.Supplier;

/**
 * An {@code IntEnumerable} is an {@link Enumerable} sequence of {@link Integer} values, providing additional
 * terminal operations for computing averages, sums, maximums, and minimums of the elements in the sequence.
 *
 * <h3>Usages</h3>
 * <pre>{@code
 * IntEnumerable numbers = ...; // obtain an IntEnumerable from somewhere
 * double avg = numbers.intAverageIgnoreNull(); // compute average ignoring nulls
 * int sum = numbers.intSum(); // compute sum of all elements
 * int max = numbers.intMaxIgnoreNull(); // compute max ignoring nulls
 * int min = numbers.intMin(); // compute min of all elements
 * }</pre>
 *
 *
 * @author REN YuanTong
 * @since 1.0.3
 */
public interface IntEnumerable extends Enumerable<Integer> {

    /**
     * Computes the arithmetic mean of {@link Integer} values in this sequence, ignoring {@code null} elements.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> numbers = Enumerable.of(1, 2, null, 3);
     * double avg = numbers.intAverageNullable(); // 2.0
     * }</pre>
     *
     * @return the average of all non-null elements
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Integer>}
     * @since 1.0.3
     */
    default double intAverageIgnoreNull() {
        return Average.intAverageIgnoreNull(this);
    }

    /**
     * Computes the arithmetic mean of {@link Integer} values in this sequence, assuming no element is {@code null}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> numbers = Enumerable.of(1, 2, 3);
     * double avg = numbers.intAverageNonNull(); // 2.0
     * }</pre>
     *
     * @return the average of all elements
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Integer>}
     * @since 1.0.3
     */
    default double intAverage() {
        return Average.intAverage(this);
    }


    /**
     * Computes the sum of {@link Integer} values in this sequence,
     * ignoring {@code null} elements.
     *
     * <p>If the sequence contains no non-null elements,
     * this method returns {@code 0}.</p>
     *
     * @return the sum of all non-null elements, or {@code 0} if none
     * @since 1.0.3
     */
    default int intSumIgnoreNull() {
        return Sum.intSumIgnoreNull(this);
    }

    /**
     * Computes the sum of {@link Integer} values in this sequence.
     *
     * <p>If the sequence is empty, this method returns {@code 0}.</p>
     *
     * @return the sum of all elements
     * @throws NullPointerException if any element is {@code null}
     * @since 1.0.3
     */
    default int intSum() {
        return Sum.intSum(this);
    }

    /**
     * Returns the maximum {@link Integer} value in this sequence,
     * ignoring {@code null} elements.
     *
     * @return the maximum non-null element
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @since 1.0.3
     */
    default int intMaxIgnoreNull() {
        return Max.intMaxIgnoreNull(this);
    }

    /**
     * Returns the maximum {@link Integer} value in this sequence.
     *
     * @return the maximum element
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @since 1.0.3
     */
    default int intMax() {
        return Max.intMax(this);
    }

    /**
     * Returns the minimum {@link Integer} value in this sequence,
     * ignoring {@code null} elements.
     *
     * @return the minimum non-null element
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @since 1.0.3
     */
    default int intMinIgnoreNull() {
        return Min.intMinIgnoreNull(this);
    }

    /**
     * Returns the minimum {@link Integer} value in this sequence.
     *
     * @return the minimum element
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @since 1.0.3
     */
    default int intMin() {
        return Min.intMin(this);
    }

    /**
     * Default implementation of {@link IntEnumerable}.
     *
     * <p>This class provides a concrete {@code IntEnumerable}
     * backed by a {@link Supplier} of {@link Enumerator}.
     *
     * <p>Instances are typically created via factory methods
     * such as {@code Linq.of(...)} or specialized numeric factories.</p>
     *
     * <p>This implementation does not perform primitive specialization;
     * values are still boxed as {@link Integer}.</p>
     *
     * @since 1.0.3
     */
    public static
    class IntLinq extends Linq<Integer> implements IntEnumerable {

        /**
         * Creates an {@code IntLinq} backed by the given enumerator factory.
         *
         * @param factory supplier that produces fresh enumerators
         */
        public IntLinq(Supplier<? extends Enumerator<Integer>> factory) {
            super(factory);
        }
    }
}
