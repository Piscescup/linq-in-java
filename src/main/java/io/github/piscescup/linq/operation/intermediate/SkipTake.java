package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.interfaces.exfunction.BinPredicate;
import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

/**
 * Provides LINQ-style skip/take operations.
 *
 * <p>
 * Includes:
 * <ul>
 *     <li>{@link #skip}: skips the first N elements.</li>
 *     <li>{@link #take}: takes the first N elements.</li>
 *     <li>{@link #skipWhile}/{@link #takeWhile}: skips/takes while predicate holds.</li>
 *     <li>{@link #skipLast}/{@link #takeLast}: skips/takes from the tail (buffering).</li>
 * </ul>
 *
 * <h2>Resource Management</h2>
 * <ul>
 *     <li>Streaming operators (skip/take/while variants) hold the source enumerator and close it in {@code close()}.</li>
 *     <li>Tail operators (skipLast/takeLast) buffer the source using try-with-resources and close immediately.</li>
 * </ul>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class SkipTake {
    private SkipTake() {
        throw new UnsupportedOperationException(
            "No instance of " + SkipTake.class.getCanonicalName() + " for you!"
        );
    }

    /* ------------------------------------------------------------ */
    /* skip / take                                                   */
    /* ------------------------------------------------------------ */

    public static <T> Enumerable<T> skip(Enumerable<T> source, int count) {
        NullCheck.requireNonNull(source);
        if (count <= 0) return source;
        return new SkipEnumerable<>(source, count);
    }

    public static <T> Enumerable<T> take(Enumerable<T> source, int count) {
        NullCheck.requireNonNull(source);
        if (count <= 0) return new EmptyEnumerable<>();
        return new TakeEnumerable<>(source, count);
    }

    /* ------------------------------------------------------------ */
    /* skipLast / takeLast (buffering)                               */
    /* ------------------------------------------------------------ */

    public static <T> Enumerable<T> skipLast(Enumerable<T> source, int count) {
        NullCheck.requireNonNull(source);
        if (count <= 0) return source;
        return new SkipLastEnumerable<>(source, count);
    }

    public static <T> Enumerable<T> takeLast(Enumerable<T> source, int count) {
        NullCheck.requireNonNull(source);
        if (count <= 0) return new EmptyEnumerable<>();
        return new TakeLastEnumerable<>(source, count);
    }

    /* ------------------------------------------------------------ */
    /* skipWhile / takeWhile                                         */
    /* ------------------------------------------------------------ */

    public static <T> Enumerable<T> skipWhile(Enumerable<T> source, Predicate<? super T> predicate) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);
        return new SkipWhileEnumerable<>(source, predicate);
    }

    public static <T> Enumerable<T> skipWhile(
        Enumerable<T> source, BinPredicate<? super T, ? super Integer> predicate
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);
        return new SkipWhileIndexedEnumerable<>(source, predicate);
    }

    public static <T> Enumerable<T> takeWhile(Enumerable<T> source, Predicate<? super T> predicate) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);
        return new TakeWhileEnumerable<>(source, predicate);
    }

    public static <T> Enumerable<T> takeWhile(
        Enumerable<T> source, BinPredicate<? super T, ? super Integer> predicate
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);
        return new TakeWhileIndexedEnumerable<>(source, predicate);
    }
}

/* ====================================================================== */
/* Small helpers                                                           */
/* ====================================================================== */

final class EmptyEnumerable<T> implements Enumerable<T> {
    @Override
    public Enumerator<T> enumerator() {
        return new EmptyEnumerator<>();
    }

    static final class EmptyEnumerator<T> extends AbstractEnumerator<T> {
        @Override
        protected boolean moveNextCore() {
            return false;
        }

        @Override
        protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
            return new EmptyEnumerator<>();
        }
    }
}

/* ====================================================================== */
/* skip                                                                     */
/* ====================================================================== */

final class SkipEnumerable<T> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final int count;

    SkipEnumerable(Enumerable<T> source, int count) {
        this.source = source;
        this.count = count;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new SkipEnumerator<>(source, count);
    }
}

final class SkipEnumerator<T> extends AbstractEnumerator<T> {
    private final Enumerable<T> source;
    private final int count;

    private Enumerator<T> e;
    private boolean skipped = false;

    SkipEnumerator(Enumerable<T> source, int count) {
        this.source = source;
        this.count = count;
    }

    @Override
    protected boolean moveNextCore() {
        if (e == null) e = source.enumerator();

        if (!skipped) {
            skipped = true;
            int n = count;
            while (n > 0 && e.moveNext()) {
                n--;
            }
        }

        if (!e.moveNext()) return false;
        this.current = e.current();
        return true;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new SkipEnumerator<>(source, count);
    }

