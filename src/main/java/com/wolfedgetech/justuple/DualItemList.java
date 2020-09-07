package com.wolfedgetech.justuple;

import java.util.AbstractList;

/**
 * A list containing exactly two items, one of both or which may be null. Regardless of the null-ness of the elements,
 * the size will always be 2.
 *
 * @param <T> the type of the element contained within the List
 */
class DualItemList<T> extends AbstractList<T> {

    private static final int SIZE = 2;

    private final T first;
    private final T second;

    DualItemList(T first, T second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index > 1) {
            throw new ArrayIndexOutOfBoundsException(index + " is outside range of 0 to " + SIZE + "exclusive.");
        }
        return index == 0 ? first : second;
    }
}
