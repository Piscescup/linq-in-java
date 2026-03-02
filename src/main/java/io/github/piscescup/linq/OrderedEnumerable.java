package io.github.piscescup.linq;

import io.github.piscescup.linq.operation.intermediate.ThenBy;

import java.util.Comparator;
import java.util.function.Function;

/**
 * Represents an ordered sequence that supports secondary ordering composition.
 *
 * @param <T> element type
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public interface OrderedEnumerable<T> extends Enumerable<T> {

    /**
     * Returns the comparator that defines the ordering of this sequence.
     *
     * @return ordering comparator
     */
    Comparator<? super T> orderingComparator();

    /**
     * Returns the original source sequence (before ordering).
     *
     * @return source sequence
     */
    Enumerable<T> source();

    /**
     * Adds a secondary ascending ordering by key (thenBy) using a key comparator.
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>Defines a secondary ordering applied after a primary ordering.</li>
     *   <li>If the sequence is not already ordered, behavior is implementation-defined (recommended: treat as {@code orderBy}).</li>
     * </ul>
     *
     * <h3>Complexity</h3>
     * <p>Typical: Time O(n log n) when realized (depends on sorting strategy). Space O(n).</p>
     *
     * @param keyExtractor extracts secondary sort key
     * @param comparator comparator for the secondary key
     * @param <K> key type
     * @return sequence with secondary ascending ordering
     * @throws NullPointerException if {@code keyExtractor} or {@code comparator} is {@code null}
     * @throws RuntimeException if ordering cannot be applied or supplied function/comparator throws
     */
    default <K> OrderedEnumerable<T> thenBy(Function<? super T, ? extends K> keyExtractor, Comparator<? super K> comparator) {
        return ThenBy.thenBy(this, keyExtractor, comparator);
    }

    /**
     * Adds a secondary ascending ordering by key (thenBy) using natural key ordering.
     *
     * <h3>Boundary</h3>
     * <ul>
     *   <li>If keys are not mutually comparable at runtime, throws {@link ClassCastException} (implementation-defined).</li>
     * </ul>
     *
     * @param keyExtractor extracts secondary sort key
     * @param <K> key type
     * @return sequence with secondary ascending ordering
     * @throws NullPointerException if {@code keyExtractor} is {@code null}
     * @throws ClassCastException if extracted keys are not mutually comparable (implementation-defined)
     * @throws RuntimeException if ordering cannot be applied or keyExtractor throws
     */
    default <K extends Comparable<? super K>> OrderedEnumerable<T> thenBy(Function<? super T, ? extends K> keyExtractor) {
        return ThenBy.thenBy(this, keyExtractor);
    }

    /**
     * Adds a secondary descending ordering by key (thenByDescending) using a key comparator.
     *
     * @param keyExtractor extracts secondary sort key
     * @param comparator comparator for the secondary key
     * @param <K> key type
     * @return sequence with secondary descending ordering
     * @throws NullPointerException if {@code keyExtractor} or {@code comparator} is {@code null}
     * @throws RuntimeException if ordering cannot be applied or supplied function/comparator throws
     */
    default <K> OrderedEnumerable<T> thenDescendingBy(
        Function<? super T, ? extends K> keyExtractor, Comparator<? super K> comparator
    ) {
        return ThenBy.thenDescendingBy(this, keyExtractor, comparator);
    }

    /**
     * Adds a secondary descending ordering by key (thenByDescending) using natural key ordering.
     *
     * @param keyExtractor extracts secondary sort key
     * @param <K> key type
     * @return sequence with secondary descending ordering
     * @throws NullPointerException if {@code keyExtractor} is {@code null}
     * @throws ClassCastException if extracted keys are not mutually comparable (implementation-defined)
     * @throws RuntimeException if ordering cannot be applied or keyExtractor throws
     */
    default <K extends Comparable<? super K>> OrderedEnumerable<T> thenDescendingBy(Function<? super T, ? extends K> keyExtractor) {
        return ThenBy.thenDescendingBy(this, keyExtractor);
    }

}
