package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.interfaces.Pair;
import io.github.piscescup.interfaces.exfunction.BinFunction;
import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.pair.ImmutablePair;
import io.github.piscescup.util.validation.NullCheck;
import org.jetbrains.annotations.NotNull;

import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.util.function.Function;

/**
 * Provides LINQ-style grouped aggregation functionality.
 *
 * <p>
 * {@code aggregateBy} groups elements of a source sequence according to a key,
 * and accumulates values for each key using a specified accumulator function.
 * The resulting sequence contains {@link Pair} entries where:
 *
 * <ul>
 *     <li>{@code Pair.first()}  = grouping key</li>
 *     <li>{@code Pair.second()} = aggregated result for that key</li>
 * </ul>
 *
 * <p>
 * The output is ordered according to the supplied {@link Comparator}.
 *
 * <h2>Overloads</h2>
 *
 * <h3>1. Key-based initialization</h3>
 * The accumulator is initialized using a function derived from the key.
 *
 * <pre>{@code
 * Enumerable<String> source = ...
 *
 * Enumerable<Pair<Character, Integer>> result =
 *     AggregateBy.aggregateBy(
 *         source,
 *         s -> s.charAt(0),     // key extractor
 *         key -> 0,            // initial accumulator from key
 *         (acc, s) -> acc + 1, // count
 *         Comparator.naturalOrder()
 *     );
 * }</pre>
 *
 * <h3>2. Fixed seed initialization</h3>
 * The accumulator is initialized using a constant seed value.
 *
 * <pre>{@code
 * Enumerable<Integer> numbers = ...
 *
 * Enumerable<Pair<Integer, Integer>> result =
 *     AggregateBy.aggregateBy(
 *         numbers,
 *         0,                          // seed
 *         n -> n % 2,                 // key: even / odd
 *         (acc, n) -> acc + n,        // sum
 *         Comparator.naturalOrder()
 *     );
 * }</pre>
 *
 * <h2>Execution Model</h2>
 *
 * <ul>
 *     <li>This is an intermediate operation.</li>
 *     <li>The source is fully consumed on first enumeration.</li>
 *     <li>Results are materialized into a snapshot list.</li>
 *     <li>Subsequent enumerations reuse the snapshot.</li>
 * </ul>
 *
 * <h2>Null Handling</h2>
 *
 * All arguments must be non-null.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class AggregateBy {
    private AggregateBy() {
        throw new UnsupportedOperationException(
            "No instance of " + AggregateBy.class.getCanonicalName() + " for you"
        );
    }

    /**
     * Groups elements by key and aggregates values using a key-derived
     * initial accumulator value.
     *
     * <p>
     * For each distinct key:
     *
     * <ol>
     *     <li>The accumulator is initialized using {@code keyMapping.apply(key)}</li>
     *     <li>Each element with the same key is processed by {@code resultMapping}</li>
     * </ol>
     *
     * @param source         the source sequence
     * @param keyExtractor   function that extracts grouping key from element
     * @param keyMapping     function that produces initial accumulator from key
     * @param resultMapping  accumulator function (accumulator, element) → new accumulator
     * @param comparator     comparator used to order keys
     *
     * @param <T> source element type
     * @param <K> grouping key type
     * @param <R> accumulator/result type
     *
     * @return an {@code Enumerable<Pair<K, R>>} containing aggregated results per key
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T, K, R> Enumerable<Pair<K, R>> aggregateBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super K, ? extends R> keyMapping,
        BinFunction<? super R, ? super T, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(keyMapping);
        NullCheck.requireNonNull(resultMapping);
        NullCheck.requireNonNull(comparator);

        return new AggregateByEnumerable<>(
            source,
            keyExtractor,
            keyMapping,
            null,
            resultMapping,
            comparator,
            true
        );
    }

    /**
     * Groups elements by key and aggregates values using a fixed seed
     * as the initial accumulator value.
     *
     * <p>
     * For each distinct key:
     *
     * <ol>
     *     <li>The accumulator is initialized using {@code seed}</li>
     *     <li>Each element with the same key is processed by {@code resultMapping}</li>
     * </ol>
     *
     * @param source         the source sequence
     * @param seed           initial accumulator value for each new key
     * @param keyExtractor   function that extracts grouping key from element
     * @param resultMapping  accumulator function (accumulator, element) → new accumulator
     * @param comparator     comparator used to order keys
     *
     * @param <T> source element type
     * @param <K> grouping key type
     * @param <R> accumulator/result type
     *
     * @return an {@code Enumerable<Pair<K, R>>} containing aggregated results per key
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T, K, R> Enumerable<Pair<K, R>> aggregateBy(
        Enumerable<T> source, R seed,
        Function<? super T, ? extends K> keyExtractor,
        BinFunction<? super R, ? super T, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(seed);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(resultMapping);
        NullCheck.requireNonNull(comparator);

        return new AggregateByEnumerable<>(
            source,
            keyExtractor,
            null,
            seed,
            resultMapping,
            comparator,
            false
        );
    }

}

/**
 * Enumerable implementation for {@link AggregateBy}.
 *
 * <p>
 * This class defers execution until enumeration begins.
 * Each call to {@link #enumerator()} produces a new enumerator instance.
 *
 * @param <T> source element type
 * @param <K> grouping key type
 * @param <R> accumulator/result type
 */
