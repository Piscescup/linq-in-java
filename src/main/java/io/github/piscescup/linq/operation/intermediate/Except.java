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
 * Provides LINQ-style set difference operations.
 *
 * <p>
 * {@code except} returns the set difference of two sequences:
 * it yields elements that appear in {@code source} but not in {@code other}.
 *
 * <p>
 * The result is distinct (set semantics): duplicates in {@code source} are removed,
 * i.e. each resulting element/key is returned at most once, preserving encounter order
 * of its first occurrence in {@code source}.
 *
 * <p>
 * This operation is lazy. On the first enumeration, {@code other} is fully consumed
 * to build an exclusion set. Then {@code source} is streamed and filtered.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Except {
    private Except() {
        throw new UnsupportedOperationException(
            "No instance of " + Except.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Returns distinct elements from {@code source} that do not appear in {@code other},
     * using {@code equals/hashCode}.
     *
     * @param source source sequence
     * @param other  sequence whose elements are excluded
     * @param <T> element type
     * @return a sequence containing set difference of {@code source} and {@code other}
     * @throws NullPointerException if any argument is null
     */
    public static <T> Enumerable<T> except(Enumerable<T> source, Enumerable<? extends T> other) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        return new ExceptEnumerable<>(source, other, null, null, Mode.ELEMENT_HASH);
    }

    /**
     * Returns distinct elements from {@code source} that do not appear in {@code other},
     * using the provided comparator (compare == 0 treated as equal).
     *
     * <p>
     * Internally uses a {@link TreeSet}. Whether {@code null} elements are supported depends
     * on the comparator (e.g. use {@code Comparator.nullsFirst(...)} if needed).
     *
     * @param source source sequence
     * @param other  sequence whose elements are excluded
     * @param comparator comparator used for equality
     * @param <T> element type
     * @return a sequence containing set difference of {@code source} and {@code other}
     * @throws NullPointerException if any argument is null
     */
    public static <T> Enumerable<T> except(
        Enumerable<T> source, Enumerable<? extends T> other,
        Comparator<? super T> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(comparator);
        return new ExceptEnumerable<>(source, other, comparator, null, Mode.ELEMENT_TREE);
    }

    /**
     * Returns distinct elements from {@code source} whose keys do not appear in {@code other},
     * using {@code equals/hashCode} on keys.
     *
     * @param source source sequence
     * @param other  sequence whose keys are excluded
     * @param keyExtractor key selector for set comparison
     * @param <T> element type
     * @param <K> key type
     * @return a sequence containing elements from {@code source} whose keys are not in {@code other}
     * @throws NullPointerException if any argument is null
     */
    public static <T, K> Enumerable<T> exceptBy(
        Enumerable<T> source,
        Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(keyExtractor);
        return new ExceptByEnumerable<>(source, other, keyExtractor, null, Mode.KEY_HASH);
    }
    /**
     * Returns distinct elements from {@code source} whose keys do not appear in {@code other},
     * using the provided comparator on keys (compare == 0 treated as equal).
     *
     * @param source source sequence
     * @param other  sequence whose keys are excluded
     * @param keyExtractor key selector for set comparison
     * @param comparator comparator used for key equality
     * @param <T> element type
     * @param <K> key type
     * @return a sequence containing elements from {@code source} whose keys are not in {@code other}
     * @throws NullPointerException if any argument is null
     */
    public static <T, K> Enumerable<T> exceptBy(
        Enumerable<T> source,
        Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(comparator);
        return new ExceptByEnumerable<>(source, other, keyExtractor, comparator, Mode.KEY_TREE);
    }

    enum Mode {
        ELEMENT_HASH,
        ELEMENT_TREE,
        KEY_HASH,
        KEY_TREE
    }
}

class ExceptEnumerable<T> implements Enumerable<T> {

    private final Enumerable<T> source;
    private final Enumerable<? extends T> other;
    private final Comparator<? super T> comparator; // for tree mode
    private final Except.Mode mode;

    ExceptEnumerable(
        Enumerable<T> source,
        Enumerable<? extends T> other,
        Comparator<? super T> comparator,
        Object unused,
        Except.Mode mode
    ) {
        this.source = source;
        this.other = other;
        this.comparator = comparator;
        this.mode = mode;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new ExceptEnumerator<>(source, other, comparator, mode);
    }
}

class ExceptByEnumerable<T, K> implements Enumerable<T> {

    private final Enumerable<T> source;
    private final Enumerable<? extends K> other;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Comparator<? super K> comparator;
    private final Except.Mode mode;

    ExceptByEnumerable(
        Enumerable<T> source,
        Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator,
        Except.Mode mode
    ) {
        this.source = source;
        this.other = other;
        this.keyExtractor = keyExtractor;
        this.comparator = comparator;
        this.mode = mode;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new ExceptByEnumerator<>(source, other, keyExtractor, comparator, mode);
    }
}

class ExceptEnumerator<T> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Enumerable<? extends T> other;
    private final Comparator<? super T> comparator;
    private final Except.Mode mode;

    private Enumerator<T> sourceEnumerator;
    private Set<T> excluded; // holds elements from other + seen results

    ExceptEnumerator(
        Enumerable<T> source,
        Enumerable<? extends T> other,
        Comparator<? super T> comparator,
        Except.Mode mode
    ) {
        this.source = source;
        this.other = other;
        this.comparator = comparator;
        this.mode = mode;
    }

    @Override
    protected boolean moveNextCore() {
        if (sourceEnumerator == null) {
            excluded = createSet();
            loadOtherIntoExcluded(excluded);
            sourceEnumerator = source.enumerator();
        }

        while (sourceEnumerator.moveNext()) {
            T item = sourceEnumerator.current();

            if (excluded.add(item)) {
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
            default -> throw new IllegalStateException("Invalid mode for ExceptEnumerator: " + mode);
        };
    }

    private void loadOtherIntoExcluded(Set<T> excluded) {
        try (Enumerator<? extends T> e = other.enumerator()) {
            while (e.moveNext()) {
                excluded.add(e.current());
            }
        }
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new ExceptEnumerator<>(source, other, comparator, mode);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
        excluded = null;
    }
}

class ExceptByEnumerator<T, K> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Enumerable<? extends K> other;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Comparator<? super K> comparator;
    private final Except.Mode mode;

    private Enumerator<T> sourceEnumerator;
    private Set<K> excludedKeys;

    ExceptByEnumerator(
        Enumerable<T> source,
        Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator,
        Except.Mode mode
    ) {
        this.source = source;
        this.other = other;
        this.keyExtractor = keyExtractor;
        this.comparator = comparator;
        this.mode = mode;
    }

    @Override
    protected boolean moveNextCore() {

        // Lazy initialization
        if (sourceEnumerator == null) {
            excludedKeys = createKeySet();
            loadOtherKeysIntoExcluded(excludedKeys);
            sourceEnumerator = source.enumerator();
        }

        while (sourceEnumerator.moveNext()) {
            T item = sourceEnumerator.current();
            K key = keyExtractor.apply(item);

            if (excludedKeys.add(key)) {
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
            default -> throw new IllegalStateException(
                "Invalid mode for ExceptByEnumerator: " + mode
            );
        };
    }

    private void loadOtherKeysIntoExcluded(Set<K> excludedKeys) {
        try (Enumerator<? extends K> e = other.enumerator()) {
            while (e.moveNext()) {
                excludedKeys.add(e.current());
            }
        }
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new ExceptByEnumerator<>(source, other, keyExtractor, comparator, mode);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
        excludedKeys = null;
    }
}
