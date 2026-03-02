package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.*;

/**
 * Provides LINQ-style group join operation (left outer group join).
 *
 * <p>
 * A group join correlates elements of two sequences based on matching keys.
 * For each element in the outer sequence ({@code source}), the result includes
 * the element and a sequence of all matching elements from the inner sequence ({@code other}).
 *
 * <p>
 * This corresponds to a left outer join that produces grouped results:
 *
 * <pre>{@code
 * from s in source
 * join o in other on s.Key equals o.Key into group
 * select resultMapping(s, group)
 * }</pre>
 *
 * <h2>Execution Model</h2>
 *
 * <ul>
 *     <li>The inner sequence ({@code other}) is fully consumed once to build a lookup table.</li>
 *     <li>The outer sequence ({@code source}) is streamed lazily.</li>
 *     <li>For each outer element, matching inner elements are provided as an {@link Enumerable}.</li>
 * </ul>
 *
 * <h2>Resource Management</h2>
 *
 * <ul>
 *     <li>{@code other} is consumed using try-with-resources and closed immediately after building the lookup.</li>
 *     <li>{@code source} is streamed and must be closed by closing the resulting {@link Enumerator}.</li>
 * </ul>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class GroupJoin {
    private GroupJoin() {
        throw new UnsupportedOperationException(
            "No instance of " + GroupJoin.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Performs a group join between two sequences using {@code equals/hashCode} for key comparison.
     *
     * <p>
     * For each element in {@code source}, all matching elements in {@code other}
     * with equal keys are grouped and passed to {@code resultMapping}.
     *
     * <p>
     * If no matching elements are found in {@code other}, an empty sequence is supplied.
     *
     * @param source outer sequence
     * @param other inner sequence
     * @param selfKeyExtractor extracts key from outer element
     * @param otherKeyExtractor extracts key from inner element
     * @param resultMapping produces result from outer element and grouped inner sequence
     *
     * @param <T> outer element type
     * @param <O> inner element type
     * @param <K> key type
     * @param <R> result type
     *
     * @return a sequence of grouped join results
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T, O, K, R> Enumerable<R> groupJoin(
        Enumerable<T> source, Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super Enumerable<O>, ? extends R> resultMapping
    ) {
        return groupJoin(
            source, other,
            selfKeyExtractor, otherKeyExtractor,
            resultMapping,
            null
        );
    }

    /**
     * Performs a group join between two sequences using the provided comparator
     * for key comparison.
     *
     * <p>
     * Keys are considered equal when {@code comparator.compare(a, b) == 0}.
     *
     * <p>
     * Internally uses a {@link TreeMap}. Whether {@code null} keys are supported
     * depends on the provided comparator (for example, use
     * {@code Comparator.nullsFirst(...)} if null keys are possible).
     *
     * @param source outer sequence
     * @param other inner sequence
     * @param selfKeyExtractor extracts key from outer element
     * @param otherKeyExtractor extracts key from inner element
     * @param resultMapping produces result from outer element and grouped inner sequence
     * @param comparator comparator used for key equality
     *
     * @param <T> outer element type
     * @param <O> inner element type
     * @param <K> key type
     * @param <R> result type
     *
     * @return a sequence of grouped join results
     *
     * @throws NullPointerException if source, other, key extractors, resultMapping,
     *                              or comparator is null
     */
    public static <T, O, K, R> Enumerable<R> groupJoin(
        Enumerable<T> source, Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super Enumerable<O>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(selfKeyExtractor);
        NullCheck.requireNonNull(otherKeyExtractor);
        NullCheck.requireNonNull(resultMapping);
        // comparator 可为 null（使用 equals/hashCode 模式）

        return new GroupJoinEnumerable<>(
            source, other,
            selfKeyExtractor, otherKeyExtractor,
            resultMapping,
            comparator
        );
    }
}

/**
 * Enumerable implementation for group join.
 *
 * <p>
 * Each call to {@link #enumerator()} creates a new enumerator instance.
 *
 * @param <T> outer element type
 * @param <O> inner element type
 * @param <K> key type
 * @param <R> result type
 */
class GroupJoinEnumerable<T, O, K, R> implements Enumerable<R> {

    private final Enumerable<T> source;
    private final Enumerable<? extends O> other;
    private final Function<? super T, ? extends K> selfKeyExtractor;
    private final Function<? super O, ? extends K> otherKeyExtractor;
    private final BiFunction<? super T, ? super Enumerable<O>, ? extends R> resultMapping;
    private final Comparator<? super K> comparator; // nullable

    GroupJoinEnumerable(
        Enumerable<T> source,
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super Enumerable<O>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        this.source = source;
        this.other = other;
        this.selfKeyExtractor = selfKeyExtractor;
        this.otherKeyExtractor = otherKeyExtractor;
        this.resultMapping = resultMapping;
        this.comparator = comparator;
    }

    @Override
    public Enumerator<R> enumerator() {
        return new GroupJoinEnumerator<>(
            source, other,
            selfKeyExtractor, otherKeyExtractor,
            resultMapping,
            comparator
        );
    }
}

