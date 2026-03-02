package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;

import java.util.Comparator;
import java.util.function.Function;


import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.OrderedEnumerable;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides LINQ-style ordering operations.
 *
 * <p>
 * Ordering is a buffering operation: it consumes the entire source sequence,
 * sorts it in memory, and then yields the sorted snapshot.
 *
 * <h2>Resource Management</h2>
 * <ul>
 *     <li>The source enumerator is consumed using try-with-resources and closed immediately.</li>
 *     <li>The returned sequence is backed by an in-memory snapshot and holds no external resources.</li>
 * </ul>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Order {
    private Order() {
        throw new UnsupportedOperationException(
            "No instance of " + Order.class.getCanonicalName() + " for you!"
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> OrderedEnumerable<T> order(Enumerable<T> source) {
        return order(
            source,
            (t1, t2) -> ((Comparable<T>) t1).compareTo(t2)
        );
    }

    public static <T> OrderedEnumerable<T> order(Enumerable<T> source, Comparator<? super T> comparator) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(comparator);
        // 返回的是 OrderedEnumerable 的实例，但签名保持 Enumerable<T> 不破坏现有 API
        return new OrderedEnumerableImpl<>(source, comparator);
    }

    @SuppressWarnings("unchecked")
    public static <T, K> OrderedEnumerable<T> orderBy(
        Enumerable<T> source, Function<? super T, ? extends K> keyExtractor
    ) {
        return orderBy(
            source, keyExtractor,
            (k1, k2) -> ((Comparable<K>) k1).compareTo(k2)
        );
    }

    public static <T, K> OrderedEnumerable<T> orderBy(
        Enumerable<T> source, Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(comparator);

        Comparator<T> elementComparator = (a, b) -> comparator.compare(
            keyExtractor.apply(a),
            keyExtractor.apply(b)
        );

        return new OrderedEnumerableImpl<>(source, elementComparator);
    }

    @SuppressWarnings("unchecked")
    public static <T> OrderedEnumerable<T> orderDescending(Enumerable<T> source) {
        return orderDescending(source, (Comparator<? super T>) Comparator.reverseOrder());
    }

    public static <T> OrderedEnumerable<T> orderDescending(Enumerable<T> source, Comparator<? super T> comparator) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(comparator);
        return new OrderedEnumerableImpl<>(source, comparator);
    }

    @SuppressWarnings("unchecked")
    public static <K, T> OrderedEnumerable<T> orderDescendingBy(
        Enumerable<T> source, Function<? super T, ? extends K> keyExtractor
    ) {
        return orderDescendingBy(
            source, keyExtractor,
            (k1, k2) -> ((Comparable<K>) k1).compareTo(k2)
        );
    }

    public static <K, T> OrderedEnumerable<T> orderDescendingBy(
        Enumerable<T> source, Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(comparator);

        Comparator<T> elementComparator = (a, b) -> comparator.compare(
            keyExtractor.apply(a),
            keyExtractor.apply(b)
        );

        return new OrderedEnumerableImpl<>(source, elementComparator.reversed());
    }
}


record OrderedEnumerableImpl<T>(Enumerable<T> source, Comparator<? super T> orderingComparator)
    implements OrderedEnumerable<T>
{

    @Override
    public Enumerator<T> enumerator() {
        return new OrderEnumerator<>(source, orderingComparator);
    }
}

final class OrderEnumerator<T> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Comparator<? super T> comparator;

    private List<T> snapshot; // built once
    private int index = -1;

    OrderEnumerator(Enumerable<T> source, Comparator<? super T> comparator) {
        this.source = source;
        this.comparator = comparator;
    }

    @Override
    protected boolean moveNextCore() {
        if (snapshot == null) {
            snapshot = buildSnapshot(source, comparator);
        }

        int next = index + 1;
        if (next >= snapshot.size()) return false;

        index = next;
        this.current = snapshot.get(index);
        return true;
    }

    private static <T> List<T> buildSnapshot(
        Enumerable<T> source,
        Comparator<? super T> comparator
    ) {
        final List<T> list = new ArrayList<>();

        try (Enumerator<T> e = source.enumerator()) {
            while (e.moveNext()) {
                list.add(e.current());
            }
        }

        // Java List.sort is stable (TimSort)
        list.sort(comparator);
        return list;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        OrderEnumerator<T> e = new OrderEnumerator<>(source, comparator);
        e.snapshot = this.snapshot;
        e.index = -1;
        return e;
    }
}