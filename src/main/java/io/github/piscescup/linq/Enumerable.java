package io.github.piscescup.linq;


import io.github.piscescup.interfaces.Pair;
import io.github.piscescup.interfaces.exfunction.BinFunction;
import io.github.piscescup.interfaces.exfunction.BinPredicate;
import io.github.piscescup.linq.operation.intermediate.*;
import io.github.piscescup.linq.operation.terminal.*;
import io.github.piscescup.util.validation.NullCheck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


/**
 * Represents a LINQ-style queryable sequence of elements (similar to C# {@code IEnumerable<T>}).
 *
 * <p>This interface is the core abstraction of the library. It supports a pull-based iteration model
 * via {@link #enumerator()}, and provides fluent query operators such as filtering, projection, ordering,
 * grouping, joining, set operations, and aggregations.</p>
 *
 * <h2>Execution model</h2>
 * <ul>
 *   <li><b>Deferred execution:</b> most intermediate operations (e.g. {@code where}, {@code select}, {@code orderBy})
 *       are expected to be lazy. The source is not traversed until enumeration begins.</li>
 *   <li><b>Fresh enumerator:</b> each call to {@link #enumerator()} must return a new enumerator positioned
 *       before the first element.</li>
 *   <li><b>Multiple enumeration:</b> repeated enumeration may or may not yield identical results, depending on
 *       the underlying data source.</li>
 *   <li><b>Null policy:</b> unless explicitly documented otherwise (or annotated {@link Nullable}),
 *       {@code null} functional parameters and comparators are rejected (typically with {@link NullPointerException}).</li>
 * </ul>
 *
 * <h2>Examples</h2>
 *
 * <h3>Filtering and projection</h3>
 * <pre>{@code
 * Enumerable<Integer> numbers = Enumerable.of(1, 2, 3, 4, 5);
 * List<Integer> result = numbers
 *     .where(n -> n % 2 == 0)
 *     .select(n -> n * 10)
 *     .toList();
 * // result = [20, 40]
 * }</pre>
 *
 * <h3>Grouping</h3>
 * <pre>{@code
 * record Person(String name, int age) {}
 * Enumerable<Person> people = Enumerable.of(
 *     new Person("Alice", 20),
 *     new Person("Bob", 18),
 *     new Person("Cindy", 20)
 * );
 *
 * List<Pair<Integer, Long>> grouped =
 *     people.countBy(Person::age, Comparator.naturalOrder()).toList();
 * }</pre>
 *
 * <h3>Join</h3>
 * <pre>{@code
 * record User(int id, String name) {}
 * record Order(int userId, String item) {}
 *
 * Enumerable<User> users = Enumerable.of(new User(1, "Alice"), new User(2, "Bob"));
 * Enumerable<Order> orders = Enumerable.of(new Order(1, "Book"), new Order(1, "Pen"));
 *
 * List<String> lines = users.join(
 *     orders,
 *     User::id,
 *     Order::userId,
 *     (u, o) -> u.name() + " bought " + o.item()
 * ).toList();
 * }</pre>
 *
 * @param <T> the element type of the sequence
 * @author Ren YuanTong
 * @since 1.0.0
 */
public interface Enumerable<T> extends Iterable<T> {

    /**
     * Returns a new enumerator that iterates over this sequence.
     *
     * <p>Each invocation must return a <b>fresh</b> enumerator instance positioned before the first element.</p>
     *
     * <p>Typical usage:</p>
     * <pre>{@code
     * try (Enumerator<T> e = enumerable.enumerator()) {
     *     while (e.moveNext()) {
     *         T x = e.current();
     *         // ...
     *     }
     * }
     * }</pre>
     *
     * @return a new {@link Enumerator} for this sequence
     * @throws RuntimeException if the underlying source cannot create an enumerator (implementation-defined)
     */
    Enumerator<T> enumerator();

    /**
     * Returns an iterator over the elements in this sequence.
     *
     * <p>This default implementation delegates to {@link #enumerator()}.</p>
     *
     * @return an iterator over the elements in this sequence
     */
    @Override
    @NotNull
    default Iterator<T> iterator() {
        return enumerator();
    }

    /**
     * Aggregates the sequence into an accumulator of type {@code A}, then maps the final accumulator to {@code R}.
     *
     * <p>Starts with {@code identity}. For each element {@code x}:
     * {@code acc = aggregator.apply(acc, x)}. After enumeration, returns
     * {@code resultSelector.apply(acc)}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * String joined = words.aggregate(
     *     new StringBuilder(),
     *     (sb, w) -> sb.append(w).append(","),
     *     StringBuilder::toString
     * );
     * }</pre>
     *
     * @param identity the initial accumulator value
     * @param aggregator combines the current accumulator and an element into a new accumulator
     * @param resultSelector maps the final accumulator to the result
     * @param <A> the accumulator type
     * @param <R> the result type
     * @return the aggregated result
     * @throws NullPointerException if {@code aggregator} or {@code resultSelector} is {@code null}
     * @throws RuntimeException if enumeration fails or any supplied function throws
     */
    default <A, R> R aggregate(
        A identity,
        BinFunction<? super A, ? super T, ? extends A> aggregator,
        Function<? super A, ? extends R> resultSelector
    ) {
        return Aggregate.aggregate(this, identity, aggregator, resultSelector);
    }

    /**
     * Aggregates the sequence starting from {@code seed}.
     *
     * <p>Starts with {@code seed}. For each element {@code x}:
     * {@code acc = aggregator.apply(acc, x)}. Returns the final accumulator.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * int sum = numbers.aggregate(0, (acc, x) -> acc + x);
     * }</pre>
     *
     * @param seed the initial accumulator value
     * @param aggregator combines the current accumulator and an element into a new accumulator
     * @param <R> the accumulator (and return) type
     * @return the final accumulator value
     * @throws NullPointerException if {@code aggregator} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code aggregator} throws
     */
    default <R> R aggregate(
        R seed,
        BinFunction<? super R, ? super T, ? extends R> aggregator
    ) {
        return Aggregate.aggregate(this, seed, aggregator);
    }

    /**
     * Aggregates the sequence without an explicit seed (typically uses the first element as the seed).
     *
     * <p>Typical semantics: uses the first element as the initial accumulator, then combines remaining elements
     * using {@code aggregator}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * int max = numbers.aggregate((a, b) -> Math.max(a, b));
     * }</pre>
     *
     * @param aggregator combines the current accumulator and an element into a new accumulator
     * @return the aggregation result
     * @throws NullPointerException if {@code aggregator} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or {@code aggregator} throws
     */
    default T aggregate(
        BinFunction<? super T, ? super T, ? extends T> aggregator
    ) {
        return Aggregate.aggregate(this, aggregator);
    }

    /**
     * Aggregates elements grouped by key, producing pairs of {@code (key, aggregatedValue)}.
     *
     * <p>For each element {@code x}, the key is {@code keyExtractor.apply(x)}. If the key is new, initializes
     * the per-key accumulator as {@code keyMapping.apply(key)}. Then updates it with
     * {@code resultMapping.apply(acc, x)}. After enumeration, returns one pair per distinct key.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * // Sum scores by userId
     * Enumerable<Pair<Integer, Integer>> sums = rows.aggregateBy(
     *     Row::userId,
     *     id -> 0,
     *     (acc, row) -> acc + row.score(),
     *     Comparator.naturalOrder()
     * );
     * }</pre>
     *
     * @param keyExtractor extracts the grouping key from each element
     * @param keyMapping maps a key to the initial accumulator value for that key
     * @param resultMapping updates the accumulator for a key given the current element
     * @param comparator comparator for keys (also determines map strategy / ordering depending on implementation)
     * @param <K> the key type
     * @param <R> the per-key accumulator / aggregated value type
     * @return a sequence of {@code (key, aggregatedValue)} pairs
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or any supplied function/comparator throws
     */
    default <K, R> Enumerable<Pair<K, R>> aggregateBy(
        Function<? super T, ? extends K> keyExtractor,
        Function<? super K, ? extends R> keyMapping,
        BinFunction<? super R, ? super T, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        return AggregateBy.aggregateBy(
            this, keyExtractor, keyMapping, resultMapping, comparator
        );
    }

    /**
     * Aggregates elements grouped by key using a constant seed for each key.
     *
     * <p>For each element {@code x}, the key is {@code keyExtractor.apply(x)}. If the key is new, initializes
     * the per-key accumulator to {@code seed} (sharing/copying semantics are implementation-defined).
     * Then updates it with {@code resultMapping.apply(acc, x)}. After enumeration, returns one pair per key.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Pair<String, StringBuilder>> builders = words.aggregateBy(
     *     String::substring, // example key extractor
     *     new StringBuilder(),
     *     (sb, w) -> sb.append(w).append(' '),
     *     Comparator.naturalOrder()
     * );
     * }</pre>
     *
     * @param seed the initial accumulator value used for each new key
     * @param keyExtractor extracts the grouping key from each element
     * @param resultMapping updates the accumulator for a key given the current element
     * @param comparator comparator for keys
     * @param <K> the key type
     * @param <R> the per-key accumulator / aggregated value type
     * @return a sequence of {@code (key, aggregatedValue)} pairs
     * @throws NullPointerException if {@code keyExtractor}, {@code resultMapping}, or {@code comparator} is {@code null}
     * @throws RuntimeException if enumeration fails or any supplied function/comparator throws
     */
    default <K, R> Enumerable<Pair<K, R>> aggregateBySeed(
        R seed,
        Function<? super T, ? extends K> keyExtractor,
        BinFunction<? super R, ? super T, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        return AggregateBy.aggregateBy(this, seed, keyExtractor, resultMapping, comparator);
    }

