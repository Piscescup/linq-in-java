package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.Linq;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.linq.primitive.DoubleEnumerable;
import io.github.piscescup.linq.primitive.IntEnumerable;
import io.github.piscescup.linq.primitive.LongEnumerable;
import io.github.piscescup.util.validation.NullCheck;

import java.util.function.*;


/**
 * Provides LINQ-style projection operations.
 *
 * <p>
 * Includes:
 * <ul>
 *     <li>{@link #select}: projects each element of a sequence into a new form.</li>
 *     <li>{@link #selectMany}: projects each element to an inner sequence and flattens the resulting sequences.</li>
 * </ul>
 *
 * <p>
 * These operations are streaming and lazy. The source sequence is enumerated only when the
 * returned {@link Enumerator} is advanced.
 *
 * <h2>Resource Management</h2>
 * <ul>
 *     <li>The source enumerator is held during streaming and must be closed by closing the resulting enumerator.</li>
 *     <li>For {@code selectMany}, the current inner enumerator is also closed when exhausted or when the outer
 *     enumerator is closed.</li>
 * </ul>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Select {
    private Select() {
        throw new UnsupportedOperationException(
            "No instance of " + Select.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Projects each element of the source sequence into a new form.
     *
     * @param source source sequence
     * @param selector projection function
     * @param <T> source element type
     * @param <R> result element type
     * @return a sequence whose elements are the result of invoking {@code selector} on each source element
     * @throws NullPointerException if any argument is null
     */
    public static <T, R> Enumerable<R> select(
        Enumerable<T> source, Function<? super T, ? extends R> selector
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(selector);
        return new SelectEnumerable<>(source, selector);
    }

    /**
     * Projects each source element to an inner sequence and flattens the resulting sequences.
     *
     * <p>
     * For each source element {@code t}, {@code collectionSelector} produces an inner sequence of {@code C}.
     * Each inner element {@code c} is then combined with {@code t} via {@code resultSelector} to produce {@code R}.
     *
     * @param source source sequence
     * @param collectionSelector maps a source element to an inner sequence
     * @param resultSelector combines the outer element and each inner element into a result element
     *
     * @param <T> source element type
     * @param <C> inner element type
     * @param <R> result element type
     *
     * @return a flattened sequence of results
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T, C, R> Enumerable<R> selectMany(
        Enumerable<T> source,
        Function<? super T, ? extends Enumerable<? extends C>> collectionSelector,
        BiFunction<? super T, ? super C, ? extends R> resultSelector
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(collectionSelector);
        NullCheck.requireNonNull(resultSelector);
        return new SelectManyEnumerable<>(source, collectionSelector, resultSelector);
    }

    public static <T> IntEnumerable selectToInt(
        Enumerable<T> source,
        ToIntFunction<? super T> intMapping
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(intMapping);

        return new IntEnumerable.IntLinq(() -> new SelectToIntEnumerator<>(source.enumerator(), intMapping));
    }

    public static <T> LongEnumerable selectToLong(
        Enumerable<T> source,
        ToLongFunction<? super T> mapper
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(mapper);

        return new LongEnumerable.LongLinq(
            () -> new SelectToLongEnumerator<>(source.enumerator(), mapper)
        );
    }

    public static <T> DoubleEnumerable selectToDouble(
        Enumerable<T> source,
        ToDoubleFunction<? super T> mapper
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(mapper);

        return new DoubleEnumerable.DoubleLinq(
            () -> new SelectToDoubleEnumerator<>(source.enumerator(), mapper)
        );
    }


}

/* ====================================================================== */
/* Package-private implementation classes                                  */
/* ====================================================================== */

final class SelectEnumerable<T, R> implements Enumerable<R> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends R> selector;

    SelectEnumerable(Enumerable<T> source, Function<? super T, ? extends R> selector) {
        this.source = source;
        this.selector = selector;
    }

    @Override
    public Enumerator<R> enumerator() {
        return new SelectEnumerator<>(source, selector);
    }
}

final class SelectEnumerator<T, R> extends AbstractEnumerator<R> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends R> selector;

    private Enumerator<T> sourceEnumerator;

    SelectEnumerator(Enumerable<T> source, Function<? super T, ? extends R> selector) {
        this.source = source;
        this.selector = selector;
    }

    @Override
    protected boolean moveNextCore() {

        if (sourceEnumerator == null) {
            sourceEnumerator = source.enumerator();
        }

        if (!sourceEnumerator.moveNext()) {
            return false;
        }

        T item = sourceEnumerator.current();
        this.current = selector.apply(item);
        return true;
    }

    @Override
    protected AbstractEnumerator<R> clone() throws CloneNotSupportedException {
        return new SelectEnumerator<>(source, selector);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
    }
}

