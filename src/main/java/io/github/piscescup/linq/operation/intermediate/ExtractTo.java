package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

/**
 * Provides LINQ-style type extraction operation (similar to C# OfType).
 *
 * <p>
 * Filters the source sequence by runtime type and casts matching elements to the target type.
 * Elements that are not instances of {@code clazz} are skipped.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Enumerable<Object> source = Enumerable.of(1, "a", 2L, "b");
 *
 * Enumerable<String> strings = ExtractTo.extractTo(source, String.class);
 * // Produces: "a", "b"
 * }</pre>
 *
 * <p>
 * This operation is streaming and lazy: it does not buffer elements.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class ExtractTo {
    private ExtractTo() {
        throw new UnsupportedOperationException(
            "No instance of " + ExtractTo.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Extracts elements of the specified runtime type from the source sequence.
     *
     * @param source source sequence
     * @param clazz  target runtime type
     * @param <T>    source element type
     * @param <C>    target element type
     * @return a sequence containing only elements that are instances of {@code clazz}
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T, C> Enumerable<C> extractTo(Enumerable<T> source, Class<C> clazz) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(clazz);
        return new ExtractToEnumerable<>(source, clazz);
    }
}

class ExtractToEnumerable<T, C> implements Enumerable<C> {

    private final Enumerable<T> source;
    private final Class<C> clazz;

    ExtractToEnumerable(Enumerable<T> source, Class<C> clazz) {
        this.source = source;
        this.clazz = clazz;
    }

    @Override
    public Enumerator<C> enumerator() {
        return new ExtractToEnumerator<>(source, clazz);
    }
}

class ExtractToEnumerator<T, C> extends AbstractEnumerator<C> {

    private final Enumerable<T> source;
    private final Class<C> clazz;

    private Enumerator<T> sourceEnumerator;

    ExtractToEnumerator(Enumerable<T> source, Class<C> clazz) {
        this.source = source;
        this.clazz = clazz;
    }

    @Override
    protected boolean moveNextCore() {
        if (sourceEnumerator == null) {
            sourceEnumerator = source.enumerator();
        }

        while (sourceEnumerator.moveNext()) {
            T item = sourceEnumerator.current();
            if (clazz.isInstance(item)) {
                this.current = clazz.cast(item);
                return true;
            }
        }
        return false;
    }

    @Override
    protected AbstractEnumerator<C> clone() throws CloneNotSupportedException {
        return new ExtractToEnumerator<>(source, clazz);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
    }
}