    /**
     * Returns {@code true} if all elements satisfy the predicate.
     *
     * <p>The predicate is evaluated until a non-matching element is found. If the sequence is empty,
     * this method returns {@code true}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * boolean allPositive = numbers.all(x -> x > 0);
     * }</pre>
     *
     * @param predicate the predicate to apply to each element
     * @return {@code true} if all elements satisfy the predicate; {@code true} for an empty sequence
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code predicate} throws
     */
    default boolean all(Predicate<? super T> predicate) {
        return AllAny.all(this, predicate);
    }

    /**
     * Returns {@code true} if any element satisfies the predicate.
     *
     * <p>The predicate is evaluated until a matching element is found. If the sequence is empty,
     * this method returns {@code false}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * boolean hasNull = seq.any(Objects::isNull);
     * }</pre>
     *
     * @param predicate the predicate to apply to elements
     * @return {@code true} if any element satisfies the predicate; {@code false} for an empty sequence
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code predicate} throws
     */
    default boolean any(Predicate<? super T> predicate) {
        return AllAny.any(this, predicate);
    }

    /**
     * Returns a sequence that yields all elements of this sequence followed by {@code element}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> withTail = nums.append(99);
     * }</pre>
     *
     * @param element the element to append
     * @return a new sequence ending with {@code element}
     * @throws RuntimeException if the new sequence cannot be created (implementation-defined)
     */
    default Enumerable<T> append(T element) {
        return AppendPrepend.append(this, element);
    }

    /**
     * Returns this instance as an {@link Enumerable}.
     *
     * <p>This is mostly useful for fluent APIs and adaptation points.</p>
     *
     * @return this sequence as an {@link Enumerable}
     * @throws RuntimeException if adaptation fails (implementation-defined)
     */
    default Enumerable<T> toEnumerable() {
        return this;
    }

    /**
     * Computes the arithmetic mean of {@link Integer} values in this sequence, ignoring {@code null} elements.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Integer>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> numbers = Enumerable.of(1, 2, null, 3);
     * double avg = numbers.intAverageNullable(); // 2.0
     * }</pre>
     *
     * @return the average of all non-null elements
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Integer>}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default double intAverageNullable() {
        return Average.intAverageNullable((Enumerable<Integer>) this);
    }

    /**
     * Computes the arithmetic mean of {@link Integer} values in this sequence, assuming no element is {@code null}.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Integer>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> numbers = Enumerable.of(1, 2, 3);
     * double avg = numbers.intAverageNonNull(); // 2.0
     * }</pre>
     *
     * @return the average of all elements
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Integer>}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default double intAverageNonNull() {
        return Average.intAverageNonNull((Enumerable<Integer>) this);
    }

    /**
     * Computes the arithmetic mean of {@link Long} values in this sequence, ignoring {@code null} elements.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Long>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * @return the average of all non-null elements
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Long>}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default double longAverageNullable() {
        return Average.longAverageNullable((Enumerable<Long>) this);
    }

    /**
     * Computes the arithmetic mean of {@link Long} values in this sequence, assuming no element is {@code null}.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Long>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * @return the average of all elements
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Long>}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default double longAverageNonNull() {
        return Average.longAverageNonNull((Enumerable<Long>) this);
    }

    /**
     * Computes the arithmetic mean of {@link Double} values in this sequence, ignoring {@code null} elements.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Double>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * @return the average of all non-null elements
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Double>}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default double doubleAverageNullable() {
        return Average.doubleAverageNullable((Enumerable<Double>) this);
    }

    /**
     * Computes the arithmetic mean of {@link Double} values in this sequence, assuming no element is {@code null}.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Double>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * @return the average of all elements
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Double>}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default double doubleAverageNonNull() {
        return Average.doubleAverageNonNull((Enumerable<Double>) this);
    }

    /**
     * Computes the arithmetic mean of {@link Float} values in this sequence, ignoring {@code null} elements.
     *
     * <p>The result is returned as {@code double}.</p>
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Float>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * @return the average of all non-null elements
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Float>}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default double floatAverageNullable() {
        return Average.floatAverageNullable((Enumerable<Float>) this);
    }

    /**
     * Computes the arithmetic mean of {@link Float} values in this sequence, assuming no element is {@code null}.
     *
     * <p>The result is returned as {@code double}.</p>
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<Float>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * @return the average of all elements
     * @throws NullPointerException if any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<Float>}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default double floatAverageNonNull() {
        return Average.floatAverageNonNull((Enumerable<Float>) this);
    }

    /**
     * Computes the arithmetic mean of {@link BigDecimal} values in this sequence, ignoring {@code null} elements,
     * using the provided {@link MathContext}.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<BigDecimal>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<BigDecimal> values = Enumerable.of(
     *     new BigDecimal("1.0"),
     *     null,
     *     new BigDecimal("2.0")
     * );
     * BigDecimal avg = values.decimalAverageNullable(MathContext.DECIMAL64); // 1.5
     * }</pre>
     *
     * @param context the math context used for division (precision/rounding)
     * @return the average of all non-null elements
     * @throws NullPointerException if {@code context} is {@code null}
     * @throws IllegalArgumentException if the sequence contains no non-null elements
     * @throws ClassCastException if this sequence is not an {@code Enumerable<BigDecimal>}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default BigDecimal decimalAverageNullable(MathContext context) {
        NullCheck.requireNonNull(context, "context");
        return Average.decimalAverageNullable((Enumerable<BigDecimal>) this, context);
    }

    /**
     * Computes the arithmetic mean of {@link BigDecimal} values in this sequence, assuming no element is {@code null},
     * using the provided {@link MathContext}.
     *
     * <p><b>Type requirement:</b> this method is only valid when this sequence is actually an
     * {@code Enumerable<BigDecimal>}. Otherwise, a {@link ClassCastException} may occur.</p>
     *
     * @param context the math context used for division (precision/rounding)
     * @return the average of all elements
     * @throws NullPointerException if {@code context} is {@code null} or any element is {@code null}
     * @throws IllegalArgumentException if the sequence is empty
     * @throws ClassCastException if this sequence is not an {@code Enumerable<BigDecimal>}
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    default BigDecimal decimalAverageNonNull(MathContext context) {
        NullCheck.requireNonNull(context, "context");
        return Average.decimalAverageNonNull((Enumerable<BigDecimal>) this, context);
    }

    /**
     * Computes the average by projecting each element to a {@code double}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * double avgAge = people.average(Person::age);
     * }</pre>
     *
     * @param doubleMapping mapping from elements to {@code double} values
     * @return the average of projected values
     * @throws NullPointerException if {@code doubleMapping} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or {@code doubleMapping} throws
     */
    default double average(ToDoubleFunction<? super T> doubleMapping) {
        return Average.average(this, doubleMapping);
    }

    /**
     * Computes the average by projecting each element to an {@code int} (returned as {@code double}).
     *
     * @param intMapping mapping from elements to {@code int} values
     * @return the average of projected values
     * @throws NullPointerException if {@code intMapping} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or {@code intMapping} throws
     */
    default double average(ToIntFunction<? super T> intMapping) {
        return Average.average(this, intMapping);
    }

    /**
     * Computes the average by projecting each element to a {@code long} (returned as {@code double}).
     *
     * @param longMapping mapping from elements to {@code long} values
     * @return the average of projected values
     * @throws NullPointerException if {@code longMapping} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or {@code longMapping} throws
     */
    default double average(ToLongFunction<? super T> longMapping) {
        return Average.average(this, longMapping);
    }

    /**
     * Splits the sequence into arrays of size {@code size}. The last chunk may be smaller.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer[]> chunks = nums.chunk(3);
     * }</pre>
     *
     * @param size the chunk size (must be positive)
     * @return a sequence of chunks
     * @throws IllegalArgumentException if {@code size <= 0}
     * @throws RuntimeException if chunking cannot be performed (implementation-defined)
     */
    default Enumerable<T[]> chunk(int size) {
        return Chunk.chunk(this, size);
    }

    /**
     * Splits the sequence into chunks (lists) of size {@code size}. The last chunk may be smaller.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<List<Integer>> chunks = nums.chunkAsList(2);
     * }</pre>
     *
     * @param size the chunk size (must be positive)
     * @return a sequence of lists, each representing a chunk
     * @throws NullPointerException if this sequence is {@code null} (not typical in instance context)
     * @throws IllegalArgumentException if {@code size <= 0}
     * @throws RuntimeException if chunking cannot be performed (implementation-defined)
     */
    default Enumerable<List<T>> chunkAsList(int size) {
        return Chunk.chunkAsList(this, size);
    }

