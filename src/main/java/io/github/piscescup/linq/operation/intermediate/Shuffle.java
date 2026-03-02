package io.github.piscescup.linq.operation.intermediate;

import io.github.piscescup.linq.Enumerable;
import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.linq.enumerator.AbstractEnumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * Provides conditional shuffle operation.
 *
 * <p>
 * Shuffles only elements that satisfy the given predicate.
 * Elements that do not satisfy the predicate retain their original
 * relative order and position.
 *
 * <h2>Execution Model</h2>
 * <ul>
 *     <li>The entire source sequence is buffered in memory.</li>
 *     <li>Matching elements are shuffled using {@link Collections#shuffle(List)}.</li>
 *     <li>The final sequence preserves original non-matching positions.</li>
 * </ul>
 *
 * <h2>Resource Management</h2>
 * <ul>
 *     <li>The source enumerator is consumed using try-with-resources and closed immediately.</li>
 * </ul>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Shuffle {
    private Shuffle() {
        throw new UnsupportedOperationException(
            "No instance of " + Shuffle.class.getCanonicalName() + " for you!"
        );
    }

    /**
     * Shuffles only elements that match the given predicate.
     *
     * @param source source sequence
     * @param predicate condition determining which elements are shuffled
     * @param <T> element type
     * @return a sequence with matching elements shuffled
     * @throws NullPointerException if any argument is null
     */
    public static <T> Enumerable<T> shuffle(
        Enumerable<T> source, Predicate<? super T> predicate
    ) {
        NullCheck.requireNonNull(source);
        NullCheck.requireNonNull(predicate);
        return new ShuffleEnumerable<>(source, predicate);
    }
}

/* ====================================================================== */
/* Package-private implementation classes                                  */
/* ====================================================================== */

final class ShuffleEnumerable<T> implements Enumerable<T> {

    private final Enumerable<T> source;
    private final Predicate<? super T> predicate;

    ShuffleEnumerable(Enumerable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    public Enumerator<T> enumerator() {
        return new ShuffleEnumerator<>(source, predicate);
    }
}

final class ShuffleEnumerator<T> extends AbstractEnumerator<T> {

    private final Enumerable<T> source;
    private final Predicate<? super T> predicate;

    private List<T> snapshot;
    private int index = -1;

    ShuffleEnumerator(Enumerable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }

    @Override
    protected boolean moveNextCore() {

        if (snapshot == null) {
            snapshot = buildSnapshot(source, predicate);
        }

        int next = index + 1;
        if (next >= snapshot.size()) {
            return false;
        }

        index = next;
        this.current = snapshot.get(index);
        return true;
    }

    private static <T> List<T> buildSnapshot(
        Enumerable<T> source,
        Predicate<? super T> predicate
    ) {
        final List<T> original = new ArrayList<>();
        final List<Integer> shuffleIndexes = new ArrayList<>();
        final List<T> shuffleElements = new ArrayList<>();

        try (Enumerator<T> e = source.enumerator()) {
            while (e.moveNext()) {
                T item = e.current();
                int position = original.size();

                original.add(item);

                if (predicate.test(item)) {
                    shuffleIndexes.add(position);
                    shuffleElements.add(item);
                }
            }
        }

        // Shuffle matching elements
        Collections.shuffle(shuffleElements);

        // Put shuffled elements back to their original positions
        for (int i = 0; i < shuffleIndexes.size(); i++) {
            int position = shuffleIndexes.get(i);
            original.set(position, shuffleElements.get(i));
        }

        return original;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        ShuffleEnumerator<T> e = new ShuffleEnumerator<>(source, predicate);
        e.snapshot = this.snapshot;
        e.index = -1;
        return e;
    }
}