/**
 * Enumerator implementation for group join.
 *
 * <p>
 * The inner sequence is fully consumed once to build a key-based lookup.
 * The outer sequence is then streamed lazily.
 *
 * <p>
 * For each outer element:
 * <ul>
 *     <li>If matching inner elements exist, they are wrapped in a list-backed {@link Enumerable}.</li>
 *     <li>If no matches exist, an empty {@link Enumerable} is supplied.</li>
 * </ul>
 *
 * <p>
 * The lookup is stored in memory for the lifetime of this enumerator.
 *
 * @param <T> outer element type
 * @param <O> inner element type
 * @param <K> key type
 * @param <R> result type
 */
class GroupJoinEnumerator<T, O, K, R> extends AbstractEnumerator<R> {

    private final Enumerable<T> source;
    private final Enumerable<? extends O> other;
    private final Function<? super T, ? extends K> selfKeyExtractor;
    private final Function<? super O, ? extends K> otherKeyExtractor;
    private final BiFunction<? super T, ? super Enumerable<O>, ? extends R> resultMapping;
    private final Comparator<? super K> comparator; // nullable

    private Enumerator<T> sourceEnumerator;

    // lookup：key -> list of inner elements (built once)
    private Map<K, List<O>> lookup;

    GroupJoinEnumerator(
        Enumerable<T> source,
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super Enumerable<O>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        this.source = source;
        this.other = other;
        this.selfKeyExtractor = selfKeyExtractor;
        this.otherKeyExtractor = otherKeyExtractor;
        this.resultMapping = resultMapping;
        this.comparator = comparator;
    }

    @Override
    protected boolean moveNextCore() {

        if (lookup == null) {
            // Build inner lookup once; other enumerator is always closed here.
            lookup = buildLookup(other, otherKeyExtractor, comparator);
            // Enumerate outer lazily (will be closed in this.close()).
            sourceEnumerator = source.enumerator();
        }

        if (!sourceEnumerator.moveNext()) {
            return false;
        }

        T outer = sourceEnumerator.current();
        K key = selfKeyExtractor.apply(outer);

        List<O> matches = lookup.get(key);
        Enumerable<O> group = (matches == null || matches.isEmpty())
            ? EmptyEnumerable.instance()
            : new ListBackedEnumerable<>(matches);

        this.current = resultMapping.apply(outer, group);
        return true;
    }

    @Override
    protected AbstractEnumerator<R> clone() throws CloneNotSupportedException {
        return new GroupJoinEnumerator<>(
            source, other,
            selfKeyExtractor, otherKeyExtractor,
            resultMapping,
            comparator
        );
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
        // lookup is pure memory; no resource to close
        lookup = null;
    }

    private static <O, K> Map<K, List<O>> buildLookup(
        Enumerable<? extends O> other,
        Function<? super O, ? extends K> otherKeyExtractor,
        Comparator<? super K> comparator
    ) {
        final Map<K, List<O>> map = (comparator == null)
            ? new HashMap<>()
            : new TreeMap<>(comparator);

        // other enumerator is closed immediately after building the lookup
        try (Enumerator<? extends O> e = other.enumerator()) {
            while (e.moveNext()) {
                O item = e.current();
                K key = otherKeyExtractor.apply(item);

                List<O> bucket = map.get(key);
                if (bucket == null && !map.containsKey(key)) {
                    bucket = new ArrayList<>();
                    map.put(key, bucket);
                }
                bucket.add(item);
            }
        }

        return map;
    }


    private record ListBackedEnumerable<E>(List<E> list) implements Enumerable<E> {

        @Override
            public Enumerator<E> enumerator() {
                return new ListEnumerator<>(list);
            }
        }

    private static final class ListEnumerator<E> extends AbstractEnumerator<E> {
        private final List<E> list;
        private int index = -1;

        ListEnumerator(List<E> list) {
            this.list = list;
        }

        @Override
        protected boolean moveNextCore() {
            int next = index + 1;
            if (next >= list.size()) return false;
            index = next;
            this.current = list.get(index);
            return true;
        }

        @Override
        protected AbstractEnumerator<E> clone() throws CloneNotSupportedException {
            return new ListEnumerator<>(list);
        }
    }

    private static final class EmptyEnumerable<E> implements Enumerable<E> {
        private static final EmptyEnumerable<?> INSTANCE = new EmptyEnumerable<>();

        @SuppressWarnings("unchecked")
        static <E> EmptyEnumerable<E> instance() {
            return (EmptyEnumerable<E>) INSTANCE;
        }

        @Override
        public Enumerator<E> enumerator() {
            return new EmptyEnumerator<>();
        }
    }

    private static final class EmptyEnumerator<E> extends AbstractEnumerator<E> {
        @Override
        protected boolean moveNextCore() {
            return false;
        }

        @Override
        protected AbstractEnumerator<E> clone() throws CloneNotSupportedException {
            return new EmptyEnumerator<>();
        }
    }
}
