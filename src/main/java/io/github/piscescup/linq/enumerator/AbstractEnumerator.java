package io.github.piscescup.linq.enumerator;

import io.github.piscescup.linq.Enumerator;
import io.github.piscescup.util.validation.NullCheck;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
abstract class AbstractEnumerator<T> implements Enumerator<T> {

    protected int state;
    protected T current;
    private boolean checkedNext;
    private boolean hasNext;

    @Override
    public abstract boolean moveNext();

    @Override
    public T current() {
        return this.current;
    }

    @Override
    public boolean hasNext() {
        if (!this.checkedNext) {
            this.hasNext = this.moveNext();
            this.checkedNext = true;
        }
        return this.hasNext;
    }

    @Override
    public T next() {
        if (this.hasNext()) {
            this.checkedNext = false;
            return this.current();
        }
        throw new NoSuchElementException();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        NullCheck.requireNonNull(action);
        while (this.moveNext())
            action.accept(this.current());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        this.current = null;
        this.state = -1;
    }
}