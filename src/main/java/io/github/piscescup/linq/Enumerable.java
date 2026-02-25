package io.github.piscescup.linq;


import io.github.piscescup.interfaces.Pair;
import io.github.piscescup.interfaces.exfunction.BinFunction;
import io.github.piscescup.linq.enumerable.Groupable;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.function.*;
import java.util.stream.Gatherer;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public interface Enumerable<T> {

    <E extends Enumerator<T>> E enumerator();

    // Enumerable<T> where(Predicate<? super T> predicate);
    // <R> Enumerable<R> select(Function<? super T, ? extends R> selector);
    // Enumerable<T> take(int count);
    // Enumerable<T> skip(int count);
    //
    // List<T> toList();
    // Optional<T> first();
    // Optional<T> first(Predicate<? super T> predicate);
    // long count();
    // boolean any();
    // boolean any(Predicate<? super T> predicate);

    // Aggregation Methods
    <A, R> R aggregate(A identity, BinFunction<A, T, R> aggregator, Function<A, R> resultSelector);

    <R> R aggregate(R seed, BinFunction<R, T, R> aggregator);

    <R> R aggregate(BinFunction<R, T, R> aggregator);

    <K, R> Enumerable<Pair<K, R>> aggregateBy(
        Function<T, K> keyExtractor, Function<K, R> keyMapping,
        BinFunction<R, T, R> resultMapping,
        Comparator<K> comparator
    );

    <K, R> Enumerable<Pair<K, R>> aggregateBy(
        R seed, Function<T, K> keyExtractor, BinFunction<R, T, R> resultMapping,
        Comparator<K> comparator
    );

    // All and Any Methods
    boolean all(Predicate<T> predicate);

    boolean any(Predicate<T> predicate);

    // Append Method
    Enumerable<T> append(T element);

    // Transform Methods
    Enumerable<T> toEnumerable();

    // Stat Methods
    double average(ToDoubleFunction<T> doubleMapping);
    double average(ToIntFunction<T> intMapping);
    double average(ToLongFunction<T> longMapping);

    Enumerable<T[]> chunk(int size);

    Enumerable<T> concat(Enumerable<T> other);

    boolean contains(T element);
    boolean contains(T element, Comparator<T> comparator);

    long count();
    long count(Predicate<T> predicate);

    <K> Enumerable<Pair<K, Long>> countBy(Function<T, K> keyExtractor, Comparator<T> comparator);

    @Nullable
    T defaultIfEmpty();

    T defaultIfEmpty(T defaultElement);

    Enumerable<T> distinct();
    Enumerable<T> distinct(Comparator<T> comparator);

    <K> Enumerable<T> distinctBy(Function<T, K> keyExtractor);
    <K> Enumerable<T> distinctBy(Function<T, K> keyExtractor, Comparator<T> comparator);

    T elementAt(int index);
    T elementAtDefault(int index, T defaultElement);

    Enumerable<T> empty();

    Enumerable<T> except(Enumerable<T> other);
    Enumerable<T> except(Enumerable<T> other, Comparator<T> comparator);

    <K> Enumerable<T> exceptBy(Enumerable<T> other, Function<T, K> keyExtractor);
    <K> Enumerable<T> exceptBy(Enumerable<T> other, Function<T, K> keyExtractor, Comparator<T> comparator);

    T first();
    T first(Predicate<T> predicate);

    T firstOrDefault(T defaultElement);
    T firstOrDefault(T defaultElement, Predicate<T> predicate);

    <K, E, R> Enumerable<R> groupBy(
        Function<T, K> keyExtractor, Function<K, E> elementSelector,
        BinFunction<K, Enumerable<E>, R> resultMapping
    );
    <K, E, R> Enumerable<R> groupBy(
        Function<T, K> keyExtractor, Function<K, E> elementSelector,
        BinFunction<K, Enumerable<E>, R> resultMapping,
        Comparator<K> comparator
    );

    <K, E> Enumerable<Groupable<K, E>> groupBy(
        Function<T, K> keyExtractor, Function<K, E> elementSelector
    );
    <K, E> Enumerable<Groupable<K, E>> groupBy(
        Function<T, K> keyExtractor, Function<K, E> elementSelector,
        Comparator<K> Comparator
    );

    <K, R> Enumerable<R> groupBy(Function<T, K> keyExtractor, BinFunction<K, Enumerable<T>, R> resultMapping);
    <K, R> Enumerable<R> groupBy(
        Function<T, K> keyExtractor, BinFunction<K, Enumerable<T>, R> resultMapping,
        Comparator<K> comparator
    );

    <K> Enumerable<Groupable<K, T>> groupBy(Function<T, K> keyExtractor);
    <K> Enumerable<Groupable<K, T>> groupBy(Function<T, K> keyExtractor, Comparator<K> comparator);




}
