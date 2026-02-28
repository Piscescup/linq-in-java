package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.Comparator;

/**
 * Provides LINQ-style {@code contains} operations for {@link Enumerable}.
 *
 * <p>If no comparator is specified, {@link Comparator#naturalOrder()} is used.
 * Therefore, the element type {@code T} must implement {@link Comparable}
 * when using the overload without a comparator.
 *
 * <p>This class is stateless and cannot be instantiated.
 *
 * <h2>Example (Natural Ordering)</h2>
 * <pre>{@code
 * Enumerable<Integer> numbers = ...
 *
 * boolean result = Contains.contains(numbers, 10);
 * }</pre>
 *
 * <h2>Example (Custom Comparator)</h2>
 * <pre>{@code
 * Comparator<String> ignoreCase = String.CASE_INSENSITIVE_ORDER;
 *
 * boolean result = Contains.contains(
 *     names,
 *     "alice",
 *     ignoreCase
 * );
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Contains {

    private Contains() {
        throw new UnsupportedOperationException(
            "No instance of " + Contains.class.getName() + " for you!"
        );
    }

    /**
     * Determines whether the specified element exists in the source sequence
     * using natural ordering.
     *
     * @param <T>     the element type
     * @param source  the source sequence
     * @param element the element to locate
     * @return {@code true} if the element is found; {@code false} otherwise
     *
     * @throws NullPointerException if {@code source} or {@code element} is {@code null}
     */
    public static <T> boolean contains(Enumerable<T> source, T element) {
        return contains(source, element, null);
    }

    /**
     * Determines whether the specified element exists in the source sequence
     * using the provided comparator.
     *
     * <p>If {@code comparator} is {@code null}, natural ordering
     * ({@link Comparator#naturalOrder()}) is used.
     *
     * @param <T>        the element type
     * @param source     the source sequence
     * @param element    the element to locate
     * @param comparator the comparator used for equality comparison;
     *                   may be {@code null}
     *
     * @return {@code true} if the element is found; {@code false} otherwise
     *
     * @throws NullPointerException if {@code source} or {@code element} is {@code null}
     * @throws ClassCastException   if natural ordering is used and
     *                              {@code T} does not implement {@link Comparable}
     */
    public static <T> boolean contains(
        Enumerable<T> source,
        T element,
        Comparator<? super T> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(element);

        @SuppressWarnings("unchecked")
        Comparator<? super T> cmp =
            (Comparator<? super T>)
                NullCheck.requireNonNullOrElse(
                    comparator,
                    Comparator.naturalOrder()
                );

        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                if (cmp.compare(element, enumerator.current()) == 0) {
                    return true;
                }
            }
        }

        return false;
    }
}