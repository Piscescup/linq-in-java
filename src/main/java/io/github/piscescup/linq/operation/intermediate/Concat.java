package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

/**
 * Provides LINQ-style concatenation operation.
 *
 * <p>
 * Concatenates two sequences by iterating the first sequence and then the second sequence.
 * This operation is lazy and does not buffer elements.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Enumerable<Integer> a = Enumerable.of(1, 2);
 * Enumerable<Integer> b = Enumerable.of(3, 4);
 *
 * Enumerable<Integer> c = Concat.concat(a, b);
 * // Produces: 1, 2, 3, 4
 * }</pre>
 *
 * <h2>Execution characteristics</h2>
 * <ul>
 *     <li>Streaming / lazy</li>
 *     <li>O(1) additional memory</li>
 *     <li>The second sequence is not enumerated until the first is fully consumed</li>
 * </ul>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Concat {

    private Concat() {
        throw new UnsupportedOperationException(
            "No instance of " + Concat.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Concatenates two sequences.
     *
     * <p>
     * Elements from {@code enumerable1} are returned first. After {@code enumerable1} is exhausted,
     * elements from {@code enumerable2} are returned.
     *
     * @param enumerable1 the first sequence
     * @param enumerable2 the second sequence
     * @param <T>         element type of the resulting sequence
     * @return a sequence that iterates {@code enumerable1} then {@code enumerable2}
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T> Enumerable<T> concat(
        Enumerable<T> enumerable1, Enumerable<? extends T> enumerable2
    ) {
        NullCheck.requireNonNull(enumerable1);
        NullCheck.requireNonNull(enumerable2);
        return new ConcatEnumerable<>(enumerable1, enumerable2);
    }
}

class ConcatEnumerable<T> implements Enumerable<T> {

    private final Enumerable<T> first;
    private final Enumerable<? extends T> second;

    ConcatEnumerable(Enumerable<T> first, Enumerable<? extends T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new ConcatEnumerator<>(first, second);
    }
}

class ConcatEnumerator<T> extends AbstractEnumerator<T> {

    private final Enumerable<T> first;
    private final Enumerable<? extends T> second;

    private Enumerator<T> firstEnumerator;
    private Enumerator<? extends T> secondEnumerator;

    /**
     * true means we are still enumerating the first sequence; once exhausted, switch to second.
     */
    private boolean inFirst = true;

    ConcatEnumerator(Enumerable<T> first, Enumerable<? extends T> second) {
        this.first = first;
        this.second = second;
    }

    @Override
    protected boolean moveNextCore() {

        if (inFirst) {
            if (firstEnumerator == null) {
                firstEnumerator = first.enumerator();
            }

            if (firstEnumerator.moveNext()) {
                this.current = firstEnumerator.current();
                return true;
            }

            // first exhausted -> close it and switch to second
            inFirst = false;
            if (firstEnumerator != null) {
                firstEnumerator.close();
                firstEnumerator = null;
            }
        }

        if (secondEnumerator == null) {
            secondEnumerator = second.enumerator();
        }

        if (secondEnumerator.moveNext()) {
            this.current = secondEnumerator.current();
            return true;
        }

        return false;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new ConcatEnumerator<>(first, second);
    }

    @Override
    public void close() {
        if (firstEnumerator != null) {
            firstEnumerator.close();
            firstEnumerator = null;
        }
        if (secondEnumerator != null) {
            secondEnumerator.close();
            secondEnumerator = null;
        }
    }
}