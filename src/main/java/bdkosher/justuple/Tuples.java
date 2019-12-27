package bdkosher.justuple;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Factory methods for creating Tuple instances.
 */
public abstract class Tuples {

    private Tuples() {
    }

    /**
     * Return the given tuples as a Map whose keys correspond to the tuples' first members and whose values
     * correspond to the tuples' respective second members.
     * <p>
     * If there are at least two tuples that have identical first members (according to Object.equals(Object)), an
     * IllegalStateException will be thrown.
     *
     * @param tuples cannot be null but may be empty
     * @return a map whose size is equal to the number of tuples passed in
     * @throws IllegalStateException if there are multiple tuples with equal first members
     */
    public static <K, V> Map<K, V> map(Iterable<Tuple<K, V>> tuples) {
        return StreamSupport.stream(tuples.spliterator(), false)
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
    }

    /**
     * Return the given tuples as a Map whose keys correspond to the tuples' first members and whose values
     * correspond to the tuples' respective second members.
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
     * Return the provided tuples as a Map with keys corresponding to the unique first members that exist for all
     * provided tuples. The values of the Map are all tuples' second members who share that first member value. For
     * example, if tuples (1,1) and (1,2) were provided as arguments, the returned map would have one entry with key=1
     * and a value that is the list [1,2].
     *
     * @param tuples cannot be null but may be empty
     * @return a map whose size is equal to the number of unique first member values among all provided tuples
     */
    public static <K, V> Map<K, List<V>> mapAll(Iterable<Tuple<K, V>> tuples) {
        return unboxOptionalKeys(
                StreamSupport.stream(tuples.spliterator(), false)
                        .collect(mapOfFirstToListOfSecondCollector())
        );
    }

    /**
     * Return the provided tuples as a Map with keys corresponding to the unique first members that exist for all
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
     * Return a Set of tuples derived from the provided Map. Each Map entry corresponds to exactly one tuple instance.
     * Null Map keys and values are supported.
     *
     * @param map cannot be null
     * @param <U> the key type
     * @param <V> the value type
     * @return a potentially empty Set of Tuple instances corresponding to the provided Map entries.
     */
    public static <U, V> Set<Tuple<U, V>> from(Map<U, V> map) {
        return map.entrySet().stream()
                .map(Tuple::of)
                .collect(Collectors.toSet());
    }

    /**
     * Return a list of tuples containing all non-null elements available from the iterable.
     * <p>
     * Every pair of adjacent non-null elements are combined into a Tuple. Null elements are ignored.
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
    public static <S> List<Tuple<S, S>> from(Iterable<S> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .filter(Objects::nonNull)
                .collect(collector());
    }

    /**
     * Return a Collector to collect a stream of objects of type {@code S} into a list of tuples of type {@code S}.
     * <p>
     * Adjacent items in the Stream will be put into the same tuple. An odd number of items results in a final tuple
     * that has a {@code null} second member.
     * <p>
     * This collector may be used in a parallel stream although it is not recommended.
     * <p>
     * This collector is not suitable for use with Streams emitting {@code null} items. Such streams will cause a
     * {@code NullPointerException} to be thrown.
     *
     * @throws NullPointerException if the Stream emits a null item
     */
    public static <S> Collector<S, List<Tuple<S, S>>, List<Tuple<S, S>>> collector() {
        return collector(ArrayList<Tuple<S, S>>::new); // type params needed for OpenJDK compilation
    }

    /**
     * Return a Collector to collect a stream of objects of type {@code S} into a list of tuples of type {@code S}.
     * The provided {@code Supplier} generates the List instance used to collect the tuples.
     * <p>
     * Adjacent items in the Stream will be put into the same tuple. An odd number of items results in a final tuple
     * that has a {@code null} second member.
     * <p>
     * This collector may be used in a parallel stream although it is not recommended.
     * <p>
     * This collector is not suitable for use with Streams emitting {@code null} items. Such streams will cause a
     * {@code NullPointerException} to be thrown.
     *
     * @param supplier supplies the List implementation that Tuples will be collected into
     * @throws NullPointerException if the Stream emits a null item
     */
    public static <S> Collector<S, List<Tuple<S, S>>, List<Tuple<S, S>>> collector(Supplier<List<Tuple<S, S>>> supplier) {
        return Collector.of(
                supplier,
                Tuples::accumulateItemInTupleList,
                Tuples::combineTupleLists,
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
        for (Tuple<S, S> otherTuple : otherTuples) {
            if (otherTuple.getFirst() != null) {
                accumulateItemInTupleList(tuples, otherTuple.getFirst());
            }
            if (otherTuple.getSecond() != null) {
                accumulateItemInTupleList(tuples, otherTuple.getSecond());
            }
        }
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
}