    /**
     * Concatenates this sequence with {@code other}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> all = a.concat(b);
     * }</pre>
     *
     * @param other the sequence to append
     * @return a sequence that yields elements from this sequence, then {@code other}
     * @throws NullPointerException if {@code other} is {@code null}
     * @throws RuntimeException if enumeration fails in either sequence
     */
    default Enumerable<T> concat(Enumerable<? extends T> other) {
        return Concat.concat(this, other);
    }

    /**
     * Returns whether the sequence contains an element equal to {@code element}.
     *
     * @param element the element to search for (may be {@code null} depending on sequence semantics)
     * @return {@code true} if the sequence contains the element
     * @throws RuntimeException if enumeration fails
     */
    default boolean contains(T element) {
        return Contains.contains(this, element);
    }

    /**
     * Returns whether the sequence contains an element equal to {@code element}, using comparator-defined equality.
     *
     * <p>Equality is defined as {@code comparator.compare(a, element) == 0}.</p>
     *
     * @param element the element to search for
     * @param comparator comparator defining equality
     * @return {@code true} if any element is considered equal to {@code element} by the comparator
     * @throws NullPointerException if {@code comparator} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code comparator} throws
     */
    default boolean contains(T element, Comparator<? super T> comparator) {
        return Contains.contains(this, element, comparator);
    }

    /**
     * Counts all elements in the sequence.
     *
     * @return number of elements
     * @throws RuntimeException if enumeration fails
     */
    default long count() {
        return Count.count(this);
    }

    /**
     * Counts elements matching a predicate.
     *
     * @param predicate predicate to test elements
     * @return number of matching elements
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code predicate} throws
     */
    default long count(Predicate<? super T> predicate) {
        return Count.count(this, predicate);
    }

    /**
     * Counts elements by key, producing pairs {@code (key, count)}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * List<Pair<Character, Long>> freq = names
     *     .selectMany(s -> Enumerable.of(s.chars().mapToObj(c -> (char)c).toArray(Character[]::new)))
     *     .countBy(Function.identity(), Comparator.naturalOrder())
     *     .toList();
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param comparator comparator for keys (may influence ordering / strategy)
     * @param <K> the key type
     * @return a sequence of {@code (key, count)} pairs
     * @throws NullPointerException if {@code keyExtractor} or {@code comparator} is {@code null}
     * @throws RuntimeException if enumeration fails or any supplied function/comparator throws
     */
    default <K> Enumerable<Pair<K, Long>> countBy(
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        return CountBy.countBy(this, keyExtractor, comparator);
    }

    /**
     * Returns a sequence that yields {@code defaultElement} if this sequence is empty; otherwise yields this sequence.
     *
     * <p>This overload uses {@code null} as the default element.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> safe = seq.defaultIfEmpty(); // yields null if empty
     * }</pre>
     *
     * @return a sequence that yields {@code null} if empty, otherwise yields original elements
     * @throws RuntimeException if enumeration fails
     */
    @Nullable
    default Enumerable<T> defaultIfEmpty() {
        return defaultIfEmpty(null);
    }

    /**
     * Returns a sequence that yields {@code defaultElement} if this sequence is empty; otherwise yields this sequence.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> safe = seq.defaultIfEmpty("(none)");
     * }</pre>
     *
     * @param defaultElement the element to yield if the sequence is empty
     * @return a sequence that yields {@code defaultElement} if empty, otherwise yields original elements
     * @throws RuntimeException if enumeration fails
     */
    default Enumerable<T> defaultIfEmpty(T defaultElement) {
        return DefaultIfEmpty.defaultIfEmpty(this, defaultElement);
    }

    /**
     * Returns a sequence with duplicate elements removed using {@link Object#equals(Object)} / {@link Object#hashCode()}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> distinct = numbers.distinct();
     * }</pre>
     *
     * @return a distinct sequence
     * @throws RuntimeException if enumeration fails
     */
    default Enumerable<T> distinct() {
        return Distinct.distinct(this);
    }

    /**
     * Returns a sequence with duplicate elements removed using comparator-defined equality.
     *
     * <p>Equality is defined as {@code comparator.compare(a, b) == 0}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> distinct = words.distinct(String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param comparator comparator defining equality
     * @return a distinct sequence
     * @throws NullPointerException if {@code comparator} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code comparator} throws
     */
    default Enumerable<T> distinct(Comparator<? super T> comparator) {
        return Distinct.distinct(this, comparator);
    }

    /**
     * Returns a sequence with duplicate elements removed by extracted key using key equality
     * ({@link Object#equals(Object)} / {@link Object#hashCode()}).
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Person> distinctByAge = people.distinctBy(Person::getAge);
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param <K> the key type
     * @return a distinct-by-key sequence
     * @throws NullPointerException if {@code keyExtractor} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code keyExtractor} throws
     */
    default <K> Enumerable<T> distinctBy(Function<? super T, ? extends K> keyExtractor) {
        return Distinct.distinctBy(this, keyExtractor);
    }

    /**
     * Returns a sequence with duplicate elements removed by extracted key using comparator-defined key equality.
     *
     * <p>Key equality is defined as {@code comparator.compare(k1, k2) == 0}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Person> distinctByName = people.distinctBy(Person::getName, String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param comparator comparator defining key equality
     * @param <K> the key type
     * @return a distinct-by-key sequence
     * @throws NullPointerException if {@code keyExtractor} or {@code comparator} is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <K> Enumerable<T> distinctBy(
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        return Distinct.distinctBy(this, keyExtractor, comparator);
    }

    /**
     * Returns the element at the specified 0-based index.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * String element = seq.elementAt(2); // third element
     * }</pre>
     *
     * @param index 0-based index
     * @return the element at {@code index}
     * @throws IndexOutOfBoundsException if {@code index < 0} or the sequence has fewer than {@code index + 1} elements
     * @throws RuntimeException if enumeration fails
     */
    default T elementAt(int index) {
        return ElementAt.elementAt(this, index);
    }

    /**
     * Returns the element at the specified 0-based index, or {@code defaultElement} if out of range.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * String element = seq.elementAtDefault(10, "default");
     * }</pre>
     *
     * @param index 0-based index
     * @param defaultElement value returned when the index is out of range
     * @return element at {@code index}, or {@code defaultElement} if out of range
     * @throws RuntimeException if enumeration fails
     */
    default T elementAtDefault(int index, T defaultElement) {
        return ElementAt.elementAtOrDefault(this, index, defaultElement);
    }

    /**
     * Produces elements of this sequence that are not present in {@code other}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> result = numbers1.except(numbers2);
     * }</pre>
     *
     * @param other sequence to exclude
     * @return elements not present in {@code other}
     * @throws NullPointerException if {@code other} is {@code null}
     * @throws RuntimeException if enumeration fails
     */
    default Enumerable<T> except(Enumerable<? extends T> other) {
        return Except.except(this, other);
    }

    /**
     * Produces elements of this sequence that are not present in {@code other}, using comparator-defined equality.
     *
     * <p>Equality is defined as {@code comparator.compare(a, b) == 0}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> result = words1.except(words2, String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param other sequence to exclude
     * @param comparator comparator defining equality
     * @return elements not present in {@code other}
     * @throws NullPointerException if {@code other} or {@code comparator} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code comparator} throws
     */
    default Enumerable<T> except(Enumerable<? extends T> other, Comparator<? super T> comparator) {
        return Except.except(this, other, comparator);
    }

    /**
     * Produces elements whose extracted keys do not appear in {@code other}'s extracted keys.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Person> result = people.exceptBy(others, Person::getId);
     * }</pre>
     *
     * @param other other sequence
     * @param keyExtractor extracts key from elements of this sequence
     * @param <K> key type
     * @return elements whose key does not appear in {@code other}'s keys
     * @throws NullPointerException if {@code other} or {@code keyExtractor} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code keyExtractor} throws
     */
    default <K> Enumerable<T> exceptBy(
        Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor
    ) {
        return Except.exceptBy(this, other, keyExtractor);
    }

    /**
     * Produces elements whose extracted keys do not appear in {@code other}'s extracted keys,
     * using comparator-defined key equality.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Person> result = people.exceptBy(others, Person::getName, String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param other other sequence
     * @param keyExtractor extracts key from elements of this sequence
     * @param comparator key comparator defining equality
     * @param <K> key type
     * @return elements whose key does not appear in {@code other}'s keys
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <K> Enumerable<T> exceptBy(
        Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        return Except.exceptBy(this, other, keyExtractor, comparator);
    }

    /**
     * Returns the first element of the sequence.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * String first = seq.first();
     * }</pre>
     *
     * @return the first element
     * @throws NoSuchElementException if the sequence is empty
     * @throws RuntimeException if enumeration fails
     */
    default T first() {
        return First.first(this);
    }

