package io.github.piscescup.linq;

import java.util.*;

/**
 * @author REN YuanTong
 * @since 1.0.0
 */
public interface Groupable<K, E> {
    K key();

    List<E> elements();
}
