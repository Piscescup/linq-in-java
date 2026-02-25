package io.github.piscescup.linq.enumerable;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author REN YuanTong
 * @since 1.0.0
 */
public interface Groupable<K, E> {
    K getKey();

}

class ReadOnlyGroup<K, E> implements Groupable<K, E>, List<E> {
    private K key;
    private List<E> elements;

    ReadOnlyGroup(K key) {
        this.key = key;
        this.elements = new ArrayList<>();
    }

    ReadOnlyGroup(K key, List<E> elements) {
        this.key = key;
        this.elements = elements;
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return elements.iterator();
    }

    @Override
    public @NotNull Object[] toArray() {
        return elements.toArray();
    }

    @Override
    public @NotNull <T> T[] toArray(@NotNull T[] a) {
        return elements.toArray(a);
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return elements.retainAll(c);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E get(int index) {
        return elements.get(index);
    }

    @Override
    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return elements.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return elements.lastIndexOf(o);
    }

    @Override
    public @NotNull ListIterator<E> listIterator() {
        return elements.listIterator();
    }

    @Override
    public @NotNull ListIterator<E> listIterator(int index) {
        return elements.listIterator(index);
    }

    @Override
    public @NotNull List<E> subList(int fromIndex, int toIndex) {
        return elements.subList(fromIndex, toIndex);
    }

    @Override
    public K getKey() {
        return this.key;
    }
}