    /**
     * Returns the first element that matches {@code predicate}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * int firstEven = numbers.first(n -> n % 2 == 0);
     * }</pre>
     *
     * @param predicate predicate to match
     * @return the first matching element
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws NoSuchElementException if no element matches the predicate
     * @throws RuntimeException if enumeration fails or {@code predicate} throws
     */
    default T first(Predicate<? super T> predicate) {
        return First.first(this, predicate);
    }

    /**
     * Returns the first element, or {@code defaultElement} if the sequence is empty.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * String first = seq.firstOrDefault("none");
     * }</pre>
     *
     * @param defaultElement value returned when empty
     * @return the first element, or {@code defaultElement} if empty
     * @throws RuntimeException if enumeration fails
     */
    default T firstOrDefault(T defaultElement) {
        return First.firstOrDefault(this, defaultElement);
    }

    /**
     * Returns the first element that matches {@code predicate}, or {@code defaultElement} if none matches.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * int firstEven = numbers.firstOrDefault(-1, n -> n % 2 == 0);
     * }</pre>
     *
     * @param defaultElement value returned when no match exists
     * @param predicate predicate to match
     * @return the first matching element, or {@code defaultElement} if none matches
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code predicate} throws
     */
    default T firstOrDefault(T defaultElement, Predicate<? super T> predicate) {
        return First.firstOrDefault(this, predicate, defaultElement);
    }

    /**
     * Groups elements by key, maps each element to {@code E}, then maps each group to {@code R}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> lines = ...;
     * Enumerable<Integer> sizes = lines.groupBy(
     *     s -> s.length(),
     *     Function.identity(),
     *     (len, group) -> group.count()
     * );
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param elementSelector maps each element to group element type {@code E}
     * @param resultMapping maps {@code (key, groupEnumerable)} to the result element
     * @param <K> key type
     * @param <E> group element type
     * @param <R> result element type
     * @return grouped results
     * @throws NullPointerException if {@code keyExtractor}, {@code elementSelector}, or {@code resultMapping} is {@code null}
     * @throws RuntimeException if enumeration fails or any supplied function throws
     */
    default <K, E, R> Enumerable<R> groupResultBy(
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        BinFunction<? super K, ? super Enumerable<E>, ? extends R> resultMapping
    ) {
        return Group.groupBy(this, keyExtractor, elementSelector, resultMapping);
    }

    /**
     * Groups elements by key using an explicit comparator, maps each element to {@code E}, then maps each group to {@code R}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> results = people.groupBy(
     *     Person::getLastName,
     *     Person::getFirstName,
     *     (last, firstNames) -> last + ": " + firstNames.toList(),
     *     String.CASE_INSENSITIVE_ORDER
     * );
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param elementSelector maps each element to group element type {@code E}
     * @param resultMapping maps {@code (key, groupEnumerable)} to the result element
     * @param comparator comparator defining key equality/ordering (implementation-defined grouping strategy)
     * @param <K> key type
     * @param <E> group element type
     * @param <R> result element type
     * @return grouped results
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <K, E, R> Enumerable<R> groupResultBy(
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        BinFunction<? super K, ? super Enumerable<E>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        return Group.groupBy(this, keyExtractor, elementSelector, resultMapping, comparator);
    }

    /**
     * Groups elements into {@link Groupable} objects (key + group sequence), mapping each element to {@code E}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Groupable<Integer, String>> groups = words.groupBy(String::length, String::toLowerCase);
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param elementSelector maps each element to group element type {@code E}
     * @param <K> key type
     * @param <E> group element type
     * @return groups as {@link Groupable} objects
     * @throws NullPointerException if {@code keyExtractor} or {@code elementSelector} is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function throws
     */
    default <K, E> Enumerable<Groupable<K, E>> groupBy(
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector
    ) {
        return Group.groupBy(this, keyExtractor, elementSelector);
    }

    /**
     * Groups elements into {@link Groupable} objects (key + group sequence) using an explicit key comparator,
     * mapping each element to {@code E}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Groupable<String, String>> groups = people.groupBy(
     *     Person::getLastName,
     *     Person::getFirstName,
     *     String.CASE_INSENSITIVE_ORDER
     * );
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param elementSelector maps each element to group element type {@code E}
     * @param comparator comparator defining key equality/ordering
     * @param <K> key type
     * @param <E> group element type
     * @return groups as {@link Groupable} objects
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <K, E> Enumerable<Groupable<K, E>> groupBy(
        Function<? super T, ? extends K> keyExtractor,
        Function<? super T, ? extends E> elementSelector,
        Comparator<? super K> comparator
    ) {
        return Group.groupBy(this, keyExtractor, elementSelector, comparator);
    }

    /**
     * Groups elements by key and maps each group to {@code R} (group elements remain {@code T}).
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> countsByLength = words.groupBy(String::length, (len, group) -> group.count());
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param resultMapping maps {@code (key, groupEnumerable)} to the result element
     * @param <K> key type
     * @param <R> result element type
     * @return grouped results
     * @throws NullPointerException if {@code keyExtractor} or {@code resultMapping} is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function throws
     */
    default <K, R> Enumerable<R> groupResultBy(
        Function<? super T, ? extends K> keyExtractor,
        BinFunction<? super K, ? super Enumerable<T>, ? extends R> resultMapping
    ) {
        return Group.groupBy(this, keyExtractor, resultMapping);
    }

    /**
     * Groups elements by key using a comparator and maps each group to {@code R} (group elements remain {@code T}).
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> countsByLength = words.groupBy(String::length, (len, group) -> group.count(), Comparator.naturalOrder());
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param resultMapping maps {@code (key, groupEnumerable)} to the result element
     * @param comparator comparator defining key equality/ordering
     * @param <K> key type
     * @param <R> result element type
     * @return grouped results
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <K, R> Enumerable<R> groupResultBy(
        Function<? super T, ? extends K> keyExtractor,
        BinFunction<? super K, ? super Enumerable<T>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        return Group.groupBy(this, keyExtractor, resultMapping, comparator);
    }

    /**
     * Groups elements into {@link Groupable} objects (key + group), using original element type {@code T}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Groupable<Integer, String>> groups = words.groupBy(String::length);
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param <K> key type
     * @return groups as {@link Groupable} objects
     * @throws NullPointerException if {@code keyExtractor} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code keyExtractor} throws
     */
    default <K> Enumerable<Groupable<K, T>> groupBy(Function<? super T, ? extends K> keyExtractor) {
        return Group.groupBy(this, keyExtractor);
    }

    /**
     * Groups elements into {@link Groupable} objects (key + group) using an explicit key comparator,
     * using original element type {@code T}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Groupable<String, Person>> groups = people.groupBy(Person::getLastName, String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param keyExtractor extracts the key for each element
     * @param comparator comparator defining key equality/ordering
     * @param <K> key type
     * @return groups as {@link Groupable} objects
     * @throws NullPointerException if {@code keyExtractor} or {@code comparator} is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <K> Enumerable<Groupable<K, T>> groupBy(
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        return Group.groupBy(this, keyExtractor, comparator);
    }

    /**
     * Group-joins this sequence with {@code other} (LINQ {@code GroupJoin} semantics).
     *
     * <p>For each element {@code t} in this sequence, finds all elements {@code o} in {@code other}
     * whose keys match, and produces a result {@code R} via {@code resultMapping}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> results = customers.groupJoin(orders,
     *     Customer::getId,
     *     Order::getCustomerId,
     *     (c, os) -> c.getName() + " has " + os.count() + " orders"
     * );
     * }</pre>
     *
     * @param other the other sequence
     * @param selfKeyExtractor extracts the key from elements of this sequence
     * @param otherKeyExtractor extracts the key from elements of {@code other}
     * @param resultMapping maps {@code (t, matchingGroup)} to the output element
     * @param <O> the element type of {@code other}
     * @param <K> the key type used for matching
     * @param <R> the result element type
     * @return a sequence of results produced per element of this sequence
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function throws
     */
    default <O, K, R> Enumerable<R> groupJoin(
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super Enumerable<O>, ? extends R> resultMapping
    ) {
        return GroupJoin.groupJoin(this, other, selfKeyExtractor, otherKeyExtractor, resultMapping);
    }

    /**
     * Group-joins this sequence with {@code other} using an explicit key comparator.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> results = customers.groupJoin(orders,
     *     Customer::getId,
     *     Order::getCustomerId,
     *     (c, os) -> c.getName() + " has " + os.count() + " orders",
     *     Comparator.naturalOrder()
     * );
     * }</pre>
     *
     * @param other the other sequence
     * @param selfKeyExtractor extracts the key from elements of this sequence
     * @param otherKeyExtractor extracts the key from elements of {@code other}
     * @param resultMapping maps {@code (t, matchingGroup)} to the output element
     * @param comparator comparator defining key equality
     * @param <O> the element type of {@code other}
     * @param <K> the key type used for matching
     * @param <R> the result element type
     * @return a sequence of results produced per element of this sequence
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <O, K, R> Enumerable<R> groupJoin(
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super Enumerable<O>, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        return GroupJoin.groupJoin(this, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator);
    }

    /**
     * Returns the intersection of this sequence and {@code other}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> result = numbers1.intersect(numbers2);
     * }</pre>
     *
     * @param other the other sequence
     * @return elements that appear in both sequences (semantics are implementation-defined)
     * @throws NullPointerException if {@code other} is {@code null}
     * @throws RuntimeException if enumeration fails
     */
    default Enumerable<T> intersect(Enumerable<? extends T> other) {
        return Intersect.intersect(this, other);
    }

