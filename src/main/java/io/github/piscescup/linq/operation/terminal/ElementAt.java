package io.github.piscescup.linq.operation.terminal;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;

/**
 * Provides LINQ-style index-based element retrieval operations
 * for {@link Enumerable}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Enumerable<String> names = ...
 *
 * String second = ElementAt.elementAt(names, 1);
 *
 * String fallback = ElementAt.elementAtOrDefault(names, 10, "Unknown");
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class ElementAt {

    private ElementAt() {
        throw new UnsupportedOperationException(
            "No instance of " + ElementAt.class.getName() + " for you!"
        );
    }

    /**
     * Returns the element at the specified zero-based index in the sequence.
     *
     * <p>This method iterates through the sequence until the specified index
     * is reached.
     *
     * @param <T>    the element type
     * @param source the source sequence
     * @param index  the zero-based index
     * @return the element at the specified position
     *
     * @throws NullPointerException      if {@code source} is {@code null}
     * @throws IndexOutOfBoundsException if {@code index} is negative
     *                                   or exceeds the sequence bounds
     */
    public static <T> T elementAt(Enumerable<T> source, int index) {
        if (source == null)
            throw new NullPointerException("source");
        if (index < 0)
            throw new IndexOutOfBoundsException("index must be non-negative");

        int currentIndex = 0;
        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                if (currentIndex == index)
                    return enumerator.current();
                currentIndex++;
            }
        }
        throw new IndexOutOfBoundsException("index was out of range");
    }

    /**
     * Returns the element at the specified zero-based index in the sequence,
     * or returns {@code defaultValue} if the index is out of range.
     *
     * <p>If {@code index} is negative, {@code defaultValue} is returned immediately.
     *
     * @param <T>          the element type
     * @param source       the source sequence
     * @param index        the zero-based index
     * @param defaultValue the value to return if the index is invalid
     * @return the element at the specified position, or {@code defaultValue}
     *
     * @throws NullPointerException if {@code source} is {@code null}
     */
    public static <T> T elementAtOrDefault(
        Enumerable<T> source,
        int index,
        T defaultValue
    ) {
        if (source == null)
            throw new NullPointerException("source");
        if (index < 0)
            return defaultValue;

        int currentIndex = 0;
        try (Enumerator<T> enumerator = source.enumerator()) {
            while (enumerator.moveNext()) {
                if (currentIndex == index)
                    return enumerator.current();
                currentIndex++;
            }
        }
        return defaultValue;
    }
}