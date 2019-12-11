package bdkosher.justuple;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

public class TupleTest {

    @Test
    void tuple_is_serializable_when_both_arguments_are() {
        Tuple<String, String> tuple = Tuple.of("foo", "bar");

        assertThat(tuple).isInstanceOf(Serializable.class);
    }

    @Test
    void tuple_is_not_serializable_when_an_argument_is_not() {
        Tuple<Object, String> firstArgNotSerializable = Tuple.of(new Object(), "bar");
        assertThat(firstArgNotSerializable)
                .isNotInstanceOf(Serializable.class);

        Tuple<String, Object> secondArgNotSerializable = Tuple.of("foo", new Object());
        assertThat(secondArgNotSerializable)
                .isNotInstanceOf(Serializable.class);

        Tuple<Object, Object> neitherArgIsSerializable = Tuple.of(new Object(), new Object());
        assertThat(neitherArgIsSerializable)
                .isNotInstanceOf(Serializable.class);
    }

    @Test
    void reversed_serializable_tuple_is_itself_serializable() {
        Tuple<String, String> tuple = Tuple.of("foo", "bar").reverse();

        assertThat(tuple).isInstanceOf(Serializable.class);
    }

    @Test
    void reversed_nonserializable_tuple_is_not_serializable() {
        Tuple<Object, Object> tuple = Tuple.of(new Object(), new Object()).reverse();

        assertThat(tuple).isNotInstanceOf(Serializable.class);
    }

    @Test
    void toList_returns_sized_two_list_with_differing_types() {
        Tuple<String, Integer> tuple = Tuple.of("foo", 12);

        List<Object> list = tuple.toList();
        assertThat(list).hasSize(2);
        assertThat(list.get(0)).isEqualTo(tuple.getFirst());
        assertThat(list.get(1)).isEqualTo(tuple.getSecond());

        Iterator<Object> itr = list.iterator();
        assertThat(itr.next()).isEqualTo(tuple.getFirst());
        assertThat(itr.next()).isEqualTo(tuple.getSecond());
        assertThat(itr.hasNext()).isFalse();
    }

    @Test
    void toList_returns_sized_two_list_even_if_elements_are_null() {
        Tuple<String, String> tuple = Tuple.of(null, null);

        List<Object> list = tuple.toList();
        assertThat(list).hasSize(2);
        assertThat(list).containsExactly(null, null);

        list.forEach(obj -> assertThat(obj).isNull());
    }

