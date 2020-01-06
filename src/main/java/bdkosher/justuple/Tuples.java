package bdkosher.justuple;

import java.util.*;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Factory methods for creating multiple Tuple instances from a Collections API type and vice versa.
 */
public abstract class Tuples {

    private Tuples() {
        /* prevent instantiation */
    }

    /**
     * Return the provided tuples as a Map whose keys correspond to the tuples' first members and whose values
     * correspond to the tuples' respective second members.
     * <p>
     * If there are at least two tuples that have identical first members (according to Object.equals(Object)), an
     * IllegalStateException will be thrown.
     *
     * @param tuples cannot be null but may be empty
     * @param <K>    the key type
     * @param <V>    the value type
     * @return a map whose size is equal to the number of tuples passed in
     * @throws IllegalStateException if there are multiple tuples with equal first members
     */
    public static <K, V> Map<K, V> map(Iterable<Tuple<K, V>> tuples) {
        return map(StreamSupport.stream(tuples.spliterator(), false));
    }

    /**
     * Return the provided tuples as a Map whose keys correspond to the tuples' first members and whose values
     * correspond to the tuples' respective second members.
     * <p>
     * If there are at least two tuples that have identical first members (according to Object.equals(Object)), an
     * IllegalStateException will be thrown.
     *
     * @param tuples cannot be null but may be empty
     * @param <K>    the key type
     * @param <V>    the value type
     * @return a map whose size is equal to the number of tuples passed in
     * @throws IllegalStateException if there are multiple tuples with equal first members
     */
    public static <K, V> Map<K, V> map(Collection<Tuple<K, V>> tuples) {
        return map(tuples.stream());
    }

    /**
     * Return the provided tuples as a Map whose keys correspond to the tuples' first members and whose values
     * correspond to the tuples' respective second members.
     * <p>
     * If there are at least two tuples that have identical first members (according to Object.equals(Object)), an
     * IllegalStateException will be thrown.
     *
     * @param tuples cannot be null but may be empty
     * @param <K>    the key type
     * @param <V>    the value type
     * @return a map whose size is equal to the number of tuples passed in
     * @throws IllegalStateException if there are multiple tuples with equal first members
     */
    @SafeVarargs
    public static <K, V> Map<K, V> map(Tuple<K, V>... tuples) {
        return map(Arrays.stream(tuples));
    }

    /**
     * Return the provided tuples as a Map whose keys correspond to the tuples' first members and whose values
     * correspond to the tuples' respective second members.
     * <p>
     * If there are at least two tuples that have identical first members (according to Object.equals(Object)), an
     * IllegalStateException will be thrown.
     *
     * @param tuples cannot be null but may be empty. The Stream will be consumed by this method.
     * @param <K>    the key type
     * @param <V>    the value type
     * @return a map whose size is equal to the number of tuples passed in
     * @throws IllegalStateException if there are multiple tuples with equal first members
     */
    public static <K, V> Map<K, V> map(Stream<Tuple<K, V>> tuples) {
        return tuples.collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
    }

    /**
     * Return the provided tuples as a Map with keys corresponding to the unique first members that exist for all
     * provided tuples. The values of the Map are all tuples' second members who share that first member value. For
     * example, if tuples (1,1) and (1,2) were provided as arguments, the returned map would have one entry with key=1
     * and a value that is the list [1,2].
     *
     * @param tuples cannot be null but may be empty
     * @param <K>    the key type
     * @param <V>    the value type
     * @return a map whose size is equal to the number of unique first member values among all provided tuples
     */
    public static <K, V> Map<K, List<V>> mapAll(Iterable<Tuple<K, V>> tuples) {
        return mapAll(StreamSupport.stream(tuples.spliterator(), false));
    }

