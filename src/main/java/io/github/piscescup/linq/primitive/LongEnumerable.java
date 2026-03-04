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
 * An {@code LongEnumerable} is an {@link Enumerable} sequence of {@link Long} values, providing additional
 * terminal operations for computing averages, sums, maximums, and minimums of the elements in the sequence.
 *
 * <h3>Usages</h3>
 * <pre>{@code
 * LongEnumerable numbers = ...; // obtain a LongEnumerable from somewhere
 * double avg = numbers.longAverageIgnoreNull(); // compute average ignoring nulls
 * long sum = numbers.longSum(); // compute sum of all elements
 * long max = numbers.longMaxIgnoreNull(); // compute max ignoring nulls
 * long min = numbers.longMin(); // compute min of all elements
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.3
 */
public interface LongEnumerable extends Enumerable<Long> {

    /**
     * Computes the arithmetic mean of {@link Long} values in this sequence, ignoring {@code null} elements.
     *
     * @return the average of all non-null elements
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Long>}
     * @since 1.0.3
     */
    default double longAverageIgnoreNull() {
        return Average.longAverageIgnoreNull(this);
    }

    /**
     * Computes the arithmetic mean of {@link Long} values in this sequence.
     *
     * @return the average of all elements
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Long>}
     * @since 1.0.3
     */
    default double longAverage() {
        return Average.longAverage(this);
    }

    /**
     * Computes the sum of {@link Long} values in this sequence, ignoring {@code null} elements.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Long>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * <p>If the sequence contains no non-null elements, this method returns {@code 0}.</p>
     *
     * @return the sum of all non-null elements, or {@code 0} if none
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Long>}
     * @since 1.0.3
     */
    default long longSumIgnoreNull() {
        return Sum.longSumIgnoreNull(this);
    }

    /**
     * Computes the sum of {@link Long} values in this sequence.
     *
     * <p>If the sequence is empty, this method returns {@code 0}.</p>
     *
     * @return the sum of all elements, or {@code 0} if the sequence is empty
     * @throws NullPointerException if any element is {@code null}
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Long>}
     * @since 1.0.3
     */
    default long longSum() {
        return Sum.longSum(this);
    }

    /**
     * Returns the maximum {@link Long} value in this sequence, ignoring {@code null} elements.
     *
     * @return the maximum non-null element
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Long>}
     * @since 1.0.3
     */
    default long longMaxIgnoreNull() {
        return Max.longMaxIgnoreNull(this);
    }

    /**
     * Returns the maximum {@link Long} value in this sequence.
     *
     * @return the maximum element
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Long>}
     * @since 1.0.3
     */
    default long longMax() {
        return Max.longMax(this);
    }

    /**
     * Returns the minimum {@link Long} value in this sequence, ignoring {@code null} elements.
     *
     * @return the minimum non-null element
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Long>}
     * @since 1.0.3
     */
    default long longMinIgnoreNull() {
        return Min.longMinIgnoreNull(this);
    }

    /**
     * Returns the minimum {@link Long} value in this sequence.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Long>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * @return the minimum element
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Long>}
     * @since 1.0.3
     */
    default long longMin() {
        return Min.longMin(this);
    }

    /**
     * Default implementation of {@link LongEnumerable}.
     *
     * <p>This class provides a concrete {@code LongEnumerable}
     * backed by a {@link Supplier} of {@link Enumerator}.
     *
     * <p>Instances of this class are typically created through
     * factory methods such as {@code Linq.of(...)} or other
     * numeric factory helpers.</p>
     *
     * <p>This implementation operates on boxed {@link Long} values.
     * It does not provide primitive specialization; therefore,
     * boxing and unboxing may occur during aggregation operations.</p>
     *
     * @since 1.0.3
     */
    public static
    class LongLinq extends Linq<Long> implements LongEnumerable {

        /**
         * Creates a {@code LongLinq} backed by the given enumerator factory.
         *
         * <p>The provided {@code factory} must produce a fresh
         * {@link Enumerator} instance on each invocation.</p>
         *
         * @param factory a supplier that produces enumerators
         */
        public LongLinq(Supplier<? extends Enumerator<Long>> factory) {
            super(factory);
        }
    }

}
