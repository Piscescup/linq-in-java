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
 * An {@code DoubleEnumerable} is an {@link Enumerable} sequence of {@link Double} values, providing additional
 * terminal operations for computing averages, sums, maximums, and minimums of the elements in the sequence.
 * <h3>Usages</h3>
 * <pre>{@code
 * DoubleEnumerable numbers = ...; // obtain a LongEnumerable from somewhere
 * double avg = numbers.doubleAverageIgnoreNull(); // compute average ignoring nulls
 * double sum = numbers.doubleSum(); // compute sum of all elements
 * double max = numbers.doubleMaxIgnoreNull(); // compute max ignoring nulls
 * double min = numbers.doubleMin(); // compute min of all elements
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.3
 */
public interface DoubleEnumerable extends Enumerable<Double> {
    /**
     * Computes the arithmetic mean of {@link Double} values in this sequence, ignoring {@code null} elements.
     *
     * @return the average of all non-null elements
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException       if this sequence is not an {@code Enumerable<Double>}
     * @since 1.0.3
     */
    default double doubleAverageIgnoreNull() {
        return Average.doubleAverageIgnoreNull(this);
    }

    /**
     * Computes the arithmetic mean of {@link Double} values in this sequence.
     *
     * @return the average of all elements
     * @throws NullPointerException     if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException       if this sequence is not an {@code Enumerable<Double>}
     * @since 1.0.3
     */
    default double doubleAverage() {
        return Average.doubleAverage(this);
    }

    /**
     * Computes the sum of {@link Double} values in this sequence, ignoring {@code null} elements.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Double>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * <p>If the sequence contains no non-null elements, this method returns {@code 0.0}.</p>
     *
     * @return the sum of all non-null elements, or {@code 0.0} if none
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Double>}
     * @since 1.0.3
     */
    default double doubleSumIgnoreNull() {
        return Sum.doubleSumIgnoreNull(this);
    }

    /**
     * Computes the sum of {@link Double} values in this sequence.
     *
     * <p>If the sequence is empty, this method returns {@code 0.0}.</p>
     *
     * @return the sum of all elements, or {@code 0.0} if empty
     * @throws NullPointerException if any element is {@code null}
     * @throws ClassCastException   if this sequence is not an {@code Enumerable<Double>}
     * @since 1.0.3
     */
    default double doubleSum() {
        return Sum.doubleSum(this);
    }

    /**
     * Returns the maximum {@link Double} value in this sequence, ignoring {@code null} elements.
     *
     * <p>Comparison is performed using {@link Double#compare(double, double)}.</p>
     *
     * @return the maximum non-null element
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Double>}
     * @since 1.0.3
     */
    default double doubleMaxIgnoreNull() {
        return Max.doubleMaxIgnoreNull(this);
    }

    /**
     * Returns the maximum {@link Double} value in this sequence.
     *
     * <p>Comparison is performed using {@link Double#compare(double, double)}.</p>
     *
     * @return the maximum element
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Double>}
     * @since 1.0.3
     */
    default double doubleMax() {
        return Max.doubleMax(this);
    }

    /**
     * Returns the minimum {@link Double} value in this sequence, ignoring {@code null} elements.
     *
     * <p>Comparison is performed using {@link Double#compare(double, double)}.</p>
     *
     * @return the minimum non-null element
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Double>}
     * @since 1.0.3
     */
    default double doubleMinIgnoreNull() {
        return Min.doubleMinIgnoreNull(this);
    }

    /**
     * Returns the minimum {@link Double} value in this sequence.
     *
     * <p>Comparison is performed using {@link Double#compare(double, double)}.</p>
     *
     * @return the minimum element
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Double>}
     * @since 1.0.3
     */
    default double doubleMin() {
        return Min.doubleMin(this);
    }

    /**
     * Default implementation of {@link DoubleEnumerable}.
     *
     * <p>This class provides a concrete {@code DoubleEnumerable}
     * backed by a {@link Supplier} of {@link Enumerator}.</p>
     *
     * <p>Instances are typically created via factory methods such as
     * {@code Linq.of(...)} or other numeric-specific factory helpers.</p>
     *
     * <p>This implementation operates on boxed {@link Double} values.
     * It does not provide primitive specialization. As a result,
     * boxing and unboxing may occur during aggregation operations
     * such as sum, min, max, and average.</p>
     *
     * <p>For performance-critical scenarios, a future primitive-based
     * implementation (e.g., using a dedicated {@code DoubleEnumerator})
     * may avoid boxing overhead.</p>
     *
     * @since 1.0.3
     */
    public static
    class DoubleLinq extends Linq<Double> implements DoubleEnumerable {

        /**
         * Creates a {@code DoubleLinq} backed by the given enumerator factory.
         *
         * <p>The provided {@code factory} must supply a new
         * {@link Enumerator} instance on each invocation.</p>
         *
         * @param factory a supplier that produces fresh enumerators
         */
        public DoubleLinq(Supplier<? extends Enumerator<Double>> factory) {
            super(factory);
        }
    }
}