final class SelectManyEnumerable<T, C, R> implements Enumerable<R> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends Enumerable<? extends C>> collectionSelector;
    private final BiFunction<? super T, ? super C, ? extends R> resultSelector;

    SelectManyEnumerable(
        Enumerable<T> source,
        Function<? super T, ? extends Enumerable<? extends C>> collectionSelector,
        BiFunction<? super T, ? super C, ? extends R> resultSelector
    ) {
        this.source = source;
        this.collectionSelector = collectionSelector;
        this.resultSelector = resultSelector;
    }

    @Override
    public Enumerator<R> enumerator() {
        return new SelectManyEnumerator<>(source, collectionSelector, resultSelector);
    }
}

final class SelectManyEnumerator<T, C, R> extends AbstractEnumerator<R> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends Enumerable<? extends C>> collectionSelector;
    private final BiFunction<? super T, ? super C, ? extends R> resultSelector;

    private Enumerator<T> outerEnumerator;
    private Enumerator<? extends C> innerEnumerator;

    private T currentOuter;

    SelectManyEnumerator(
        Enumerable<T> source,
        Function<? super T, ? extends Enumerable<? extends C>> collectionSelector,
        BiFunction<? super T, ? super C, ? extends R> resultSelector
    ) {
        this.source = source;
        this.collectionSelector = collectionSelector;
        this.resultSelector = resultSelector;
    }

    @Override
    protected boolean moveNextCore() {

        if (outerEnumerator == null) {
            outerEnumerator = source.enumerator();
        }

        while (true) {

            // If we have an active inner enumerator, try to advance it.
            if (innerEnumerator != null) {
                if (innerEnumerator.moveNext()) {
                    C inner = innerEnumerator.current();
                    this.current = resultSelector.apply(currentOuter, inner);
                    return true;
                } else {
                    // Inner exhausted -> close and clear, then move to next outer.
                    innerEnumerator.close();
                    innerEnumerator = null;
                    currentOuter = null;
                }
            }

            // Move to next outer element.
            if (!outerEnumerator.moveNext()) {
                return false;
            }

            currentOuter = outerEnumerator.current();

            Enumerable<? extends C> innerSeq = collectionSelector.apply(currentOuter);
            // Allow selector to return null? Typically not; treat as NPE to match strict LINQ semantics.
            if (innerSeq == null) {
                throw new NullPointerException("collectionSelector returned null");
            }

            innerEnumerator = innerSeq.enumerator();
        }
    }

    @Override
    protected AbstractEnumerator<R> clone() throws CloneNotSupportedException {
        return new SelectManyEnumerator<>(source, collectionSelector, resultSelector);
    }

    @Override
    public void close() {
        if (innerEnumerator != null) {
            innerEnumerator.close();
            innerEnumerator = null;
        }
        if (outerEnumerator != null) {
            outerEnumerator.close();
            outerEnumerator = null;
        }
        currentOuter = null;
    }
}


final class SelectToIntEnumerator<T> extends AbstractEnumerator<Integer> {

    private final Enumerator<T> source;
    private final ToIntFunction<? super T> mapper;

    public SelectToIntEnumerator(Enumerator<T> source, ToIntFunction<? super T> mapper) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    protected boolean moveNextCore() {
        if (!source.moveNext())
            return false;

        T current = source.current();
        this.current = mapper.applyAsInt(current);
        return true;
    }

    @Override
    public void close() {
        source.close();
    }

    @Override
    protected AbstractEnumerator<Integer> clone() throws CloneNotSupportedException {
        return new SelectToIntEnumerator<>(source, mapper);
    }
}

final class SelectToLongEnumerator<T>
    extends AbstractEnumerator<Long> {

    private final Enumerator<T> source;
    private final ToLongFunction<? super T> mapper;

    public SelectToLongEnumerator(
        Enumerator<T> source,
        ToLongFunction<? super T> mapper
    ) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    protected boolean moveNextCore() {
        if (!source.moveNext())
            return false;

        this.current = mapper.applyAsLong(source.current());
        return true;
    }

    @Override
    public void close() {
        source.close();
    }

    @Override
    protected AbstractEnumerator<Long> clone() throws CloneNotSupportedException {
        return new SelectToLongEnumerator<>(source, mapper);
    }
}

final class SelectToDoubleEnumerator<T>
    extends AbstractEnumerator<Double> {

    private final Enumerator<T> source;
    private final ToDoubleFunction<? super T> mapper;

    public SelectToDoubleEnumerator(
        Enumerator<T> source,
        ToDoubleFunction<? super T> mapper
    ) {
        this.source = source;
        this.mapper = mapper;
    }

    @Override
    protected boolean moveNextCore() {
        if (!source.moveNext())
            return false;

        this.current = mapper.applyAsDouble(source.current());
        return true;
    }

    @Override
    public void close() {
        source.close();
    }

    @Override
    protected AbstractEnumerator<Double> clone() throws CloneNotSupportedException {
        return new SelectToDoubleEnumerator<>(source, mapper);
    }
}
