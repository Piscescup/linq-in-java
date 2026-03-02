package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;

import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * Provides numeric aggregation operations for {@link Enumerable} sequences.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Sum {

    private Sum() {
        throw new UnsupportedOperationException(
            "No instance " + Sum.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Computes the sum of {@code int}-mapped values of the elements in the given sequence.
     *
     * <p>{@code null} elements are ignored.
     *
     * <p>Overflow is detected using {@link Math#addExact(long, long)}.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * Enumerable<Person> people = ...
     * long totalAge = Sum.sum(people, Person::getAge);
     * }</pre>
     *
     * @param source the source sequence
     * @param intMapping a function mapping each element to an {@code int} value
     * @param <T> the element type
     * @return the sum of all mapped values
     * @throws NullPointerException if {@code source} or {@code intMapping} is null
     * @throws ArithmeticException if numeric overflow occurs
     * @since 1.0.0
     */
    public static <T> long sum(
        Enumerable<T> source,
        ToIntFunction<? super T> intMapping
    ) {
        long sum = 0;

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                T current = enumerator.current();
                if (current == null) continue;

                int value = intMapping.applyAsInt(current);
                sum = Math.addExact(sum, value);
            }
        }

        return sum;
    }

    /**
     * Computes the sum of {@code double}-mapped values of the elements in the given sequence.
     *
     * <p>{@code null} elements are ignored.
     *
     * <p>Unlike integer-based overloads, this method does not perform overflow detection.
     * Floating-point arithmetic follows IEEE 754 semantics.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * Enumerable<Order> orders = ...
     * double totalPrice = Sum.sum(orders, Order::getPrice);
     * }</pre>
     *
     * @param source the source sequence
     * @param doubleMapping a function mapping each element to a {@code double} value
     * @param <T> the element type
     * @return the sum of all mapped values
     * @throws NullPointerException if {@code source} or {@code doubleMapping} is null
     * @since 1.0.0
     */
    public static <T> double sum(
        Enumerable<T> source,
        ToDoubleFunction<? super T> doubleMapping
    ) {
        double sum = 0;

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                T current = enumerator.current();
                if (current == null) continue;

                double value = doubleMapping.applyAsDouble(current);
                sum += value;
            }
        }

        return sum;
    }

    /**
     * Computes the sum of {@code long}-mapped values of the elements in the given sequence.
     *
     * <p>{@code null} elements are ignored.
     *
     * <p>Overflow is detected using {@link Math#addExact(long, long)}.
     *
     * <p><b>Example:</b>
     * <pre>{@code
     * Enumerable<Transaction> tx = ...
     * long totalAmount = Sum.sum(tx, Transaction::getAmount);
     * }</pre>
     *
     * @param source the source sequence
     * @param longMapping a function mapping each element to a {@code long} value
     * @param <T> the element type
     * @return the sum of all mapped values
     * @throws NullPointerException if {@code source} or {@code longMapping} is null
     * @throws ArithmeticException if numeric overflow occurs
     * @since 1.0.0
     */
    public static <T> long sum(
        Enumerable<T> source,
        ToLongFunction<? super T> longMapping
    ) {
        long sum = 0;

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                T current = enumerator.current();
                if (current == null) continue;

                long value = longMapping.applyAsLong(current);
                sum = Math.addExact(sum, value);
            }
        }

        return sum;
    }
}