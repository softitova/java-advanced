package ru.ifmo.ctddev.titova.arrayset;

import java.util.*;


public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

    private List<E> list = null;
    private Comparator<E> comparator = null;

    public ArraySet() {
        this(Collections.emptyList(), null );
    }

    public ArraySet(Comparator<E> comparator) {
        list = Collections.emptyList();
        this.comparator = comparator;
    }

    public ArraySet(Collection<E> collection, Comparator<E> comparator) {
        this(collection, comparator, false);
    }

    private ArraySet(Collection<E> collection, Comparator<E> comparator, boolean isSorted) {
        this.comparator = comparator;
        if (isSorted) {
            this.list = (List<E>) collection;
        } else {
            TreeSet<E> treeSet = new TreeSet<E>(comparator);
            treeSet.addAll(collection);
            list = new ArrayList<E>(treeSet);
        }
    }

    public ArraySet(Collection<E> collection) {
        this(collection, null);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public Iterator<E> iterator() {
        return Collections.unmodifiableList(list).iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(list, o, (Comparator<Object>) comparator) >= 0;
    }

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return list.get(size() - 1);
    }

    private int findHeadIndex(E element, boolean inclusive) {
        int index = Collections.binarySearch(list, element, comparator);
        return index < 0 ? ~index : !inclusive ? ++index : index;
    }

    private int findTailIndex(E element, boolean inclusive) {
        int index = Collections.binarySearch(list, element, comparator);
        return index < 0 ? ~index - 1 : !inclusive ? --index : index;
    }

    @Override
    public E lower(E e) {
        int index = findTailIndex(e, false);
        return index >= 0 ? list.get(index) : null;
    }

    @Override
    public E floor(E e) {
        int index = findTailIndex(e, true);
        return index >= 0 ? list.get(index) : null;
    }

    @Override
    public E ceiling(E e) {
        int index = findHeadIndex(e, true);
        return index < list.size() ? list.get(index) : null;
    }

    @Override
    public E higher(E e) {
        int index = findHeadIndex(e, false);
        return index < list.size() ? list.get(index) : null;
    }


    @Override
    public E pollLast() {
        throw new UnsupportedOperationException("pollLast is unsupported opperation");
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException("pollFirst is unsupported opperation");
    }

    private E get(int index) {
        if (index >= size() || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        return list.get(index);
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int fromI = findHeadIndex(fromElement, fromInclusive);
        int toI = findTailIndex(toElement, toInclusive) + 1;
        if (toI + 1 == fromI) {
            toI = fromI;
        }
        return new ArraySet<>(list.subList(fromI, toI), comparator, true);
    }
//java -cp java-advanced-2017/lib/*:java-advanced-2017/artifacts/ArraySetTest.jar:untitled/out/production/HW2 info.kgeorgiy.java.advanced.arrayset.Tester NavigableSet ru.ifmo.ctddev.titova.arrayset.ArraySe

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        int index = findTailIndex(toElement, inclusive) + 1;
        return new ArraySet<>(list.subList(0, index), comparator, true);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        int index = findHeadIndex(fromElement, inclusive);
        return new ArraySet<>(list.subList(index, list.size()), comparator, true);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }


    @Override
    public NavigableSet<E> descendingSet() {
        return new ArraySet<>(new DescendingList<>(list), Collections.reverseOrder(comparator), true);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return new DescendingList<>(list).iterator();
    }

    private class DescendingList<T> extends AbstractList<T> implements RandomAccess {

        private final List<T> destList;
        private final boolean rev;

        DescendingList(List<T> list) {

            if (!(list instanceof RandomAccess)) {
                throw new IllegalArgumentException("must be random access");
            }
            if (list instanceof DescendingList) {
                DescendingList<T> t = (DescendingList<T>) list;
                this.destList = t.destList;
                rev = !t.rev;
            } else {
                this.destList = list;
                rev = false;
            }
        }

        @Override
        public int size() {
            return destList.size();
        }

        @Override
        public T get(int index) {
            return rev ? destList.get(index)
                    : destList.get(size() - index - 1);
        }

    }
}