    /**
     * Return the provided tuples as a Map with keys corresponding to the unique first members that exist for all
     * provided tuples. The values of the Map are all tuples' second members who share that first member value. For
     * example, if tuples (1,1) and (1,2) were provided as arguments, the returned map would have one entry with key=1
     * and a value that is the list [1,2].
     *
     * @param tuples cannot be null but may be empty
     * @param <K>    the key type
     * @param <V>    the value type
     * @return a map whose size is equal to the number of unique first member values among all provided tuples
     */
    public static <K, V> Map<K, List<V>> mapAll(Collection<Tuple<K, V>> tuples) {
        return mapAll(tuples.stream());
    }

    /**
     * Return the provided tuples as a Map with keys corresponding to the unique first members that exist for all
     * provided tuples. The values of the Map are all tuples' second members who share that first member value. For
     * example, if tuples (1,1) and (1,2) were provided as arguments, the returned map would have one entry with key=1
     * and a value that is the list [1,2].
     *
     * @param tuples cannot be null but may be empty
     * @param <K>    the key type
     * @param <V>    the value type
     * @return a map whose size is equal to the number of unique first member values among all provided tuples
     */
    @SafeVarargs
    public static <K, V> Map<K, List<V>> mapAll(Tuple<K, V>... tuples) {
        return mapAll(Arrays.stream(tuples));
    }

