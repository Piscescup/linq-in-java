package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

/**
 * Provides LINQ-style default-if-empty operation.
 *
 * <p>
 * Returns the elements of the source sequence, or a single default value
 * if the sequence is empty.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Enumerable<Integer> numbers = Enumerable.empty();
 *
 * numbers.defaultIfEmpty(0);
 * // Produces: 0
 * }</pre>
 *
 * <p>
 * This operation is lazy and does not buffer the sequence.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class DefaultIfEmpty {

    private DefaultIfEmpty() {
        throw new UnsupportedOperationException(
            "No instance of " + DefaultIfEmpty.class.getName() + " for you!"
        );
    }

    /**
     * Returns the elements of the source sequence, or the specified default value
     * if the sequence contains no elements.
     *
     * @param source       source sequence
     * @param defaultValue value to return if sequence is empty
     * @param <T>          element type
     * @return a sequence that yields the source elements or a single default value
     *
     * @throws NullPointerException if source is null
     */
    public static <T> Enumerable<T> defaultIfEmpty(
        Enumerable<T> source,
        T defaultValue
    ) {
        NullCheck.requireNonNull(source);
        return new DefaultIfEmptyEnumerable<>(source, defaultValue);
    }
}

class DefaultIfEmptyEnumerable<T> implements Enumerable<T> {

    private final Enumerable<T> source;
    private final T defaultValue;

    DefaultIfEmptyEnumerable(Enumerable<T> source, T defaultValue) {
        this.source = source;
        this.defaultValue = defaultValue;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new DefaultIfEmptyEnumerator<>(source, defaultValue);
    }
}

class DefaultIfEmptyEnumerator<T> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final T defaultValue;

    private Enumerator<T> sourceEnumerator;
    private boolean checked = false;
    private boolean hasElements = false;
    private boolean defaultEmitted = false;

    DefaultIfEmptyEnumerator(Enumerable<T> source, T defaultValue) {
        this.source = source;
        this.defaultValue = defaultValue;
    }

    @Override
    protected boolean moveNextCore() {

        if (!checked) {
            checked = true;
            sourceEnumerator = source.enumerator();

            if (sourceEnumerator.moveNext()) {
                hasElements = true;
                this.current = sourceEnumerator.current();
                return true;
            }

            // source empty
            hasElements = false;
        }

        if (hasElements) {
            if (sourceEnumerator.moveNext()) {
                this.current = sourceEnumerator.current();
                return true;
            }
            return false;
        }

        // source empty case
        if (!defaultEmitted) {
            defaultEmitted = true;
            this.current = defaultValue;
            return true;
        }

        return false;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new DefaultIfEmptyEnumerator<>(source, defaultValue);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
    }
}


