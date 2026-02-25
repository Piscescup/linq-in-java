package io.github.piscescup.linq;

/**
 * @author REN YuanTong
 * @since 1.0.0
 */
public interface Enumerator<T> extends AutoCloseable {
    boolean moveNext();
    T current();

    @Override
    default void close() {}
}