    /**
     * Returns the intersection of this sequence and {@code other} using comparator-defined equality.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> result = words1.intersect(words2, String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param other the other sequence
     * @param comparator comparator defining equality
     * @return intersection result
     * @throws NullPointerException if {@code other} or {@code comparator} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code comparator} throws
     */
    default Enumerable<T> intersect(Enumerable<? extends T> other, Comparator<? super T> comparator) {
        return Intersect.intersect(this, other, comparator);
    }

    /**
     * Returns elements whose extracted key appears in {@code other}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Person> result = people.intersectBy(ids, Person::getId);
     * }</pre>
     *
     * @param other keys to keep
     * @param keyExtractor extracts key from this sequence elements
     * @param <K> key type
     * @return intersection-by-key result
     * @throws NullPointerException if {@code other} or {@code keyExtractor} is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function throws
     */
    default <K> Enumerable<T> intersectBy(
        Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor
    ) {
        return Intersect.intersectBy(this, other, keyExtractor);
    }

    /**
     * Returns elements whose extracted key appears in {@code other}, using comparator-defined key equality.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Person> result = people.intersectBy(names, Person::getName, String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param other keys to keep
     * @param keyExtractor extracts key from this sequence elements
     * @param comparator comparator defining key equality
     * @param <K> key type
     * @return intersection-by-key result
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <K> Enumerable<T> intersectBy(
        Enumerable<? extends K> other,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        return Intersect.intersectBy(this, other, keyExtractor, comparator);
    }

    /**
     * Joins this sequence with {@code other} (LINQ {@code Join} semantics).
     *
     * <p>For each matching pair {@code (t, o)} whose keys match, yields {@code resultMapping.apply(t, o)}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> results = customers.join(orders,
     *     Customer::getId,
     *     Order::getCustomerId,
     *     (c, o) -> c.getName() + " bought " + o.getProduct()
     * );
     * }</pre>
     *
     * @param other the other sequence
     * @param selfKeyExtractor extracts key from elements of this sequence
     * @param otherKeyExtractor extracts key from elements of {@code other}
     * @param resultMapping maps a matching pair to a result
     * @param <O> the element type of {@code other}
     * @param <K> the key type used for matching
     * @param <R> the result element type
     * @return joined results
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function throws
     */
    default <O, K, R> Enumerable<R> join(
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super O, ? extends R> resultMapping
    ) {
        return Join.join(this, other, selfKeyExtractor, otherKeyExtractor, resultMapping);
    }

    /**
     * Joins this sequence with {@code other} using comparator-defined key equality.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> results = customers.join(orders,
     *     Customer::getId,
     *     Order::getCustomerId,
     *     (c, o) -> c.getName() + " bought " + o.getProduct(),
     *     Comparator.naturalOrder()
     * );
     * }</pre>
     *
     * @param other the other sequence
     * @param selfKeyExtractor extracts key from elements of this sequence
     * @param otherKeyExtractor extracts key from elements of {@code other}
     * @param resultMapping maps a matching pair to a result
     * @param comparator comparator defining key equality
     * @param <O> the element type of {@code other}
     * @param <K> the key type used for matching
     * @param <R> the result element type
     * @return joined results
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <O, K, R> Enumerable<R> join(
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super O, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        return Join.join(this, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator);
    }

    /**
     * Returns the last element of the sequence.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * String last = seq.last();
     * }</pre>
     *
     * @return the last element
     * @throws NoSuchElementException if the sequence is empty
     * @throws RuntimeException if enumeration fails
     */
    default T last() {
        return Last.last(this);
    }

    /**
     * Returns the last element that matches {@code predicate}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * int lastEven = numbers.last(n -> n % 2 == 0);
     * }</pre>
     *
     * @param predicate predicate to match
     * @return the last matching element
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws NoSuchElementException if no element matches the predicate
     * @throws RuntimeException if enumeration fails or {@code predicate} throws
     */
    default T last(Predicate<? super T> predicate) {
        return Last.last(this, predicate);
    }

    /**
     * Returns the last element, or {@code defaultElement} if the sequence is empty.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * String last = seq.lastOrDefault("none");
     * }</pre>
     *
     * @param defaultElement value returned when empty
     * @return the last element, or {@code defaultElement} if empty
     * @throws RuntimeException if enumeration fails
     */
    default T lastOrDefault(T defaultElement) {
        return Last.lastOrDefault(this, defaultElement);
    }

    /**
     * Returns the last element that matches {@code predicate}, or {@code defaultElement} if none matches.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * int lastEven = numbers.lastOrDefault(n -> n % 2 == 0, -1);
     * }</pre>
     *
     * @param predicate predicate to match
     * @param defaultElement value returned when no match exists
     * @return the last matching element, or {@code defaultElement} if none matches
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or {@code predicate} throws
     */
    default T lastOrDefault(Predicate<? super T> predicate, T defaultElement) {
        return Last.lastOrDefault(this, predicate, defaultElement);
    }

    /**
     * Left-joins this sequence with {@code other}.
     *
     * <p>For each element {@code t} in this sequence, yields one result per match in {@code other}.
     * If no match exists, yields exactly one result with {@code o = null}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> results = customers.leftJoin(orders,
     *     Customer::getId,
     *     Order::getCustomerId,
     *     (c, o) -> c.getName() + " bought " + (o == null ? "nothing" : o.getProduct())
     * );
     * }</pre>
     *
     * @param other the other sequence
     * @param selfKeyExtractor extracts key from elements of this sequence
     * @param otherKeyExtractor extracts key from elements of {@code other}
     * @param resultMapping maps {@code (t, oOrNull)} to the result element
     * @param <O> the element type of {@code other}
     * @param <K> the key type used for matching
     * @param <R> the result element type
     * @return left-joined results
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function throws
     */
    default <O, K, R> Enumerable<R> leftJoin(
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping
    ) {
        return Join.leftJoin(this, other, selfKeyExtractor, otherKeyExtractor, resultMapping);
    }

    /**
     * Left-joins this sequence with {@code other} using comparator-defined key equality.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> results = customers.leftJoin(orders,
     *     Customer::getId,
     *     Order::getCustomerId,
     *     (c, o) -> c.getName() + " bought " + (o == null ? "nothing" : o.getProduct()),
     *     Comparator.naturalOrder()
     * );
     * }</pre>
     *
     * @param other the other sequence
     * @param selfKeyExtractor extracts key from elements of this sequence
     * @param otherKeyExtractor extracts key from elements of {@code other}
     * @param resultMapping maps {@code (t, oOrNull)} to the result element
     * @param comparator comparator defining key equality
     * @param <O> the element type of {@code other}
     * @param <K> the key type used for matching
     * @param <R> the result element type
     * @return left-joined results
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <O, K, R> Enumerable<R> leftJoin(
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super @Nullable O, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        return Join.leftJoin(this, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator);
    }

    /**
     * Returns the maximum projected {@code int} value.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * int maxAge = people.maxByInt(Person::getAge);
     * }</pre>
     *
     * @param intMapping mapping from elements to {@code int}
     * @return the maximum projected value
     * @throws NullPointerException if {@code intMapping} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or mapper throws
     */
    default int maxByInt(ToIntFunction<? super T> intMapping) {
        return Max.max(this, intMapping);
    }

    /**
     * Returns the maximum projected {@code long} value.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * long maxId = people.maxByLong(Person::getId);
     * }</pre>
     *
     * @param longMapping mapping from elements to {@code long}
     * @return the maximum projected value
     * @throws NullPointerException if {@code longMapping} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or mapper throws
     */
    default long maxByLong(ToLongFunction<? super T> longMapping) {
        return Max.max(this, longMapping);
    }

    /**
     * Returns the maximum projected {@code double} value.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * double maxSalary = people.maxByDouble(Person::getSalary);
     * }</pre>
     *
     * @param doubleMapping mapping from elements to {@code double}
     * @return the maximum projected value
     * @throws NullPointerException if {@code doubleMapping} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or mapper throws
     */
    default double maxByDouble(ToDoubleFunction<? super T> doubleMapping) {
        return Max.max(this, doubleMapping);
    }

