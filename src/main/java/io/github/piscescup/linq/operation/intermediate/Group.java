package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.interfaces.exfunction.BinFunction;
import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Groupable;

import java.util.Comparator;
import java.util.function.Function;

import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;
import org.jetbrains.annotations.NotNull;

import java.util.*;


/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Group {
    private Group() {
        throw new UnsupportedOperationException(
            "No instance of " + Group.class.getCanonicalName() + " for you!"
        );
    }


    public static <T, K, E, R> Enumerable<R> groupBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        BinFunction<? super K, ? super Enumerable<E>, ? extends R> resultMapping
    ) {
        return groupBy(source, keyExtractor, elementSelector, resultMapping, null);
    }

    public static <T, K, E, R> Enumerable<R> groupBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        BinFunction<? super K, ? super Enumerable<E>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(elementSelector);
        NullCheck.requireNonNull(resultMapping);

        return new GroupByProjectEnumerable<>(
            source, keyExtractor, elementSelector, resultMapping, comparator
        );
    }

    /* ------------------------------------------------------------ */
    /* groupBy: key + elementSelector -> Groupable                  */
    /* ------------------------------------------------------------ */

    public static <T, K, E> Enumerable<Groupable<K, E>> groupBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector
    ) {
        return groupBy(source, keyExtractor, elementSelector, (Comparator<? super K>) null);
    }

    public static <T, K, E> Enumerable<Groupable<K, E>> groupBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(elementSelector);

        return new GroupByGroupableEnumerable<>(
            source, keyExtractor, elementSelector, comparator
        );
    }

    /* ------------------------------------------------------------ */
    /* groupBy: key + resultMapping (elementSelector = identity)     */
    /* ------------------------------------------------------------ */

    public static <T, K, R> Enumerable<R> groupBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        BinFunction<? super K, ? super Enumerable<T>, ? extends R> resultMapping
    ) {
        return groupBy(source, keyExtractor, resultMapping, null);
    }

    public static <T, K, R> Enumerable<R> groupBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        BinFunction<? super K, ? super Enumerable<T>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);
        NullCheck.requireNonNull(resultMapping);

        return new GroupByProjectEnumerable<>(
            source,
            keyExtractor,
            Function.identity(),
            (k, e) -> resultMapping.apply(k, (Enumerable<T>) e),
            comparator
        );
    }

    /* ------------------------------------------------------------ */
    /* groupBy: key -> Groupable (elementSelector = identity)        */
    /* ------------------------------------------------------------ */

    public static <T, K> Enumerable<Groupable<K, T>> groupBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor
    ) {
        return groupBy(source, keyExtractor, (Comparator<? super K>) null);
    }

    public static <T, K> Enumerable<Groupable<K, T>> groupBy(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(keyExtractor);

        return new GroupByGroupableEnumerable<>(
            source,
            keyExtractor,
            Function.identity(),
            comparator
        );
    }
}

/* ------------------------------------------------------------ */
/* Internal: GroupBy -> Groupable                                */
/* ------------------------------------------------------------ */

class GroupByGroupableEnumerable<T, K, E> implements Enumerable<Groupable<K, E>> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Function<? super T, ? extends E> elementSelector;
    private final Comparator<? super K> comparator; // nullable

    GroupByGroupableEnumerable(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        Comparator<? super K> comparator
    ) {
        this.source = source;
        this.keyExtractor = keyExtractor;
        this.elementSelector = elementSelector;
        this.comparator = comparator;
    }

    @Override
    public Enumerator<Groupable<K, E>> enumerator() {
        return new GroupByGroupableEnumerator<>(source, keyExtractor, elementSelector, comparator);
    }
}

class GroupByGroupableEnumerator<T, K, E> extends AbstractEnumerator<Groupable<K, E>> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Function<? super T, ? extends E> elementSelector;
    private final Comparator<? super K> comparator; // nullable

    private List<Groupable<K, E>> snapshot;
    private int index = -1;

    GroupByGroupableEnumerator(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        Comparator<? super K> comparator
    ) {
        this.source = source;
        this.keyExtractor = keyExtractor;
        this.elementSelector = elementSelector;
        this.comparator = comparator;
    }

    @Override
    protected boolean moveNextCore() {
        if (snapshot == null) {
            snapshot = buildGroupableSnapshot(source, keyExtractor, elementSelector, comparator);
        }

        int next = index + 1;
        if (next >= snapshot.size()) return false;

        index = next;
        this.current = snapshot.get(index);
        return true;
    }

    @Override
    protected AbstractEnumerator<Groupable<K, E>> clone() throws CloneNotSupportedException {
        GroupByGroupableEnumerator<T, K, E> e = new GroupByGroupableEnumerator<>(
            source, keyExtractor, elementSelector, comparator
        );
        e.snapshot = this.snapshot;
        e.index = -1;
        return e;
    }

    private static <T, K, E> List<Groupable<K, E>> buildGroupableSnapshot(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        Comparator<? super K> comparator
    ) {
        final Map<K, List<E>> map = (comparator == null)
            ? new LinkedHashMap<>()
            : new TreeMap<>(comparator);

        try (Enumerator<T> e = source.enumerator()) {
            while (e.moveNext()) {
                T item = e.current();
                K key = keyExtractor.apply(item);
                E element = elementSelector.apply(item);

                List<E> bucket = map.get(key);
                if (bucket == null && !map.containsKey(key)) {
                    bucket = new ArrayList<>();
                    map.put(key, bucket);
                }
                bucket.add(element);
            }
        }

        final List<Groupable<K, E>> list = new ArrayList<>(map.size());
        for (Map.Entry<K, List<E>> entry : map.entrySet()) {
            list.add(new Grouping<>(entry.getKey(), entry.getValue()));
        }
        return list;
    }
}

