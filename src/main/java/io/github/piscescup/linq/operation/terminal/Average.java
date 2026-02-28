package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;


/**
 * Provides average (mean) aggregation operations for {@link Enumerable} sequences.
 *
 * <p>Two aggregation modes are supported:
 * <ul>
 *     <li><b>Nullable</b> — All elements must be non-null. A {@link NullPointerException}
 *         is thrown if a null element is encountered.</li>
 *     <li><b>NonNull</b> — Null elements are ignored during aggregation.</li>
 * </ul>
 *
 * <p>If no elements are aggregated (empty sequence or all elements are null
 * in {@code NonNull} mode), an {@link IllegalArgumentException} is thrown.
 *
 * <p>This is a utility class and cannot be instantiated.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Average {
    private Average() {
        throw new UnsupportedOperationException(
            "No instance of " + Average.class.getName() + " for you!"
        );
    }

    /**
     * Computes the arithmetic mean of an {@link Integer} sequence.
     * All elements must be non-null.
     *
     * @param source the source sequence
     * @return the average value
     * @throws NullPointerException if {@code source} is {@code null} or a null element is encountered
     * @throws IllegalArgumentException if the sequence is empty
     */
    public static double intAverageNullable(Enumerable<Integer> source) {
        NullCheck.requireNonNull(source);

        long sum = 0L;
        long count = 0L;

        try (Enumerator<Integer> e = source.enumerator()) {
            while (e.moveNext()) {
                Integer value = e.current();
                if (value == null) {
                    throw new NullPointerException("Null element encountered.");
                }
                sum += value;
                count++;
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence is empty.");
        }

        return (double) sum / count;
    }

    /**
     * Computes the arithmetic mean of an {@link Integer} sequence, ignoring null elements.
     *
     * @param source the source sequence
     * @return the average of non-null elements
     * @throws NullPointerException if {@code source} is {@code null}
     * @throws IllegalArgumentException if no non-null elements exist
     */
    public static double intAverageNonNull(Enumerable<Integer> source) {
        NullCheck.requireNonNull(source);

        long sum = 0L;
        long count = 0L;

        try (Enumerator<Integer> e = source.enumerator()) {
            while (e.moveNext()) {
                Integer value = e.current();
                if (value != null) {
                    sum += value;
                    count++;
                }
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence contains no non-null elements.");
        }

        return (double) sum / count;
    }

    /**
     * Computes the arithmetic mean of a {@link Long} sequence.
     * All elements must be non-null.
     *
     * @param source the source sequence
     * @return the average value
     * @throws NullPointerException if {@code source} is {@code null} or a null element is encountered
     * @throws IllegalArgumentException if the sequence is empty
     */
    public static double longAverageNullable(Enumerable<Long> source) {
        NullCheck.requireNonNull(source);

        long sum = 0L;
        long count = 0L;

        try (Enumerator<Long> e = source.enumerator()) {
            while (e.moveNext()) {
                Long value = e.current();
                if (value == null) {
                    throw new NullPointerException("Null element encountered.");
                }
                sum += value;
                count++;
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence is empty.");
        }

        return (double) sum / count;
    }

    /**
     * Computes the arithmetic mean of a {@link Long} sequence, ignoring null elements.
     *
     * @param source the source sequence
     * @return the average of non-null elements
     * @throws NullPointerException if {@code source} is {@code null}
     * @throws IllegalArgumentException if no non-null elements exist
     */
    public static double longAverageNonNull(Enumerable<Long> source) {
        NullCheck.requireNonNull(source);

        long sum = 0L;
        long count = 0L;

        try (Enumerator<Long> e = source.enumerator()) {
            while (e.moveNext()) {
                Long value = e.current();
                if (value != null) {
                    sum += value;
                    count++;
                }
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence contains no non-null elements.");
        }

        return (double) sum / count;
    }

    /**
     * Computes the arithmetic mean of a {@link Float} sequence.
     * All elements must be non-null.
     *
     * @param source the source sequence
     * @return the average value
     * @throws NullPointerException if {@code source} is {@code null} or a null element is encountered
     * @throws IllegalArgumentException if the sequence is empty
     */
    public static double floatAverageNullable(Enumerable<Float> source) {
        NullCheck.requireNonNull(source);

        double sum = 0.0;
        long count = 0L;

        try (Enumerator<Float> e = source.enumerator()) {
            while (e.moveNext()) {
                Float value = e.current();
                if (value == null) {
                    throw new NullPointerException("Null element encountered.");
                }
                sum += value;
                count++;
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence is empty.");
        }

        return sum / count;
    }

    /**
     * Computes the arithmetic mean of a {@link Float} sequence, ignoring null elements.
     *
     * @param source the source sequence
     * @return the average of non-null elements
     * @throws NullPointerException if {@code source} is {@code null}
     * @throws IllegalArgumentException if no non-null elements exist
     */
    public static double floatAverageNonNull(Enumerable<Float> source) {
        NullCheck.requireNonNull(source);

        double sum = 0.0;
        long count = 0L;

        try (Enumerator<Float> e = source.enumerator()) {
            while (e.moveNext()) {
                Float value = e.current();
                if (value != null) {
                    sum += value;
                    count++;
                }
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence contains no non-null elements.");
        }

        return sum / count;
    }

    /**
     * Computes the arithmetic mean of a {@link Double} sequence.
     * All elements must be non-null.
     *
     * @param source the source sequence
     * @return the average value
     * @throws NullPointerException if {@code source} is {@code null} or a null element is encountered
     * @throws IllegalArgumentException if the sequence is empty
     */
    public static double doubleAverageNullable(Enumerable<Double> source) {
        NullCheck.requireNonNull(source);

        double sum = 0.0;
        long count = 0L;

        try (Enumerator<Double> e = source.enumerator()) {
            while (e.moveNext()) {
                Double value = e.current();
                if (value == null) {
                    throw new NullPointerException("Null element encountered.");
                }
                sum += value;
                count++;
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence is empty.");
        }

        return sum / count;
    }

    /**
     * Computes the arithmetic mean of a {@link Double} sequence, ignoring null elements.
     *
     * @param source the source sequence
     * @return the average of non-null elements
     * @throws NullPointerException if {@code source} is {@code null}
     * @throws IllegalArgumentException if no non-null elements exist
     */
    public static double doubleAverageNonNull(Enumerable<Double> source) {
        NullCheck.requireNonNull(source);

        double sum = 0.0;
        long count = 0L;

        try (Enumerator<Double> e = source.enumerator()) {
            while (e.moveNext()) {
                Double value = e.current();
                if (value != null) {
                    sum += value;
                    count++;
                }
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence contains no non-null elements.");
        }

        return sum / count;
    }

    /**
     * Computes the arithmetic mean of a {@link BigDecimal} sequence.
     * All elements must be non-null.
     *
     * <p>The calculation uses the provided {@link MathContext} for addition and division.
     *
     * @param source the source sequence
     * @param context the math context defining precision and rounding
     * @return the average value
     * @throws NullPointerException if {@code source}, {@code context}, or an element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     */
    public static BigDecimal decimalAverageNullable(
        Enumerable<BigDecimal> source,
        MathContext context
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(context);

        BigDecimal sum = BigDecimal.ZERO;
        long count = 0L;

        try (Enumerator<BigDecimal> e = source.enumerator()) {
            while (e.moveNext()) {
                BigDecimal value = e.current();
                if (value == null) {
                    throw new NullPointerException("Null element encountered.");
                }
                sum = sum.add(value, context);
                count++;
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence is empty.");
        }

        return sum.divide(BigDecimal.valueOf(count), context);
    }

    /**
     * Computes the arithmetic mean of a {@link BigDecimal} sequence, ignoring null elements.
     *
     * @param source the source sequence
     * @param context the math context defining precision and rounding
     * @return the average of non-null elements
     * @throws NullPointerException if {@code source} or {@code context} is {@code null}
     * @throws IllegalArgumentException if no non-null elements exist
     */
    public static BigDecimal decimalAverageNonNull(
        Enumerable<BigDecimal> source,
        MathContext context
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(context);

        BigDecimal sum = BigDecimal.ZERO;
        long count = 0L;

        try (Enumerator<BigDecimal> e = source.enumerator()) {
            while (e.moveNext()) {
                BigDecimal value = e.current();
                if (value != null) {
                    sum = sum.add(value, context);
                    count++;
                }
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence contains no non-null elements.");
        }

        return sum.divide(BigDecimal.valueOf(count), context);
    }

    /**
     * Computes the arithmetic mean of a mapped integer projection, ignoring null source elements.
     *
     * @param source the source sequence
     * @param intMapping mapping function producing {@code int} values
     * @param <T> element type
     * @return the average value
     * @throws NullPointerException if {@code source} or {@code intMapping} is {@code null}
     * @throws IllegalArgumentException if no non-null elements exist
     */
    public static <T> double average(
        Enumerable<T> source,
        ToIntFunction<? super T> intMapping
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(intMapping);

        long sum = 0L;
        long count = 0L;

        try (Enumerator<T> e = source.enumerator()) {
            while (e.moveNext()) {
                T current = e.current();
                if (current != null) {
                    sum += intMapping.applyAsInt(current);
                    count++;
                }
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence contains no non-null elements.");
        }

        return (double) sum / count;
    }

    /**
     * Computes the arithmetic mean of a mapped long projection, ignoring null source elements.
     *
     * @param source the source sequence
     * @param longMapping mapping function producing {@code long} values
     * @param <T> element type
     * @return the average value
     * @throws NullPointerException if {@code source} or {@code longMapping} is {@code null}
     * @throws IllegalArgumentException if no non-null elements exist
     */
    public static <T> double average(
        Enumerable<T> source,
        ToLongFunction<? super T> longMapping
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(longMapping);

        long sum = 0L;
        long count = 0L;

        try (Enumerator<T> e = source.enumerator()) {
            while (e.moveNext()) {
                T current = e.current();
                if (current != null) {
                    sum += longMapping.applyAsLong(current);
                    count++;
                }
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence contains no non-null elements.");
        }

        return (double) sum / count;
    }

    /**
     * Computes the arithmetic mean of a mapped double projection, ignoring null source elements.
     *
     * @param source the source sequence
     * @param doubleMapping mapping function producing {@code double} values
     * @param <T> element type
     * @return the average value
     * @throws NullPointerException if {@code source} or {@code doubleMapping} is {@code null}
     * @throws IllegalArgumentException if no non-null elements exist
     */
    public static <T> double average(
        Enumerable<T> source,
        ToDoubleFunction<? super T> doubleMapping
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(doubleMapping);

        double sum = 0.0;
        long count = 0L;

        try (Enumerator<T> e = source.enumerator()) {
            while (e.moveNext()) {
                T current = e.current();
                if (current != null) {
                    sum += doubleMapping.applyAsDouble(current);
                    count++;
                }
            }
        }

        if (count == 0L) {
            throw new IllegalArgumentException("Source sequence contains no non-null elements.");
        }

        return sum / count;
    }
}
