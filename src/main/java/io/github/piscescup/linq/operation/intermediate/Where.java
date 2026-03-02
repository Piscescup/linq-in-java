package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.function.Predicate;

/**
 * Provides LINQ-style filtering (Where) operation.
 *
 * <p>
 * Filters a sequence of values based on a predicate.
 *
 * <p>
 * This is a streaming and lazy operator:
 * <ul>
 *     <li>No buffering is performed.</li>
 *     <li>Elements are evaluated one by one as the sequence is enumerated.</li>
 * </ul>
 *
 * <h2>Resource Management</h2>
 * <ul>
 *     <li>The underlying source enumerator is held during streaming.</li>
 *     <li>It is closed when the resulting enumerator is closed.</li>
 * </ul>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Enumerable<Integer> evens =
 *     Where.where(numbers, n -> n % 2 == 0);
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class Where {
    private Where() {
        throw new UnsupportedOperationException(
            "No instance of " + Where.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Filters elements of the source sequence using the given predicate.
     *
     * @param source source sequence
     * @param predicate condition used to test each element
     * @param <T> element type
     * @return a sequence containing only elements that satisfy the predicate
     * @throws NullPointerException if source or predicate is null
     */
    public static <T> Enumerable<T> where(
        Enumerable<T> source, Predicate<? super T> predicate
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);
        return new WhereEnumerable<>(source, predicate);
    }
}

/* ====================================================================== */
/* Package-private implementation classes                                  */
/* ====================================================================== */

final class WhereEnumerable<T> implements Enumerable<T> {

    private final Enumerable<T> source;
    private final Predicate<? super T> predicate;

    WhereEnumerable(Enumerable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new WhereEnumerator<>(source, predicate);
    }
}

final class WhereEnumerator<T> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Predicate<? super T> predicate;

    private Enumerator<T> sourceEnumerator;

    WhereEnumerator(Enumerable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    protected boolean moveNextCore() {

        if (sourceEnumerator == null) {
            sourceEnumerator = source.enumerator();
        }

        while (sourceEnumerator.moveNext()) {
            T item = sourceEnumerator.current();
            if (predicate.test(item)) {
                this.current = item;
                return true;
            }
        }

        return false;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new WhereEnumerator<>(source, predicate);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
    }
}