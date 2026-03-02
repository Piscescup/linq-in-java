package io.github.piscescup.linq.enumerator;

import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 * A base implementation of {@link Enumerator} that bridges the semantics of
 * C#-style enumeration ({@link #moveNext()} + {@link #current()}) and Java's
 * {@link java.util.Iterator} contract ({@link #hasNext()} + {@link #next()}).
 *
 * <p><b>Core rule:</b> Implementations must not advance the underlying cursor twice
 * when mixing {@code hasNext()/next()} with {@code moveNext()}.
 * This class provides a consistent look-ahead (peek) mechanism so that:
 * <ul>
 *     <li>{@link #hasNext()} may advance once (peek), and {@link #next()} consumes that peek</li>
 *     <li>{@link #moveNext()} consumes an existing peek without advancing again</li>
 *     <li>{@link #forEachRemaining(Consumer)} respects a previously peeked element</li>
 * </ul>
 *
 * <p>Subclasses only need to implement {@link #moveNextCore()}, which should advance
 * the underlying cursor exactly once.
 *
 * <p><b>Thread safety:</b> Not thread-safe. This base class may optionally enforce
 * thread affinity (single-thread usage).
 *
 * @param <T> element type
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public abstract class AbstractEnumerator<T> implements Enumerator<T> {

    /**
     * State for generator/state-machine style enumerators.
     * Use it consistently in subclasses if needed.
     */
    protected int state;

    /**
     * The current element (valid only after a successful advancement).
     */
    protected T current;

    // Look-ahead cache used by Iterator-style hasNext/next
    private boolean peeked;
    private boolean hasPeekedValue;

    // Closed flag for idempotent close and defensive checks
    private boolean closed;

    // Optional: enforce single-thread usage (can be removed if you don't want it)
    private final long threadId = Thread.currentThread().threadId();

    /**
     * Advances the underlying cursor by one element and updates {@link #current}.
     *
     * <p><b>Contract for subclasses:</b>
     * <ul>
     *     <li>Return {@code true} and set {@link #current} when an element is produced.</li>
     *     <li>Return {@code false} when the end is reached.</li>
     *     <li>Do not implement any peek/lookahead logic here; this method should advance exactly once.</li>
     * </ul>
     *
     * @return {@code true} if moved to next element; {@code false} if end reached
     * @since 1.0.0
     */
    protected abstract boolean moveNextCore();

    /**
     * Ensures the enumerator is used on the same thread that created it.
     * Remove this check if you don't want thread affinity.
     */
    protected final void ensureThreadAffinity() {
        if (Thread.currentThread().threadId() != threadId) {
            throw new IllegalStateException("Enumerator cannot be used across threads.");
        }
    }

    /**
     * Ensures the enumerator is not closed.
     */
    protected final void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Enumerator is closed.");
        }
    }

    /**
     * C#-style: advances to next element.
     *
     * <p>If {@link #hasNext()} has already performed a look-ahead, this method will
     * consume that peeked result without advancing the underlying cursor again.
     */
    @Override
    public final boolean moveNext() {
        ensureThreadAffinity();
        ensureOpen();

        if (peeked) {
            peeked = false;
            return hasPeekedValue;
        }
        return moveNextCore();
    }

    /**
     * Returns the current element.
     *
     * <p><b>Note:</b> This method does not validate cursor position (before first / after last),
     * because many enumerator implementations intentionally keep it lightweight.
     * If you want strict behavior, subclasses may override and throw {@link IllegalStateException}
     * when appropriate.
     */
    @Override
    public T current() {
        ensureThreadAffinity();
        ensureOpen();
        return current;
    }

    /**
     * Java Iterator-style: returns whether more elements exist.
     *
     * <p>This method performs a single-element look-ahead (peek) by calling
     * {@link #moveNextCore()} at most once per peek cycle.
     */
    @Override
    public final boolean hasNext() {
        ensureThreadAffinity();
        ensureOpen();

        if (!peeked) {
            hasPeekedValue = moveNextCore();
            peeked = true;
        }
        return hasPeekedValue;
    }

    /**
     * Java Iterator-style: returns next element and advances.
     *
     * <p>This method consumes the look-ahead performed by {@link #hasNext()}.
     */
    @Override
    public final T next() {
        ensureThreadAffinity();
        ensureOpen();

        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        // current already set by moveNextCore() during peek
        peeked = false;
        return current;
    }

    /**
     * Performs the given action for each remaining element.
     *
     * <p>This implementation respects a previously peeked element (from {@link #hasNext()}),
     * so it will not skip elements.
     */
    @Override
    public final void forEachRemaining(Consumer<? super T> action) {
        ensureThreadAffinity();
        ensureOpen();
        NullCheck.requireNonNull(action);

        while (hasNext()) {
            action.accept(next());
        }
    }

    /**
     * Removes the last returned element (optional operation).
     *
     * <p>Default implementation does not support removal.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Resets the enumerator (optional operation).
     *
     * <p>Default implementation does not support reset.
     */
    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    /**
     * Closes this enumerator and releases resources.
     *
     * <p>This method is idempotent.
     */
    @Override
    public void close() {
        if (closed) return;

        closed = true;
        current = null;
        state = -1;

        // clear peek cache
        peeked = false;
        hasPeekedValue = false;
    }

    @Override
    protected abstract AbstractEnumerator<T> clone() throws CloneNotSupportedException;
}
