package io.github.piscescup.linq;

import java.util.*;

/**
 * Represents a grouping of elements that share a common key.
 *
 * <p>
 * A {@code Groupable<K, E>} is the result of a grouping operation
 * (such as {@code groupBy}). It encapsulates:
 * </p>
 *
 * <ul>
 *   <li>A grouping {@link #key()}</li>
 *   <li>A collection of elements {@link #elements()} associated with that key</li>
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Enumerable<String> words = Linq.of("apple", "ant", "banana");
 *
 * Enumerable<Groupable<Character, String>> groups =
 *     words.groupBy(w -> w.charAt(0));
 *
 * for (Groupable<Character, String> group : groups) {
 *     System.out.println(group.key() + " -> " + group.elements());
 * }
 *
 * // Possible output:
 * // a -> [apple, ant]
 * // b -> [banana]
 * }</pre>
 *
 *
 * @param <K> the key type
 * @param <E> the element type contained in the group
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public interface Groupable<K, E> {

    /**
     * Returns the key associated with this group.
     *
     * @return the grouping key
     */
    K key();

    /**
     * Returns the elements that belong to this group.
     *
     * <p>
     * The returned list represents the materialized elements of the group.
     * Modifiability depends on the concrete implementation.
     * </p>
     *
     * @return a list of grouped elements
     */
    List<E> elements();

    /**
     * Returns the grouped elements as an {@link Enumerable}.
     *
     * <h3>Example</h3>
     *
     * <pre>{@code
     * Enumerable<Groupable<Integer, String>> groups =
     *     words.groupBy(String::length);
     *
     * for (Groupable<Integer, String> g : groups) {
     *     int count = g.elementsAsEnumerable().count();
     *     System.out.println(g.key() + " -> " + count);
     * }
     * }</pre>
     *
     * @return an {@link Enumerable} view of the grouped elements
     *
     * @since 1.0.0
     */
    default Enumerable<E> elementsAsEnumerable() {
        return Linq.fromIterable(elements());
    }
}