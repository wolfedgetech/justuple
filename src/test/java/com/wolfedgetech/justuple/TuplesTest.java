package com.wolfedgetech.justuple;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class TuplesTest {

    @Test
    void map_returns_empty_when_no_tuples_provided() {
        Map<Object, Object> emptyCollectionMap = Tuples.map(Collections.emptyList());
        assertThat(emptyCollectionMap).isEmpty();

        Map<Object, Object> emptyVarargsMap = Tuples.map();
        assertThat(emptyVarargsMap.isEmpty());
    }

    @Test
    void map_throws_IllegalStateException_when_duplicate_first_members() {
        List<Tuple<String, String>> list = Arrays.asList(
                Tuple.of("foo", "bar"),
                Tuple.of("foo", "baz")
        );
        assertThatIllegalStateException().isThrownBy(() -> Tuples.map(list));

        Tuple<String, String>[] array = list.toArray(new Tuple[0]);
        assertThatIllegalStateException().isThrownBy(() -> Tuples.map(array));
    }

    @Test
    void map_has_same_number_of_entries_as_tuple_args() {
        List<Tuple<String, Integer>> list = Arrays.asList(
                Tuple.of("foo", 1),
                Tuple.of("bar", 2),
                Tuple.of("baz", 3)
        );
        Tuple<String, Integer>[] array = list.toArray(new Tuple[0]);

        BiConsumer<Map<String, Integer>, String> assertMapIsCorrect = (map, message) -> {
            assertThat(map)
                    .withFailMessage(message + " had unexpected size")
                    .hasSize(3);
            assertThat(map)
                    .withFailMessage(message + " had unexpected keys")
                    .containsOnlyKeys("foo", "bar", "baz");
            assertThat(map.get("foo"))
                    .withFailMessage(message + " had unexpected value for key=foo")
                    .isEqualTo(1);
            assertThat(map.get("bar"))
                    .withFailMessage(message + " had unexpected value for key=bar")
                    .isEqualTo(2);
            assertThat(map.get("baz"))
                    .withFailMessage(message + " had unexpected value for key=baz")
                    .isEqualTo(3);
        };

        assertMapIsCorrect.accept(Tuples.map(list), "map from list");
        assertMapIsCorrect.accept(Tuples.map(array), "map from array");
    }

    @Test
    void toTuples_works_for_empty_input() {
        List<Tuple<Object, Object>> tuples = Tuples.from(Collections.emptyList());

        assertThat(tuples).isEmpty();
    }

    @Test
    void toTuples_given_a_single_item_list_returns_a_list_of_one_tuple_with_null_second_member() {
        List<Tuple<String, String>> tuples = Tuples.from(Collections.singletonList("foo"));

        assertThat(tuples).hasSize(1);
        Tuple<String, String> tuple = tuples.get(0);
        assertThat(tuple.getFirst()).isEqualTo("foo");
        assertThat(tuple.getSecond()).isNull();
    }

    @Test
    void toTuples_even_number_of_items_returns_half_as_many_balanced_tuples() {
        List<String> list = Arrays.asList("A", "A", "B", "B", "C", "C", "D", "D", "E", "E", "F", "F", "G", "G");

        List<Tuple<String, String>> tuples = Tuples.from(list);
        assertThat(tuples).hasSize(list.size() / 2);

        tuples.forEach(tuple -> assertThat(tuple.getFirst()).isEqualTo(tuple.getSecond()));
    }

    @Test
    void toTuples_odd_number_of_items_returns_half_as_many_balanced_tuples_plus_one_with_null_2nd_member() {
        List<String> list = Arrays.asList("A", "A", "B", "B", "C", "C", "D", "D", "E", "E", "F", "F", "G", "G", "H");

        List<Tuple<String, String>> tuples = Tuples.from(list);

        int expectedTupleListSize = list.size() / 2 + 1;
        assertThat(tuples).hasSize(expectedTupleListSize);

        tuples.stream()
                .limit(expectedTupleListSize - 1) // skip the final tuple
                .forEach(tuple -> assertThat(tuple.getFirst()).isEqualTo(tuple.getSecond()));

        Tuple<String, String> lastTuple = tuples.get(expectedTupleListSize - 1);
        assertThat(lastTuple.getFirst()).isEqualTo("H");
        assertThat(lastTuple.getSecond()).isNull();
    }

    @Test
    void mapAll_empty_arg_returns_empty_map() {
        Map<Object, List<Object>> mapFromCollection = Tuples.mapAll(Collections.emptySet());
        assertThat(mapFromCollection).isEmpty();

        Map<Object, List<Object>> mapFromVarargs = Tuples.mapAll();
        assertThat(mapFromVarargs).isEmpty();
    }

    @Test
    void mapAll_combines_common_first_members_into_single_entry() {
        List<Tuple<String, Integer>> collection = Arrays.asList(
                Tuple.of("foo", 1),
                Tuple.of("foo", 2),
                Tuple.of("foo", 3),
                Tuple.of("bar", 1)
        );
        Tuple<String, Integer>[] array = collection.toArray(new Tuple[0]);

        BiConsumer<Map<String, List<Integer>>, String> assertMapIsCorrect = (map, message) -> {
            assertThat(map)
                    .withFailMessage(message + " had unexpected size")
                    .hasSize(2);
            assertThat(map)
                    .withFailMessage(message + " had unexpected keys")
                    .containsOnlyKeys("foo", "bar");
            assertThat(map.get("foo"))
                    .withFailMessage(message + " had unexpected value for key=foo")
                    .containsExactly(1, 2, 3);
            assertThat(map.get("bar"))
                    .withFailMessage(message + " had unexpected value for key=bar")
                    .containsExactly(1);
        };

        assertMapIsCorrect.accept(Tuples.mapAll(collection), "mapAll from collection");
        assertMapIsCorrect.accept(Tuples.mapAll(array), "mapAll from array");
    }

    @Test
    void mapAll_supports_null_tuple_members() {
        List<Tuple<String, Integer>> collection = Arrays.asList(
                Tuple.of("foo", 1),
                Tuple.of("foo", null),
                Tuple.of(null, 1),
                Tuple.of(null, 2),
                Tuple.of(null, null)
        );
        Tuple<String, Integer>[] array = collection.toArray(new Tuple[0]);

        BiConsumer<Map<String, List<Integer>>, String> assertMapIsCorrect = (map, message) -> {
            assertThat(map)
                    .withFailMessage(message + " had unexpected size")
                    .hasSize(2);
            assertThat(map)
                    .withFailMessage(message + " had unexpected keys")
                    .containsOnlyKeys("foo", null);
            assertThat(map.get("foo"))
                    .withFailMessage(message + " had unexpected value for key=foo")
                    .containsExactly(1, null);
            assertThat(map.get(null))
                    .withFailMessage(message + " had unexpected value for key=null")
                    .containsExactly(1, 2, null);
        };

        assertMapIsCorrect.accept(Tuples.mapAll(collection), "mapAll from collection");
        assertMapIsCorrect.accept(Tuples.mapAll(array), "mapAll from collection");
    }

    @Test
    void from_method_empty_map_returns_empty_set() {
        Set<Tuple<Object, Object>> tuples = Tuples.from(Collections.emptyMap());
        assertThat(tuples).isEmpty();
    }

    @Test
    void from_method_returned_set_has_same_size_as_input_map() {
        Map<String, Integer> map = new HashMap<>();
        map.put("foo", 1);
        map.put("bar", 2);
        map.put("baz", 3);

        Set<Tuple<String, Integer>> tuples = Tuples.from(map);
        assertThat(tuples).hasSize(3);
        assertThat(tuples).contains(Tuple.of("foo", 1));
        assertThat(tuples).contains(Tuple.of("bar", 2));
        assertThat(tuples).contains(Tuple.of("baz", 3));
    }

    @Test
    void from_method_handles_null_items_from_input_Iterable() {
        List<String> list = Arrays.asList("foo", null, null, "bar", null, "baz");

        List<Tuple<String, String>> tuples = Tuples.from(list);
        assertThat(tuples).containsExactly(
                Tuple.of("foo", null),
                Tuple.of(null, "bar"),
                Tuple.of(null, "baz")
        );
    }

    @Test
    void collect_a_serial_stream_into_tuples() {
        List<Tuple<Integer, Integer>> tuples = IntStream.range(0, 1000)
                .boxed()
                .collect(Tuples.collector());

        assertThat(tuples).hasSize(500);
        tuples.forEach(tuple -> assertThat(tuple.getSecond()).isEqualTo(tuple.getFirst() + 1));
    }

    @Test
    void collect_a_parallel_stream_into_tuples() {
        List<Tuple<Integer, Integer>> tuples = IntStream.range(0, 1000)
                .parallel()
                .boxed()
                .collect(Tuples.collector());

        assertThat(tuples).hasSize(500);
        tuples.forEach(tuple ->
                assertThat(tuple.getSecond()).isEqualTo(tuple.getFirst() + 1));
    }

    @Test
    void tuplesCollector_can_handle_nulls_emitted_from_stream() {
        List<String> list = Arrays.asList("foo", null, "bar", "baz", null, "buk");

        List<Tuple<String, String>> tuples = list.stream().collect(Tuples.collector());
        assertThat(tuples).containsExactly(
                Tuple.of("foo", null),
                Tuple.of("bar", "baz"),
                Tuple.of(null, "buk")
        );
    }

    @Test
    void collected_tuples_when_final_tuple_is_partial_retains_serializability() {
        List<Tuple<Integer, Integer>> tuples = IntStream.rangeClosed(0, 2)
                .parallel()
                .boxed()
                .collect(Tuples.collector());

        assertThat(tuples).containsExactly(
                Tuple.of(0, 1),
                Tuple.of(2, null)
        );
        Tuple<Integer, Integer> finalTuple = tuples.get(1);
        assertThat(finalTuple)
                .withFailMessage("Final Tuple should be serializable when sole member is serializable.")
                .isInstanceOf(Serializable.class);
    }

    @Test
    void map_using_noncollection_iterable() {
        Iterable<Tuple<String, Integer>> iterable = new NonCollectionIterable<>(Arrays.asList(
                Tuple.of("foo", 12),
                Tuple.of("bar", 24))
        );

        Map<String, Integer> map = Tuples.map(iterable);
        assertThat(map).containsOnlyKeys("foo", "bar");
        assertThat(map.get("foo")).isEqualTo(12);
        assertThat(map.get("bar")).isEqualTo(24);
    }

    @Test
    void mapAll_using_noncollection_iterable() {
        Iterable<Tuple<String, Integer>> iterable = new NonCollectionIterable<>(Arrays.asList(
                Tuple.of("foo", 12),
                Tuple.of("bar", 24),
                Tuple.of("bar", 36))
        );

        Map<String, List<Integer>> map = Tuples.mapAll(iterable);
        assertThat(map).containsOnlyKeys("foo", "bar");
        assertThat(map.get("foo")).containsExactly(12);
        assertThat(map.get("bar")).containsExactly(24, 36);
    }

    @Test
    void from_using_noncollection_iterable() {
        Iterable<Object> iterable = new NonCollectionIterable<>(Arrays.asList("foo", 12, "bar", 24));

        List<Tuple<Object, Object>> tuples = Tuples.from(iterable);
        assertThat(tuples).containsExactly(
                Tuple.of("foo", 12),
                Tuple.of("bar", 24)
        );
    }

    @Test
    void zip_two_iterables_of_same_size() {
        List<Integer> firstItems = IntStream.range(0, 1000)
                .boxed()
                .collect(Collectors.toList());
        List<String> secondItems = firstItems.stream()
                .map(i -> i.toString())
                .collect(Collectors.toList());

        List<Tuple<Integer, String>> tuples = Tuples.zip(firstItems, secondItems);

        assertThat(tuples).hasSize(1000);
        int first = 0;
        for (Tuple<Integer, String> tuple : tuples) {
            String second = Integer.toString(first);
            assertThat(tuple).isEqualTo(Tuple.of(first, second));
            first++;
        }
    }

    @Test
    void zip_two_empty_arrays_results_in_empty_list() {
        List<Tuple<String, String>> tuples = Tuples.zip(new String[0], new String[0]);

        assertThat(tuples).isEmpty();
    }

    @Test
    void zip_two_iterables_first_is_larger_than_second() {
        List<Integer> firstItems = IntStream.range(0, 1000)
                .boxed()
                .collect(Collectors.toList());
        List<String> secondItems = firstItems.stream()
                .map(i -> i.toString())
                .collect(Collectors.toList());

        firstItems.removeIf(i -> i >= 500); // remove the second half
        assertThat(firstItems).hasSize(500); // precondition check

        List<Tuple<Integer, String>> tuples = Tuples.zip(firstItems, secondItems);

        assertThat(tuples).hasSize(1000);
        int first = 0;
        for (Tuple<Integer, String> tuple : tuples) {
            String second = Integer.toString(first);
            if (first < 500) {
                assertThat(tuple).isEqualTo(Tuple.of(first, second));
            } else {
                assertThat(tuple).isEqualTo(Tuple.of(null, second));
            }
            first++;
        }
    }

    @Test
    void zip_two_iterables_second_is_larger_than_first() {
        List<Integer> firstItems = IntStream.range(0, 1000)
                .boxed()
                .collect(Collectors.toList());
        List<String> secondItems = firstItems.stream()
                .map(i -> i.toString())
                .collect(Collectors.toList());

        secondItems.removeIf(i -> Integer.parseInt(i) >= 500); // remove the second half
        assertThat(secondItems).hasSize(500); // precondition check

        List<Tuple<Integer, String>> tuples = Tuples.zip(firstItems, secondItems);

        assertThat(tuples).hasSize(1000);
        int first = 0;
        for (Tuple<Integer, String> tuple : tuples) {
            String second = Integer.toString(first);
            if (first < 500) {
                assertThat(tuple).isEqualTo(Tuple.of(first, second));
            } else {
                assertThat(tuple).isEqualTo(Tuple.of(first, null));
            }
            first++;
        }
    }

    @Test
    void zip_a_tuple_of_sets() {
        Tuple<Set<String>, Set<Integer>> tupleOfSets = Tuple.of(Collections.singleton("foo"), Collections.singleton(1));

        List<Tuple<String, Integer>> tuples = Tuples.zip(tupleOfSets);
        assertThat(tuples).containsExactly(
                Tuple.of("foo", 1)
        );
    }

    @Test
    void unzip_tuples_array() {
        Tuple<List<String>, List<Integer>> unzipped = Tuples.unzip(Tuple.of("foo", 1), Tuple.of("bar", 2), Tuple.of("baz", 3));

        assertThat(unzipped.getFirst()).containsExactly("foo", "bar", "baz");
        assertThat(unzipped.getSecond()).containsExactly(1, 2, 3);
    }

    @Test
    void unzip_tuples_collection_with_some_partial_tuples() {
        Tuple<List<Integer>, List<String>> unzipped = Tuples.unzip(Arrays.asList(
                Tuple.of(1, "foo"),
                Tuple.of(2, null),
                Tuple.of(null, "bar"),
                Tuple.of(4, "baz")
        ));

        assertThat(unzipped.getFirst()).containsExactly(1, 2, null, 4);
        assertThat(unzipped.getSecond()).containsExactly("foo", null, "bar", "baz");
    }

    private static class NonCollectionIterable<S> implements Iterable<S> {

        private final Collection<S> collection;

        NonCollectionIterable(Collection<S> collection) {
            this.collection = collection;
        }

        @Override
        public Iterator<S> iterator() {
            return collection.iterator();
        }
    }
}
