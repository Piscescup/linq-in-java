package io.github.piscescup.stream;

import io.github.piscescup.interfaces.Pair;
import io.github.piscescup.pair.ImmutablePair;
import io.github.piscescup.util.validation.NullCheck;
import io.github.piscescup.utils.Indexed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.*;

/**
 * Stream utility methods that provide lightweight, LINQ-like extensions for {@link Stream}.
 *
 * <p>This class focuses on filling gaps in the {@link Stream JDK Stream API} (e.g. {@code distinctBy}, {@code zip},
 * {@code first/single}, {@code index}, {@code pairwise}), while keeping the API static and minimal.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class StreamUtils {

    private StreamUtils() {
        throw new UnsupportedOperationException("No " + StreamUtils.class.getCanonicalName() + " instance for you!");
    }

    /* ------------------------------------------------------------ */
    /* LINQ-style aliases (optional)                                 */
    /* ------------------------------------------------------------ */

    /**
     * LINQ alias of {@link Stream#filter(Predicate)}.
     *
     * <pre>{@code
     * List<Integer> list = List.of(1, 2, 3, 4, 5);
     *
     * List<Integer> even =
     *     StreamUtils.where(list.stream(), i -> i % 2 == 0)
     *                .toList();
     *
     * // Result: [2, 4]
     * }</pre>
     *
     * @param stream the source stream
     * @param predicate filter predicate
     * @param <T> element type
     * @return filtered stream
     */
    public static <T> @NotNull Stream<T> where(
        final @NotNull Stream<T> stream,
        final @NotNull Predicate<? super T> predicate
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        NullCheck.requireNonNull(predicate, "predicate must not be null");
        return stream.filter(predicate);
    }

    /**
     * LINQ alias of {@link Stream#map(Function)}.
     *
     * <pre>{@code
     * List<String> words = List.of("java", "stream");
     *
     * List<Integer> lengths =
     *     StreamUtils.select(words.stream(), String::length)
     *                .toList();
     *
     * // Result: [4, 6]
     * }</pre>
     *
     * @param stream the source stream
     * @param mapper mapping function
     * @param <T> source element type
     * @param <R> mapped element type
     * @return mapped stream
     */
    public static <T, R> @NotNull Stream<R> select(
        final @NotNull Stream<T> stream,
        final @NotNull Function<? super T, ? extends R> mapper
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        NullCheck.requireNonNull(mapper, "mapper must not be null");
        return stream.map(mapper);
    }

    /**
     * LINQ alias of {@link Stream#flatMap(Function)}.
     *
     * <pre>{@code
     * List<List<Integer>> data =
     *     List.of(List.of(1, 2), List.of(3, 4));
     *
     * List<Integer> flat =
     *     StreamUtils.selectMany(data.stream(), List::stream)
     *                .toList();
     *
     * // Result: [1, 2, 3, 4]
     * }</pre>
     *
     * @param stream the source stream
     * @param mapper mapping function producing inner streams
     * @param <T> source element type
     * @param <R> flattened element type
     * @return flattened stream
     */
    public static <T, R> @NotNull Stream<R> selectMany(
        final @NotNull Stream<T> stream,
        final @NotNull Function<? super T, ? extends Stream<? extends R>> mapper
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        NullCheck.requireNonNull(mapper, "mapper must not be null");
        return stream.flatMap(t -> {
            final Stream<? extends R> inner = mapper.apply(t);
            if (inner == null) {
                return Stream.empty();
            }
            @SuppressWarnings("unchecked")
            final Stream<R> cast = (Stream<R>) inner;
            return cast;
        });
    }

    /* ------------------------------------------------------------ */
    /* Basic composition                                             */
    /* ------------------------------------------------------------ */

    /**
     * Returns a stream consisting of the elements of {@code stream}, followed by {@code value}.
     *
     * <pre>{@code
     * List<Integer> list =
     *     StreamUtils.append(Stream.of(1, 2, 3), 4)
     *                .toList();
     *
     * // Result: [1, 2, 3, 4]
     * }</pre>
     *
     * @param stream the source stream
     * @param value the element to append (may be null)
     * @param <T> element type
     * @return concatenated stream
     */
    public static <T> @NotNull Stream<T> append(final @NotNull Stream<? extends T> stream, final @Nullable T value) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        return Stream.concat(stream, Stream.of(value));
    }

    /**
     * Returns a stream consisting of {@code value}, followed by the elements of {@code stream}.
     *
     * <pre>{@code
     * List<String> list =
     *     StreamUtils.prepend(Stream.of("b", "c"), "a")
     *                .toList();
     *
     * // Result: ["a", "b", "c"]
     * }</pre>
     *
     * @param stream the source stream
     * @param value the element to prepend (may be null)
     * @param <T> element type
     * @return concatenated stream
     */
    public static <T> @NotNull Stream<T> prepend(final @NotNull Stream<? extends T> stream, final @Nullable T value) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        return Stream.concat(Stream.of(value), stream);
    }

    /* ------------------------------------------------------------ */
    /* Missing building blocks                                       */
    /* ------------------------------------------------------------ */

    /**
     * Returns a stream consisting of the distinct elements of the input stream,
     * where distinctness is determined by {@code keyExtractor}.
     *
     * <p>This implementation is safe for parallel streams.</p>
     *
     * <pre>{@code
     * record Person(String name, String email) {}
     *
     * List<Person> persons = List.of(
     *     new Person("Alice", "a@test.com"),
     *     new Person("Bob",   "a@test.com"),
     *     new Person("Tom",   "b@test.com")
     * );
     *
     * List<Person> result =
     *     StreamUtils.distinctBy(persons.stream(), Person::email)
     *                .toList();
     *
     * // Keeps first occurrence per key:
     * // [Alice, Tom]
     * }</pre>
     *
     * @param stream source stream
     * @param keyExtractor distinct key extractor
     * @param <T> element type
     * @param <K> key type
     * @return stream with distinct elements by key
     */
    public static <T, K> @NotNull Stream<T> distinctBy(
        final @NotNull Stream<T> stream,
        final @NotNull Function<? super T, ? extends K> keyExtractor
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        NullCheck.requireNonNull(keyExtractor, "keyExtractor must not be null");

        final Set<K> seen = ConcurrentHashMap.newKeySet();
        return stream.filter(t -> seen.add(keyExtractor.apply(t)));
    }

    /**
     * Collects the stream into a {@link Map} using key/value mappers.
     *
     * <p>If duplicate keys exist, this method throws an {@link IllegalStateException}.
     * Consider using {@link #toMap(Stream, Function, Function, BinaryOperator)}.</p>
     *
     * <pre>{@code
     * List<String> list = List.of("a", "bb", "ccc");
     *
     * Map<String, Integer> map =
     *     StreamUtils.toMap(
     *         list.stream(),
     *         s -> s,
     *         String::length
     *     );
     *
     * // Result: {a=1, bb=2, ccc=3}
     * }</pre>
     *
     * @throws IllegalStateException if duplicate keys exist
     */
    public static <T, K, V> @NotNull Map<K, V> toMap(
        final @NotNull Stream<T> stream,
        final @NotNull Function<? super T, ? extends K> keyMapper,
        final @NotNull Function<? super T, ? extends V> valueMapper
    ) {
        return toMap(stream, keyMapper, valueMapper, (a, b) -> {
            throw new IllegalStateException("Duplicate key detected. Provide a mergeFunction to resolve conflicts.");
        }, HashMap::new);
    }

    /**
     * Collects the stream into a {@link Map} using key/value mappers and a merge function.
     *
     * <pre>{@code
     * List<String> list = List.of("a", "bb", "c", "dd");
     *
     * Map<Integer, String> map =
     *     StreamUtils.toMap(
     *         list.stream(),
     *         String::length,
     *         s -> s,
     *         (a, b) -> a + "," + b
     *     );
     *
     * // Result: {1=a,c, 2=bb,dd}
     * }</pre>
     *
     * @param mergeFunction merge function used when duplicate keys exist
     */
    public static <T, K, V> @NotNull Map<K, V> toMap(
        final @NotNull Stream<T> stream,
        final @NotNull Function<? super T, ? extends K> keyMapper,
        final @NotNull Function<? super T, ? extends V> valueMapper,
        final @NotNull BinaryOperator<V> mergeFunction
    ) {
        return toMap(stream, keyMapper, valueMapper, mergeFunction, HashMap::new);
    }

    /**
     * Collects the stream into a {@link Map} using key/value mappers, a merge function and a map supplier.
     *
     * <pre>{@code
     * List<String> list = List.of("a", "bb", "c", "dd");
     *
     * // Use LinkedHashMap to preserve iteration order
     * Map<Integer, String> map =
     *     StreamUtils.toMap(
     *         list.stream(),
     *         String::length,
     *         s -> s,
     *         (a, b) -> a + "," + b,
     *         LinkedHashMap::new
     *     );
     *
     * // Result: {1=a,c, 2=bb,dd}
     * }</pre>
     */
    public static <T, K, V> @NotNull Map<K, V> toMap(
        final @NotNull Stream<T> stream,
        final @NotNull Function<? super T, ? extends K> keyMapper,
        final @NotNull Function<? super T, ? extends V> valueMapper,
        final @NotNull BinaryOperator<V> mergeFunction,
        final @NotNull Supplier<Map<K, V>> mapSupplier
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        NullCheck.requireNonNull(keyMapper, "keyMapper must not be null");
        NullCheck.requireNonNull(valueMapper, "valueMapper must not be null");
        NullCheck.requireNonNull(mergeFunction, "mergeFunction must not be null");
        NullCheck.requireNonNull(mapSupplier, "mapSupplier must not be null");

        return stream.collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction, mapSupplier));
    }

    /**
     * Groups the stream elements by key.
     *
     * <pre>{@code
     * List<String> words = List.of("one", "two", "three", "four");
     *
     * Map<Integer, List<String>> map =
     *     StreamUtils.groupBy(words.stream(), String::length);
     *
     * // Result:
     * // {
     * //   3=[one, two],
     * //   4=[four],
     * //   5=[three]
     * // }
     * }</pre>
     */
    public static <T, K> @NotNull Map<K, List<T>> groupBy(
        final @NotNull Stream<T> stream,
        final @NotNull Function<? super T, ? extends K> keyExtractor
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        NullCheck.requireNonNull(keyExtractor, "keyExtractor must not be null");
        return stream.collect(Collectors.groupingBy(keyExtractor));
    }

    /**
     * Groups the stream elements by key with a downstream collector.
     *
     * <pre>{@code
     * List<String> words = List.of("one", "two", "three", "four");
     *
     * Map<Integer, Long> counts =
     *     StreamUtils.groupBy(words.stream(), String::length, Collectors.counting());
     *
     * // Result: {3=2, 4=1, 5=1}
     * }</pre>
     */
    public static <T, K, A, D> @NotNull Map<K, D> groupBy(
        final @NotNull Stream<T> stream,
        final @NotNull Function<? super T, ? extends K> keyExtractor,
        final @NotNull Collector<? super T, A, D> downstream
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        NullCheck.requireNonNull(keyExtractor, "keyExtractor must not be null");
        NullCheck.requireNonNull(downstream, "downstream must not be null");
        return stream.collect(Collectors.groupingBy(keyExtractor, downstream));
    }

    /**
     * Zips two streams into one by applying {@code zipper} on pairs of elements.
     * The resulting stream ends when either input stream ends.
     *
     * <pre>{@code
     * Stream<Integer> a = Stream.of(1, 2, 3);
     * Stream<String>  b = Stream.of("A", "B");
     *
     * List<String> result =
     *     StreamUtils.zip(a, b, (x, y) -> x + y)
     *                .toList();
     *
     * // Result: ["1A", "2B"]
     * }</pre>
     *
     * <pre>{@code
     * // Zip into pairs
     * List<Map.Entry<Integer, String>> pairs =
     *     StreamUtils.zip(Stream.of(1, 2), Stream.of("x", "y"),
     *                     (i, s) -> new AbstractMap.SimpleImmutableEntry<>(i, s))
     *                .toList();
     * }</pre>
     *
     * <p>The returned stream closes both input streams when closed.</p>
     */
    public static <A, B, R> @NotNull Stream<R> zip(
        final @NotNull Stream<? extends A> a,
        final @NotNull Stream<? extends B> b,
        final @NotNull BiFunction<? super A, ? super B, ? extends R> zipper
    ) {
        NullCheck.requireNonNull(a, "a must not be null");
        NullCheck.requireNonNull(b, "b must not be null");
        NullCheck.requireNonNull(zipper, "zipper must not be null");

        final Spliterator<? extends A> spA = a.spliterator();
        final Spliterator<? extends B> spB = b.spliterator();

        final long est = Math.min(spA.estimateSize(), spB.estimateSize());
        final int characteristics = spA.characteristics() & spB.characteristics()
            & (Spliterator.ORDERED | Spliterator.SIZED | Spliterator.NONNULL);

        final Iterator<? extends A> itA = Spliterators.iterator(spA);
        final Iterator<? extends B> itB = Spliterators.iterator(spB);

        final Spliterator<R> sp = new Spliterators.AbstractSpliterator<>(est, characteristics) {
            @Override
            public boolean tryAdvance(final Consumer<? super R> action) {
                NullCheck.requireNonNull(action, "action must not be null");
                if (itA.hasNext() && itB.hasNext()) {
                    action.accept(zipper.apply(itA.next(), itB.next()));
                    return true;
                }
                return false;
            }
        };

        return StreamSupport.stream(sp, false)
            .onClose(a::close)
            .onClose(b::close);
    }

    /* ------------------------------------------------------------ */
    /* Terminal helpers                                              */
    /* ------------------------------------------------------------ */

    /**
     * Returns the first element of the stream, or {@code null} if the stream is empty.
     *
     * <p>This consumes the stream.</p>
     *
     * <pre>{@code
     * Integer v = StreamUtils.firstOrNull(Stream.<Integer>empty());
     * // Result: null
     * }</pre>
     *
     * <pre>{@code
     * Integer v = StreamUtils.firstOrNull(Stream.of(10, 20, 30));
     * // Result: 10
     * }</pre>
     */
    public static <T> @Nullable T firstOrNull(final @NotNull Stream<? extends T> stream) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        final Iterator<? extends T> it = stream.iterator();
        return it.hasNext() ? it.next() : null;
    }

    /**
     * Returns the first element of the stream, or {@code other} if empty.
     *
     * <pre>{@code
     * int v = StreamUtils.firstOrElse(Stream.<Integer>empty(), 10);
     * // Result: 10
     * }</pre>
     *
     * <pre>{@code
     * int v = StreamUtils.firstOrElse(Stream.of(1, 2, 3), 10);
     * // Result: 1
     * }</pre>
     */
    public static <T> @Nullable T firstOrElse(final @NotNull Stream<? extends T> stream, final @Nullable T other) {
        final T v = firstOrNull(stream);
        return v != null ? v : other;
    }

    /**
     * Returns the first element of the stream, or supplies one if empty.
     *
     * <pre>{@code
     * int v = StreamUtils.firstOrElseGet(Stream.<Integer>empty(), () -> 42);
     * // Result: 42
     * }</pre>
     *
     * <pre>{@code
     * int v = StreamUtils.firstOrElseGet(Stream.of(7), () -> 42);
     * // Result: 7
     * }</pre>
     */
    public static <T> @Nullable T firstOrElseGet(
        final @NotNull Stream<? extends T> stream,
        final @NotNull Supplier<? extends T> supplier
    ) {
        NullCheck.requireNonNull(supplier, "supplier must not be null");
        final T v = firstOrNull(stream);
        return v != null ? v : supplier.get();
    }

    /**
     * Returns the only element of the stream.
     *
     * <pre>{@code
     * int v = StreamUtils.single(Stream.of(5));
     * // Result: 5
     * }</pre>
     *
     * @throws NoSuchElementException if the stream is empty
     * @throws IllegalStateException if the stream has more than one element
     */
    public static <T> @NotNull T single(final @NotNull Stream<? extends T> stream) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        final Iterator<? extends T> it = stream.iterator();
        if (!it.hasNext()) {
            throw new NoSuchElementException("Stream is empty.");
        }
        final T first = it.next();
        if (it.hasNext()) {
            throw new IllegalStateException("Stream contains more than one element.");
        }
        @SuppressWarnings("ConstantConditions")
        final T nonNull = (T) first;
        return nonNull;
    }

    /**
     * Returns the only element of the stream, or {@code null} if empty.
     *
     * <pre>{@code
     * Integer v = StreamUtils.singleOrNull(Stream.<Integer>empty());
     * // Result: null
     * }</pre>
     *
     * <pre>{@code
     * int v = StreamUtils.singleOrNull(Stream.of(9));
     * // Result: 9
     * }</pre>
     *
     * @throws IllegalStateException if the stream has more than one element
     */
    public static <T> @Nullable T singleOrNull(final @NotNull Stream<? extends T> stream) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        final Iterator<? extends T> it = stream.iterator();
        if (!it.hasNext()) {
            return null;
        }
        final T first = it.next();
        if (it.hasNext()) {
            throw new IllegalStateException("Stream contains more than one element.");
        }
        return first;
    }

    /* ------------------------------------------------------------ */
    /* Indexing                                                      */
    /* ------------------------------------------------------------ */



    /**
     * Returns a stream of {@link Indexed} elements.
     *
     * <pre>{@code
     * StreamUtils.index(Stream.of("a", "b", "c"))
     *            .forEach(System.out::println);
     *
     * // Indexed[index=0, value=a]
     * // Indexed[index=1, value=b]
     * // Indexed[index=2, value=c]
     * }</pre>
     *
     * <pre>{@code
     * List<Long> indices =
     *     StreamUtils.index(Stream.of("x", "y"))
     *                .map(StreamUtils.Indexed::index)
     *                .toList();
     *
     * // Result: [0, 1]
     * }</pre>
     */
    public static <T> @NotNull Stream<Indexed<T>> index(final @NotNull Stream<? extends T> stream) {
        NullCheck.requireNonNull(stream, "stream must not be null");

        final Spliterator<? extends T> sp = stream.spliterator();
        final Iterator<? extends T> it = Spliterators.iterator(sp);

        final Spliterator<Indexed<T>> isp = new Spliterators.AbstractSpliterator<>(
            sp.estimateSize(),
            sp.characteristics() & (Spliterator.ORDERED | Spliterator.SIZED | Spliterator.NONNULL)
        ) {
            long idx = 0;

            @Override
            public boolean tryAdvance(final Consumer<? super Indexed<T>> action) {
                NullCheck.requireNonNull(action, "action must not be null");
                if (it.hasNext()) {
                    final T v = it.next();
                    action.accept(new Indexed<>(idx++, v));
                    return true;
                }
                return false;
            }
        };

        return StreamSupport.stream(isp, false).onClose(stream::close);
    }

    /* ------------------------------------------------------------ */
    /* Pairwise                                                      */
    /* ------------------------------------------------------------ */

    /**
     * Produces adjacent pairs from the stream: (a0,a1), (a1,a2), (a2,a3)...
     *
     * <p>For input size {@code n}, output size is {@code max(0, n-1)}.</p>
     *
     * <pre>{@code
     * List<Map.Entry<Integer, Integer>> list =
     *     StreamUtils.pairwise(Stream.of(1, 2, 3, 4))
     *                .toList();
     *
     * // Result: [(1,2), (2,3), (3,4)]
     * }</pre>
     *
     * <pre>{@code
     * // Compute differences between adjacent values
     * List<Integer> diffs =
     *     StreamUtils.pairwise(Stream.of(10, 13, 20))
     *                .map(e -> e.getValue() - e.getKey())
     *                .toList();
     *
     * // Result: [3, 7]
     * }</pre>
     *
     * @param stream source stream
     * @param <T> element type
     * @return adjacent pairs as {@link Pair}
     */
    public static <T> @NotNull Stream<Pair<T, T>> pairwise(final @NotNull Stream<? extends T> stream) {
        NullCheck.requireNonNull(stream, "stream must not be null");

        final Iterator<? extends T> it = stream.iterator();
        if (!it.hasNext()) {
            return Stream.empty();
        }

        final Spliterator<Pair<T, T>> sp = new Spliterators.AbstractSpliterator<>(
            Long.MAX_VALUE,
            Spliterator.ORDERED
        ) {
            T prev = (T) it.next();

            @Override
            public boolean tryAdvance(final Consumer<? super Pair<T, T>> action) {
                NullCheck.requireNonNull(action, "action must not be null");
                if (!it.hasNext()) {
                    return false;
                }

                final T next = (T) it.next();
                action.accept(ImmutablePair.of(prev, next));
                prev = next;
                return true;
            }
        };

        return StreamSupport.stream(sp, false).onClose(stream::close);
    }

    /**
     * Splits the stream into consecutive chunks of {@code size}.
     *
     * <p>The last chunk may have fewer than {@code size} elements.</p>
     *
     * <pre>{@code
     * List<List<Integer>> chunks =
     *     StreamUtils.chunk(Stream.of(1,2,3,4,5), 2)
     *                .toList();
     *
     * // Result: [[1,2], [3,4], [5]]
     * }</pre>
     *
     * @param stream source stream
     * @param size chunk size, must be positive
     * @param <T> element type
     * @return stream of chunks
     * @throws IllegalArgumentException if {@code size <= 0}
     */
    public static <T> @NotNull Stream<List<T>> chunk(
        final @NotNull Stream<? extends T> stream,
        final int size
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive, but was: " + size);
        }

        final Iterator<? extends T> it = stream.iterator();

        final Spliterator<List<T>> sp = new Spliterators.AbstractSpliterator<>(
            Long.MAX_VALUE,
            Spliterator.ORDERED
        ) {
            @Override
            public boolean tryAdvance(final Consumer<? super List<T>> action) {
                NullCheck.requireNonNull(action, "action must not be null");

                if (!it.hasNext()) {
                    return false;
                }

                final ArrayList<T> buf = new ArrayList<>(size);
                int i = 0;
                while (i < size && it.hasNext()) {
                    final T v = (T) it.next();
                    buf.add(v);
                    i++;
                }

                action.accept(Collections.unmodifiableList(buf));
                return true;
            }
        };

        return StreamSupport.stream(sp, false).onClose(stream::close);
    }

    /**
     * Produces sliding windows from the stream.
     *
     * <p>Each window has length {@code size}. After emitting a window, the window moves forward by {@code step}.</p>
     *
     * <p>If the stream does not have enough elements to form the first full window, the result is empty.</p>
     *
     * <pre>{@code
     * List<List<Integer>> windows =
     *     StreamUtils.window(Stream.of(1,2,3,4,5), 3, 1)
     *                .toList();
     *
     * // Result: [[1,2,3], [2,3,4], [3,4,5]]
     * }</pre>
     *
     * <pre>{@code
     * List<List<Integer>> windows =
     *     StreamUtils.window(Stream.of(1,2,3,4,5,6), 3, 2)
     *                .toList();
     *
     * // Result: [[1,2,3], [3,4,5]]
     * }</pre>
     *
     * @param stream source stream
     * @param size window size, must be positive
     * @param step step size, must be positive
     * @param <T> element type
     * @return stream of windows
     * @throws IllegalArgumentException if {@code size <= 0} or {@code step <= 0}
     */
    public static <T> @NotNull Stream<List<T>> window(
        final @NotNull Stream<? extends T> stream,
        final int size,
        final int step
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive, but was: " + size);
        }
        if (step <= 0) {
            throw new IllegalArgumentException("step must be positive, but was: " + step);
        }

        final Iterator<? extends T> it = stream.iterator();

        final Spliterator<List<T>> sp = new Spliterators.AbstractSpliterator<>(
            Long.MAX_VALUE,
            Spliterator.ORDERED
        ) {
            final ArrayDeque<T> deque = new ArrayDeque<>(size);
            boolean initialized = false;
            boolean done = false;

            private void initIfNeeded() {
                if (initialized) return;
                initialized = true;

                while (deque.size() < size && it.hasNext()) {
                    final T v = (T) it.next();
                    deque.addLast(v);
                }

                if (deque.size() < size) {
                    done = true;
                }
            }

            @Override
            public boolean tryAdvance(final Consumer<? super List<T>> action) {
                NullCheck.requireNonNull(action, "action must not be null");
                if (done) return false;

                initIfNeeded();
                if (done) return false;

                // emit current window
                action.accept(Collections.unmodifiableList(new ArrayList<>(deque)));

                // slide forward by step:
                // 1) remove step elements from front (if possible)
                for (int i = 0; i < step && !deque.isEmpty(); i++) {
                    deque.removeFirst();
                }

                // 2) refill to size
                while (deque.size() < size && it.hasNext()) {
                    final T v = (T) it.next();
                    deque.addLast(v);
                }

                if (deque.size() < size) {
                    done = true;
                }
                return true;
            }
        };

        return StreamSupport.stream(sp, false).onClose(stream::close);
    }

    /**
     * Returns a stream of accumulated results (prefix scan).
     *
     * <p>The first emitted element is {@code seed}, then each next element is computed by applying
     * {@code accumulator} to the previous accumulated value and the next stream element.</p>
     *
     * <pre>{@code
     * List<Integer> acc =
     *     StreamUtils.scan(Stream.of(1,2,3), 0, Integer::sum)
     *                .toList();
     *
     * // Result: [0, 1, 3, 6]
     * }</pre>
     *
     * @param stream source stream
     * @param seed initial value
     * @param accumulator accumulator function
     * @param <T> input element type
     * @param <R> accumulated type
     * @return accumulated stream
     */
    public static <T, R> @NotNull Stream<R> scan(
        final @NotNull Stream<? extends T> stream,
        final @Nullable R seed,
        final @NotNull BiFunction<? super R, ? super T, ? extends R> accumulator
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        NullCheck.requireNonNull(accumulator, "accumulator must not be null");

        final Iterator<? extends T> it = stream.iterator();

        final Spliterator<R> sp = new Spliterators.AbstractSpliterator<>(
            Long.MAX_VALUE,
            Spliterator.ORDERED
        ) {
            boolean emittedSeed = false;
            R acc = seed;

            @Override
            public boolean tryAdvance(final Consumer<? super R> action) {
                NullCheck.requireNonNull(action, "action must not be null");

                if (!emittedSeed) {
                    emittedSeed = true;
                    action.accept(acc);
                    return true;
                }

                if (!it.hasNext()) {
                    return false;
                }

                final T next = (T) it.next();

                acc = accumulator.apply(acc, next);
                action.accept(acc);
                return true;
            }
        };

        return StreamSupport.stream(sp, false).onClose(stream::close);
    }

    /**
     * Returns a stream consisting of the first {@code n} elements of {@code stream}.
     *
     * <pre>{@code
     * List<Integer> list =
     *     StreamUtils.take(Stream.of(1,2,3,4,5), 3)
     *                .toList();
     * // Result: [1, 2, 3]
     * }</pre>
     *
     * @throws IllegalArgumentException if {@code n < 0}
     */
    public static <T> @NotNull Stream<T> take(
        final @NotNull Stream<T> stream,
        final long n
    ) {

        NullCheck.requireNonNull(stream, "stream must not be null");
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, but was: " + n);
        }
        return stream.limit(n);
    }

    /**
     * Returns a stream consisting of the first {@code n} elements of {@code iterable}.
     *
     * <pre>{@code
     * List<Integer> list =
     *     StreamUtils.take(List.of(1,2,3,4,5), 2)
     *                .toList();
     * // Result: [1, 2]
     * }</pre>
     */
    public static <T> @NotNull Stream<T> take(
        final @NotNull Iterable<T> iterable,
        final long n
    ) {
        NullCheck.requireNonNull(iterable, "iterable must not be null");
        return take(StreamSupport.stream(iterable.spliterator(), false), n);
    }

    /**
     * Returns a stream consisting of the elements of {@code stream} after skipping the first {@code n}.
     *
     * <pre>{@code
     * List<Integer> list =
     *     StreamUtils.skip(Stream.of(1,2,3,4,5), 2)
     *                .toList();
     * // Result: [3, 4, 5]
     * }</pre>
     *
     * @throws IllegalArgumentException if {@code n < 0}
     */
    public static <T> @NotNull Stream<T> skip(
        final @NotNull Stream<T> stream,
        final long n
    ) {
        NullCheck.requireNonNull(stream, "stream must not be null");
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, but was: " + n);
        }
        return stream.skip(n);
    }

    /**
     * Returns a stream consisting of the elements of {@code iterable} after skipping the first {@code n}.
     *
     * <pre>{@code
     * List<Integer> list =
     *     StreamUtils.skip(List.of(1,2,3,4,5), 4)
     *                .toList();
     * // Result: [5]
     * }</pre>
     */
    public static <T> @NotNull Stream<T> skip(
        final @NotNull Iterable<T> iterable,
        final long n
    ) {
        NullCheck.requireNonNull(iterable, "iterable must not be null");
        return skip(StreamSupport.stream(iterable.spliterator(), false), n);
    }
}