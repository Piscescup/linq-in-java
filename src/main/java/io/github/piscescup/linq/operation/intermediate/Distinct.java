package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;

import java.util.Comparator;
import java.util.function.Function;

import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Provides LINQ-style distinct operations.
 *
 * <p>
 * Removes duplicate elements from a sequence while preserving encounter order
 * (i.e., the first occurrence of each distinct element is returned).
 *
 * <p>
 * This is a streaming operation: elements are returned lazily as the source is enumerated.
 * Internally, a set is maintained to track seen elements (or keys), so memory usage grows
 * with the number of distinct items.
 *
 * <h2>Examples</h2>
 * <pre>{@code
 * Enumerable<Integer> nums = Enumerable.of(1, 2, 2, 3, 1);
 * Distinct.distinct(nums); // 1, 2, 3
 *
 * Enumerable<String> words = Enumerable.of("a", "bb", "c", "dd", "eee");
 * Distinct.distinctBy(words, String::length); // "a", "bb", "eee"
 * }</pre>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Distinct {
    private Distinct() {
        throw new UnsupportedOperationException(
            "No instance of " + Distinct.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Returns distinct elements from the source sequence, using {@code equals/hashCode}.
     *
     * @param source source sequence
     * @param <T> element type
     * @return a sequence that yields distinct elements
     * @throws NullPointerException if source is null
     */
    public static <T> Enumerable<T> distinct(Enumerable<T> source) {
        NullCheck.requireNonNull(source);
        return new DistinctEnumerable<>(source, null, null, Mode.ELEMENT_HASH);
    }

    /**
     * Returns distinct elements from the source sequence, using the provided comparator.
     *
     * <p>
     * Internally uses a {@link TreeSet}. Note that whether {@code null} elements are supported
     * depends on the provided comparator.
     *
     * @param source source sequence
     * @param comparator comparator for element equality (compare == 0 treated as duplicate)
     * @param <T> element type
     * @return a sequence that yields distinct elements
     * @throws NullPointerException if source or comparator is null
     */
    public static <T> Enumerable<T> distinct(Enumerable<T> source, Comparator<? super T> comparator) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(comparator);
        return new DistinctEnumerable<>(source, comparator, null, Mode.ELEMENT_TREE);
    }

    /**
     * Returns distinct elements from the source sequence by a key, using {@code equals/hashCode} on keys.
     *
     * @param source source sequence
     * @param keyExtractor key extractor
     * @param <T> element type
     * @param <K> key type
     * @return a sequence that yields elements whose keys are distinct
     * @throws NullPointerException if source or keyExtractor is null
     */
    public static <T, K> Enumerable<T> distinctBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        return new DistinctByEnumerable<>(source, keyExtractor, null, Mode.KEY_HASH);
    }

    /**
     * Returns distinct elements from the source sequence by a key, using the provided comparator on keys.
     *
     * <p>
     * Internally uses a {@link TreeSet}. Note that whether {@code null} keys are supported
     * depends on the provided comparator.
     *
     * @param source source sequence
     * @param keyExtractor key extractor
     * @param comparator comparator for keys (compare == 0 treated as duplicate)
     * @param <T> element type
     * @param <K> key type
     * @return a sequence that yields elements whose keys are distinct
     * @throws NullPointerException if any argument is null
     */
    public static <T, K> Enumerable<T> distinctBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(comparator);
        return new DistinctByEnumerable<>(source, keyExtractor, comparator, Mode.KEY_TREE);
    }

    enum Mode {
        ELEMENT_HASH,
        ELEMENT_TREE,
        KEY_HASH,
        KEY_TREE
    }
}

/* ------------------------------------------------------------ */
/* Enumerable implementations                                     */
/* ------------------------------------------------------------ */

class DistinctEnumerable<T> implements Enumerable<T> {

    private final Enumerable<T> source;
    private final Comparator<? super T> comparator; // only used for tree mode

    private final Distinct.Mode mode;

    DistinctEnumerable(
        Enumerable<T> source,
        Comparator<? super T> comparator,
        Object unused,
        Distinct.Mode mode
    ) {
        this.source = source;
        this.comparator = comparator;
        this.mode = mode;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new DistinctEnumerator<>(source, comparator, mode);
    }
}

class DistinctByEnumerable<T, K> implements Enumerable<T> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Comparator<? super K> comparator; // only used for tree mode
    private final Distinct.Mode mode;

    DistinctByEnumerable(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator,
        Distinct.Mode mode
    ) {
        this.source = source;
        this.keyExtractor = keyExtractor;
        this.comparator = comparator;
        this.mode = mode;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new DistinctByEnumerator<>(source, keyExtractor, comparator, mode);
    }
}

/* ------------------------------------------------------------ */
/* Enumerator implementations                                     */
/* ------------------------------------------------------------ */

class DistinctEnumerator<T> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Comparator<? super T> comparator;
    private final Distinct.Mode mode;

    private Enumerator<T> sourceEnumerator;
    private Set<T> seen;

    DistinctEnumerator(
        Enumerable<T> source,
        Comparator<? super T> comparator,
        Distinct.Mode mode
    ) {
        this.source = source;
        this.comparator = comparator;
        this.mode = mode;
    }

    @Override
    protected boolean moveNextCore() {
        if (sourceEnumerator == null) {
            sourceEnumerator = source.enumerator();
            seen = createSet();
        }

        while (sourceEnumerator.moveNext()) {
            T item = sourceEnumerator.current();
            if (seen.add(item)) {
                this.current = item;
                return true;
            }
        }
        return false;
    }

    private Set<T> createSet() {
        return switch (mode) {
            case ELEMENT_HASH -> new HashSet<>();
            case ELEMENT_TREE -> new TreeSet<>(comparator);
            default -> throw new IllegalStateException("Invalid mode for DistinctEnumerator: " + mode);
        };
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        // 与你框架其他实现一致：clone 返回从头开始的新枚举器（不复制进度/seen）
        return new DistinctEnumerator<>(source, comparator, mode);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
        seen = null;
    }
}

class DistinctByEnumerator<T, K> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Comparator<? super K> comparator;
    private final Distinct.Mode mode;

    private Enumerator<T> sourceEnumerator;
    private Set<K> seenKeys;

    DistinctByEnumerator(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator,
        Distinct.Mode mode
    ) {
        this.source = source;
        this.keyExtractor = keyExtractor;
        this.comparator = comparator;
        this.mode = mode;
    }

    @Override
    protected boolean moveNextCore() {
        if (sourceEnumerator == null) {
            sourceEnumerator = source.enumerator();
            seenKeys = createKeySet();
        }

        while (sourceEnumerator.moveNext()) {
            T item = sourceEnumerator.current();
            K key = keyExtractor.apply(item);
            if (seenKeys.add(key)) {
                this.current = item;
                return true;
            }
        }
        return false;
    }

    private Set<K> createKeySet() {
        return switch (mode) {
            case KEY_HASH -> new HashSet<>();
            case KEY_TREE -> new TreeSet<>(comparator);
            default -> throw new IllegalStateException("Invalid mode for DistinctByEnumerator: " + mode);
        };
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new DistinctByEnumerator<>(source, keyExtractor, comparator, mode);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
        seenKeys = null;
    }
}