class AggregateByEnumerable<T, K, R> implements Enumerable<Pair<K, R>> {
    private final Enumerable<T> source;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Function<? super K, ? extends R> keyMapping; // optional
    private final R seed;                                      // optional
    private final BinFunction<? super R, ? super T, ? extends R> resultMapping;
    private final Comparator<? super K> comparator;
    private final boolean useKeyMapping;

    AggregateByEnumerable(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super K, ? extends R> keyMapping,
        R seed,
        BinFunction<? super R, ? super T, ? extends R> resultMapping,
        Comparator<? super K> comparator,
        boolean useKeyMapping
    ) {
        this.source = source;
        this.keyExtractor = keyExtractor;
        this.keyMapping = keyMapping;
        this.seed = seed;
        this.resultMapping = resultMapping;
        this.comparator = comparator;
        this.useKeyMapping = useKeyMapping;
    }

    @Override
    public Enumerator<Pair<K, R>> enumerator() {
        return new AggregateByEnumerator<>(
            source, keyExtractor, keyMapping, seed, resultMapping, comparator, useKeyMapping
        );
    }

}

/**
 * Enumerator implementation for grouped aggregation.
 *
 * <p>
 * On first iteration, the source sequence is fully consumed and
 * aggregated into a {@link TreeMap} ordered by the provided comparator.
 *
 * <p>
 * Results are then materialized into an immutable snapshot list of pairs.
 * Subsequent iterations reuse the snapshot.
 *
 * <p>
 * This design trades memory for predictable iteration and stable ordering.
 *
 * @param <T> source element type
 * @param <K> grouping key type
 * @param <R> accumulator/result type
 */
class AggregateByEnumerator<T, K, R> extends AbstractEnumerator<Pair<K, R>> {
    private final Enumerable<T> source;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Function<? super K, ? extends R> keyMapping; // optional
    private final R seed;                                      // optional
    private final BinFunction<? super R, ? super T, ? extends R> resultMapping;
    private final Comparator<? super K> comparator;
    private final boolean useKeyMapping;

    private List<Pair<K, R>> snapshot;       // computed once
    private int index = -1;

    AggregateByEnumerator(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super K, ? extends R> keyMapping,
        R seed,
        BinFunction<? super R, ? super T, ? extends R> resultMapping,
        Comparator<? super K> comparator,
        boolean useKeyMapping
    ) {
        this.source = source;
        this.keyExtractor = keyExtractor;
        this.keyMapping = keyMapping;
        this.seed = seed;
        this.resultMapping = resultMapping;
        this.comparator = comparator;
        this.useKeyMapping = useKeyMapping;
    }

    @Override
    protected boolean moveNextCore() {
        if (snapshot == null) {
            snapshot = buildSnapshot(
                source, keyExtractor, keyMapping, seed, resultMapping, comparator, useKeyMapping
            );
        }

        int next = index + 1;
        if (next >= snapshot.size()) {
            return false;
        }

        index = next;
        this.current = snapshot.get(index);
        return true;
    }

    @Override
    protected AbstractEnumerator<Pair<K, R>> clone() throws CloneNotSupportedException {
        AggregateByEnumerator<T, K, R> e = new AggregateByEnumerator<>(
            source, keyExtractor, keyMapping, seed, resultMapping, comparator, useKeyMapping
        );
        e.snapshot = this.snapshot;
        e.index = -1;
        return e;
    }

    private static <T, K, R> List<Pair<K, R>> buildSnapshot(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super K, ? extends R> keyMapping,
        R seed,
        BinFunction<? super R, ? super T, ? extends R> resultMapping,
        Comparator<? super K> comparator,
        boolean useKeyMapping
    ) {
        final Map<K, R> map = new TreeMap<>(comparator);

        try (Enumerator<T> e = source.enumerator()) {
            while (e.moveNext()) {
                T item = e.current();
                K key = keyExtractor.apply(item);

                R acc = map.get(key);
                if (acc == null && !map.containsKey(key)) {
                    // 第一次看到该 key：初始化累积值
                    acc = useKeyMapping ? keyMapping.apply(key) : seed;
                }

                R next = resultMapping.apply(acc, item);
                map.put(key, next);
            }
        }

        final List<Pair<K, R>> list = new ArrayList<>(map.size());
        for (Map.Entry<K, R> entry : map.entrySet()) {
            list.add(ImmutablePair.of(entry.getKey(), entry.getValue()));
        }
        return list;
    }
}
