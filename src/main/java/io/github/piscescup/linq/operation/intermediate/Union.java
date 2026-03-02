package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

/**
 * Provides LINQ-style set union operations.
 *
 * <p>
 * {@code union} yields distinct elements that appear in either {@code source} or {@code other}.
 * The result has set semantics: duplicates are removed, and each element is returned at most once.
 *
 * <p>
 * The encounter order is the first occurrence order:
 * <ul>
 *     <li>All distinct elements from {@code source} are yielded first.</li>
 *     <li>Then elements from {@code other} are yielded if they were not seen in {@code source}.</li>
 * </ul>
 *
 * <p>
 * This operation is streaming and lazy:
 * <ul>
 *     <li>{@code source} is enumerated first, then {@code other}.</li>
 *     <li>A "seen" set is maintained to ensure distinctness.</li>
 * </ul>
 *
 * <h2>Resource Management</h2>
 * <ul>
 *     <li>The returned enumerator holds underlying enumerators while streaming and closes them in {@link Enumerator#close()}.</li>
 * </ul>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Union {
    private Union() {
        throw new UnsupportedOperationException(
            "No instance of " + Union.class.getCanonicalName() + " for you!"
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> Enumerable<T> union(
        Enumerable<T> source, Enumerable<? extends T> other
    ) {
        return union(
            source, other,
            (Comparator<? super T>) Comparator.naturalOrder()
        );
    }

    /**
     * Returns distinct elements that appear in either of the two sequences.
     *
     * <p>
     * Elements are compared using {@code comparator} (compare == 0 treated as equal).
     *
     * @param source first sequence
     * @param other second sequence
     * @param comparator comparator used for equality (compare == 0)
     * @param <T> element type
     * @return a sequence representing the union of the two sequences
     * @throws NullPointerException if any argument is null
     */
    public static <T> Enumerable<T> union(
        Enumerable<T> source, Enumerable<? extends T> other,
        Comparator<? super T> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(comparator);
        return new UnionEnumerable<>(source, other, comparator);
    }

    @SuppressWarnings("unchecked")
    public static <T, K> Enumerable<T> unionBy(
        Enumerable<T> source, Enumerable<? extends T> other,
        Function<? super T, ? extends K> keyExtractor
    ) {
        return unionBy(
            source, other, keyExtractor,
            (Comparator<? super K>) Comparator.naturalOrder()
        );
    }

    /**
     * Returns distinct elements that appear in either sequence, comparing by extracted keys.
     *
     * <p>
     * Keys are compared using {@code comparator} (compare == 0 treated as equal).
     *
     * @param source first sequence
     * @param other second sequence
     * @param keyExtractor extracts key from an element
     * @param comparator comparator used for key equality (compare == 0)
     *
     * @param <T> element type
     * @param <K> key type
     *
     * @return a sequence representing the union of the two sequences by key
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T, K> Enumerable<T> unionBy(
        Enumerable<T> source, Enumerable<? extends T> other,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(comparator);
        return new UnionByEnumerable<>(source, other, keyExtractor, comparator);
    }
}

/* ====================================================================== */
/* Package-private implementation classes                                  */
/* ====================================================================== */

final class UnionEnumerable<T> implements Enumerable<T> {

    private final Enumerable<T> source;
    private final Enumerable<? extends T> other;
    private final Comparator<? super T> comparator;

    UnionEnumerable(
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
        return new UnionEnumerator<>(source, other, comparator);
    }
}

final class UnionEnumerator<T> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Enumerable<? extends T> other;
    private final Comparator<? super T> comparator;

    private Enumerator<T> sourceEnumerator;
    private Enumerator<? extends T> otherEnumerator;

    private boolean inSource = true;

    private Set<T> seen;

    UnionEnumerator(
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

        if (seen == null) {
            seen = createSet(comparator);
        }

        while (true) {

            if (inSource) {
                if (sourceEnumerator == null) {
                    sourceEnumerator = source.enumerator();
                }

                while (sourceEnumerator.moveNext()) {
                    T item = sourceEnumerator.current();
                    if (seen.add(item)) {
                        this.current = item;
                        return true;
                    }
                }

                // source exhausted -> close and switch
                inSource = false;
                if (sourceEnumerator != null) {
                    sourceEnumerator.close();
                    sourceEnumerator = null;
                }
            }

            if (otherEnumerator == null) {
                otherEnumerator = other.enumerator();
            }

            while (otherEnumerator.moveNext()) {
                T item = otherEnumerator.current();
                if (seen.add(item)) {
                    this.current = item;
                    return true;
                }
            }

            return false;
        }
    }

    private static <T> Set<T> createSet(Comparator<? super T> comparator) {
        // Using TreeSet ensures comparator-based equality (compare == 0).
        // NOTE: This differs from HashSet which relies on equals/hashCode.
        return new TreeSet<>(comparator);
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new UnionEnumerator<>(source, other, comparator);
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
        if (seen != null) {
            seen.clear();
            seen = null;
        }
    }
}

/* ---------------------------------------------------------------------- */
/* unionBy                                                                 */
/* ---------------------------------------------------------------------- */

final class UnionByEnumerable<T, K> implements Enumerable<T> {

    private final Enumerable<T> source;
    private final Enumerable<? extends T> other;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Comparator<? super K> comparator;

    UnionByEnumerable(
        Enumerable<T> source,
        Enumerable<? extends T> other,
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
        return new UnionByEnumerator<>(source, other, keyExtractor, comparator);
    }
}

final class UnionByEnumerator<T, K> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Enumerable<? extends T> other;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Comparator<? super K> comparator;

    private Enumerator<T> sourceEnumerator;
    private Enumerator<? extends T> otherEnumerator;

    private boolean inSource = true;

    private Set<K> seenKeys;

    UnionByEnumerator(
        Enumerable<T> source,
        Enumerable<? extends T> other,
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

        if (seenKeys == null) {
            seenKeys = new TreeSet<>(comparator);
        }

        while (true) {

            if (inSource) {
                if (sourceEnumerator == null) {
                    sourceEnumerator = source.enumerator();
                }

                while (sourceEnumerator.moveNext()) {
                    T item = sourceEnumerator.current();
                    K key = keyExtractor.apply(item);
                    if (seenKeys.add(key)) {
                        this.current = item;
                        return true;
                    }
                }

                inSource = false;
                if (sourceEnumerator != null) {
                    sourceEnumerator.close();
                    sourceEnumerator = null;
                }
            }

            if (otherEnumerator == null) {
                otherEnumerator = other.enumerator();
            }

            while (otherEnumerator.moveNext()) {
                T item = otherEnumerator.current();
                K key = keyExtractor.apply(item);
                if (seenKeys.add(key)) {
                    this.current = item;
                    return true;
                }
            }

            return false;
        }
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new UnionByEnumerator<>(source, other, keyExtractor, comparator);
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
        if (seenKeys != null) {
            seenKeys.clear();
            seenKeys = null;
        }
    }
}