    /**
     * Returns the element whose extracted key is maximal, using the provided comparator.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Person oldest = people.maxBy(Person::getAge, Comparator.naturalOrder());
     * }</pre>
     *
     * @param keyExtractor extracts a key from each element
     * @param comparator comparator for keys
     * @param <K> key type
     * @return the element with the maximum key
     * @throws NullPointerException if {@code keyExtractor} or {@code comparator} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <K> T maxBy(Function<? super T, ? extends K> keyExtractor, Comparator<? super K> comparator) {
        return Max.maxBy(this, keyExtractor, comparator);
    }

    /**
     * Returns the element whose extracted key is maximal, using natural key ordering.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Person oldest = people.maxBy(Person::getAge);
     * }</pre>
     *
     * @param keyExtractor extracts a key from each element
     * @param <K> key type (must be {@link Comparable})
     * @return the element with the maximum key
     * @throws NullPointerException if {@code keyExtractor} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws ClassCastException if keys are not mutually comparable (implementation-defined)
     * @throws RuntimeException if enumeration fails or {@code keyExtractor} throws
     */
    default <K extends Comparable<? super K>> T maxBy(Function<? super T, ? extends K> keyExtractor) {
        return Max.maxBy(this, keyExtractor);
    }

    /**
     * Returns the minimum projected {@code int} value.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * int minAge = people.minByInt(Person::getAge);
     * }</pre>
     *
     * @param intMapping mapping from elements to {@code int}
     * @return the minimum projected value
     * @throws NullPointerException if {@code intMapping} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or mapper throws
     */
    default int minByInt(ToIntFunction<? super T> intMapping) {
        return Min.min(this, intMapping);
    }

    /**
     * Returns the minimum projected {@code long} value.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * long minId = people.minByLong(Person::getId);
     * }</pre>
     *
     * @param longMapping mapping from elements to {@code long}
     * @return the minimum projected value
     * @throws NullPointerException if {@code longMapping} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or mapper throws
     */
    default long minByLong(ToLongFunction<? super T> longMapping) {
        return Min.min(this, longMapping);
    }

    /**
     * Returns the minimum projected {@code double} value.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * double minSalary = people.minByDouble(Person::getSalary);
     * }</pre>
     *
     * @param doubleMapping mapping from elements to {@code double}
     * @return the minimum projected value
     * @throws NullPointerException if {@code doubleMapping} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or mapper throws
     */
    default double minByDouble(ToDoubleFunction<? super T> doubleMapping) {
        return Min.min(this, doubleMapping);
    }

    /**
     * Returns the element whose extracted key is minimal, using the provided comparator.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Person youngest = people.minBy(Person::getAge, Comparator.naturalOrder());
     * }</pre>
     *
     * @param keyExtractor extracts a key from each element
     * @param comparator comparator for keys
     * @param <K> key type
     * @return the element with the minimum key
     * @throws NullPointerException if {@code keyExtractor} or {@code comparator} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <K> T minBy(Function<? super T, ? extends K> keyExtractor, Comparator<? super K> comparator) {
        return Min.minBy(this, keyExtractor, comparator);
    }

    /**
     * Returns the element whose extracted key is minimal, using natural key ordering.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Person youngest = people.minBy(Person::getAge);
     * }</pre>
     *
     * @param keyExtractor extracts a key from each element
     * @param <K> key type (must be {@link Comparable})
     * @return the element with the minimum key
     * @throws NullPointerException if {@code keyExtractor} is {@code null}
     * @throws NoSuchElementException if the sequence is empty (typical; implementation-defined)
     * @throws ClassCastException if keys are not mutually comparable (implementation-defined)
     * @throws RuntimeException if enumeration fails or {@code keyExtractor} throws
     */
    default <K extends Comparable<? super K>> T minBy(Function<? super T, ? extends K> keyExtractor) {
        return Min.minBy(this, keyExtractor);
    }

    /**
     * Filters elements by runtime type and casts them to {@code C}.
     *
     * <p>Equivalent to: {@code where(clazz::isInstance).select(clazz::cast)}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> strings = mixed.extractTo(String.class);
     * }</pre>
     *
     * @param clazz the desired runtime type
     * @param <C> the desired element type
     * @return a sequence containing only elements that are instances of {@code clazz}
     * @throws NullPointerException if {@code clazz} is {@code null}
     * @throws RuntimeException if enumeration fails
     */
    default <C> Enumerable<C> extractTo(Class<C> clazz) {
        return ExtractTo.extractTo(this, clazz);
    }

    /**
     * Orders elements using natural ordering.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * OrderedEnumerable<Integer> sorted = numbers.order();
     * }</pre>
     *
     * @return an ordered sequence
     * @throws ClassCastException if elements are not mutually comparable (implementation-defined)
     * @throws RuntimeException if ordering cannot be performed (implementation-defined)
     */
    default OrderedEnumerable<T> order() {
        return Order.order(this);
    }

    /**
     * Orders elements using the provided comparator.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * OrderedEnumerable<String> sorted = words.order(String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param comparator comparator to use
     * @return an ordered sequence
     * @throws NullPointerException if {@code comparator} is {@code null}
     * @throws RuntimeException if ordering cannot be performed or {@code comparator} throws
     */
    default OrderedEnumerable<T> order(Comparator<? super T> comparator) {
        return Order.order(this, comparator);
    }

    /**
     * Orders elements by extracted key using natural key ordering.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * OrderedEnumerable<Person> sorted = people.orderBy(Person::getAge);
     * }</pre>
     *
     * @param keyExtractor extracts the key
     * @param <K> key type
     * @return an ordered sequence
     * @throws NullPointerException if {@code keyExtractor} is {@code null}
     * @throws ClassCastException if keys are not mutually comparable (implementation-defined)
     * @throws RuntimeException if ordering cannot be performed or {@code keyExtractor} throws
     */
    default <K> OrderedEnumerable<T> orderBy(Function<? super T, ? extends K> keyExtractor) {
        return Order.orderBy(this, keyExtractor);
    }

    /**
     * Orders elements by extracted key using the provided key comparator.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * OrderedEnumerable<Person> sorted = people.orderBy(Person::getName, String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param keyExtractor extracts the key
     * @param comparator comparator for keys
     * @param <K> key type
     * @return an ordered sequence
     * @throws NullPointerException if {@code keyExtractor} or {@code comparator} is {@code null}
     * @throws RuntimeException if ordering cannot be performed or supplied function/comparator throws
     */
    default <K> OrderedEnumerable<T> orderBy(Function<? super T, ? extends K> keyExtractor, Comparator<? super K> comparator) {
        return Order.orderBy(this, keyExtractor, comparator);
    }

    /**
     * Orders elements descending using natural ordering.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * OrderedEnumerable<Integer> sorted = numbers.orderDescending();
     * }</pre>
     *
     * @return a descending-ordered sequence
     * @throws ClassCastException if elements are not mutually comparable (implementation-defined)
     * @throws RuntimeException if ordering cannot be performed
     */
    default OrderedEnumerable<T> orderDescending() {
        return Order.orderDescending(this);
    }

    /**
     * Orders elements descending using the provided comparator.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * OrderedEnumerable<String> sorted = words.orderDescending(String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param comparator comparator to use
     * @return a descending-ordered sequence
     * @throws NullPointerException if {@code comparator} is {@code null}
     * @throws RuntimeException if ordering cannot be performed or comparator throws
     */
    default OrderedEnumerable<T> orderDescending(Comparator<? super T> comparator) {
        return Order.orderDescending(this, comparator);
    }

    /**
     * Orders elements descending by extracted key using natural key ordering.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * OrderedEnumerable<Person> sorted = people.orderDescendingBy(Person::getAge);
     * }</pre>
     *
     * @param keyExtractor extracts the key
     * @param <K> key type
     * @return a descending-ordered sequence
     * @throws NullPointerException if {@code keyExtractor} is {@code null}
     * @throws ClassCastException if keys are not mutually comparable (implementation-defined)
     * @throws RuntimeException if ordering cannot be performed or keyExtractor throws
     */
    default <K> OrderedEnumerable<T> orderDescendingBy(Function<? super T, ? extends K> keyExtractor) {
        return Order.orderDescendingBy(this, keyExtractor);
    }

    /**
     * Orders elements descending by extracted key using the provided key comparator.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * OrderedEnumerable<Person> sorted = people.orderDescendingBy(Person::getName, String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param keyExtractor extracts the key
     * @param comparator comparator for keys
     * @param <K> key type
     * @return a descending-ordered sequence
     * @throws NullPointerException if {@code keyExtractor} or {@code comparator} is {@code null}
     * @throws RuntimeException if ordering cannot be performed or supplied function/comparator throws
     */
    default <K> OrderedEnumerable<T> orderDescendingBy(
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        return Order.orderDescendingBy(this, keyExtractor, comparator);
    }

    /**
     * Returns a sequence that yields {@code element} followed by all elements of this sequence.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> withZero = numbers.prepend(0);
     * }</pre>
     *
     * @param element the element to prepend
     * @return a new sequence beginning with {@code element}
     * @throws RuntimeException if the new sequence cannot be created (implementation-defined)
     */
    default Enumerable<T> prepend(T element) {
        return AppendPrepend.prepend(this, element);
    }

