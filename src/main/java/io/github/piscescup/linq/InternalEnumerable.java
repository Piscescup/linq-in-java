package io.github.piscescup.linq;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * A sequence of elements that can be enumerated. All Enumerable are {@link InternalEnumerable}.
 *
 * @author REN YuanTong
 * @since 1.0.3
 */
public interface InternalEnumerable<T> extends Iterable<T> {
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
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @NotNull
    default Iterator<T> iterator() {
        return enumerator();
    }

    /**
     * Performs the given action for each element of the {@code InternalEnumerable}
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
