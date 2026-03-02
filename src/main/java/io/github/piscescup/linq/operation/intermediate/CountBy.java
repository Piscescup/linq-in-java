package io.github.piscescup.linq.operation.intermediate;


import io.github.piscescup.interfaces.Pair;
import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.pair.ImmutablePair;
import io.github.piscescup.util.validation.NullCheck;

import java.util.*;
import java.util.function.Function;

/**
 * Provides LINQ-style grouped counting operation.
 *
 * <p>
 * Groups elements by key and counts the number of elements in each group.
 * The output is ordered according to the supplied {@link Comparator}.
 *
 * <h2>Example</h2>
 * <pre>{@code
 * Enumerable<String> words = Enumerable.of("a", "bb", "c", "dd", "eee");
 *
 * Enumerable<Pair<Integer, Long>> result =
 *     CountBy.countBy(words, String::length, Comparator.naturalOrder());
 *
 * // Produces:
 * // (1, 2)  // "a", "c"
 * // (2, 2)  // "bb", "dd"
 * // (3, 1)  // "eee"
 * }</pre>
 *
 * <p>
 * This operation is lazy but buffering: the source is fully consumed on the first enumeration
 * to build a snapshot of counts.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class CountBy {
    private CountBy()  {
        throw new UnsupportedOperationException(
            "No instance of " + CountBy.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Counts elements in the source sequence for each key.
     *
     * @param source       the source sequence
     * @param keyExtractor extracts grouping key from element
     * @param comparator   comparator used to order keys
     * @param <T>          source element type
     * @param <K>          key type
     * @return an {@link Enumerable} of pairs (key, count)
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T, K> Enumerable<Pair<K, Long>> countBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(comparator);

        return new CountByEnumerable<>(source, keyExtractor, comparator);
    }
}

class CountByEnumerable<T, K> implements Enumerable<Pair<K, Long>> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Comparator<? super K> comparator;

    CountByEnumerable(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        this.source = source;
        this.keyExtractor = keyExtractor;
        this.comparator = comparator;
    }

    @Override
    public Enumerator<Pair<K, Long>> enumerator() {
        return new CountByEnumerator<>(source, keyExtractor, comparator);
    }
}

class CountByEnumerator<T, K> extends AbstractEnumerator<Pair<K, Long>> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Comparator<? super K> comparator;

    private List<Pair<K, Long>> snapshot;
    private int index = -1;

    CountByEnumerator(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        this.source = source;
        this.keyExtractor = keyExtractor;
        this.comparator = comparator;
    }

    @Override
    protected boolean moveNextCore() {
        if (snapshot == null) {
            snapshot = buildSnapshot(source, keyExtractor, comparator);
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
    protected AbstractEnumerator<Pair<K, Long>> clone() throws CloneNotSupportedException {
        CountByEnumerator<T, K> e = new CountByEnumerator<>(source, keyExtractor, comparator);
        e.snapshot = this.snapshot;
        e.index = -1;
        return e;
    }

    private static <T, K> List<Pair<K, Long>> buildSnapshot(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        final Map<K, Long> map = new TreeMap<>(comparator);

        try (Enumerator<T> e = source.enumerator()) {
            while (e.moveNext()) {
                T item = e.current();
                K key = keyExtractor.apply(item);

                Long old = map.get(key);
                if (old == null && !map.containsKey(key)) {
                    map.put(key, 1L);
                } else {
                    map.put(key, old + 1L);
                }
            }
        }

        final List<Pair<K, Long>> list = new ArrayList<>(map.size());
        for (Map.Entry<K, Long> entry : map.entrySet()) {
            list.add(ImmutablePair.of(entry.getKey(), entry.getValue()));
        }
        return list;
    }
}