    @Override
    public void close() {
        if (e != null) {
            e.close();
            e = null;
        }
    }
}

/* ====================================================================== */
/* take                                                                     */
/* ====================================================================== */

final class TakeEnumerable<T> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final int count;

    TakeEnumerable(Enumerable<T> source, int count) {
        this.source = source;
        this.count = count;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new TakeEnumerator<>(source, count);
    }
}

final class TakeEnumerator<T> extends AbstractEnumerator<T> {
    private final Enumerable<T> source;
    private final int count;

    private Enumerator<T> e;
    private int taken = 0;

    TakeEnumerator(Enumerable<T> source, int count) {
        this.source = source;
        this.count = count;
    }

    @Override
    protected boolean moveNextCore() {
        if (taken >= count) return false;
        if (e == null) e = source.enumerator();

        if (!e.moveNext()) return false;
        taken++;
        this.current = e.current();
        return true;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new TakeEnumerator<>(source, count);
    }

    @Override
    public void close() {
        if (e != null) {
            e.close();
            e = null;
        }
    }
}

/* ====================================================================== */
/* skipWhile                                                                */
/* ====================================================================== */

final class SkipWhileEnumerable<T> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final Predicate<? super T> predicate;

    SkipWhileEnumerable(Enumerable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new SkipWhileEnumerator<>(source, predicate);
    }
}

final class SkipWhileEnumerator<T> extends AbstractEnumerator<T> {
    private final Enumerable<T> source;
    private final Predicate<? super T> predicate;

    private Enumerator<T> e;
    private boolean skippingDone = false;

    SkipWhileEnumerator(Enumerable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    protected boolean moveNextCore() {
        if (e == null) e = source.enumerator();

        while (true) {
            if (!e.moveNext()) return false;
            T item = e.current();

            if (!skippingDone) {
                if (predicate.test(item)) {
                    continue; // still skipping
                }
                skippingDone = true;
            }

            this.current = item;
            return true;
        }
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new SkipWhileEnumerator<>(source, predicate);
    }

    @Override
    public void close() {
        if (e != null) {
            e.close();
            e = null;
        }
    }
}

/* indexed skipWhile */
final class SkipWhileIndexedEnumerable<T> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final BinPredicate<? super T, ? super Integer> predicate;

    SkipWhileIndexedEnumerable(Enumerable<T> source, BinPredicate<? super T, ? super Integer> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new SkipWhileIndexedEnumerator<>(source, predicate);
    }
}

final class SkipWhileIndexedEnumerator<T> extends AbstractEnumerator<T> {
    private final Enumerable<T> source;
    private final BinPredicate<? super T, ? super Integer> predicate;

    private Enumerator<T> e;
    private boolean skippingDone = false;
    private int index = 0;

    SkipWhileIndexedEnumerator(Enumerable<T> source, BinPredicate<? super T, ? super Integer> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    protected boolean moveNextCore() {
        if (e == null) e = source.enumerator();

        while (true) {
            if (!e.moveNext()) return false;
            T item = e.current();

            if (!skippingDone) {
                boolean shouldSkip = predicate.test(item, index);
                index++;
                if (shouldSkip) {
                    continue;
                }
                skippingDone = true;
            }

            this.current = item;
            return true;
        }
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new SkipWhileIndexedEnumerator<>(source, predicate);
    }

    @Override
    public void close() {
        if (e != null) {
            e.close();
            e = null;
        }
    }
}

/* ====================================================================== */
/* takeWhile                                                                */
/* ====================================================================== */

final class TakeWhileEnumerable<T> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final Predicate<? super T> predicate;

    TakeWhileEnumerable(Enumerable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new TakeWhileEnumerator<>(source, predicate);
    }
}

final class TakeWhileEnumerator<T> extends AbstractEnumerator<T> {
    private final Enumerable<T> source;
    private final Predicate<? super T> predicate;

    private Enumerator<T> e;
    private boolean done = false;

    TakeWhileEnumerator(Enumerable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    protected boolean moveNextCore() {
        if (done) return false;
        if (e == null) e = source.enumerator();

        if (!e.moveNext()) {
            done = true;
            return false;
        }

        T item = e.current();
        if (!predicate.test(item)) {
            done = true;
            return false;
        }

        this.current = item;
        return true;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new TakeWhileEnumerator<>(source, predicate);
    }

    @Override
    public void close() {
        if (e != null) {
            e.close();
            e = null;
        }
    }
}

/* indexed takeWhile */
final class TakeWhileIndexedEnumerable<T> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final BinPredicate<? super T, ? super Integer> predicate;

