package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.function.Predicate;

/**
 * Provides quantifier operations for {@link Enumerable} sequences.
 *
 * <p>This class supplies LINQ-style {@code all} and {@code any} operators,
 * which evaluate boolean conditions over a sequence of elements.
 *
 * <p>These methods determine whether:
 * <ul>
 *     <li>All elements satisfy a given predicate</li>
 *     <li>Any element exists in the sequence</li>
 *     <li>Any element satisfies a given predicate</li>
 * </ul>
 *
 * <p>Evaluation is performed lazily over the sequence and stops as soon
 * as the result can be determined (short-circuit behavior).
 *
 * <p>This is a utility class and cannot be instantiated.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class AllAny {

    private AllAny() {
        throw new UnsupportedOperationException(
            "No instance of " + AllAny.class.getName() + " for you!"
        );
    }

    /**
     * Determines whether all elements of a sequence satisfy a condition.
     *
     * <p>The evaluation stops immediately when an element that does not
     * satisfy the predicate is encountered.
     *
     * <h3>Example</h3>
     *
     * <pre>{@code
     * Enumerable<Integer> numbers = Enumerable.of(2, 4, 6, 8);
     *
     * boolean result = AllAny.all(
     *     numbers,
     *     n -> n % 2 == 0
     * );
     *
     * // result: true
     * }</pre>
     *
     * @param source    the source sequence
     * @param predicate the condition to test for each element
     * @param <T>       the element type
     * @return {@code true} if all elements satisfy the condition;
     *         {@code true} if the sequence is empty;
     *         {@code false} otherwise
     *
     * @throws NullPointerException if {@code source} or {@code predicate} is {@code null}
     */
    public static <T> boolean all(
        Enumerable<T> source, Predicate<? super T> predicate
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                if (!predicate.test(enumerator.current()))
                    return false;
            }
        }
        return true;
    }

    /**
     * Determines whether a sequence contains any elements.
     *
     * <p>This method returns {@code true} as soon as the first element
     * is encountered.
     *
     * <h3>Example</h3>
     *
     * <pre>{@code
     * Enumerable<String> items = Enumerable.of("A", "B", "C");
     *
     * boolean hasElements = AllAny.any(items);
     *
     * // hasElements: true
     * }</pre>
     *
     * @param source the source sequence
     * @param <T>    the element type
     * @return {@code true} if the sequence contains at least one element;
     *         {@code false} if it is empty
     *
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public static <T> boolean any(Enumerable<T> source) {
        NullCheck.requireNonNull(source);

        try (Enumerator<T> enumerator = source.enumerator()) {
            return enumerator.moveNext();
        }
    }

    /**
     * Determines whether any element of a sequence satisfies a condition.
     *
     * <p>The evaluation stops immediately when an element that satisfies
     * the predicate is encountered.
     *
     * <h3>Example</h3>
     *
     * <pre>{@code
     * Enumerable<Integer> numbers = Enumerable.of(1, 3, 5, 8);
     *
     * boolean containsEven = AllAny.any(
     *     numbers,
     *     n -> n % 2 == 0
     * );
     *
     * // containsEven: true
     * }</pre>
     *
     * @param source    the source sequence
     * @param predicate the condition to test for each element
     * @param <T>       the element type
     * @return {@code true} if at least one element satisfies the condition;
     *         {@code false} otherwise
     *
     * @throws NullPointerException if {@code source} or {@code predicate} is {@code null}
     */
    public static <T> boolean any(
        Enumerable<T> source, Predicate<? super T> predicate
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                if (predicate.test(enumerator.current()))
                    return true;
            }
        }
        return false;
    }
}