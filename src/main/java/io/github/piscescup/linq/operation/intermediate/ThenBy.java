package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.OrderedEnumerable;
import io.github.piscescup.util.validation.NullCheck;

import java.util.Comparator;
import java.util.function.Function;


/**
 * Secondary ordering operators for {@link OrderedEnumerable}.
 *
 * <p>
 * Equivalent to C# LINQ's ThenBy / ThenByDescending.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class ThenBy {
    private ThenBy() {
        throw new UnsupportedOperationException(
            "No instance of " + ThenBy.class.getCanonicalName() + " for you!"
        );
    }

    @SuppressWarnings("unchecked")
    public static <T, K> OrderedEnumerable<T> thenBy(
        OrderedEnumerable<T> source,
        Function<? super T, ? extends K> keyExtractor
    ) {
        return thenBy(
            source, keyExtractor,
            (k1, k2) -> ((Comparable<K>) k1).compareTo(k2)
        );
    }

    public static <T, K> OrderedEnumerable<T> thenBy(
        OrderedEnumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(comparator);

        Comparator<? super T> combined = ((Comparator<T>) source.orderingComparator())
            .thenComparing(keyExtractor, comparator);

        return new OrderedEnumerableImpl<>(source.source(), combined);
    }

    @SuppressWarnings("unchecked")
    public static <T, K> OrderedEnumerable<T> thenDescendingBy(
        OrderedEnumerable<T> source,
        Function<? super T, ? extends K> keyExtractor
    ) {
        return thenDescendingBy(
            source, keyExtractor,
            (k1, k2) -> ((Comparable<K>) k1).compareTo(k2)
        );
    }

    public static <T, K> OrderedEnumerable<T> thenDescendingBy(
        OrderedEnumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(comparator);

        Comparator<? super T> combined = ((Comparator<T>) source.orderingComparator())
            .thenComparing(keyExtractor, comparator.reversed());

        return new OrderedEnumerableImpl<>(source.source(), combined);
    }
}