/* ------------------------------------------------------------ */
/* Internal: GroupBy -> Project R                                */
/* ------------------------------------------------------------ */

class GroupByProjectEnumerable<T, K, E, R> implements Enumerable<R> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Function<? super T, ? extends E> elementSelector;
    private final BinFunction<? super K, ? super Enumerable<E>, ? extends R> resultMapping;
    private final Comparator<? super K> comparator; // nullable

    GroupByProjectEnumerable(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        BinFunction<? super K, ? super Enumerable<E>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        this.source = source;
        this.keyExtractor = keyExtractor;
        this.elementSelector = elementSelector;
        this.resultMapping = resultMapping;
        this.comparator = comparator;
    }

    @Override
    public Enumerator<R> enumerator() {
        return new GroupByProjectEnumerator<>(
            source, keyExtractor, elementSelector, resultMapping, comparator
        );
    }
}

class GroupByProjectEnumerator<T, K, E, R> extends AbstractEnumerator<R> {

    private final Enumerable<T> source;
    private final Function<? super T, ? extends K> keyExtractor;
    private final Function<? super T, ? extends E> elementSelector;
    private final BinFunction<? super K, ? super Enumerable<E>, ? extends R> resultMapping;
    private final Comparator<? super K> comparator; // nullable

    private List<R> snapshot;
    private int index = -1;

    GroupByProjectEnumerator(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        BinFunction<? super K, ? super Enumerable<E>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        this.source = source;
        this.keyExtractor = keyExtractor;
        this.elementSelector = elementSelector;
        this.resultMapping = resultMapping;
        this.comparator = comparator;
    }

    @Override
    protected boolean moveNextCore() {
        if (snapshot == null) {
            snapshot = buildProjectSnapshot(
                source, keyExtractor, elementSelector, resultMapping, comparator
            );
        }

        int next = index + 1;
        if (next >= snapshot.size()) return false;

        index = next;
        this.current = snapshot.get(index);
        return true;
    }

    @Override
    protected AbstractEnumerator<R> clone() throws CloneNotSupportedException {
        GroupByProjectEnumerator<T, K, E, R> e = new GroupByProjectEnumerator<>(
            source, keyExtractor, elementSelector, resultMapping, comparator
        );
        e.snapshot = this.snapshot;
        e.index = -1;
        return e;
    }

    private static <T, K, E, R> List<R> buildProjectSnapshot(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        BinFunction<? super K, ? super Enumerable<E>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        final Map<K, List<E>> map = (comparator == null)
            ? new LinkedHashMap<>()
            : new TreeMap<>(comparator);

        try (Enumerator<T> e = source.enumerator()) {
            while (e.moveNext()) {
                T item = e.current();
                K key = keyExtractor.apply(item);
                E element = elementSelector.apply(item);

                List<E> bucket = map.get(key);
                if (bucket == null && !map.containsKey(key)) {
                    bucket = new ArrayList<>();
                    map.put(key, bucket);
                }
                bucket.add(element);
            }
        }

        final List<R> list = new ArrayList<>(map.size());
        for (Map.Entry<K, List<E>> entry : map.entrySet()) {
            Enumerable<E> groupEnumerable = new ListBackedEnumerable<>(entry.getValue());
            list.add(resultMapping.apply(entry.getKey(), groupEnumerable));
        }
        return list;
    }
}

record Grouping<K, E>(K key, List<E> elements) implements Groupable<K, E> {

    public Enumerator<E> enumerator() {
        return new ListEnumerator<>(elements);
    }

    @Override
    public @NotNull String toString() {
        return "Key '%s' -> %s".formatted(key, elements);
    }
}

class ListBackedEnumerable<E> implements Enumerable<E> {

    private final List<E> list;

    ListBackedEnumerable(List<E> list) {
        this.list = list;
    }

    @Override
    public Enumerator<E> enumerator() {
        return new ListEnumerator<>(list);
    }
}

class ListEnumerator<E> extends AbstractEnumerator<E> {

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