    TakeWhileIndexedEnumerable(Enumerable<T> source, BinPredicate<? super T, ? super Integer> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new TakeWhileIndexedEnumerator<>(source, predicate);
    }
}

final class TakeWhileIndexedEnumerator<T> extends AbstractEnumerator<T> {
    private final Enumerable<T> source;
    private final BinPredicate<? super T, ? super Integer> predicate;

    private Enumerator<T> e;
    private int index = 0;
    private boolean done = false;

    TakeWhileIndexedEnumerator(Enumerable<T> source, BinPredicate<? super T, ? super Integer> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    protected boolean moveNextCore() {
        if (done) return false;
        if (e == null) e = source.enumerator();

        if (!e.moveNext()) {
            done = true;
            return false;
        }

        T item = e.current();
        boolean keep = predicate.test(item, index);
        index++;

        if (!keep) {
            done = true;
            return false;
        }

        this.current = item;
        return true;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new TakeWhileIndexedEnumerator<>(source, predicate);
    }

    @Override
    public void close() {
        if (e != null) {
            e.close();
            e = null;
        }
    }
}

/* ====================================================================== */
/* takeLast / skipLast (buffering)                                         */
/* ====================================================================== */

final class TakeLastEnumerable<T> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final int count;

    TakeLastEnumerable(Enumerable<T> source, int count) {
        this.source = source;
        this.count = count;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new TakeLastEnumerator<>(source, count);
    }
}

final class TakeLastEnumerator<T> extends AbstractEnumerator<T> {
    private final Enumerable<T> source;
    private final int count;

    private List<T> snapshot;
    private int index = -1;

    TakeLastEnumerator(Enumerable<T> source, int count) {
        this.source = source;
        this.count = count;
    }

    @Override
    protected boolean moveNextCore() {
        if (snapshot == null) {
            snapshot = buildSnapshot(source, count);
        }

        int next = index + 1;
        if (next >= snapshot.size()) return false;

        index = next;
        this.current = snapshot.get(index);
        return true;
    }

    private static <T> List<T> buildSnapshot(Enumerable<T> source, int count) {
        if (count <= 0) return List.of();

        Deque<T> ring = new ArrayDeque<>(count);

        try (Enumerator<T> e = source.enumerator()) { // ✅ buffering close
            while (e.moveNext()) {
                if (ring.size() == count) {
                    ring.removeFirst();
                }
                ring.addLast(e.current());
            }
        }

        return new ArrayList<>(ring);
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        TakeLastEnumerator<T> e = new TakeLastEnumerator<>(source, count);
        e.snapshot = this.snapshot;
        e.index = -1;
        return e;
    }
}

final class SkipLastEnumerable<T> implements Enumerable<T> {
    private final Enumerable<T> source;
    private final int count;

    SkipLastEnumerable(Enumerable<T> source, int count) {
        this.source = source;
        this.count = count;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new SkipLastEnumerator<>(source, count);
    }
}

final class SkipLastEnumerator<T> extends AbstractEnumerator<T> {
    private final Enumerable<T> source;
    private final int count;

    private Enumerator<T> e;
    private Deque<T> buffer;
    private boolean initialized = false;

    SkipLastEnumerator(Enumerable<T> source, int count) {
        this.source = source;
        this.count = count;
    }

    @Override
    protected boolean moveNextCore() {
        if (count <= 0) {
            if (e == null) e = source.enumerator();
            if (!e.moveNext()) return false;
            this.current = e.current();
            return true;
        }

        if (!initialized) {
            initialized = true;
            e = source.enumerator();
            buffer = new ArrayDeque<>(count);

            // prefill buffer with first 'count' elements
            while (buffer.size() < count && e.moveNext()) {
                buffer.addLast(e.current());
            }

            // If source has <= count elements, skip all => return false forever.
            if (buffer.size() < count) {
                return false;
            }
        }

        // Now: buffer has exactly 'count' elements, and e is positioned right after it.
        // We can emit next output by reading one more element: output oldest, push new.
        if (!e.moveNext()) {
            // no more items to push => remaining in buffer are the last 'count', should be skipped
            return false;
        }

        T nextItem = e.current();
        T output = buffer.removeFirst();
        buffer.addLast(nextItem);

        this.current = output;
        return true;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new SkipLastEnumerator<>(source, count);
    }

    @Override
    public void close() {
        if (e != null) {
            e.close();
            e = null;
        }
        if (buffer != null) {
            buffer.clear();
            buffer = null;
        }
    }
}
