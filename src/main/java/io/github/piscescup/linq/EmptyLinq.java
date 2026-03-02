package io.github.piscescup.linq;

import io.github.piscescup.linq.enumerator.AbstractEnumerator;

final class EmptyLinq<T> implements Enumerable<T> {

    static final EmptyLinq<?> INSTANCE = new EmptyLinq<>();

    private EmptyLinq() {}

    @Override
    public Enumerator<T> enumerator() {
        return new EmptyEnumerator<>();
    }
}

final class EmptyEnumerator<T> extends AbstractEnumerator<T> {

    @Override
    protected boolean moveNextCore() {
        return false;
    }

    @Override
    protected AbstractEnumerator<T> clone() throws CloneNotSupportedException {
        return new EmptyEnumerator<>();
    }

    @Override
    public void close() {
        // nothing to close
    }
}