package bdkosher.justuple;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.*;
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
    void swapped_serializable_tuple_is_itself_serializable() {
        Tuple<String, String> tuple = Tuple.of("foo", "bar").swapped();

        assertThat(tuple).isInstanceOf(Serializable.class);
    }

    @Test
    void swapped_nonserializable_tuple_is_not_serializable() {
        Tuple<Object, Object> tuple = Tuple.of(new Object(), new Object()).swapped();

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
    void partial_tuple_with_non_serializable_value_is_not_serializable() {
        Tuple<Object, Object> partial = Tuple.partial(new Object());

        assertThat(partial).isNotInstanceOf(Serializable.class);
    }

}
