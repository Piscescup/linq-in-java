package io.github.piscescup.linq;

import io.github.piscescup.linq.enumerator.ArrayEnumerator;
import io.github.piscescup.linq.enumerator.IterableEnumerator;
import io.github.piscescup.linq.enumerator.IteratorEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.Iterator;
import java.util.function.Supplier;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class Linq<T> implements Enumerable<T> {

    private final Supplier<? extends Enumerator<T>> factory;

    private Linq(Supplier<? extends Enumerator<T>> factory) {
        NullCheck.requireNonNull(factory);
        this.factory = factory;
    }

    @Override
    public Enumerator<T> enumerator() {
        return factory.get();
    }

    /**
     * Returns an empty sequence of the same element type.
     *
     * <h3>Behavior</h3>
     * <ul>
     *   <li>Returns a sequence that yields no elements.</li>
     *   <li>The returned sequence should be safe to enumerate multiple times.</li>
     * </ul>
     *
     * <h3>Complexity</h3>
     * <p>Time: O(1). Space: O(1).</p>
     *
     * @return empty sequence
     * @throws RuntimeException if empty sequence cannot be created (implementation-defined)
     */
    @SuppressWarnings("unchecked")
    static <T> Enumerable<T> empty() {
        return (Enumerable<T>) EmptyLinq.INSTANCE;
    }

    /**
     * Wraps an {@link Enumerable} as a {@link Linq} sequence.
     *
     * <p>Useful if you want a canonical concrete type as the chain root.</p>
     */
    public static <T> Enumerable<T> from(Enumerable<T> source) {
        NullCheck.requireNonNull(source);
        return new Linq<>(source::enumerator);
    }

    /**
     * Creates a sequence from an {@link Iterable}.
     *
     * @param iterable iterable source
     * @param <T> element type
     * @return a sequence enumerating the iterable
     * @throws NullPointerException if {@code iterable} is null
     */
    public static <T> Enumerable<T> fromIterable(Iterable<? extends T> iterable) {
        NullCheck.requireNonNull(iterable);
        return new Linq<>(() -> new IterableEnumerator<>(iterable));
    }

    /**
     * Creates a sequence from an iterator factory.
     *
     * <p>
     * We require a factory (not a single iterator instance) to guarantee that each enumeration
     * gets a fresh iterator.
     *
     * @param iteratorFactory factory that creates a new iterator each time
     * @param <T> element type
     * @return a sequence enumerating iterators produced by {@code iteratorFactory}
     * @throws NullPointerException if {@code iteratorFactory} is null
     */
    public static <T> Enumerable<T> fromIterator(Supplier<? extends Iterator<? extends T>> iteratorFactory) {
        NullCheck.requireNonNull(iteratorFactory);
        return new Linq<>(() -> new IteratorEnumerator<>(iteratorFactory.get()));
    }

    /**
     * Creates a sequence from an array.
     *
     * @param items items
     * @param <T> element type
     * @return sequence over the array
     * @throws NullPointerException if {@code items} is null
     */
    @SafeVarargs
    public static <T> Enumerable<T> of(T... items) {
        NullCheck.requireNonNull(items);
        return new Linq<>(() -> new ArrayEnumerator<>(items));
    }

}
