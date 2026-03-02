package io.github.piscescup.linq.enumerator;

/**
 * An enumerator that produces a sequence containing the same element
 * repeated a specified number of times.
 *
 * <p>This enumerator yields exactly {@code count} copies of {@code element}.</p>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Enumerator<String> e = new RepeatEnumerator<>("A", 3);
 *
 * while (e.moveNext()) {
 *     System.out.println(e.current());
 * }
 *
 * // Output:
 * // A
 * // A
 * // A
 * }</pre>
 *
 * <h2>Behavior</h2>
 * <ul>
 *   <li>If {@code count == 0}, the sequence is empty.</li>
 *   <li>If {@code count < 0}, an {@link IllegalArgumentException} is thrown.</li>
 *   <li>The element may be {@code null}; {@code null} values are repeated as-is.</li>
 * </ul>
 *
 * @param <T> the element type
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class RepeatEnumerator<T> extends AbstractEnumerator<T> {

    /**
     * The element to repeat.
     */
    private final T element;

    /**
     * The total number of repetitions.
     */
    private final int count;

    /**
     * Current repetition index (starts before first element).
     */
    private int index = -1;

    /**
     * Constructs a new {@code RepeatEnumerator}.
     *
     * @param element the element to repeat (may be {@code null})
     * @param count   the number of times to repeat (must be non-negative)
     *
     * @throws IllegalArgumentException if {@code count < 0}
     */
    public RepeatEnumerator(T element, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }

        this.element = element;
        this.count = count;
    }

    /**
     * Advances to the next repetition.
     *
     * @return {@code true} if another repetition exists; {@code false} otherwise
     */
    @Override
    protected boolean moveNextCore() {
        if (++index >= count) {
            return false;
        }

        this.current = element;
        return true;
    }

    /**
     * Creates a shallow copy of this enumerator,
     * preserving the current iteration state.
     *
     * @return a cloned {@code RepeatEnumerator}
     */
    @Override
    protected AbstractEnumerator<T> clone() {
        RepeatEnumerator<T> e = new RepeatEnumerator<>(element, count);
        e.index = this.index;
        return e;
    }
}