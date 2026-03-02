package io.github.piscescup.linq.enumerator;

import io.github.piscescup.util.validation.NullCheck;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class IteratorEnumerator<T> extends AbstractEnumerator<T> {
    private final Iterator<? extends T> iterator;

    public IteratorEnumerator(Iterator<? extends T> iterator) {
        NullCheck.requireNonNull(iterator);
        this.iterator = iterator;
    }

    @Override
    protected boolean moveNextCore() {
        if (!iterator.hasNext()) return false;

        T value = (T) iterator.next();
        this.current = value;
        return true;
    }

    @Override
    public AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException(
            "IteratorEnumerator cannot be cloned. Use Linq.fromIterator(Supplier) to get fresh enumerators."
        );
    }
}