    /**
     * Return the provided tuples as a Map with keys corresponding to the unique first members that exist for all
     * provided tuples. The values of the Map are all tuples' second members who share that first member value. For
     * example, if tuples (1,1) and (1,2) were provided as arguments, the returned map would have one entry with key=1
     * and a value that is the list [1,2].
     *
     * @param tuples cannot be null but may be empty. The Stream will be consumed by this method.
     * @param <K>    the key type
     * @param <V>    the value type
     * @return a map whose size is equal to the number of unique first member values among all provided tuples
     */
    public static <K, V> Map<K, List<V>> mapAll(Stream<Tuple<K, V>> tuples) {
        return unboxOptionalKeys(tuples.collect(mapOfFirstToListOfSecondCollector()));
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
     * Null keys and values are supported.
     *
     * @param map cannot be null but may be empty
     * @param <U> the key type and Tuple's first member type
     * @param <V> the value type and Tuple's second member type
     * @return a potentially empty Set of Tuple instances corresponding to the provided Map entries.
     */
    public static <U, V> Set<Tuple<U, V>> from(Map<U, V> map) {
        return map.entrySet().stream()
                .map(Tuple::of)
                .collect(Collectors.toSet());
    }

    /**
     * Return a List of tuples containing all pairs of items available from the provided Iterable.
     * <p>
     * Every pair of adjacent items are combined into one Tuple.
     * <p>
     * If the input has an even number of elements, the returned list of Tuples is half the size of input.
     * <p>
     * If the input has an odd number of elements, the returned list is half the size of input plus one, with
     * the final tuple in the returned list having a null second member.
     *
     * @param items cannot be null but may be empty
     * @param <S>   the type of elements emitted by the Iterable's iterator and the type of the Tuples' members
     * @return a potentially empty Set of Tuple instances corresponding to the provided Map entries.
     */
    public static <S> List<Tuple<S, S>> from(Iterable<S> items) {
        return from(StreamSupport.stream(items.spliterator(), false));
    }

    /**
     * Return a List of tuples containing all pairs of items available from the provided List.
     * <p>
     * Every pair of adjacent items are combined into one Tuple.
     * <p>
     * If the input has an even number of elements, the returned list of Tuples is half the size of input.
     * <p>
     * If the input has an odd number of elements, the returned list is half the size of input plus one, with
     * the final tuple in the returned list having a null second member.
     *
     * @param items cannot be null but may be empty
     * @param <S>   the type of elements contained within the List and the type of the Tuples' members
     * @return a potentially empty Set of Tuple instances corresponding to the provided Map entries.
     */
    public static <S> List<Tuple<S, S>> from(List<S> items) {
        return from(items.stream());
    }

    /**
     * Return a List of tuples containing all pairs of items available from the provided Stream.
     * <p>
     * Every pair of adjacent items are combined into one Tuple.
     * <p>
     * If the input has an even number of elements, the returned list of Tuples is half the size of input.
     * <p>
     * If the input has an odd number of elements, the returned list is half the size of input plus one, with
     * the final tuple in the returned list having a null second member.
     *
     * @param items cannot be null but may be empty. The Stream will be consumed by this method.
     * @param <S>   the type of elements emitted by the Stream and the type of the Tuples' members
     * @return a potentially empty Set of Tuple instances corresponding to the provided Map entries.
     */
    public static <S> List<Tuple<S, S>> from(Stream<S> items) {
        return items.collect(collector());
    }

    /**
     * Return a Collector to collect a stream of objects of type {@code S} into a list of tuples of type {@code S}.
     * <p>
     * Adjacent items in the Stream will be put into the same tuple. An odd number of items results in a final tuple
     * that has a {@code null} second member.
     * <p>
     * This collector may be used in a parallel stream although it is not recommended.
     *
     * @param <S> the type of elements emitted by the Stream and the type of the Tuples' members
     * @return a Collector which produces a List of Tuple instances
     */
    public static <S> Collector<S, List<Tuple<S, S>>, List<Tuple<S, S>>> collector() {
        return collector(ArrayList<Tuple<S, S>>::new); // explicit type params needed for OpenJDK 8 compilation
    }

    /**
     * Return a Collector to collect a stream of objects of type {@code S} into a list of tuples of type {@code S}.
     * The provided {@code Supplier} generates the List instance used to collect the tuples.
     * <p>
     * Adjacent items in the Stream will be put into the same tuple. An odd number of items results in a final tuple
     * that has a {@code null} second member.
     * <p>
     * This collector may be used in a parallel stream although it is not recommended.
     *
     * @param supplier supplies the List implementation that Tuples will be collected into
     * @param <S>      the type of elements emitted by the Stream and the type of the Tuples' members
     * @return a Collector which produces a List of Tuple instances
     */
    public static <S> Collector<S, List<Tuple<S, S>>, List<Tuple<S, S>>> collector(Supplier<List<Tuple<S, S>>> supplier) {
        return Collector.of(
                supplier,
                Tuples::accumulateItem,
                (list1, list2) -> combineTupleLists(list1, list2, supplier),
                UnaryOperator.identity(),
                Collector.Characteristics.IDENTITY_FINISH
        );
    }

    /*
     * If parallelization resulted in the list containing a tuple that is partially constructed, we need to rebuild
     * the tuples, e.g.
     *
     * [(1,2),(3,null)] + [(4,5),(6,7)] should combine into [(1,2),(3,4),(5,6),(7,null)]
     */
    private static <S> List<Tuple<S, S>> combineTupleLists(
            List<Tuple<S, S>> tuples,
            List<Tuple<S, S>> otherTuples,
            Supplier<List<Tuple<S, S>>> supplier) {

        List<Tuple<S, S>> combinedTuples = supplier.get();
        accumulateTuplesItems(combinedTuples, tuples);
        accumulateTuplesItems(combinedTuples, otherTuples);
        return combinedTuples;
    }

    private static <S> void accumulateTuplesItems(List<Tuple<S, S>> accumulation, List<Tuple<S, S>> itemsInTuples) {
        for (Tuple<S, S> tuple : itemsInTuples) {
            accumulateItem(accumulation, tuple.getFirst());
            if (!tuple.isPartial()) {
                accumulateItem(accumulation, tuple.getSecond());
            }
        }
    }

    /*
     * Append the item to the list of tuples, either by setting it as the second element of the final partial tuple
     * in the list, or constructing a new partial tuple from the item and appending that to the list. Because this
     * method is unaware if the provided item is the final item to be accumulated, this method will result in a
     * partial tuple being the final element of the list if there are an odd number of items accumulated ultimately.
     */
    private static <S> void accumulateItem(List<Tuple<S, S>> tuples, S item) {
        if (tuples.isEmpty()) {
            tuples.add(Tuple.partial(item));
        } else {
            int lastIndex = tuples.size() - 1;
            Tuple<S, S> tuple = tuples.get(lastIndex);
            if (tuple.isPartial()) {
                Tuple<S, S> combinedTuple = tuple.withSecond(item);
                tuples.set(lastIndex, combinedTuple);
            } else {
                tuples.add(Tuple.partial(item));
            }
        }
    }

    /**
     * Combines items within the single provided Tuple into a List of Tuples containing the individual items. The
     * size of the returned List is the size of the larger of the two members of the Tuple. If there is a difference in
     * size between the number of items in the provide Tuple, then the excess items will result in Tuples with
     * one @{code null} value.
     * <p>
     * For example, given a Tuple of a one-item List and a two-item List, a List of two Tuples will be returned, the
     * second of which will have a @{code null} first value and a non-null second value.
     *
     * @param tuple may not be null, nor can it have null values
     * @param <U>   the type of the Tuples first members
     * @param <V>   the type of the Tuples second members
     * @return a non-null but potentially empty List
     */
    public static <U, V> List<Tuple<U, V>> zip(Tuple<? extends Iterable<U>, ? extends Iterable<V>> tuple) {
        return zip(tuple.getFirst(), tuple.getSecond());
    }

    /**
     * Combines the items from the first argument with the items into the second argument into a List of Tuples. The
     * size of the returned List is the size of the larger of the two arguments. If there is a difference in
     * size between the two arguments, then the excess items will result in Tuples with one @{code null} value.
     * <p>
     * For example, given a List of one item as the first argument and a List of two items in the second, a List of
     * two Tuples will be returned, the second of which will have a @{code null} first value and a non-null second
     * value.
     *
     * @param firstItems  may not be null but can be empty
     * @param secondItems may not be null but can be empty
     * @param <U>         the type of the Tuples first members
     * @param <V>         the type of the Tuples second members
     * @return a non-null but potentially empty List
     */
    public static <U, V> List<Tuple<U, V>> zip(U[] firstItems, V[] secondItems) {
        return zip(Arrays.stream(firstItems), Arrays.stream(secondItems));
    }

    /**
     * Combines the items from the first argument with the items into the second argument into a List of Tuples. The
     * size of the returned List is the size of the larger of the two arguments. If there is a difference in
     * size between the two arguments, then the excess items will result in Tuples with one @{code null} value.
     * <p>
     * For example, given a List of one item as the first argument and a List of two items in the second, a List of
     * two Tuples will be returned, the second of which will have a @{code null} first value and a non-null second
     * value.
     *
     * @param firstItems  may not be null but can be empty
     * @param secondItems may not be null but can be empty
     * @param <U>         the type of the Tuples first members
     * @param <V>         the type of the Tuples second members
     * @return a non-null but potentially empty List
     */
    public static <U, V> List<Tuple<U, V>> zip(Iterable<U> firstItems, Iterable<V> secondItems) {
        return zip(firstItems.iterator(), secondItems.iterator());
    }

    /**
     * Combines the items from the first argument with the items into the second argument into a List of Tuples. The
     * size of the returned List is the size of the larger of the two arguments. If there is a difference in
     * size between the two arguments, then the excess items will result in Tuples with one @{code null} value.
     * <p>
     * For example, given a List of one item as the first argument and a List of two items in the second, a List of
     * two Tuples will be returned, the second of which will have a @{code null} first value and a non-null second
     * value.
     * <p>
     * This method will call terminal operations on both of the provided Streams.
     *
     * @param firstItems  may not be null but can be empty
     * @param secondItems may not be null but can be empty
     * @param <U>         the type of the Tuples first members
     * @param <V>         the type of the Tuples second members
     * @return a non-null but potentially empty List
     */
    public static <U, V> List<Tuple<U, V>> zip(Stream<U> firstItems, Stream<V> secondItems) {
        return zip(firstItems.iterator(), secondItems.iterator());
    }

    private static <U, V> List<Tuple<U, V>> zip(Iterator<U> firstItems, Iterator<V> secondItems) {
        TupleZipper<U, V> zipper = new TupleZipper<>(firstItems, secondItems);
        List<Tuple<U, V>> tuples = new LinkedList<>();
        while (zipper.hasNext()) {
            tuples.add(zipper.next());
        }
        return tuples;
    }

    /**
     * Extracts the individual items contained within the provided tuples and returns a single Tuple of those items.
     * The returned Tuple holds the items in a List that is ordered according to how the provided Tuples are ordered.
     * <p>
     * Tuples containing {@code null} values are supported.
     *
     * @param tuples zero or more Tuples
     * @param <U>    the type of items in the returned Tuple's first member
     * @param <V>the type of items in the returned Tuple's second member
     * @return a single Tuple whose members are Lists of the items found in the provided Tuples
     */
    @SafeVarargs
    public static <U, V> Tuple<List<U>, List<V>> unzip(Tuple<U, V>... tuples) {
        return unzip(Arrays.stream(tuples));
    }

    /**
     * Extracts the individual items contained within the provided tuples and returns a single Tuple of those items.
     * The returned Tuple holds the items in a List that is ordered according to how the provided Tuples are ordered.
     * <p>
     * Tuples containing {@code null} values are supported.
     *
     * @param tuples a non-null Iterable of zero or more Tuples
     * @param <U>    the type of items in the returned Tuple's first member
     * @param <V>the type of items in the returned Tuple's second member
     * @return a single Tuple whose members are Lists of the items found in the provided Tuples
     */
    public static <U, V> Tuple<List<U>, List<V>> unzip(Iterable<Tuple<U, V>> tuples) {
        return unzip(tuples.iterator());
    }

    /**
     * Extracts the individual items contained within the provided tuples and returns a single Tuple of those items.
     * The returned Tuple holds the items in a List that is ordered according to how the provided Tuples are ordered.
     * <p>
     * Tuples containing {@code null} values are supported.
     * <p>
     * This method will call a terminal operation on the provided Stream.
     *
     * @param tuples a non-null Stream of zero or more Tuples
     * @param <U>    the type of items in the returned Tuple's first member
     * @param <V>the type of items in the returned Tuple's second member
     * @return a single Tuple whose members are Lists of the items found in the provided Tuples
     */
    public static <U, V> Tuple<List<U>, List<V>> unzip(Stream<Tuple<U, V>> tuples) {
        return unzip(tuples.iterator());
    }

    private static <U, V> Tuple<List<U>, List<V>> unzip(Iterator<Tuple<U, V>> tuples) {
        List<U> first = new LinkedList<>();
        List<V> second = new LinkedList<>();
        while (tuples.hasNext()) {
            Tuple<U, V> tuple = tuples.next();
            first.add(tuple.getFirst());
            second.add(tuple.getSecond());
        }
        return Tuple.of(first, second);
    }

    private static class TupleZipper<U, V> implements Iterator<Tuple<U, V>> {

        private final Iterator<U> firstIterator;
        private final Iterator<V> secondIterator;

        TupleZipper(Iterator<U> firstIterator, Iterator<V> secondIterator) {
            this.firstIterator = Objects.requireNonNull(firstIterator, "First Items cannot be null.");
            this.secondIterator = Objects.requireNonNull(secondIterator, "Second Items cannot be null.");
        }

        @Override
        public boolean hasNext() {
            return firstIterator.hasNext() || secondIterator.hasNext();
        }

        U nextFirst() {
            return next(firstIterator);
        }

        V nextSecond() {
            return next(secondIterator);
        }

        private static <T> T next(Iterator<T> iterator) {
            return iterator.hasNext() ? iterator.next() : null;
        }

        @Override
        public Tuple<U, V> next() {
            return Tuple.of(nextFirst(), nextSecond());
        }
    }
}
