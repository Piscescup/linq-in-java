package io.github.piscescup.linq;

import io.github.piscescup.linq.enumerator.*;
import io.github.piscescup.linq.primitive.DoubleEnumerable;
import io.github.piscescup.linq.primitive.FloatEnumerable;
import io.github.piscescup.linq.primitive.IntEnumerable;
import io.github.piscescup.linq.primitive.LongEnumerable;
import io.github.piscescup.util.validation.NullCheck;

import java.util.Iterator;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Default implementation of {@link Enumerable}.
 *
 * <p>
 * {@code Linq<T>} represents a lazy, queryable sequence backed by
 * an {@link Enumerator} factory. Each call to {@link #enumerator()}
 * produces a <b>fresh</b> enumerator instance.
 * </p>
 *
 * <p>
 * The instance is typically constructed with an {@link Supplier}
 * that creates a new {@link Enumerator} on demand.
 * </p>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Enumerable<Integer> numbers = Linq.range(1, 5);
 *
 * List<Integer> evens = numbers
 *     .where(n -> n % 2 == 0)
 *     .toList();
 *
 * // evens = [2, 4]
 * }</pre>
 *
 * @param <T> the element type of the sequence
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class Linq<T> implements Enumerable<T> {

    private final Supplier<? extends Enumerator<T>> factory;

    protected Linq(Supplier<? extends Enumerator<T>> factory) {
        NullCheck.requireNonNull(factory);
        this.factory = factory;
    }

    @Override
    public Enumerator<T> enumerator() {
        return factory.get();
    }

    /**
     * Returns an empty sequence of the same element type.
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>Returns a sequence that yields no elements.</li>
     *   <li>The returned sequence should be safe to enumerate multiple times.</li>
     * </ul>
     *
     * <h3>Complexity</h3>
     * <p>Time: O(1). Space: O(1).</p>
     *
     * @return empty sequence
     * @throws RuntimeException if empty sequence cannot be created (implementation-defined)
     */
    @SuppressWarnings("unchecked")
    public static <T> Enumerable<T> empty() {
        return (Enumerable<T>) EmptyLinq.INSTANCE;
    }

    /**
     * Wraps an {@link Enumerable} as a {@link Linq} sequence.
     *
     * <p>Useful if you want a canonical concrete type as the chain root.</p>
     */
    public static <T> Enumerable<T> from(Enumerable<T> source) {
        NullCheck.requireNonNull(source);
        return new Linq<>(source::enumerator);
    }

    /**
     * Creates a sequence from an {@link Iterable}.
     *
     * @param iterable iterable source
     * @param <T> element type
     * @return a sequence enumerating the iterable
     * @throws NullPointerException if {@code iterable} is null
     */
    public static <T> Enumerable<T> fromIterable(Iterable<? extends T> iterable) {
        NullCheck.requireNonNull(iterable);
        return new Linq<>(() -> new IterableEnumerator<>(iterable));
    }

    /**
     * Creates a sequence from an iterator factory.
     *
     * <p>
     * We require a factory (not a single iterator instance) to guarantee that each enumeration
     * gets a fresh iterator.
     *
     * @param iteratorFactory factory that creates a new iterator each time
     * @param <T> element type
     * @return a sequence enumerating iterators produced by {@code iteratorFactory}
     * @throws NullPointerException if {@code iteratorFactory} is null
     */
    public static <T> Enumerable<T> fromIterator(Supplier<? extends Iterator<? extends T>> iteratorFactory) {
        NullCheck.requireNonNull(iteratorFactory);
        return new Linq<>(() -> new IteratorEnumerator<>(iteratorFactory.get()));
    }

    /**
     * Creates a sequence from an array.
     *
     * @param items items
     * @param <T> element type
     * @return sequence over the array
     * @throws NullPointerException if {@code items} is null
     */
    @SafeVarargs
    public static <T> Enumerable<T> of(T... items) {
        NullCheck.requireNonNull(items);
        return new Linq<>(() -> new ArrayEnumerator<>(items));
    }

    /**
     * Creates a sequence from an array.
     *
     * @param array the source array
     * @param <T> element type
     * @return sequence over the array
     * @throws NullPointerException if {@code items} is null
     */
    public static <T> Enumerable<T> fromArray(T[] array) {
        NullCheck.requireNonNull(array);
        return new Linq<>(() -> new ArrayEnumerator<>(array));
    }

    /**
     * Generates a sequence containing the same element repeated a specified number of times.
     *
     * <p>The resulting sequence contains exactly {@code count} copies of {@code element}.</p>
     *
     * <h3>Example</h3>
     *
     * <pre>{@code
     * List<String> result = Linq.repeat("A", 3).toList();
     * // result = ["A", "A", "A"]
     *
     * List<String> empty = Linq.repeat("A", 0).toList();
     * // result = []
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>If {@code count == 0}, the sequence is empty.</li>
     *   <li>If {@code count < 0}, an {@link IllegalArgumentException} is thrown.</li>
     *   <li>{@code element} may be {@code null}; null values are repeated as-is.</li>
     * </ul>
     *
     * @param element the element to repeat (may be {@code null})
     * @param count   the number of repetitions (must be non-negative)
     * @param <T>     the element type
     * @return a lazy {@link Enumerable} producing repeated elements
     *
     * @since 1.0.0
     */
    public static <T> Enumerable<T> repeat(T element, int count) {
        return new Linq<>(() -> new RepeatEnumerator<>(element, count));
    }

    /**
     * Generates a sequence of consecutive {@link Integer} values.
     *
     * <p>The resulting sequence contains exactly {@code count} integers,
     * starting from {@code start} (inclusive), incremented by {@code 1}.</p>
     *
     * <p>The generated sequence is:</p>
     *
     * <pre>{@code
     * start, start + 1, start + 2, ..., start + count - 1
     * }</pre>
     *
     * <h3>Example</h3>
     *
     * <pre>{@code
     * List<Integer> result = Linq.range(5, 3).toList();
     * // result = [5, 6, 7]
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>If {@code count == 0}, the sequence is empty.</li>
     *   <li>If {@code count < 0}, an {@link IllegalArgumentException} is thrown.</li>
     *   <li>Integer overflow may occur during enumeration.</li>
     * </ul>
     *
     * @param start the first value in the sequence (inclusive)
     * @param count the number of elements to generate (must be non-negative)
     * @return a lazy {@link Enumerable} producing the specified range
     *
     * @since 1.0.0
     */
    public static IntEnumerable range(int start, int count) {
        return new IntEnumerable.IntLinq(() -> new RangeEnumerator(start, count));
    }

    /**
     * Generates a sequence of {@link Integer} values with a custom step.
     *
     * <p>The resulting sequence contains exactly {@code count} integers,
     * starting from {@code start} (inclusive), where each subsequent value
     * is increased by {@code step}.</p>
     *
     * <p>The generated sequence is:</p>
     *
     * <pre>{@code
     * start, start + step, start + 2*step, ..., start + (count - 1)*step
     * }</pre>
     *
     * <h3>Example</h3>
     *
     * <pre>{@code
     * List<Integer> result = Linq.range(10, 4, 2).toList();
     * // result = [10, 12, 14, 16]
     *
     * List<Integer> descending = Linq.range(5, 3, -1).toList();
     * // result = [5, 4, 3]
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>If {@code count == 0}, the sequence is empty.</li>
     *   <li>If {@code count < 0} or {@code step == 0}, an {@link IllegalArgumentException} is thrown.</li>
     *   <li>Overflow during iteration may throw {@link ArithmeticException}.</li>
     * </ul>
     *
     * @param start the first value in the sequence (inclusive)
     * @param count the number of elements to generate (must be non-negative)
     * @param step  the increment between elements (must be non-zero)
     * @return a lazy {@link Enumerable} producing the specified stepped range
     *
     * @since 1.0.0
     */
    public static IntEnumerable range(int start, int count, int step) {
        return new IntEnumerable.IntLinq(() -> new RangeEnumerator(start, count, step));
    }

    /**
     * Creates an {@link IntEnumerable} from the given {@code Integer} array.
     *
     * <p>The input array is boxed into {@link Integer} elements. This method creates
     * a new {@code Integer[]} and copies all values.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * IntEnumerable xs = Linq.ints(1, 2, 3);
     * int sum = xs.intSum();
     * }</pre>
     *
     * @param items source values
     * @return an {@link IntEnumerable} over the provided values
     * @throws NullPointerException if {@code items} is {@code null}
     * @since 1.0.3
     */
    public static IntEnumerable ints(Integer... items) {
        NullCheck.requireNonNull(items);

        return new IntEnumerable.IntLinq(() -> new ArrayEnumerator<>(items));
    }

    /**
     * Creates a {@link LongEnumerable} from the given {@code Long} values.
     *
     * <p>The values are boxed lazily during enumeration.
     * No defensive copy of the array is performed.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * LongEnumerable xs = Linq.longs(10L, 20L, 30L);
     * long sum = xs.longSum();     // 60
     * long min = xs.longMin();     // 10
     * }</pre>
     *
     * @param items source values
     * @return a {@link LongEnumerable} representing the provided values
     * @throws NullPointerException if {@code items} is {@code null}
     * @since 1.0.3
     */
    public static LongEnumerable longs(Long... items) {
        NullCheck.requireNonNull(items);
        return new LongEnumerable.LongLinq(() -> new ArrayEnumerator<>(items));
    }

    /**
     * Creates a {@link DoubleEnumerable} from the given {@code Double} values.
     *
     * <p>The values are boxed lazily during enumeration.
     * No defensive copy of the array is performed.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * DoubleEnumerable xs = Linq.doubles(10L, 20L, 30L);
     * double sum = xs.doubleSum();     // 60
     * double min = xs.doubleMin();     // 10
     * }</pre>
     *
     * @param items source values
     * @return a {@link DoubleEnumerable} representing the provided values
     * @throws NullPointerException if {@code items} is {@code null}
     * @since 1.0.3
     */
    public static DoubleEnumerable doubles(Double... items) {
        NullCheck.requireNonNull(items);
        return new DoubleEnumerable.DoubleLinq(() -> new ArrayEnumerator<>(items));
    }

    /**
     * Creates a {@link FloatEnumerable} from the given {@code Float} values.
     *
     * <p>The values are boxed lazily during enumeration.
     * No defensive copy of the array is performed.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * FloatEnumerable xs = Linq.float(10L, 20L, 30L);
     * float sum = xs.floatSum();     // 60
     * float min = xs.floatMin();     // 10
     * }</pre>
     *
     * @param items source values
     * @return a {@link FloatEnumerable} representing the provided values
     * @throws NullPointerException if {@code items} is {@code null}
     * @since 1.0.3
     */
    public static FloatEnumerable floats(Float... items) {
        NullCheck.requireNonNull(items);
        return new FloatEnumerable.FloatLinq(() -> new ArrayEnumerator<>(items));
    }

    /**
     * Creates an {@link IntEnumerable} from primitive {@code int} values.
     *
     * <p>Values are boxed lazily during enumeration.
     * No defensive copy is performed.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * IntEnumerable xs = Linq.primitiveInts(1, 2, 3);
     * int sum = xs.intSum();
     * }</pre>
     *
     * @param items source values
     * @return an {@link IntEnumerable} representing the provided values
     * @throws NullPointerException if {@code items} is {@code null}
     * @since 1.0.3
     */
    public static IntEnumerable primitiveInts(int... items) {
        NullCheck.requireNonNull(items);

        return new IntEnumerable.IntLinq(() -> new AbstractEnumerator<Integer>() {

            private int index;

            @Override
            protected boolean moveNextCore() {
                if (index >= items.length)
                    return false;

                this.current = items[index++]; // boxing
                return true;
            }

            @Override
            protected AbstractEnumerator<Integer> clone()
                throws CloneNotSupportedException {
                throw new CloneNotSupportedException(
                    "Primitive int enumerator cannot be cloned. " +
                        "Enumerate again to obtain a fresh enumerator."
                );
            }
        });
    }

    /**
     * Creates a {@link LongEnumerable} from primitive {@code long} values.
     *
     * <p>Values are boxed lazily during enumeration.
     * No defensive copy is performed.</p>
     *
     * @param items source values
     * @return a {@link LongEnumerable} representing the provided values
     * @throws NullPointerException if {@code items} is {@code null}
     * @since 1.0.3
     */
    public static LongEnumerable primitiveLongs(long... items) {
        NullCheck.requireNonNull(items);

        return new LongEnumerable.LongLinq(() -> new AbstractEnumerator<Long>() {

            private int index;

            @Override
            protected boolean moveNextCore() {
                if (index >= items.length)
                    return false;

                this.current = items[index++]; // boxing
                return true;
            }

            @Override
            protected AbstractEnumerator<Long> clone()
                throws CloneNotSupportedException {
                throw new CloneNotSupportedException(
                    "Primitive long enumerator cannot be cloned."
                );
            }
        });
    }

    /**
     * Creates a {@link FloatEnumerable} from primitive {@code float} values.
     *
     * <p>Values are boxed lazily during enumeration.
     * No defensive copy is performed.</p>
     *
     * @param items source values
     * @return a {@link FloatEnumerable} representing the provided values
     * @throws NullPointerException if {@code items} is {@code null}
     * @since 1.0.3
     */
    public static FloatEnumerable primitiveFloats(float... items) {
        NullCheck.requireNonNull(items);

        return new FloatEnumerable.FloatLinq(() -> new AbstractEnumerator<Float>() {

            private int index;

            @Override
            protected boolean moveNextCore() {
                if (index >= items.length)
                    return false;

                this.current = items[index++]; // boxing
                return true;
            }

            @Override
            protected AbstractEnumerator<Float> clone()
                throws CloneNotSupportedException {
                throw new CloneNotSupportedException(
                    "Primitive float enumerator cannot be cloned."
                );
            }
        });
    }

    /**
     * Creates a {@link DoubleEnumerable} from primitive {@code double} values.
     *
     * <p>Values are boxed lazily during enumeration.
     * No defensive copy is performed.</p>
     *
     * @param items source values
     * @return a {@link DoubleEnumerable} representing the provided values
     * @throws NullPointerException if {@code items} is {@code null}
     * @since 1.0.3
     */
    public static DoubleEnumerable primitiveDoubles(double... items) {
        NullCheck.requireNonNull(items);

        return new DoubleEnumerable.DoubleLinq(() -> new AbstractEnumerator<Double>() {

            private int index;

            @Override
            protected boolean moveNextCore() {
                if (index >= items.length)
                    return false;

                this.current = items[index++]; // boxing
                return true;
            }

            @Override
            protected AbstractEnumerator<Double> clone()
                throws CloneNotSupportedException {
                throw new CloneNotSupportedException(
                    "Primitive double enumerator cannot be cloned."
                );
            }
        });
    }
}