    /**
     * Right-joins this sequence with {@code other} (conceptually {@code other LEFT JOIN this}).
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> results = customers.rightJoin(orders,
     *     Customer::getId,
     *     Order::getCustomerId,
     *     (c, o) -> o.getProduct() + " bought by " + (c == null ? "unknown" : c.getName())
     * );
     * }</pre>
     *
     * @param other the other sequence
     * @param selfKeyExtractor extracts key from elements of this sequence
     * @param otherKeyExtractor extracts key from elements of {@code other}
     * @param resultMapping maps a matching pair to a result
     * @param <O> the element type of {@code other}
     * @param <K> the key type used for matching
     * @param <R> the result element type
     * @return right-joined results
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function throws
     */
    default <O, K, R> Enumerable<R> rightJoin(
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super O, ? extends R> resultMapping
    ) {
        return Join.rightJoin(this, other, selfKeyExtractor, otherKeyExtractor, resultMapping);
    }

    /**
     * Right-joins this sequence with {@code other} using comparator-defined key equality.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> results = customers.rightJoin(orders,
     *     Customer::getId,
     *     Order::getCustomerId,
     *     (c, o) -> o.getProduct() + " bought by " + (c == null ? "unknown" : c.getName()),
     *     Comparator.naturalOrder()
     * );
     * }</pre>
     *
     * @param other the other sequence
     * @param selfKeyExtractor extracts key from elements of this sequence
     * @param otherKeyExtractor extracts key from elements of {@code other}
     * @param resultMapping maps a matching pair to a result
     * @param comparator comparator defining key equality
     * @param <O> the element type of {@code other}
     * @param <K> the key type used for matching
     * @param <R> the result element type
     * @return right-joined results
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <O, K, R> Enumerable<R> rightJoin(
        Enumerable<? extends O> other,
        Function<? super T, ? extends K> selfKeyExtractor,
        Function<? super O, ? extends K> otherKeyExtractor,
        BiFunction<? super T, ? super O, ? extends R> resultMapping,
        Comparator<? super K> comparator
    ) {
        return Join.rightJoin(this, other, selfKeyExtractor, otherKeyExtractor, resultMapping, comparator);
    }

    /**
     * Projects each element using {@code selector}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> lengths = words.select(String::length);
     * }</pre>
     *
     * @param selector projection function
     * @param <R> result element type
     * @return a projected sequence
     * @throws NullPointerException if {@code selector} is {@code null}
     * @throws RuntimeException if enumeration fails or selector throws
     */
    default <R> Enumerable<R> select(Function<? super T, ? extends R> selector) {
        return Select.select(this, selector);
    }

    /**
     * Projects each element into an inner sequence and flattens the result, then maps {@code (outer, inner)} to {@code R}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> words = sentences.selectMany(s -> s.split("\\s+"), (s, w) -> w);
     * }</pre>
     *
     * @param collectionSelector maps each outer element to an inner sequence
     * @param resultSelector maps {@code (outer, innerElement)} to result element
     * @param <C> inner element type
     * @param <R> result element type
     * @return a flattened mapped sequence
     * @throws NullPointerException if {@code collectionSelector} or {@code resultSelector} is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function throws
     */
    default <C, R> Enumerable<R> selectMany(
        Function<? super T, ? extends Enumerable<? extends C>> collectionSelector,
        BiFunction<? super T, ? super C, ? extends R> resultSelector
    ) {
        return Select.selectMany(this, collectionSelector, resultSelector);
    }

    /**
     * Projects each element into an inner sequence and flattens the result (identity mapping).
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> words = sentences.selectMany(s -> s.split("\\s+"));
     * }</pre>
     *
     * @param selector maps each element to an inner sequence
     * @param <C> inner element type
     * @return a flattened sequence
     * @throws NullPointerException if {@code selector} is {@code null}
     * @throws RuntimeException if enumeration fails or selector throws
     */
    default <C> Enumerable<C> selectMany(
        Function<? super T, ? extends Enumerable<? extends C>> selector
    ) {
        return selectMany(selector, (t, c) -> c);
    }

    /**
     * Returns a shuffled sequence; {@code predicate} controls which elements participate.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> shuffled = numbers.shuffle(n -> n > 0);
     * }</pre>
     *
     * @param predicate predicate controlling which elements are shuffled
     * @return a shuffled sequence
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or predicate throws
     */
    default Enumerable<T> shuffle(Predicate<? super T> predicate) {
        return Shuffle.shuffle(this, predicate);
    }

    /**
     * Returns the only element of the sequence.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * String only = seq.single();
     * }</pre>
     *
     * @return the single element
     * @throws NoSuchElementException if the sequence is empty
     * @throws IllegalStateException if the sequence contains more than one element
     * @throws RuntimeException if enumeration fails
     */
    default T single() {
        return Single.single(this);
    }

    /**
     * Returns the only element that matches {@code predicate}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * int onlyEven = numbers.single(n -> n % 2 == 0);
     * }</pre>
     *
     * @param predicate predicate to match
     * @return the single matching element
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws NoSuchElementException if no element matches
     * @throws IllegalStateException if more than one element matches
     * @throws RuntimeException if enumeration fails or predicate throws
     */
    default T single(Predicate<? super T> predicate) {
        return Single.single(this, predicate);
    }

    /**
     * Returns the single element, or {@code defaultElement} if empty; throws if more than one element exists.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * String only = seq.singleOrDefault("default");
     * }</pre>
     *
     * @param defaultElement value returned when empty
     * @return the single element, or {@code defaultElement} if empty
     * @throws IllegalStateException if the sequence contains more than one element
     * @throws RuntimeException if enumeration fails
     */
    default T singleOrDefault(T defaultElement) {
        return Single.singleOrDefault(this, defaultElement);
    }

    /**
     * Returns the single element matching {@code predicate}, or {@code defaultElement} if none matches;
     * throws if more than one match exists.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * int onlyEven = numbers.singleOrDefault(n -> n % 2 == 0, -1);
     * }</pre>
     *
     * @param predicate predicate to match
     * @param defaultElement value returned when no match exists
     * @return the single matching element, or {@code defaultElement} if none matches
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws IllegalStateException if more than one element matches
     * @throws RuntimeException if enumeration fails or predicate throws
     */
    default T singleOrDefault(Predicate<? super T> predicate, T defaultElement) {
        return Single.singleOrDefault(this, predicate, defaultElement);
    }

    /**
     * Skips the first {@code count} elements.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> afterFirstTwo = numbers.skip(2);
     * }</pre>
     *
     * @param count number of elements to skip (must be non-negative)
     * @return a sequence skipping the first {@code count} elements
     * @throws IllegalArgumentException if {@code count < 0}
     * @throws RuntimeException if enumeration fails
     */
    default Enumerable<T> skip(int count) {
        return SkipTake.skip(this, count);
    }

    /**
     * Skips the last {@code count} elements.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> withoutLastTwo = numbers.skipLast(2);
     * }</pre>
     *
     * @param count number of elements to skip from the end (must be non-negative)
     * @return a sequence without the last {@code count} elements
     * @throws IllegalArgumentException if {@code count < 0}
     * @throws RuntimeException if enumeration fails
     */
    default Enumerable<T> skipLast(int count) {
        return SkipTake.skipLast(this, count);
    }

    /**
     * Skips elements while {@code predicate} is {@code true}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> afterNegatives = numbers.skipWhile(n -> n < 0);
     * }</pre>
     *
     * @param predicate predicate to test
     * @return the remaining sequence after skipping the prefix
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or predicate throws
     */
    default Enumerable<T> skipWhile(Predicate<? super T> predicate) {
        return SkipTake.skipWhile(this, predicate);
    }

    /**
     * Skips elements while {@code predicate(element, index)} is {@code true}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> afterFirstTwo = numbers.skipWhile((n, i) -> i < 2);
     * }</pre>
     *
     * @param predicate predicate receiving {@code (element, index)}
     * @return the remaining sequence after skipping the prefix
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or predicate throws
     */
    default Enumerable<T> skipWhile(BinPredicate<? super T, ? super Integer> predicate) {
        return SkipTake.skipWhile(this, predicate);
    }

    /**
     * Takes the first {@code count} elements.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> firstTwo = numbers.take(2);
     * }</pre>
     *
     * @param count number of elements to take (must be non-negative)
     * @return a sequence containing up to {@code count} elements
     * @throws IllegalArgumentException if {@code count < 0}
     * @throws RuntimeException if enumeration fails
     */
    default Enumerable<T> take(int count) {
        return SkipTake.take(this, count);
    }

    /**
     * Takes the last {@code count} elements.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> lastTwo = numbers.takeLast(2);
     * }</pre>
     *
     * @param count number of elements to take from the end (must be non-negative)
     * @return a sequence containing the last {@code count} elements (or fewer if shorter)
     * @throws IllegalArgumentException if {@code count < 0}
     * @throws RuntimeException if enumeration fails
     */
    default Enumerable<T> takeLast(int count) {
        return SkipTake.takeLast(this, count);
    }

    /**
     * Takes elements while {@code predicate} is {@code true}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> negatives = numbers.takeWhile(n -> n < 0);
     * }</pre>
     *
     * @param predicate predicate to test
     * @return a prefix sequence while predicate holds
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or predicate throws
     */
    default Enumerable<T> takeWhile(Predicate<? super T> predicate) {
        return SkipTake.takeWhile(this, predicate);
    }

