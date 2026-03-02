package io.github.piscescup.linq.enumerator;

import io.github.piscescup.util.validation.NullCheck;

import java.util.Iterator;

/**
 * Enumerator implementation backed by an {@link Iterable}.
 *
 * <p>
 * This enumerator obtains an {@link Iterator} from the provided
 * {@link Iterable} and iterates through it sequentially.
 *
 * <h2>Characteristics</h2>
 * <ul>
 *     <li>Iteration order follows the underlying iterable's iterator order.</li>
 *     <li>The {@link Iterator} is created lazily on the first call to {@link #moveNextCore()}.</li>
 *     <li>No buffering or copying is performed.</li>
 *     <li>Time complexity per element: O(1) (amortized, depending on iterator implementation).</li>
 *     <li>Total time complexity: O(n).</li>
 *     <li>Space complexity: O(1).</li>
 * </ul>
 *
 * <h2>Fresh Enumerator Semantics</h2>
 * <p>
 * Each instance maintains its own iterator. Calling {@link #clone()}
 * creates a new enumerator which will obtain a fresh iterator from
 * the same iterable when enumeration begins.
 *
 * <h2>Thread Safety</h2>
 * <p>
 * This enumerator is not thread-safe. Concurrent access must be externally synchronized.
 *
 * <h2>Important Note</h2>
 * <p>
 * The correctness of this enumerator depends on the iterable providing
 * a fresh and independent iterator for each call to {@link Iterable#iterator()}.
 *
 * @param <T> element type
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class IterableEnumerator<T> extends AbstractEnumerator<T> {

    /**
     * Underlying iterable.
     */
    private final Iterable<? extends T> iterable;

    /**
     * Lazily initialized iterator.
     */
    private Iterator<? extends T> iterator;

    /**
     * Constructs an enumerator over the given iterable.
     *
     * @param iterable iterable source
     * @throws NullPointerException if {@code iterable} is null
     */
    public IterableEnumerator(Iterable<? extends T> iterable) {
        NullCheck.requireNonNull(iterable);
        this.iterable = iterable;
    }

    /**
     * Advances to the next element in the iterable.
     *
     * <p>
     * The iterator is created lazily on the first call.
     *
     * @return {@code true} if an element is available;
     *         {@code false} if iteration has completed
     */
    @Override
    protected boolean moveNextCore() {
        if (iterator == null) iterator = iterable.iterator();
        if (!iterator.hasNext()) return false;

        T value = (T) iterator.next();
        this.current = value;
        return true;
    }

    /**
     * Creates a new enumerator over the same iterable.
     *
     * <p>
     * The cloned enumerator starts from the beginning
     * (it will request a new iterator when enumeration begins).
     *
     * @return a new {@code IterableEnumerator}
     */
    @Override
    public AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new IterableEnumerator<>(iterable);
    }
}