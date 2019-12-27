package bdkosher.justuple;

import java.io.Serializable;
import java.util.*;

/**
 * An ordered pair of potentially null references, not necessarily of the same type. Immutable and thread-safe.
 *
 * @param <U> type of the first tuple member
 * @param <V> type of the second tuple member
 * @author Joe Wolf
 */
public class Tuple<U, V> implements Comparable<Tuple<U, V>> {

    private final U first;
    private final V second;

    private Tuple(U first, V second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Return a Tuple of the given values. If the values are {@code null} and/or {@code Serializable}, then the
     * returned Tuple will also be {@code Serializable}.
     *
     * @param first  may be null.
     * @param second may be null.
     * @param <U>    the type of the first member
     * @param <V>    the type of the second member, which may be identical to the type of the first
     * @return a Tuple of the two arguments.
     */
    public static <U, V> Tuple<U, V> of(U first, V second) {
        if (SerializableTuple.areSerializable(first, second)) {
            return new SerializableTuple<>(first, second);
        }
        return new Tuple<>(first, second);
    }

    /**
     * Return a Tuple of the first two elements available. If no elements are available, the Tuple will have
     * {@code null} members. If only one element is available, the second member alone will be null. If there are
     * more than two elements available, they are ignored and not consumed by this method.
     *
     * @param iterable may not be null but may be empty.
     * @param <S>      the type of object emitted by the iterable Tuples' members
     * @return a Tuple
     */
    public static <S> Tuple<S, S> of(Iterable<S> iterable) {
        Iterator<S> iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            return of(null, null);
        }

        S first = iterator.next();
        S second = iterator.hasNext() ? iterator.next() : null;
        return of(first, second);
    }

    /**
     * Return a Tuple from the given {@code java.util.Map.Entry}.
     *
     * @param entry may not be null.
     * @param <U> the key type of the entry and Tuple's first member
     * @param <V> the value type of the entry and Tuple's second member
     * @return a Tuple
     */
    public static <U, V> Tuple<U, V> of(Map.Entry<U, V> entry) {
        return of(entry.getKey(), entry.getValue());
    }

    public U getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    /**
     * Compare the tuple according to its first and second members in that order. If either of the member types are not
     * instances of {@code Comparable}, a {@code ClassCastException} will be thrown.
     *
     * @param other the tuple to compare this tuple with
     * @return the comparison result according to the contract of {@link java.lang.Comparable}
     * @throws ClassCastException if the Tuple's members do not implement Comparable
     */
    @Override
    public int compareTo(Tuple<U, V> other) {
        if (other == null) {
            return 1;
        }
        int firstComparison = compareNullsFirst(first, other.first);
        return firstComparison != 0 ? firstComparison : compareNullsFirst(second, other.second);
    }

    private static <T> int compareNullsFirst(T thisValue, T otherValue) {
        if (thisValue == null) {
            return otherValue == null ? 0 : -1;
        } else if (otherValue == null) {
            return 1;
        }
        // is it better to fallback compare-on-hashCode or allow ClassCastException?
        return ((Comparable<T>) thisValue).compareTo(otherValue);
    }

    /**
     * Return a {@code java.util. Map} representation of this tuple. The Map will have a single-entry even if the
     * tuple's first member is {@code null}.
     *
     * @return a single-entry Map
     */
    public Map<U, V> toMap() {
        return new SingleEntryMap<>(toMapEntry());
    }

    /**
     * Return a {@code java.util.Map.Entry} representation of this tuple. If the tuple's first member is null, the
     * entry's key will be null.
     *
     * @return a Map entry
     */
    public Map.Entry<U, V> toMapEntry() {
        return new SimpleEntry<>(first, second);
    }

    /**
     * Return the tuple as two-element list.
     *
     * @return non-null fixed-size list.
     */
    public List<Object> toList() {
        return new DualItemList<>(first, second);
    }

    /**
     * This method is identical to the {@code toList} method except that its parameterized type is {@code U}, the type
     * of the first tuple member. This method throws an exception if the second member is not also of type {@code U}.
     * If the Tuple contains members of the same type, this method should be preferred over {@code toList}.
     * Otherwise, do not use this method.
     *
     * @return a list of the tuple's first and second members
     * @throws IllegalStateException if the tuple's members' both cannot be represented as type U.
     */
    @SuppressWarnings("unchecked")
    public List<U> toTypedList() {
        if (first == null && second == null ||
                first != null && second != null && second.getClass().isAssignableFrom(first.getClass())) {
            return (List<U>) toList();
        }
        throw new IllegalStateException("Tuple members do not share a mutually-compatible type.");
    }

    /**
     * Return a new tuple with its members in reversed order; the first element is second and the second is first.
     *
     * @return a new Tuple instance
     */
    public Tuple<V, U> swapped() {
        return of(second, first);
    }

    /**
     * Return a new Tuple with the given value for the first member. The second member will be this tuple's current
     * value. This tuple will remain unchanged.
     *
     * @param value may be null
     * @return a new Tuple
     */
    public Tuple<U, V> withFirst(U value) {
        return of(value, second);
    }

    /**
     * Return a new Tuple with the given value for the second member. The second member will be this tuple's current
     * value. This tuple will remain unchanged.
     *
     * @param value may be null
     * @return a new Tuple
     */
    public Tuple<U, V> withSecond(V value) {
        return of(first, value);
    }

    /**
     * Return true when the object is the same instance or the first and second values within the tuple equal.
     *
     * @param o may be null
     * @return true if the tuples are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(first, tuple.first) &&
                Objects.equals(second, tuple.second);
    }

    /**
     * Return a hash code based on the hashes for the first and second member values.
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    /**
     * Formatted as "({first}, {second})"
     */
    @Override
    public String toString() {
        return "(" + first + ", " + second + ')';
    }

    /**
     * A serializable version of Tuple in case the Tuple's ability to be Serialized happens to be important to
     * someone somewhere for some reason.
     *
     * @param <U> not constrained to be serializable for the sake of implementation ease.
     * @param <V>
     */
    private static class SerializableTuple<U, V> extends Tuple<U, V> implements Serializable {
        private static final long serialVersionUID = 20191210;

        SerializableTuple(U first, V second) {
            super(first, second);
        }

        @Override
        public Tuple<V, U> swapped() {
            return new SerializableTuple<>(getSecond(), getFirst());
        }

        private static boolean areSerializable(Object first, Object second) {
            return isSerializable(first) && isSerializable(second);
        }

        private static boolean isSerializable(Object o) {
            return o == null || o instanceof Serializable;
        }
    }

    private static class SimpleEntry<U, V> implements Map.Entry<U, V> {

        private final U key;
        private V value;

        SimpleEntry(U key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public U getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            V original = this.value;
            this.value = value;
            return original;
        }
    }

    private static class DualItemList<T> extends AbstractList<T> {
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

    private static class SingleEntryMap<U, V> extends AbstractMap<U, V> {

        private final Map.Entry<U, V> tupleEntry;

        SingleEntryMap(Map.Entry<U, V> tupleEntry) {
            this.tupleEntry = tupleEntry;
        }

        @Override
        public Set<Entry<U, V>> entrySet() {
            return Collections.singleton(tupleEntry);
        }
    }
}