    /**
     * Takes elements while {@code predicate(element, index)} is {@code true}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> firstTwo = numbers.takeWhile((n, i) -> i < 2);
     * }</pre>
     *
     * @param predicate predicate receiving {@code (element, index)}
     * @return a prefix sequence while predicate holds
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or predicate throws
     */
    default Enumerable<T> takeWhile(BinPredicate<? super T, ? super Integer> predicate) {
        return SkipTake.takeWhile(this, predicate);
    }

    /**
     * Sums elements projected to {@code double}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * double total = numbers.sum(n -> n * 1.5);
     * }</pre>
     *
     * @param doubleMapping mapping from elements to {@code double}
     * @return the sum of projected values
     * @throws NullPointerException if {@code doubleMapping} is {@code null}
     * @throws RuntimeException if enumeration fails or mapper throws
     */
    default double sumByDouble(ToDoubleFunction<? super T> doubleMapping) {
        return Sum.sum(this, doubleMapping);
    }

    /**
     * Sums elements projected to {@code int} (accumulated as {@code long}).
     *
     * <h3>Example</h3>
     * <pre>{@code
     * long total = numbers.sum(n -> n);
     * }</pre>
     *
     * @param intMapping mapping from elements to {@code int}
     * @return the sum of projected values
     * @throws NullPointerException if {@code intMapping} is {@code null}
     * @throws RuntimeException if enumeration fails or mapper throws
     */
    default long sumByInt(ToIntFunction<? super T> intMapping) {
        return Sum.sum(this, intMapping);
    }

    /**
     * Sums elements projected to {@code long}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * long total = numbers.sum(n -> (long)n);
     * }</pre>
     *
     * @param longMapping mapping from elements to {@code long}
     * @return the sum of projected values
     * @throws NullPointerException if {@code longMapping} is {@code null}
     * @throws RuntimeException if enumeration fails or mapper throws
     */
    default long sumByLong(ToLongFunction<? super T> longMapping) {
        return Sum.sum(this, longMapping);
    }

    /**
     * Materializes the sequence into an array.
     *
     * <p>This default implementation delegates to {@link #toList()} and then calls {@link List#toArray()}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * String[] array = seq.toArray();
     * }</pre>
     *
     * @return an array containing all elements
     * @throws RuntimeException if enumeration fails
     */
    @SuppressWarnings("unchecked")
    default T[] toArray() {
        return (T[]) this.toList().toArray();
    }

    /**
     * Materializes the sequence into a {@link Set}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Set<String> set = seq.toSet();
     * }</pre>
     *
     * @return a set containing all elements
     * @throws RuntimeException if enumeration fails
     */
    default Set<T> toSet() {
        return new HashSet<>(this.toList());
    }

    /**
     * Materializes the sequence into a {@link List}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * List<T> list = seq.toList();
     * }</pre>
     *
     * @return a list containing all elements in encounter order
     * @throws RuntimeException if enumeration fails
     */
    default List<T> toList() {
        List<T> list = new ArrayList<>();
        try (Enumerator<T> enumerator = enumerator()) {
            while (enumerator.moveNext()) {
                T current = enumerator.current();
                list.add(current);
            }
        }
        return list;
    }

    /**
     * Converts this sequence to a Java {@link Stream}.
     *
     * <p>The returned stream may be single-use depending on implementation details.
     * Closing the stream triggers {@link Enumerator#close()}.</p>
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Stream<String> stream = seq.toStream();
     * }</pre>
     *
     * @return a stream view of this sequence
     * @throws RuntimeException if the stream cannot be created (implementation-defined)
     */
    default Stream<T> toStream() {
        Enumerator<T> enumerator = this.enumerator();
        return StreamSupport.stream(this.spliterator(), false)
            .onClose(enumerator::close);
    }

    /**
     * Unions this sequence with {@code other}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> union = numbers1.union(numbers2);
     * }</pre>
     *
     * @param other the other sequence
     * @return union result (semantics are implementation-defined)
     * @throws NullPointerException if {@code other} is {@code null}
     * @throws RuntimeException if enumeration fails
     */
    default Enumerable<T> union(Enumerable<? extends T> other) {
        return Union.union(this, other);
    }

    /**
     * Unions this sequence with {@code other} using comparator-defined equality.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> union = words1.union(words2, String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param other the other sequence
     * @param comparator comparator defining equality
     * @return union result
     * @throws NullPointerException if {@code other} or {@code comparator} is {@code null}
     * @throws RuntimeException if enumeration fails or comparator throws
     */
    default Enumerable<T> union(Enumerable<? extends T> other, Comparator<? super T> comparator) {
        return Union.union(this, other, comparator);
    }

    /**
     * Unions by extracted key (key equality uses {@link Object#equals(Object)} / {@link Object#hashCode()}).
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Person> union = people1.unionBy(people2, Person::getId);
     * }</pre>
     *
     * @param other the other sequence
     * @param keyExtractor extracts key from elements
     * @param <K> key type
     * @return union-by-key result
     * @throws NullPointerException if {@code other} or {@code keyExtractor} is {@code null}
     * @throws RuntimeException if enumeration fails or keyExtractor throws
     */
    default <K> Enumerable<T> unionBy(
        Enumerable<? extends T> other, Function<? super T, ? extends K> keyExtractor
    ) {
        return Union.unionBy(this, other, keyExtractor);
    }

    /**
     * Unions by extracted key using comparator-defined key equality.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Person> union = people1.unionBy(people2, Person::getName, String.CASE_INSENSITIVE_ORDER);
     * }</pre>
     *
     * @param other the other sequence
     * @param keyExtractor extracts key from elements
     * @param comparator comparator defining key equality
     * @param <K> key type
     * @return union-by-key result
     * @throws NullPointerException if any parameter is {@code null}
     * @throws RuntimeException if enumeration fails or supplied function/comparator throws
     */
    default <K> Enumerable<T> unionBy(
        Enumerable<? extends T> other,
        Function<? super T, ? extends K> keyExtractor,
        Comparator<? super K> comparator
    ) {
        return Union.unionBy(this, other, keyExtractor, comparator);
    }

    /**
     * Filters elements by {@code predicate}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Integer> evens = numbers.where(n -> n % 2 == 0);
     * }</pre>
     *
     * @param predicate predicate to test elements
     * @return a filtered sequence
     * @throws NullPointerException if {@code predicate} is {@code null}
     * @throws RuntimeException if enumeration fails or predicate throws
     */
    default Enumerable<T> where(Predicate<? super T> predicate) {
        return Where.where(this, predicate);
    }

    /**
     * Zips this sequence with {@code other} into pairs {@code (thisElement, otherElement)}.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<Pair<Integer, String>> zipped = numbers.zip(words);
     * }</pre>
     *
     * @param other the other sequence
     * @param <R> the element type of {@code other}
     * @return a sequence of pairs
     * @throws NullPointerException if {@code other} is {@code null}
     * @throws RuntimeException if enumeration fails
     */
    default <R> Enumerable<Pair<T, R>> zip(Enumerable<? extends R> other) {
        return Zip.zip(this, other);
    }

    /**
     * Zips this sequence with {@code other} using a result mapping function.
     *
     * <h3>Example</h3>
     * <pre>{@code
     * Enumerable<String> zipped = numbers.zip(words, (n, s) -> n + ": " + s);
     * }</pre>
     *
     * @param other the other sequence
     * @param resultMapping maps paired elements to a result element
     * @param <O> the element type of {@code other}
     * @param <R> the result element type
     * @return a sequence of mapped results
     * @throws NullPointerException if {@code other} or {@code resultMapping} is {@code null}
     * @throws RuntimeException if enumeration fails or resultMapping throws
     */
    default <O, R> Enumerable<R> zip(
        Enumerable<? extends O> other,
        BinFunction<? super T, ? super O, ? extends R> resultMapping
    ) {
        return Zip.zip(this, other, resultMapping);
    }

    /**
     * Performs the given action for each element of the {@code Iterable}
     * until all elements have been processed or the action throws an
     * exception.  Actions are performed in the order of iteration, if that
     * order is specified.  Exceptions thrown by the action are relayed to the
     * caller.
     * <p>
     * The behavior of this method is unspecified if the action performs
     * side-effects that modify the underlying source of elements, unless an
     * overriding class has specified a concurrent modification policy.
     *
     * <p>
     *     Specially,
     * </p>
     *
     * @implSpec
     * <p>The default implementation behaves as if:
     * <pre>{@code
     *     for (T t : this)
     *         action.accept(t);
     * }</pre>
     *
     * @param action The action to be performed for each element
     * @throws NullPointerException if the specified action is null
     */
    @Override
    default void forEach(Consumer<? super T> action) {
        try (Enumerator<T> enumerator = enumerator()) {
            while (enumerator.moveNext()) {
                T current = enumerator.current();
                action.accept(current);
            }
        }
    }
}
