package io.github.piscescup.linq.primitive;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.Linq;
import io.github.piscescup.linq.operation.terminal.Average;
import io.github.piscescup.linq.operation.terminal.Max;
import io.github.piscescup.linq.operation.terminal.Min;

import java.util.function.Supplier;

/**
 * An {@code FloatEnumerable} is an {@link Enumerable} sequence of {@link Float} values, providing additional
 * terminal operations for computing averages, maximums, and minimums of the elements in the sequence.
 *
 * <h3>Usages</h3>
 * <pre>{@code
 * FloatEnumerable numbers = ...; // obtain a FloatEnumerable from somewhere
 * double avg = numbers.floatAverageIgnoreNull(); // compute average ignoring nulls
 * float max = numbers.floatMaxIgnoreNull(); // compute max ignoring nulls
 * float min = numbers.floatMin(); // compute min of all elements
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.3
 */
public interface FloatEnumerable extends Enumerable<Float> {

    /**
     * Computes the arithmetic mean of {@link Float} values in this sequence, ignoring {@code null} elements.
     *
     * <p>The result is returned as {@code double}.</p>
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Float>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * @return the average of all non-null elements
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Float>}
     * @since 1.0.3
     */
    default double floatAverageIgnoreNull() {
        return Average.floatAverageNullable(this);
    }

    /**
     * Computes the arithmetic mean of {@link Float} values in this sequence, assuming no element is {@code null}.
     *
     * <p>The result is returned as {@code double}.</p>
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Float>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * @return the average of all elements
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Float>}
     * @since 1.0.3
     */
    default double floatAverage() {
        return Average.floatAverageNonNull(this);
    }

    /**
     * Returns the maximum {@link Float} value in this sequence, ignoring {@code null} elements.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Float>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * <p>Comparison is performed using {@link Float#compare(float, float)}.</p>
     *
     * @return the maximum non-null element
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Float>}
     * @since 1.0.3
     */
    default float floatMaxIgnoreNull() {
        return Max.floatMaxIgnoreNull(this);
    }

    /**
     * Returns the maximum {@link Float} value in this sequence.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Float>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * <p>Comparison is performed using {@link Float#compare(float, float)}.</p>
     *
     * @return the maximum element
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Float>}
     * @since 1.0.3
     */
    default float floatMax() {
        return Max.floatMax(this);
    }

    /**
     * Returns the minimum {@link Float} value in this sequence, ignoring {@code null} elements.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Float>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * <p>Comparison is performed using {@link Float#compare(float, float)}.</p>
     *
     * @return the minimum non-null element
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Float>}
     * @since 1.0.3
     */
    default float floatMinIgnoreNull() {
        return Min.floatMinIgnoreNull(this);
    }

    /**
     * Returns the minimum {@link Float} value in this sequence.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Float>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * <p>Comparison is performed using {@link Float#compare(float, float)}.</p>
     *
     * @return the minimum element
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Float>}
     * @since 1.0.3
     */
    default float floatMin() {
        return Min.floatMin(this);
    }

    /**
     * Default implementation of {@link FloatEnumerable}.
     *
     * <p>This class provides a concrete {@code FloatEnumerable}
     * backed by a {@link Supplier} of {@link Enumerator}.</p>
     *
     * <p>Instances are typically created through factory methods
     * such as {@code Linq.of(...)} or numeric-specific helper methods.</p>
     *
     * <p>This implementation operates on boxed {@link Float} values.
     * It does not provide primitive specialization. Therefore,
     * boxing and unboxing may occur during aggregation operations
     * such as sum, min, max, and average.</p>
     *
     * <p>Future primitive-based implementations (e.g., using a dedicated
     * {@code FloatEnumerator}) may eliminate boxing overhead for
     * performance-critical scenarios.</p>
     *
     * @since 1.0.3
     */
    public static
    class FloatLinq extends Linq<Float> implements FloatEnumerable {

        /**
         * Creates a {@code FloatLinq} backed by the given enumerator factory.
         *
         * <p>The provided {@code factory} must supply a new
         * {@link Enumerator} instance for each invocation.</p>
         *
         * @param factory a supplier that produces fresh enumerators
         */
        public FloatLinq(Supplier<? extends Enumerator<Float>> factory) {
            super(factory);
        }
    }

}
