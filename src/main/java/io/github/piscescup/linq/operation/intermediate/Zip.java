package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.interfaces.Pair;
import io.github.piscescup.interfaces.exfunction.BinFunction;
import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.pair.ImmutablePair;
import io.github.piscescup.util.validation.NullCheck;

/**
 * Provides LINQ-style zip operation.
 *
 * <p>
 * Combines two sequences by index. The resulting sequence ends when either input sequence ends.
 *
 * <h2>Resource Management</h2>
 * <ul>
 *     <li>This is a streaming operator.</li>
 *     <li>Both underlying enumerators are opened lazily and closed when the resulting enumerator is closed.</li>
 * </ul>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Enumerable<Integer> a = Enumerable.of(1, 2, 3);
 * Enumerable<String>  b = Enumerable.of("A", "B");
 *
 * // -> (1,"A"), (2,"B")
 * Enumerable<Pair<Integer, String>> zipped = Zip.zip(a, b);
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class Zip {
    private Zip() {
        throw new UnsupportedOperationException(
            "No instance of " + Zip.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Zips two sequences into pairs.
     *
     * <p>
     * For each index i, yields {@code (source[i], other[i])}. Enumeration stops when either sequence ends.
     *
     * @param source source sequence
     * @param other other sequence
     * @param <T> source element type
     * @param <R> other element type
     * @return zipped pair sequence
     * @throws NullPointerException if {@code source} or {@code other} is {@code null}
     */
    public static <T, R> Enumerable<Pair<T, R>> zip(
        Enumerable<T> source, Enumerable<? extends R> other
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);

        return zip(source, other, ImmutablePair::of);
    }

    /**
     * Zips with {@code other} using a result mapping function.
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>For each index i, yields {@code resultMapping.apply(source[i], other[i])}.</li>
     *   <li>Stops when either sequence ends.</li>
     * </ul>
     *
     * <h3>Complexity</h3>
     * <p>Time: O(min(n, m)). Space: O(1).</p>
     *
     * @param source source sequence
     * @param other other sequence
     * @param resultMapping maps paired elements to result
     * @param <T> source element type
     * @param <O> other element type
     * @param <R> result element type
     * @return zipped mapped sequence
     * @throws NullPointerException if {@code source}, {@code other} or {@code resultMapping} is {@code null}
     * @throws RuntimeException if enumeration fails or resultMapping throws
     */
    public static <T, O, R> Enumerable<R> zip(
        Enumerable<T> source, Enumerable<? extends O> other,
        BinFunction<? super T, ? super O, ? extends R> resultMapping
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(resultMapping);

        return new ZipEnumerable<>(source, other, resultMapping);
    }
}

/* ====================================================================== */
/* Package-private implementation classes                                  */
/* ====================================================================== */

final class ZipEnumerable<T, O, R> implements Enumerable<R> {

    private final Enumerable<T> source;
    private final Enumerable<? extends O> other;
    private final BinFunction<? super T, ? super O, ? extends R> resultMapping;

    ZipEnumerable(
        Enumerable<T> source,
        Enumerable<? extends O> other,
        BinFunction<? super T, ? super O, ? extends R> resultMapping
    ) {
        this.source = source;
        this.other = other;
        this.resultMapping = resultMapping;
    }

    @Override
    public Enumerator<R> enumerator() {
        return new ZipEnumerator<>(source, other, resultMapping);
    }
}

final class ZipEnumerator<T, O, R> extends AbstractEnumerator<R> {

    private final Enumerable<T> source;
    private final Enumerable<? extends O> other;
    private final BinFunction<? super T, ? super O, ? extends R> resultMapping;

    private Enumerator<T> sourceEnumerator;
    private Enumerator<? extends O> otherEnumerator;

    ZipEnumerator(
        Enumerable<T> source,
        Enumerable<? extends O> other,
        BinFunction<? super T, ? super O, ? extends R> resultMapping
    ) {
        this.source = source;
        this.other = other;
        this.resultMapping = resultMapping;
    }

    @Override
    protected boolean moveNextCore() {

        if (sourceEnumerator == null) {
            sourceEnumerator = source.enumerator();
        }
        if (otherEnumerator == null) {
            otherEnumerator = other.enumerator();
        }

        if (!sourceEnumerator.moveNext()) {
            return false;
        }
        if (!otherEnumerator.moveNext()) {
            return false;
        }

        T left = sourceEnumerator.current();
        O right = otherEnumerator.current();

        this.current = resultMapping.apply(left, right);
        return true;
    }

    @Override
    protected AbstractEnumerator<R> clone() throws CloneNotSupportedException {
        return new ZipEnumerator<>(source, other, resultMapping);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
        if (otherEnumerator != null) {
            otherEnumerator.close();
            otherEnumerator = null;
        }
    }
}