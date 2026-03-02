package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Function;


import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.*;

/**
 * Provides LINQ-style join operations.
 *
 * <p>
 * Includes:
 * <ul>
 *     <li>{@link #join}: inner join (only matched pairs)</li>
 *     <li>{@link #leftJoin}: left outer join (all left rows; unmatched right -> null)</li>
 *     <li>{@link #rightJoin}: right outer join (all right rows; unmatched left -> null)</li>
 * </ul>
 *
 * <h2>Execution Model</h2>
 * <ul>
 *     <li>The {@code other} sequence is fully consumed once to build a lookup table.</li>
 *     <li>The {@code source} sequence is streamed lazily.</li>
 *     <li>Results are produced in encounter order of {@code source} (and encounter order within each matching group).</li>
 * </ul>
 *
 * <h2>Resource Management</h2>
 * <ul>
 *     <li>{@code other} is consumed using try-with-resources and closed immediately after building the lookup.</li>
 *     <li>{@code source} is streamed and must be closed by closing the resulting {@link Enumerator}.</li>
 * </ul>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Join {
    private Join() {
        throw new UnsupportedOperationException(
            "No instance of " + Join.class.getCanonicalName() + " for you!"
        );
    }

    /* ------------------------------------------------------------ */
    /* Inner Join                                                    */
    /* ------------------------------------------------------------ */

    @SuppressWarnings("unchecked")
    public static <T, O, K, R> Enumerable<R> join(
        Enumerable<T> source, Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super O, ? extends R> resultMapping
    ) {
        return join(
            source, other, selfKeyExtractor, otherKeyExtractor, resultMapping,
            (k1, k2) -> ((Comparable<K>) k1).compareTo(k2)
        );
    }

    /**
     * Performs an inner join of two sequences.
     *
     * <p>
     * For each element in {@code source}, all matching elements from {@code other}
     * are paired and projected via {@code resultMapping}.
     *
     * <p>
     * Keys are considered equal when {@code comparator.compare(a, b) == 0}.
     *
     * @param source left (outer) sequence
     * @param other right (inner) sequence
     * @param selfKeyExtractor extracts join key from source element
     * @param otherKeyExtractor extracts join key from other element
     * @param resultMapping projects a matched pair into a result
     * @param comparator comparator used for key equality (compare==0)
     *
     * @param <T> left element type
     * @param <O> right element type
     * @param <K> key type
     * @param <R> result type
     *
     * @return a sequence of joined results (inner join)
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T, O, K, R> Enumerable<R> join(
        Enumerable<T> source, Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super O, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(selfKeyExtractor);
        NullCheck.requireNonNull(otherKeyExtractor);
        NullCheck.requireNonNull(resultMapping);
        NullCheck.requireNonNull(comparator);

        return new JoinEnumerable<>(source, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator, JoinMode.INNER);
    }

    /* ------------------------------------------------------------ */
    /* Left Join                                                     */
    /* ------------------------------------------------------------ */

    @SuppressWarnings("unchecked")
    public static <T, O, K, R> Enumerable<R> leftJoin(
        Enumerable<T> source, Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping
    ) {
        return leftJoin(
            source, other, selfKeyExtractor, otherKeyExtractor, resultMapping,
            (k1, k2) -> ((Comparable<K>) k1).compareTo(k2)
        );
    }

    /**
     * Performs a left outer join of two sequences.
     *
     * <p>
     * For each element in {@code source}:
     * <ul>
     *     <li>If matches exist in {@code other}, produces one result per match.</li>
     *     <li>If no match exists, produces exactly one result with {@code right = null}.</li>
     * </ul>
     *
     * @param source left (outer) sequence
     * @param other right (inner) sequence
     * @param selfKeyExtractor extracts join key from source element
     * @param otherKeyExtractor extracts join key from other element
     * @param resultMapping projects a left element and a (possibly null) right element into a result
     * @param comparator comparator used for key equality (compare==0)
     *
     * @param <T> left element type
     * @param <O> right element type
     * @param <K> key type
     * @param <R> result type
     *
     * @return a sequence of joined results (left outer join)
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T, O, K, R> Enumerable<R> leftJoin(
        Enumerable<T> source, Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(selfKeyExtractor);
        NullCheck.requireNonNull(otherKeyExtractor);
        NullCheck.requireNonNull(resultMapping);
        NullCheck.requireNonNull(comparator);

        return new LeftJoinEnumerable<>(source, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator);
    }

    /* ------------------------------------------------------------ */
    /* Right Join                                                    */
    /* ------------------------------------------------------------ */

    @SuppressWarnings("unchecked")
    public static <T, O, K, R> Enumerable<R> rightJoin(
        Enumerable<T> source, Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping
    ) {
        return rightJoin(
            source, other, selfKeyExtractor, otherKeyExtractor, resultMapping,
            (k1, k2) -> ((Comparable<K>) k1).compareTo(k2)
        );
    }

    /**
     * Performs a right outer join of two sequences.
     *
     * <p>
     * This is equivalent to swapping sides of a left join, but the projection still receives
     * {@code (left, right)} where {@code left} may be {@code null} when unmatched.
     *
     * <p>
     * For each element in {@code other}:
     * <ul>
     *     <li>If matches exist in {@code source}, produces one result per match.</li>
     *     <li>If no match exists, produces exactly one result with {@code left = null}.</li>
     * </ul>
     *
     * @param source left sequence
     * @param other right (outer) sequence
     * @param selfKeyExtractor extracts join key from source element
     * @param otherKeyExtractor extracts join key from other element
     * @param resultMapping projects a (possibly null) left element and a right element into a result
     * @param comparator comparator used for key equality (compare==0)
     *
     * @param <T> left element type
     * @param <O> right element type
     * @param <K> key type
     * @param <R> result type
     *
     * @return a sequence of joined results (right outer join)
     *
     * @throws NullPointerException if any argument is null
     */
    public static <T, O, K, R> Enumerable<R> rightJoin(
        Enumerable<T> source, Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(other);
        NullCheck.requireNonNull(selfKeyExtractor);
        NullCheck.requireNonNull(otherKeyExtractor);
        NullCheck.requireNonNull(resultMapping);
        NullCheck.requireNonNull(comparator);

        return new RightJoinEnumerable<>(source, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator);
    }

    enum JoinMode { INNER }
}

