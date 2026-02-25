package io.github.piscescup.utils;

/**
 * An element with its 0-based index.
 *
 * @param index element index
 * @param value element value
 * @param <T> element type
 */
public record Indexed<T>(long index, T value) {

}
