package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Provides LINQ-style set intersection operations.
 *
 * <p>
 * {@code intersect} yields the set intersection of two sequences:
 * it returns distinct elements from {@code source} that also appear in {@code other}.
 *
 * <p>
 * The result has set semantics: duplicates in {@code source} are removed (each element/key
 * is returned at most once), while preserving encounter order of the first occurrence in {@code source}.
 *
 * <p>
 * This operation is lazy. On the first enumeration, {@code other} is fully consumed to build
 * a membership set, then {@code source} is streamed and filtered.
 *
 * <h2>Resource Management</h2>
 * <ul>
 *     <li>{@code other} is consumed using try-with-resources and closed immediately after building the set.</li>
 *     <li>{@code source} is streamed and must be closed by closing the resulting {@link Enumerator}.</li>
 * </ul>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Intersect {
    private Intersect() {
        throw new UnsupportedOperationException(
            "No instance of " + Intersect.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Returns distinct elements from {@code source} that also appear in {@code other},
     * using {@code equals/hashCode}.
     *
     * @param source source sequence
     * @param other  other sequence whose elements form the intersection set
     * @param <T> element type
     * @return a sequence containing distinct intersection elements
     * @throws NullPointerException if any argument is null
     */
    public static <T> Enumerable<T> intersect(
        Enumerable<T> source, Enumerable<? extends T> other
    ) {
        return intersect(source, other, (Comparator<? super T>) null);
    }

    /**
     * Returns distinct elements from {@code source} that also appear in {@code other},
     * using the provided comparator (compare == 0 treated as equal).
     *
     * <p>
     * Internally uses a {@link TreeSet}. Whether {@code null} elements are supported depends
     * on the comparator (e.g. use {@code Comparator.nullsFirst(...)} if needed).
     *
     * @param source source sequence
     * @param other  other sequence whose elements form the intersection set
     * @param comparator comparator used for equality
     * @param <T> element type
     * @return a sequence containing distinct intersection elements
     * @throws NullPointerException if any argument is null
     */
    public static <T> Enumerable<T> intersect(
        Enumerable<T> source, Enumerable<? extends T> other,
        Comparator<? super T> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        // comparator may be null -> equals/hashCode mode

        return new IntersectEnumerable<>(source, other, comparator);
    }

    /**
     * Returns distinct elements from {@code source} whose extracted keys also appear in {@code other},
     * using {@code equals/hashCode} on keys.
     *
     * <p>
     * This overload treats {@code other} as a sequence of keys.
     *
     * @param source source sequence
     * @param other  other key sequence
     * @param keyExtractor extracts key from source element
     * @param <T> element type
     * @param <K> key type
     * @return a sequence containing distinct intersection elements by key
     * @throws NullPointerException if any argument is null
     */
    public static <T, K> Enumerable<T> intersectBy(
        Enumerable<T> source, Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor
    ) {
        return intersectBy(source, other, keyExtractor, (Comparator<? super K>) null);
    }

    /**
     * Returns distinct elements from {@code source} whose extracted keys also appear in {@code other},
     * using the provided comparator on keys (compare == 0 treated as equal).
     *
     * <p>
     * This overload treats {@code other} as a sequence of keys.
     *
     * <p>
     * Internally uses a {@link TreeSet}. Whether {@code null} keys are supported depends
     * on the comparator.
     *
     * @param source source sequence
     * @param other  other key sequence
     * @param keyExtractor extracts key from source element
     * @param comparator comparator used for key equality
     * @param <T> element type
     * @param <K> key type
     * @return a sequence containing distinct intersection elements by key
     * @throws NullPointerException if any argument is null
     */
    public static <T, K> Enumerable<T> intersectBy(
        Enumerable<T> source, Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(keyExtractor);
        // comparator may be null -> equals/hashCode mode

        return new IntersectByEnumerable<>(source, other, keyExtractor, comparator);
    }

}


final class IntersectEnumerable<T> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final Enumerable<? extends T> other;
    private final Comparator<? super T> comparator; // nullable

    IntersectEnumerable(
        Enumerable<T> source,
        Enumerable<? extends T> other,
        Comparator<? super T> comparator
    ) {
        this.source = source;
        this.other = other;
        this.comparator = comparator;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new IntersectEnumerator<>(source, other, comparator);
    }
}

final class IntersectByEnumerable<T, K> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final Enumerable<? extends K> other;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Comparator<? super K> comparator; // nullable

    IntersectByEnumerable(
        Enumerable<T> source,
        Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        this.source = source;
        this.other = other;
        this.keyExtractor = keyExtractor;
        this.comparator = comparator;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new IntersectByEnumerator<>(source, other, keyExtractor, comparator);
    }
}


final class IntersectEnumerator<T> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Enumerable<? extends T> other;
    private final Comparator<? super T> comparator; // nullable

    private Enumerator<T> sourceEnumerator;

    // membership set for "other"
    private Set<T> otherSet;
    // yielded set to enforce distinct results
    private Set<T> yielded;

    IntersectEnumerator(
        Enumerable<T> source,
        Enumerable<? extends T> other,
        Comparator<? super T> comparator
    ) {
        this.source = source;
        this.other = other;
        this.comparator = comparator;
    }

    @Override
    protected boolean moveNextCore() {

        if (sourceEnumerator == null) {
            otherSet = createSet();
            yielded = createSet();

            // ✅ other is fully consumed and closed here
            try (Enumerator<? extends T> e = other.enumerator()) {
                while (e.moveNext()) {
                    otherSet.add(e.current());
                }
            }

            // outer is streamed
            sourceEnumerator = source.enumerator();
        }

        while (sourceEnumerator.moveNext()) {
            T item = sourceEnumerator.current();

            // Must be in otherSet, and must not have been yielded before.
            if (otherSet.contains(item) && yielded.add(item)) {
                this.current = item;
                return true;
            }
        }

        return false;
    }

    private Set<T> createSet() {
        return (comparator == null) ? new HashSet<>() : new TreeSet<>(comparator);
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new IntersectEnumerator<>(source, other, comparator);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
        otherSet = null;
        yielded = null;
    }
}

final class IntersectByEnumerator<T, K> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Enumerable<? extends K> other;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Comparator<? super K> comparator; // nullable

    private Enumerator<T> sourceEnumerator;

    // membership set for keys from "other"
    private Set<K> otherKeys;
    // yielded keys set to enforce distinct results by key
    private Set<K> yieldedKeys;

    IntersectByEnumerator(
        Enumerable<T> source,
        Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        this.source = source;
        this.other = other;
        this.keyExtractor = keyExtractor;
        this.comparator = comparator;
    }

    @Override
    protected boolean moveNextCore() {

        if (sourceEnumerator == null) {
            otherKeys = createKeySet();
            yieldedKeys = createKeySet();

            // ✅ other keys are fully consumed and closed here
            try (Enumerator<? extends K> e = other.enumerator()) {
                while (e.moveNext()) {
                    otherKeys.add(e.current());
                }
            }

            sourceEnumerator = source.enumerator();
        }

        while (sourceEnumerator.moveNext()) {
            T item = sourceEnumerator.current();
            K key = keyExtractor.apply(item);

            if (otherKeys.contains(key) && yieldedKeys.add(key)) {
                this.current = item;
                return true;
            }
        }

        return false;
    }

    private Set<K> createKeySet() {
        return (comparator == null) ? new HashSet<>() : new TreeSet<>(comparator);
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new IntersectByEnumerator<>(source, other, keyExtractor, comparator);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
        otherKeys = null;
        yieldedKeys = null;
    }
}