/* ====================================================================== */
/* Package-private implementation classes (able / tor)                     */
/* ====================================================================== */

final class JoinEnumerable<T, O, K, R> implements Enumerable<R> {

    private final Enumerable<T> source;
    private final Enumerable<? extends O> other;
    private final Function<? super T, ? extends K> selfKeyExtractor;
    private final Function<? super O, ? extends K> otherKeyExtractor;
    private final BiFunction<? super T, ? super O, ? extends R> resultMapping;
    private final Comparator<? super K> comparator;

    private final Join.JoinMode mode;

    JoinEnumerable(
        Enumerable<T> source,
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super O, ? extends R> resultMapping,
        Comparator<? super K> comparator,
        Join.JoinMode mode
    ) {
        this.source = source;
        this.other = other;
        this.selfKeyExtractor = selfKeyExtractor;
        this.otherKeyExtractor = otherKeyExtractor;
        this.resultMapping = resultMapping;
        this.comparator = comparator;
        this.mode = mode;
    }

    @Override
    public Enumerator<R> enumerator() {
        return new JoinEnumerator<>(source, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator, mode);
    }
}

final class JoinEnumerator<T, O, K, R> extends AbstractEnumerator<R> {

    private final Enumerable<T> source;
    private final Enumerable<? extends O> other;
    private final Function<? super T, ? extends K> selfKeyExtractor;
    private final Function<? super O, ? extends K> otherKeyExtractor;
    private final BiFunction<? super T, ? super O, ? extends R> resultMapping;
    private final Comparator<? super K> comparator;
    private final Join.JoinMode mode;

    private Enumerator<T> sourceEnumerator;

    // other lookup: key -> list of O (built once)
    private Map<K, List<O>> otherLookup;

    // current outer element and its matching list iteration
    private T currentLeft;
    private List<O> currentRights;
    private int rightIndex = -1;

    JoinEnumerator(
        Enumerable<T> source,
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super O, ? extends R> resultMapping,
        Comparator<? super K> comparator,
        Join.JoinMode mode
    ) {
        this.source = source;
        this.other = other;
        this.selfKeyExtractor = selfKeyExtractor;
        this.otherKeyExtractor = otherKeyExtractor;
        this.resultMapping = resultMapping;
        this.comparator = comparator;
        this.mode = mode;
    }

