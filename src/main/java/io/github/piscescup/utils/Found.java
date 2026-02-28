package io.github.piscescup.utils;

/**
 * Internal result type that can represent:
 * <ul>
 *   <li>found=false, value=anything  (no element found)</li>
 *   <li>found=true,  value=element  (element may be null)</li>
 * </ul>
 */
public record Found<T>(T value, boolean found) {

}