    @Test
    void toList_list_is_immutable() {
        Tuple<String, String> tuple = Tuple.of("A", "B");
        List<Object> list = tuple.toList();

        assertThatThrownBy(() -> list.add("C"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.add(0, "X"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.addAll(Arrays.asList("C", "D")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.addAll(0, Arrays.asList("C", "D")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.set(0, "X"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.remove(0))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.remove("A"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.removeAll(Arrays.asList("A", "B")))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.sort(Comparator.comparing(Object::toString)))
                .isInstanceOf(UnsupportedOperationException.class);

        assertThatThrownBy(() -> list.iterator().remove())
                .isInstanceOfAny(IllegalStateException.class);
        assertThatThrownBy(() -> list.listIterator().set("X"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> list.listIterator().add("X"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void toList_get_returns_first_and_second_members() {
        Stream.of(
                Tuple.of(null, "bar"),
                Tuple.of(null, "bar"),
                Tuple.of("foo", "bar")
        ).forEach(tuple -> {
            List<Object> list = tuple.toList();
            assertThat(list.get(0)).isEqualTo(tuple.getFirst());
            assertThat(list.get(1)).isEqualTo(tuple.getSecond());
        });
    }

    @Test
    void toList_get_bounds_are_enforced_by_ArrayIndexOutOfBoundsException() {
        Tuple<String, String> tuple = Tuple.of("foo", "bar");
        List<Object> list = tuple.toList();

        assertThatThrownBy(() -> list.get(-1)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
        assertThatThrownBy(() -> list.get(2)).isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    void toMap_for_null_member_tuple() {
        Tuple<Object, Object> nullTuple = Tuple.of(null, null);

        Map<Object, Object> map = nullTuple.toMap();

        assertThat(map).hasSize(1);
        assertThat(map.containsKey(null)).isTrue();
        assertThat(map.get(null)).isNull();
        assertThat(map.containsValue(null)).isTrue();

        Set<Map.Entry<Object, Object>> entrySet = map.entrySet();
        assertThat(entrySet).hasSize(1);
        Map.Entry<Object, Object> entry = entrySet.iterator().next();
        assertThat(entry).isNotNull();
        assertThat(entry.getKey()).isNull();
        assertThat(entry.getValue()).isNull();
    }

    @Test
    void toMap_for_tuple_with_null_first_member() {
        Tuple<Object, Object> nullKeyTuple = Tuple.of(null, "foo");

        Map<Object, Object> map = nullKeyTuple.toMap();

        assertThat(map).hasSize(1);
        assertThat(map.containsKey(null)).isTrue();
        assertThat(map.get(null)).isEqualTo(nullKeyTuple.getSecond());
        assertThat(map.containsValue(nullKeyTuple.getSecond())).isTrue();

        Set<Map.Entry<Object, Object>> entrySet = map.entrySet();
        assertThat(entrySet).hasSize(1);
        Map.Entry<Object, Object> entry = entrySet.iterator().next();
        assertThat(entry).isNotNull();
        assertThat(entry.getKey()).isNull();
        assertThat(entry.getValue()).isEqualTo(nullKeyTuple.getSecond());
    }

    @Test
    void create_from_MapEntry() {
        Map<String, String> map = new HashMap<>();
        map.put("foo", "bar");
        Map.Entry<String, String> entry = map.entrySet().iterator().next();

        Tuple<String, String> tuple = Tuple.of(entry);
        assertThat(tuple.getFirst()).isEqualTo(entry.getKey());
        assertThat(tuple.getSecond()).isEqualTo(entry.getValue());

        entry.setValue("X");
        assertThat(tuple.getSecond()).isNotEqualTo(entry.getValue());
    }

    @Test
    void create_from_empty_List() {
        Tuple<Integer, Integer> tuple = Tuple.of(Collections.emptyList());

        assertThat(tuple).isNotNull();
        assertThat(tuple.getFirst()).isNull();
        assertThat(tuple.getSecond()).isNull();
    }

    @Test
    void create_from_single_element_list() {
        Tuple<Integer, Integer> tuple = Tuple.of(Collections.singletonList(12));

        assertThat(tuple.getFirst()).isEqualTo(12);
        assertThat(tuple.getSecond()).isNull();
    }

    @Test
    void create_from_size_2_list() {
        List<String> list = Arrays.asList("foo", "bar");
        Tuple<String, String> tuple = Tuple.of(list);

        assertThat(tuple.getFirst()).isEqualTo("foo");
        assertThat(tuple.getSecond()).isEqualTo("bar");
    }

    @Test
    void create_from_list_bigger_than_2() {
        List<String> list = Arrays.asList("foo", "bar", "CHICKEN WING");
        Tuple<String, String> tuple = Tuple.of(list);

        assertThat(tuple.getFirst()).isEqualTo("foo");
        assertThat(tuple.getSecond()).isEqualTo("bar");
    }

    @Test
    void toMapEntry_supports_null_tuple() {
        Tuple<Object, String> nullTuple = Tuple.of(null, null);

        Map.Entry<Object, String> entry = nullTuple.toMapEntry();

        assertThat(entry.getKey()).isNull();
        assertThat(entry.getValue()).isNull();
    }

    @Test
    void toMapEntry_supports_null_key() {
        String value = "foo";
        Tuple<Object, String> nullKey = Tuple.of(null, value);

        Map.Entry<Object, String> entry = nullKey.toMapEntry();

        assertThat(entry.getKey()).isNull();
        assertThat(entry.getValue()).isEqualTo(value);
    }

    @Test
    void toMapEntry_supports_null_value() {
        Tuple<Integer, String> tuple = Tuple.of(1, null);

        Map.Entry<Integer, String> entry = tuple.toMapEntry();

        assertThat(entry.getKey()).isEqualTo(tuple.getFirst());
        assertThat(entry.getValue()).isNull();
    }

    @Test
    void toMapEntry_is_mutable() {
        String value = "foo";
        Tuple<Integer, String> tuple = Tuple.of(1, value);

        Map.Entry<Integer, String> entry = tuple.toMapEntry();
        assertThat(entry.getKey()).isEqualTo(tuple.getFirst());
        assertThat(entry.getValue()).isEqualTo(value);

        String newValue = "baz";
        assertThat(entry.setValue(newValue)).isEqualTo(value);
        assertThat(entry.getValue()).isEqualTo(newValue);
    }

    @Test
    void toTypedList_works_with_compatible_member_types() {
        Tuple<String, String> stringTuple = Tuple.of("foo", "bar");

        List<String> strings = stringTuple.toTypedList();
        assertThat(strings).containsExactly(stringTuple.getFirst(), stringTuple.getSecond());
    }

    @Test
    void toTypedList_fails_when_nonnull_member_types_are_not_compatible() {
        Tuple<Integer, String> tuple = Tuple.of(1, "foo");

        assertThatIllegalStateException().isThrownBy(tuple::toTypedList);
    }

    @Test
    void toTypedList_fails_when_first_member_is_null_and_member_types_are_not_compatible() {
        Tuple<Integer, String> tuple = Tuple.of(null, "foo");

        assertThatIllegalStateException().isThrownBy(tuple::toTypedList);
    }

    @Test
    void toTypedList_fails_when_second_member_is_null_and_member_types_are_not_compatible() {
        Tuple<String, Integer> tuple = Tuple.of("foo", null);

        assertThatIllegalStateException().isThrownBy(tuple::toTypedList);
    }

    @Test
    void toTypedList_works_when_null_members_and_member_types_are_compatible() {
        Tuple<String, ?> nullTuple = Tuple.of(null, null);
        List<String> members = nullTuple.toTypedList();
        assertThat(members).containsExactly(null, null);
    }

    @Test
    void tuples_with_incomparable_members_cannot_be_compared() {
        Object a = new Object();
        Object b = new Object();

        Tuple<Object, Object> t1 = Tuple.of(a, b);
        Tuple<Object, Object> t2 = Tuple.of(b, a);

        assertThatThrownBy(() -> t1.compareTo(t2)).isInstanceOf(ClassCastException.class);
    }

    @Test
    void tuple_is_comparable_when_both_arguments_are() {
        Tuple<String, String> tuple1 = Tuple.of("foo", "bar");
        Tuple<String, String> tuple2 = Tuple.of("foo", "baz");
        Tuple<String, String> tuple3 = Tuple.of("bar", "foo");

        Set<Tuple<String, String>> set = new TreeSet<>();
        set.add(tuple1);
        set.add(tuple2);
        set.add(tuple3);

        assertThat(set).containsSequence(tuple3, tuple1, tuple2);
    }

    @Test
    void compareTo_null_tuple_first() {
        Tuple<String, String> tuple = Tuple.of("foo", "bar");

        assertThat(tuple.compareTo(null)).isGreaterThan(0);
    }

    @Test
    void compareTo_null_first_members_are_first() {
        Tuple<String, String> tuple = Tuple.of("foo", "bar");
        Tuple<String, String> nullFirst = Tuple.of(null, "bar");

        assertThat(tuple.compareTo(nullFirst)).isGreaterThan(0);
        assertThat(nullFirst.compareTo(tuple)).isLessThan(0);
    }

    @Test
    void compareTo_null_members_with_null_first_nonnull_second_has_nulls_first() {
        Tuple<String, String> nulls = Tuple.of(null, null);
        Tuple<String, String> nullFirst = Tuple.of(null, "bar");

        assertThat(nulls.compareTo(nullFirst)).isLessThan(0);
        assertThat(nullFirst.compareTo(nulls)).isGreaterThan(0);
    }

    @Test
    void tuples_with_null_first_member_and_identical_second_member_are_identical() {
        Tuple<String, String> firstNull1 = Tuple.of(null, "foo");
        Tuple<String, String> firstNull2 = Tuple.of(null, "foo");

        assertThat(firstNull1.equals(firstNull2)).isTrue();
        assertThat(firstNull1.hashCode()).isEqualTo(firstNull2.hashCode());
        assertThat(firstNull1.compareTo(firstNull2)).isEqualTo(0);
    }

    @Test
    void tuples_with_identical_first_member_and_null_second_member_are_identical() {
        Tuple<Long, Long> secondNull1 = Tuple.of(1L, null);
        Tuple<Long, Long> secondNull2 = Tuple.of(1L, null);

        assertThat(secondNull1.equals(secondNull2)).isTrue();
        assertThat(secondNull1.hashCode()).isEqualTo(secondNull2.hashCode());
        assertThat(secondNull1.compareTo(secondNull2)).isEqualTo(0);
    }

    @Test
    void tuples_with_null_members_are_identical() {
        Tuple<Double, Long> bothNull1 = Tuple.of(null, null);
        Tuple<Double, Long> bothNull2 = Tuple.of(null, null);

        assertThat(bothNull1.equals(bothNull2)).isTrue();
        assertThat(bothNull1.hashCode()).isEqualTo(bothNull2.hashCode());
        assertThat(bothNull1.compareTo(bothNull2)).isEqualTo(0);
    }

    @Test
    void tuple_equality_based_on_member_equality() {
        Tuple<String, String> tuple = Tuple.of("X", "Y");
        Tuple<String, String> equalTuple = Tuple.of("X", "Y");
        Tuple<String, String> firstMemberNotEqual = Tuple.of("Z", "Y");
        Tuple<String, String> secondMemberNotEqual = Tuple.of("X", "Z");
        Tuple<String, String> reversedMembers = Tuple.of("Z", "Y");

        assertThat(tuple.equals(equalTuple)).isTrue();
        assertThat(equalTuple.equals(tuple)).isTrue();

        assertThat(tuple.equals(firstMemberNotEqual)).isFalse();
        assertThat(firstMemberNotEqual.equals(tuple)).isFalse();

        assertThat(tuple.equals(secondMemberNotEqual)).isFalse();
        assertThat(secondMemberNotEqual.equals(tuple)).isFalse();

        assertThat(tuple.equals(reversedMembers)).isFalse();
        assertThat(reversedMembers.equals(tuple)).isFalse();
    }

    @Test
    void tuple_does_not_equal_nontuple() {
        Tuple<String, String> tuple = Tuple.of("foo", null);
        assertThat(tuple.equals("foo")).isFalse();
    }

    @Test
    void tuple_does_not_equal_null() {
        Tuple<String, String> tuple = Tuple.of("foo", null);
        assertThat(tuple.equals(null)).isFalse();
    }

    @Test
    void withFirst_returns_new_tuple() {
        Tuple<Integer, Integer> tuple = Tuple.of(1, 2);

        Tuple<Integer, Integer> newTuple = tuple.withFirst(2);
        assertThat(tuple.getFirst()).isEqualTo(1);
        assertThat(newTuple.getFirst()).isEqualTo(2);
        assertThat(newTuple.getSecond()).isEqualTo(2);
    }

    @Test
    void withSecond_returns_new_tuple() {
        Tuple<Integer, Integer> tuple = Tuple.of(1, 2);

        Tuple<Integer, Integer> newTuple = tuple.withSecond(1);
        assertThat(tuple.getSecond()).isEqualTo(2);
        assertThat(newTuple.getFirst()).isEqualTo(1);
        assertThat(newTuple.getSecond()).isEqualTo(1);
    }

    @Test
    void map_returns_empty_when_no_tuples_provided() {
        Map<Object, Object> emptyCollectionMap = Tuple.map(Collections.emptyList());
        assertThat(emptyCollectionMap).isEmpty();

        Map<Object, Object> emptyVarargsMap = Tuple.map();
        assertThat(emptyVarargsMap.isEmpty());
    }

    @Test
    void map_throws_IllegalStateException_when_duplicate_first_members() {
        List<Tuple<String, String>> list = Arrays.asList(
                Tuple.of("foo", "bar"),
                Tuple.of("foo", "baz")
        );
        assertThatIllegalStateException().isThrownBy(() -> Tuple.map(list));

        Tuple<String, String>[] array = list.toArray(new Tuple[0]);
        assertThatIllegalStateException().isThrownBy(() -> Tuple.map(array));
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

        assertMapIsCorrect.accept(Tuple.map(list), "map from list");
        assertMapIsCorrect.accept(Tuple.map(array), "map from array");
    }

    @Test
    void extract_works_for_empty_input() {
        List<Tuple<Object, Object>> tuples = Tuple.tuplesFrom(Collections.emptyList());

        assertThat(tuples).isEmpty();
    }

    @Test
    void extract_given_a_single_item_list_returns_a_list_of_one_tuple_with_null_second_member() {
        List<Tuple<String, String>> tuples = Tuple.tuplesFrom(Collections.singletonList("foo"));

        assertThat(tuples).hasSize(1);
        Tuple<String, String> tuple = tuples.get(0);
        assertThat(tuple.getFirst()).isEqualTo("foo");
        assertThat(tuple.getSecond()).isNull();
    }

    @Test
    void extract_even_number_of_items_returns_half_as_many_balanced_tuples() {
        List<String> list = Arrays.asList("A", "A", "B", "B", "C", "C", "D", "D", "E", "E", "F", "F", "G", "G");

        List<Tuple<String, String>> tuples = Tuple.tuplesFrom(list);
        assertThat(tuples).hasSize(list.size() / 2);

        tuples.forEach(tuple -> assertThat(tuple.getFirst()).isEqualTo(tuple.getSecond()));
    }

    @Test
    void extract_odd_number_of_items_returns_half_as_many_balanced_tuples_plus_one_with_null_2nd_member() {
        List<String> list = Arrays.asList("A", "A", "B", "B", "C", "C", "D", "D", "E", "E", "F", "F", "G", "G", "H");

        List<Tuple<String, String>> tuples = Tuple.tuplesFrom(list);

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
    void toString_shows_member_toString() {
        Tuple<String, String> allNull = Tuple.of(null, null);
        assertThat(allNull.toString()).isEqualTo("(null, null)");

        Tuple<String, String> firstNull = Tuple.of("foo", null);
        assertThat(firstNull.toString()).isEqualTo("(foo, null)");

        Tuple<String, String> secondNull = Tuple.of(null, "foo");
        assertThat(secondNull.toString()).isEqualTo("(null, foo)");

        Tuple<String, String> nonNull = Tuple.of("foo", "bar");
        assertThat(nonNull.toString()).isEqualTo("(foo, bar)");
    }

    @Test
    void mapAll_empty_arg_returns_empty_map() {
        Map<Object, List<Object>> mapFromCollection = Tuple.mapAll(Collections.emptySet());
        assertThat(mapFromCollection).isEmpty();

        Map<Object, List<Object>> mapFromVarargs = Tuple.mapAll();
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

        assertMapIsCorrect.accept(Tuple.mapAll(collection), "mapAll from collection");
        assertMapIsCorrect.accept(Tuple.mapAll(array), "mapAll from array");
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

        assertMapIsCorrect.accept(Tuple.mapAll(collection), "mapAll from collection");
        assertMapIsCorrect.accept(Tuple.mapAll(array), "mapAll from collection");
    }

    @Test
    void tuplesFrom_empty_map_returns_empty_set() {
        Set<Tuple<Object, Object>> tuples = Tuple.tuplesFrom(Collections.emptyMap());
        assertThat(tuples).isEmpty();
    }

    @Test
    void tuplesFrom_returned_set_has_same_size_as_input_map() {
        Map<String, Integer> map = new HashMap<>();
        map.put("foo", 1);
        map.put("bar", 2);
        map.put("baz", 3);

        Set<Tuple<String, Integer>> tuples = Tuple.tuplesFrom(map);
        assertThat(tuples).hasSize(3);
        assertThat(tuples).contains(Tuple.of("foo", 1));
        assertThat(tuples).contains(Tuple.of("bar", 2));
        assertThat(tuples).contains(Tuple.of("baz", 3));
    }

}