    @Override
    protected boolean moveNextCore() {

        if (otherLookup == null) {
            otherLookup = buildLookup(other, otherKeyExtractor, comparator); // closes other
            sourceEnumerator = source.enumerator();
        }

        while (true) {

            // If we already have a left element with pending right matches, emit next match.
            if (currentRights != null) {
                int nextRight = rightIndex + 1;
                if (nextRight < currentRights.size()) {
                    rightIndex = nextRight;
                    O right = currentRights.get(rightIndex);
                    this.current = resultMapping.apply(currentLeft, right);
                    return true;
                } else {
                    // Done with current left's right matches; reset state and continue to fetch next left.
                    currentLeft = null;
                    currentRights = null;
                    rightIndex = -1;
                }
            }

            // Fetch next left element
            if (!sourceEnumerator.moveNext()) {
                return false;
            }

            currentLeft = sourceEnumerator.current();
            K key = selfKeyExtractor.apply(currentLeft);

            List<O> rights = otherLookup.get(key);
            if (rights == null || rights.isEmpty()) {
                // INNER JOIN: no match => skip
                continue;
            }

            currentRights = rights;
            rightIndex = -1;
            // loop will emit first right match in the next iteration
        }
    }

    static <O, K> Map<K, List<O>> buildLookup(
        Enumerable<? extends O> other,
        Function<? super O, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        final Map<K, List<O>> map = new TreeMap<>(comparator);
        try (Enumerator<? extends O> e = other.enumerator()) {
            while (e.moveNext()) {
                O item = e.current();
                K key = keyExtractor.apply(item);

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

    @Override
    protected AbstractEnumerator<R> clone() throws CloneNotSupportedException {
        return new JoinEnumerator<>(source, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator, mode);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
        otherLookup = null;
        currentLeft = null;
        currentRights = null;
        rightIndex = -1;
    }
}

/* ------------------------------------------------------------ */
/* Left Join                                                     */
/* ------------------------------------------------------------ */

final class LeftJoinEnumerable<T, O, K, R> implements Enumerable<R> {

    private final Enumerable<T> source;
    private final Enumerable<? extends O> other;
    private final Function<? super T, ? extends K> selfKeyExtractor;
    private final Function<? super O, ? extends K> otherKeyExtractor;
    private final BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping;
    private final Comparator<? super K> comparator;

    LeftJoinEnumerable(
        Enumerable<T> source,
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping,
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
        return new LeftJoinEnumerator<>(source, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator);
    }
}

final class LeftJoinEnumerator<T, O, K, R> extends AbstractEnumerator<R> {

    private final Enumerable<T> source;
    private final Enumerable<? extends O> other;
    private final Function<? super T, ? extends K> selfKeyExtractor;
    private final Function<? super O, ? extends K> otherKeyExtractor;
    private final BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping;
    private final Comparator<? super K> comparator;

    private Enumerator<T> sourceEnumerator;
    private Map<K, List<O>> otherLookup;

    private T currentLeft;
    private List<O> currentRights;
    private int rightIndex = -1;
    private boolean emittedNullForCurrentLeft = false;

    LeftJoinEnumerator(
        Enumerable<T> source,
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping,
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

        if (otherLookup == null) {
            otherLookup = JoinEnumerator.buildLookup(other, otherKeyExtractor, comparator); // closes other
            sourceEnumerator = source.enumerator();
        }

        while (true) {

            // If we have pending right matches, emit them first.
            if (currentRights != null) {
                int nextRight = rightIndex + 1;
                if (nextRight < currentRights.size()) {
                    rightIndex = nextRight;
                    O right = currentRights.get(rightIndex);
                    this.current = resultMapping.apply(currentLeft, right);
                    return true;
                } else {
                    // finished current rights
                    currentLeft = null;
                    currentRights = null;
                    rightIndex = -1;
                    emittedNullForCurrentLeft = false;
                }
            }

            // If current left had no rights and we haven't emitted null result yet, emit it.
            if (currentLeft != null && !emittedNullForCurrentLeft) {
                emittedNullForCurrentLeft = true;
                this.current = resultMapping.apply(currentLeft, null);
                return true;
            }

            // Fetch next left
            if (!sourceEnumerator.moveNext()) {
                return false;
            }

            currentLeft = sourceEnumerator.current();
            K key = selfKeyExtractor.apply(currentLeft);

            List<O> rights = otherLookup.get(key);
            if (rights == null || rights.isEmpty()) {
                // no match => emit null once
                currentRights = null;
                rightIndex = -1;
                emittedNullForCurrentLeft = false;
                // next loop iteration will emit null branch
            } else {
                currentRights = rights;
                rightIndex = -1;
                emittedNullForCurrentLeft = true; // not needed when rights exist
                // next loop iteration will emit first right
            }
        }
    }

    @Override
    protected AbstractEnumerator<R> clone() throws CloneNotSupportedException {
        return new LeftJoinEnumerator<>(source, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
        otherLookup = null;
        currentLeft = null;
        currentRights = null;
        rightIndex = -1;
        emittedNullForCurrentLeft = false;
    }
}

/* ------------------------------------------------------------ */
/* Right Join                                                    */
/* ------------------------------------------------------------ */

final class RightJoinEnumerable<T, O, K, R> implements Enumerable<R> {

    private final Enumerable<T> source;
    private final Enumerable<? extends O> other;
    private final Function<? super T, ? extends K> selfKeyExtractor;
    private final Function<? super O, ? extends K> otherKeyExtractor;
    private final BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping;
    private final Comparator<? super K> comparator;

    RightJoinEnumerable(
        Enumerable<T> source,
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping,
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
        return new RightJoinEnumerator<>(source, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator);
    }
}

final class RightJoinEnumerator<T, O, K, R> extends AbstractEnumerator<R> {

    private final Enumerable<T> source;
    private final Enumerable<? extends O> other;
    private final Function<? super T, ? extends K> selfKeyExtractor;
    private final Function<? super O, ? extends K> otherKeyExtractor;
    private final BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping;
    private final Comparator<? super K> comparator;

    private Enumerator<? extends O> otherEnumerator;
    private Map<K, List<T>> sourceLookup;

    private O currentRight;
    private List<T> currentLefts;
    private int leftIndex = -1;
    private boolean emittedNullForCurrentRight = false;

    RightJoinEnumerator(
        Enumerable<T> source,
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping,
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

        if (sourceLookup == null) {
            sourceLookup = buildSourceLookup(source, selfKeyExtractor, comparator); // closes source enumerator used here
            otherEnumerator = other.enumerator(); // streamed, closed in close()
        }

        while (true) {

            // Emit pending left matches for current right.
            if (currentLefts != null) {
                int nextLeft = leftIndex + 1;
                if (nextLeft < currentLefts.size()) {
                    leftIndex = nextLeft;
                    T left = currentLefts.get(leftIndex);
                    this.current = resultMapping.apply(left, currentRight);
                    return true;
                } else {
                    currentRight = null;
                    currentLefts = null;
                    leftIndex = -1;
                    emittedNullForCurrentRight = false;
                }
            }

            // If current right had no lefts and we haven't emitted null yet, emit it.
            if (currentRight != null && !emittedNullForCurrentRight) {
                emittedNullForCurrentRight = true;
                this.current = resultMapping.apply(null, currentRight);
                return true;
            }

            // Fetch next right
            if (!otherEnumerator.moveNext()) {
                return false;
            }

            currentRight = otherEnumerator.current();
            K key = otherKeyExtractor.apply(currentRight);

            List<T> lefts = sourceLookup.get(key);
            if (lefts == null || lefts.isEmpty()) {
                currentLefts = null;
                leftIndex = -1;
                emittedNullForCurrentRight = false;
                // next loop will emit null branch
            } else {
                currentLefts = lefts;
                leftIndex = -1;
                emittedNullForCurrentRight = true;
                // next loop will emit first left match
            }
        }
    }

    private static <T, K> Map<K, List<T>> buildSourceLookup(
        Enumerable<T> source,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        final Map<K, List<T>> map = new TreeMap<>(comparator);
        try (Enumerator<T> e = source.enumerator()) { // ✅ closes here
            while (e.moveNext()) {
                T item = e.current();
                K key = keyExtractor.apply(item);

                List<T> bucket = map.get(key);
                if (bucket == null && !map.containsKey(key)) {
                    bucket = new ArrayList<>();
                    map.put(key, bucket);
                }
                bucket.add(item);
            }
        }
        return map;
    }

    @Override
    protected AbstractEnumerator<R> clone() throws CloneNotSupportedException {
        return new RightJoinEnumerator<>(source, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator);
    }

    @Override
    public void close() {
        if (otherEnumerator != null) {
            otherEnumerator.close();
            otherEnumerator = null;
        }
        sourceLookup = null;
        currentRight = null;
        currentLefts = null;
        leftIndex = -1;
        emittedNullForCurrentRight = false;
    }
}