package io.github.piscescup.linq.enumerator;

import io.github.piscescup.util.validation.NullCheck;

/**
 * Enumerator implementation backed by an array.
 *
 * <p>
 * This enumerator iterates sequentially over the provided array.
 * It does not copy the array and reads elements directly from it.
 *
 * <h2>Characteristics</h2>
 * <ul>
 *     <li>Iteration order matches the array index order (0 → length-1).</li>
 *     <li>No additional memory allocation during iteration.</li>
 *     <li>Time complexity per element: O(1).</li>
 *     <li>Total time complexity: O(n).</li>
 *     <li>Space complexity: O(1).</li>
 * </ul>
 *
 * <h2>Fresh Enumerator Semantics</h2>
 * <p>
 * Each instance maintains its own cursor index. Creating a new
 * {@code ArrayEnumerator} over the same array restarts iteration
 * from the beginning.
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This enumerator is not thread-safe. Concurrent access must be externally synchronized.
 *
 * @param <T> element type
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class ArrayEnumerator<T> extends AbstractEnumerator<T> {

    /**
     * Underlying array being iterated.
     */
    private final T[] array;

    /**
     * Current iteration index.
     * Points to the next element to be returned.
     */
    private int index;

    /**
     * Constructs an enumerator over the given array.
     *
     * <p>
     * The enumerator starts in a state positioned before the first element.
     *
     * @param array array to iterate over
     * @throws NullPointerException if {@code array} is null
     */
    public ArrayEnumerator(T[] array) {
        NullCheck.requireNonNull(array);
        this.array = array;
    }

    /**
     * Advances to the next element in the array.
     *
     * <p>
     * If the array has remaining elements, updates {@code current}
     * and returns {@code true}. Otherwise returns {@code false}.
     *
     * @return {@code true} if an element is available; {@code false} if iteration is complete
     */
    @Override
    protected boolean moveNextCore() {
        if (index >= array.length) return false;
        this.current = array[index++];
        return true;
    }

    /**
     * Creates a new enumerator over the same underlying array.
     *
     * <p>
     * The cloned enumerator starts from the beginning of the array
     * (index = 0), independent of the current enumerator state.
     *
     * @return a new {@code ArrayEnumerator} instance
     */
    @Override
    public AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new ArrayEnumerator<>(array);
    }
}