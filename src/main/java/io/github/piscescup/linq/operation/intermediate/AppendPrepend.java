package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

/**
 * Provides LINQ-style append and prepend operations.
 *
 * <p>
 * These operations create a new sequence by adding a single element
 * either to the end ({@link #append(Enumerable, Object)})
 * or to the beginning ({@link #prepend(Enumerable, Object)}) of a source sequence.
 *
 * <p>
 * <strong>Execution characteristics:</strong>
 * <ul>
 *     <li>Fully lazy (streaming)</li>
 *     <li>No buffering or materialization</li>
 *     <li>O(1) additional memory</li>
 *     <li>Supports infinite sequences</li>
 * </ul>
 *
 * <h2>Examples</h2>
 *
 * <pre>{@code
 * Enumerable<Integer> numbers = Enumerable.of(1, 2, 3);
 *
 * numbers.append(4);
 * // Result: 1, 2, 3, 4
 *
 * numbers.prepend(0);
 * // Result: 0, 1, 2, 3
 * }</pre>
 *
 * <p>
 * The appended or prepended element may be {@code null}.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class AppendPrepend {
    private AppendPrepend() {
        throw new UnsupportedOperationException(
            "No instance of " + AppendPrepend.class.getName() + " for you!"
        );
    }

    /**
     * Returns a sequence that contains all elements of the source sequence
     * followed by the specified element.
     *
     * <p>
     * The element is produced only after the source sequence is fully consumed.
     *
     * @param source  the source sequence
     * @param element the element to append (may be {@code null})
     * @param <T>     element type
     *
     * @return a new {@link Enumerable} with the element appended
     *
     * @throws NullPointerException if {@code source} is null
     */
    public static <T> Enumerable<T> append(Enumerable<T> source, T element) {
        NullCheck.requireNonNull(source);

        return new AppendPrependEnumerable<>(source, element, false);
    }

    /**
     * Returns a sequence that contains the specified element
     * followed by all elements of the source sequence.
     *
     * <p>
     * The prepended element is produced before the source sequence is enumerated.
     *
     * @param source  the source sequence
     * @param element the element to prepend (may be {@code null})
     * @param <T>     element type
     *
     * @return a new {@link Enumerable} with the element prepended
     *
     * @throws NullPointerException if {@code source} is null
     */
    public static <T> Enumerable<T> prepend(Enumerable<T> source, T element) {
        NullCheck.requireNonNull(source);

        return new AppendPrependEnumerable<>(source, element, true);
    }
}

/**
 * Enumerable implementation for append/prepend operations.
 *
 * <p>
 * This class defers execution until enumeration begins.
 * Each call to {@link #enumerator()} creates a new independent enumerator.
 *
 * @param <T> element type
 */
class AppendPrependEnumerable<T> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final T element;
    private final boolean prepend;

    AppendPrependEnumerable(Enumerable<T> source, T element, boolean prepend) {
        this.source = source;
        this.element = element;
        this.prepend = prepend;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new AppendPrependEnumerator<>(source, element, prepend);
    }
}

/**
 * Enumerator implementation for append and prepend operations.
 *
 * <p>
 * The enumerator operates in two logical phases:
 *
 * <ul>
 *     <li>Prepend mode:
 *         <ol>
 *             <li>Emit the specified element</li>
 *             <li>Enumerate the source sequence</li>
 *         </ol>
 *     </li>
 *     <li>Append mode:
 *         <ol>
 *             <li>Enumerate the source sequence</li>
 *             <li>Emit the specified element</li>
 *         </ol>
 *     </li>
 * </ul>
 *
 * <p>
 * The source enumerator is created lazily and only when needed.
 *
 * <p>
 * This implementation does not buffer elements.
 *
 * @param <T> element type
 */
class AppendPrependEnumerator<T> extends AbstractEnumerator<T> {
    private final Enumerable<T> source;
    private final T element;
    private final boolean prepend;

    private Enumerator<T> sourceEnumerator;
    private boolean elementEmitted = false;

    AppendPrependEnumerator(Enumerable<T> source, T element, boolean prepend) {
        this.source = source;
        this.element = element;
        this.prepend = prepend;
    }

    @Override
    protected boolean moveNextCore() {
        if (prepend) {
            if (!elementEmitted) {
                elementEmitted = true;
                this.current = element;
                return true;
            }

            if (sourceEnumerator == null) {
                sourceEnumerator = source.enumerator();
            }
            if (sourceEnumerator.moveNext()) {
                this.current = sourceEnumerator.current();
                return true;
            }
            return false;
        }

        if (sourceEnumerator == null) {
            sourceEnumerator = source.enumerator();
        }
        if (sourceEnumerator.moveNext()) {
            this.current = sourceEnumerator.current();
            return true;
        }

        if (!elementEmitted) {
            elementEmitted = true;
            this.current = element;
            return true;
        }
        return false;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new AppendPrependEnumerator<>(source, element, prepend);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
    }
}
