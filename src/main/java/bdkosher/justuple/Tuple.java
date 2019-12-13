package bdkosher.justuple;

import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    public U getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }

    /**
     * Returns a Map representation of this Tuple. The Map will have a single-entry. If the tuple's first member is
     * null then the Map's sole key will also be null.
     */
    public Map<U, V> toMap() {
        return new SingleEntryMap<>(toMapEntry());
    }

    /**
     * Returns a Map.Entry implementation of this tuple. If the tuple's first member is null, the entry's key will
     * also be null.
     */
    public Map.Entry<U, V> toMapEntry() {
        return new SimpleEntry<>(first, second);
    }

    /**
     * Returns the tuple as two-element list.
     *
     * @return non-null fixed-size list.
     */
    public List<Object> toList() {
        return new DualItemList<>(first, second);
    }

    /**
     * This method is identical to the toList method except that it is parameterized type is set to U, the type
     * of the first tuple member. This method will throw an exception if the second member is not also of type U.
     * In other words, if the Tuple contains members of the same type, this method should be preferred over toList.
     * Otherwise, do not use this method.
     *
     * @return a list of the tuple's first and second members
     * @throws IllegalStateException if the tuple's members' both cannot be represented as type U.
     */
    @SuppressWarnings("unchecked")
    public List<U> toTypedList() {
        if (first == null && second == null
                || first != null && second != null && second.getClass().isAssignableFrom(first.getClass())) {
            return (List<U>) toList();
        }
        throw new IllegalStateException("Tuple members do not share a mutually-compatible type.");
    }

    /**
     * Returns a new tuple with the members in reversed order: the first element is second and the second is first.
     *
     * @return a new Tuple instance
     */
    public Tuple<V, U> swapped() {
        return of(second, first);
    }

    /**
     * Returns a new Tuple with the given value for the first member and the current second member unchanged.
     *
     * @param value may be null
     * @return a new Tuple
     */
    public Tuple<U, V> withFirst(U value) {
        return of(value, second);
    }

    /**
     * Returns a new Tuple with the given value for the second member and the current first member unchanged.
     *
     * @param value may be null
     * @return a new Tuple
     */
    public Tuple<U, V> withSecond(V value) {
        return of(first, value);
    }

    /**
     * Constructs a new Tuple from the given values. If the values are null and/or Serializable, then the returned
     * Tuple will also be Serializable.
     *
     * @param first  may be null.
     * @param second may be null.
     * @return a Tuple of the two arguments.
     */
    public static <U, V> Tuple<U, V> of(U first, V second) {
        if (SerializableTuple.areSerializable(first, second)) {
            return new SerializableTuple<>(first, second);
        }
        return new Tuple<>(first, second);
    }

    /**
     * Constructs a new Tuple from the given List. If the List contains no elements, the returned Tuple has null
     * members. If the List only has one element, the returned Tuple's second member is null. If the List has
     * more than two elements, only the first two will be present in the Tuple; the other elements are ignored.
     *
     * @param iterable may not be null but may be empty.
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
     * Constructs a new Tuple from the given Map Entry.
     *
     * @param entry may not be null.
     * @return a Tuple
     */
    public static <U, V> Tuple<U, V> of(Map.Entry<U, V> entry) {
        return of(entry.getKey(), entry.getValue());
    }

    /**
     * Returns the provided tuples as a Map with keys corresponding to the tuples' first members and the values
     * corresponding to the tuples' second members.
     * <p>
     * If there are at least two tuples that have identical first members (according to Object.equals(Object)), an
     * IllegalStateException will be thrown.
     *
     * @param tuples cannot be null but may be empty
     * @return a map whose size is equal to the number of tuples passed in
     * @throws IllegalStateException if there are multiple tuples with equal first members
     */
    public static <K, V, C extends Collection<Tuple<K, V>>> Map<K, V> map(C tuples) {
        return tuples.stream()
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
    }

    /**
     * Returns the provided tuples as a Map with keys corresponding to the tuples' first members and the values
     * corresponding to the tuples' second members.
     * <p>
     * If there are at least two tuples that have identical first members (according to Object.equals(Object)), an
     * IllegalStateException will be thrown.
     *
     * @param tuples cannot be null but may be empty
     * @return a map whose size is equal to the number of tuples passed in
     * @throws IllegalStateException if there are multiple tuples with equal first members
     */
    @SafeVarargs
    public static <K, V> Map<K, V> map(Tuple<K, V>... tuples) {
        return Arrays.stream(tuples)
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
    }

    /**
     * Returns the provided tuples as a Map with keys corresponding to the unique first members that exist for all
     * provided tuples. The values of the Map are all tuples' second members who share that first member value. For
     * example, if tuples (1,1) and (1,2) were provided as arguments, the returned map would have one entry with key=1
     * and a value that is the list [1,2].
     *
     * @param tuples cannot be null but may be empty
     * @return a map whose size is equal to the number of unique first member values among all provided tuples
     */
    public static <K, V, C extends Collection<Tuple<K, V>>> Map<K, List<V>> mapAll(C tuples) {
        return unboxOptionalKeys(
                tuples.stream().collect(mapOfFirstToListOfSecondCollector())
        );
    }

    /**
     * Returns the provided tuples as a Map with keys corresponding to the unique first members that exist for all
     * provided tuples. The values of the Map are all tuples' second members who share that first member value. For
     * example, if tuples (1,1) and (1,2) were provided as arguments, the returned map would have one entry with key=1
     * and a value that is the list [1,2].
     *
     * @param tuples cannot be null but may be empty
     * @return a map whose size is equal to the number of unique first member values among all provided tuples
     */
    public static <K, V> Map<K, List<V>> mapAll(Tuple<K, V>... tuples) {
        return unboxOptionalKeys(
                Arrays.stream(tuples).collect(mapOfFirstToListOfSecondCollector())
        );
    }

    /*
     * groupingBy does not support null keys, so we box nulls in an Optional and unbox them with this method.
     * The Map implementation we use in this method must handle null keys.
     */
    private static <K, V> Map<K, V> unboxOptionalKeys(Map<Optional<K>, V> map) {
        Map<K, V> nullKeySafeMap = new HashMap<>();
        map.forEach((key, value) -> nullKeySafeMap.put(key.orElse(null), value));
        return nullKeySafeMap;
    }

    private static <K, V> Collector<Tuple<K, V>, ?, Map<Optional<K>, List<V>>> mapOfFirstToListOfSecondCollector() {
        return Collectors.groupingBy(
                tuple -> Optional.ofNullable(tuple.getFirst()),
                Collectors.mapping(Tuple::getSecond, Collectors.toList())
        );
    }

    /**
     * Returns a Set of Tuple instances derived from the provided Map.
     *
     * @param map cannot be null
     * @param <U> the key type
     * @param <V> the value type
     * @return a potentially empty Set of Tuple instances corresponding to the provided Map entries.
     */
    public static <U, V> Set<Tuple<U, V>> toTuples(Map<U, V> map) {
        return map.entrySet().stream()
                .map(Tuple::of)
                .collect(Collectors.toSet());
    }

    /**
     * Returns a List of tuples.
     * <p>
     * Every pair of adjacent non-null elements from the iterable are combined into a Tuple. Null elements are ignored.
     * <p>
     * If the input has an even number of non-null elements, the returned list of Tuples is half the size of input
     * .
     * If the input has an odd number of non-null elements, the returned list is half the size of input plus one, with
     * the final tuple in the returned list having a null second member.
     *
     * @param iterable cannot be null
     * @param <S>      list elements' type
     * @return a potentially empty Set of Tuple instances corresponding to the provided Map entries.
     */
    public static <S> List<Tuple<S, S>> toTuples(Iterable<S> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .filter(Objects::nonNull)
                .collect(tupleCollector());
    }

    /**
     * Returns a Collector which will collect a stream of objects of type S into a List of Tuples of type S. Adjacent
     * items in the Stream will be put into the same Tuple. An odd number of items results in a final Tuple that
     * has a null second member.
     * <p>
     * This collector may be used in a parallel stream although it is not recommended.
     * <p>
     * This collector is not suitable for use with Streams emitting null items and will throw a NullPointerException.
     *
     * @throws NullPointerException if the Stream emits a null item
     */
    public static <S> Collector<S, List<Tuple<S, S>>, List<Tuple<S, S>>> tupleCollector() {
        return tupleCollector(ArrayList<Tuple<S, S>>::new); // type params needed for OpenJDK compilation
    }

    /**
     * Returns a Collector which will collect a stream of objects of type S into a List of Tuples of type S. Adjacent
     * items in the Stream will be put into the same Tuple. An odd number of items results in a final Tuple that
     * has a null second member.
     * <p>
     * This collector may be used in a parallel stream although it is not recommended.
     * <p>
     * This collector is not suitable for use with Streams emitting null items and will throw a NullPointerException.
     *
     * @param supplier supplies the List to which the Tuples are to be collected into
     * @throws NullPointerException if the Stream emits a null item
     */
    public static <S> Collector<S, List<Tuple<S, S>>, List<Tuple<S, S>>> tupleCollector(Supplier<List<Tuple<S, S>>> supplier) {
        return Collector.of(
                supplier,
                Tuple::accumulateItemInTupleList,
                Tuple::combineTupleLists,
                UnaryOperator.identity(),
                Collector.Characteristics.IDENTITY_FINISH
        );
    }

    /*
     * For use in collector generator. Assumes that Tuple items are non-null since null is used to mark where Tuple
     * generation is incomplete.
     *
     * If the parallelization was split so that the final tuple has a null second member, we need to rebuild the tuples:
     *
     * [(1,2),(3,null)] + [(4,5),(6,7)] should combine into [(1,2),(3,4),(5,6),(7,null)]
     *
     * Likewise, if the list of other tuples begins with a tuple with first member null...
     *
     * [(1,2),(3,4)] + [(null, 5),(6,7)] should combine into [(1,2),(3,4),(5,6),(7,null)]
     */
    private static <S> List<Tuple<S, S>> combineTupleLists(List<Tuple<S, S>> tuples, List<Tuple<S, S>> otherTuples) {
        otherTuples.stream().forEachOrdered(tuple -> {
            if (tuple.getFirst() != null) {
                accumulateItemInTupleList(tuples, tuple.getFirst());
            }
            if (tuple.getSecond() != null) {
                accumulateItemInTupleList(tuples, tuple.getSecond());
            }
        });
        return tuples;
    }

    /*
     * null is used to mark in-progress tuples, therefore we cannot accept null items
     */
    private static <S> void accumulateItemInTupleList(List<Tuple<S, S>> tuples, S item) {
        Objects.requireNonNull(item, "Null items not allowed. Consider filtering them from Stream.");
        if (tuples.isEmpty()) {
            tuples.add(Tuple.of(item, null));
        } else {
            int lastIndex = tuples.size() - 1;
            Tuple<S, S> tuple = tuples.get(lastIndex);
            if (tuple.getSecond() == null) {
                tuples.set(lastIndex, tuple.withSecond(item));
            } else {
                tuples.add(Tuple.of(item, null));
            }
        }
    }

    /**
     * Equals when the object is the same instance or the first and second values within the tuple equal.
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
     * Compares the tuple according to its first and second members in that order. If the member types are not
     * Comparable, then a ClassCastException will be thrown.
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