package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides LINQ-style chunking operation.
 *
 * <p>
 * Splits a sequence into consecutive chunks of the specified size.
 * The last chunk may contain fewer elements.
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * Enumerable<Integer> numbers = Enumerable.of(1,2,3,4,5);
 *
 * numbers.chunk(2);
 * // Produces:
 * // [1,2]
 * // [3,4]
 * // [5]
 * }</pre>
 *
 * <p>
 * This operation is lazy and buffers at most {@code size} elements at a time.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Chunk {

    private Chunk() {
        throw new UnsupportedOperationException(
            "No instance of " + Chunk.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Splits the source sequence into chunks of the specified size.
     *
     * @param source the source sequence
     * @param size   size of each chunk (must be positive)
     * @param <T>    element type
     * @return an {@link Enumerable} of arrays representing chunks
     *
     * @throws NullPointerException     if source is null
     * @throws IllegalArgumentException if size <= 0
     */
    public static <T> Enumerable<T[]> chunk(Enumerable<T> source, int size, Class<T> clazz) {
        NullCheck.requireNonNull(source);
        if (size <= 0) {
            throw new IllegalArgumentException("Chunk size must be greater than zero.");
        }
        return new ChunkEnumerable<>(source, size, clazz);
    }

    /**
     * Splits the source sequence into chunks of the specified size,
     * returning each chunk as a {@link List}.
     *
     * <p>
     * The returned lists preserve the original encounter order within each chunk.
     * The last chunk may contain fewer elements.
     *
     * <p>
     * This operation is lazy and buffers at most {@code size} elements at a time.
     *
     * <pre>{@code
     * Enumerable<Integer> numbers = Enumerable.of(1,2,3,4,5);
     *
     * Enumerable<List<Integer>> chunks = Chunk.chunkAsList(numbers, 2);
     * // Produces:
     * // [1, 2]
     * // [3, 4]
     * // [5]
     * }</pre>
     *
     * @param source the source sequence
     * @param size   size of each chunk (must be positive)
     * @param <T>    element type
     *
     * @return an {@link Enumerable} of lists representing chunks
     *
     * @throws NullPointerException     if source is null
     * @throws IllegalArgumentException if size <= 0
     */
    public static <T> Enumerable<List<T>> chunkAsList(Enumerable<T> source, int size) {
        NullCheck.requireNonNull(source);
        if (size <= 0) {
            throw new IllegalArgumentException("Chunk size must be greater than zero.");
        }
        return new ChunkAsListEnumerable<>(source, size);
    }
}

class ChunkEnumerable<T> implements Enumerable<T[]> {

    private final Enumerable<T> source;
    private final int size;
    private final Class<T> clazz;

    ChunkEnumerable(Enumerable<T> source, int size, Class<T> clazz) {
        this.source = source;
        this.size = size;
        this.clazz = clazz;
    }

    @Override
    public Enumerator<T[]> enumerator() {
        return new ChunkEnumerator<>(source, size, clazz);
    }
}

class ChunkEnumerator<T> extends AbstractEnumerator<T[]> {

    private final Enumerable<T> source;
    private final int size;
    private final Class<T> clazz;

    private Enumerator<T> sourceEnumerator;

    ChunkEnumerator(Enumerable<T> source, int size, Class<T> clazz) {
        this.source = source;
        this.size = size;
        this.clazz = clazz;
    }

    @Override
    protected boolean moveNextCore() {

        if (sourceEnumerator == null) {
            sourceEnumerator = source.enumerator();
        }

        if (!sourceEnumerator.moveNext()) {
            return false;
        }

        @SuppressWarnings("unchecked")
        T[] buffer = (T[]) Array.newInstance(clazz, size);

        int count = 0;

        do {
            buffer[count++] = sourceEnumerator.current();
        } while (count < size && sourceEnumerator.moveNext());

        if (count == size) {
            this.current = buffer;
        } else {
            @SuppressWarnings("unchecked")
            T[] last = (T[]) Array.newInstance(clazz, count);
            System.arraycopy(buffer, 0, last, 0, count);
            this.current = last;
        }

        return true;
    }

    @Override
    protected AbstractEnumerator<T[]> clone() throws CloneNotSupportedException {
        return new ChunkEnumerator<>(source, size, clazz);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
    }
}

class ChunkAsListEnumerable<T> implements Enumerable<List<T>> {

    private final Enumerable<T> source;
    private final int size;

    ChunkAsListEnumerable(Enumerable<T> source, int size) {
        this.source = source;
        this.size = size;
    }

    @Override
    public Enumerator<List<T>> enumerator() {
        return new ChunkAsListEnumerator<>(source, size);
    }
}

class ChunkAsListEnumerator<T> extends AbstractEnumerator<List<T>> {

    private final Enumerable<T> source;
    private final int size;

    private Enumerator<T> sourceEnumerator;

    ChunkAsListEnumerator(Enumerable<T> source, int size) {
        this.source = source;
        this.size = size;
    }

    @Override
    protected boolean moveNextCore() {

        if (sourceEnumerator == null) {
            sourceEnumerator = source.enumerator();
        }

        if (!sourceEnumerator.moveNext()) {
            return false;
        }

        // 这里可以预设容量，减少扩容
        List<T> list = new ArrayList<>(size);

        int count = 0;
        do {
            list.add(sourceEnumerator.current());
            count++;
        } while (count < size && sourceEnumerator.moveNext());

        this.current = list;
        return true;
    }

    @Override
    protected AbstractEnumerator<List<T>> clone() throws CloneNotSupportedException {
        return new ChunkAsListEnumerator<>(source, size);
    }

    @Override
    public void close() {
        if (sourceEnumerator != null) {
            sourceEnumerator.close();
            sourceEnumerator = null;
        }
    }
}
