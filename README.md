# justuple
Purposefully tiny library providing Tuples for Java 8+.

## Basic Features

* Immutable
* Supports `null`
* Easily converted to and from Java collections (Iterables, Maps, Lists, and Sets)
* Parameterized member types
* Comparable, provided member types are `Comparable`
* Serializable, provided member types are `Serializable`

## Usage

There are only two classes to import, the latter of which is not needed unless you need the factory methods that 
allow conversions to or from Java collections.

```java
import bdkosher.justuple.Tuple;
import bdkosher.justuple.Tuples;
```

### Creating a Tuple

#### `Tuple.of` Static Factory Method

The primary way to create a new Tuple is using the `of` factory method.

```java
Tuple<Integer, String> tuple = Tuple.of(1, "foo");
  
  assertThat(tuple.getFirst()).isEqualTo(1);
  assertThat(tuple.getSecond()).isEqualTo("foo");
```

Instead of passing in the individual members, you can provide a `Map.Entry` argument.

```java
  Map<String, LocalDate> map = ...
  Tuple<String, LocalDate> tupleFromEntry = Tuple.of(map.entrySet().iterator.first());
```

You can call `of` with an `Iterable` argument, such as `List`. The `Iterable` can emit any number of elements but only
the first two will be present in the returned `Tuple`. 
Anything fewer than two elements results in a `Tuple` with one or more `null` members.

| Input (`Iterable<T>`) | Output (`Tuple<T, T>`) |
| --------------------- | ---------------------- |
| `[]`                  | `(null, null)`         |
| `[1]`                 | `(1, null)`            |
| `[1, 2]`              | `(1, 2)`               |
| `[1, 2, 3, 4, 5]`     | `(1, 2)` extra elements ignored |

```java
  List<Long> list = ... 
  Tuple<Long, Long> tupleFromList = Tuple.of(list);
```

#### `Tuple::withFirst` and `Tuple::withSecond` Instance Methods

Tuples are immutable, but you can create new instances from existing ones using `withFirst` and `withSecond`.

```java
  Tuple<Integer, String> newTuple = tuple.withFirst(100);
  
  assertThat(newTuple.getFirst()).isEqualTo(100);
  assertThat(newTuple.getSecond())
      .as("Tuple created using withFirst retains original tuple's second member")
      .isEqualTo(tuple.getSecond());
  
  Tuple<Integer, String> newerTuple = tuple.withSecond("bar"); 
  
  assertThat(newerTuple.getFirst()).isEqualTo(tuple.getFirst());
  assertThat(newerTuple.getSecond())
      .as("Tuple created using withSecond retains original tuple's first member"
      .isEqualTo("bar");
```

#### `Tuple::swapped` Instance Method

The `swapped` instance method returns a new Tuple with the first and second members swapped.

```java
  Tuple<Integer, String> swapped = Tuple.of("foo", 12).swapped();

  assertThat(swapped.getFirst()).isEqualTo(12);
  assertThat(swapped.getSecond()).isEqualTo("foo");
```

### Creating Multiple Tuples

The `Tuples` class (note the "s" on the end) contains static methods for creating multiple `Tuple` instances 
(note the lack of "s") from a collection/map/stream of individual items.

#### `Tuples.from` Static Factory Method

Use the `from` factory method to create a `List<Tuple<S,S>>` from some `Iterable<S>`. Subsequent elements emitted by
the `Iterable` are combined into a `Tuple`. If the overall number of elements emitted is null, the final `Tuple`
instance will have a `null` second member value.

| Input (`Iterable<T>`) | Output (`List<Tuple<T, T>>`)  |
| ----------------------| ----------------------------- |
| `[]`                  | `[]`                          | 
| `[1]`                 | `[(1, null)]`                 |
| `[1, 2, 3, 4]`        | `[(1, 2), (3, 4)]`            |
| `[1, 2, 3, 4, 5]`     | `[(1, 2), (3, 4), (5, null)]` |

```java
  List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

  List<Tuple<Integer, Integer>> tuples = Tuple.tuplesFrom(list);

  assertThat(tuples).hasSize(3);
  assertThat(tuples.get(0)).isEqualTo(Tuple.of(1, 2));
  assertThat(tuples.get(2)).isEqualTo(Tuple.of(5, null));
```

The `from` method also accepts a `Map<K, V>` argument, returning a corresponding `Set<Tuple<K, V>>`.

```java
  Map<String, Integer> map = Map.of("foo", 1, "bar", 2, "baz", 3); // Java 9+
   
  Set<Tuple<String, Integer>> tuples = Tuple.tuplesFrom(map);
  assertThat(tuples).hasSize(3);
```

#### Creating from a `java.util.Stream`

The `Tuples.collector()` factory methods return a custom `Collector` that can generate a List of Tuples from a
Stream.

```java
  List<String> list = Arrays.asList("foo", "bar", "FOO", "BAR");

  List<Tuple<String, String> tuples = list.stream()
      .filter(Objects::nonNull)
      .collect(Tuple.tupleCollector());

  assertThat(tuples).containsExactly(
      Tuple.of("foo", "bar"),
      Tuple.of("FOO", "BAR")
  );
```

### Converting to Java Collections

#### `Tuple::toList` and `Tuple::toTypedList` Instance Methods

A single `Tuple` instance can be converted into a `List`.

```java
  Tuple<String, Integer> tuple = Tuple.of("Foo", 99);

  List<Object> list = tuple.toList();
  assertThat(list).containsExactly("Foo", 99);
```

When the `Tuple` shares the same type for both members, the `toTypedList` method can be used instead to return a list
parameterized to that type rather than `java.util.Object`

```java
  Tuple<String, String> tuple = Tuple.of("Foo", "99");

  List<String> stringList = tuple.toTypedList();
  assertThat(list).containsExactly("Foo", "99");
```

#### `Tuple::toMap` and `Tuple::toMapEntry` Instance Methods

Besides lists, a single `Tuple` instances can be converted to a `Map` or `Map.Entry`.

```java
  Tuple<Integer, String> tuple = Tuple.of(20, "Bar");

  Map<Integer, String> map = tuple.toMapEntry();
  assertThat(map).hasSize(1);
  assertThat(map.get(20)).isEqualTo("Bar");

  Map.Entry<Integer, String> entry = tuple.toMapEntry();
  assertThat(entry.getKey()).isEqualTo(20);
  assertThat(entry.getValue()).isEqualTo("Bar");
```

#### `Tuples.map` and `Tuples.mapAll` Static Methods

The `map` method converts multiple `Tuple<K, V>` instances into a single `Map<K, V>`.

```java
  Map<String, Integer> map = Tuple.map(
      Tuple.of("foo", 1),
      Tuple.of("bar", 2)
  );
  assertThat(map).containsOnlyKeys("foo", "bar");
  assertThat(map.get("foo")).isEqualTo(1);
  assertThat(map.get("bar")).isEqualTo(2);
``` 

If there are multiple tuples with identical keys, the `map` method will throw an `IllegalStateException`. 
To overcome this problem, use the `mapAll` method to produce a `Map<K, List<V>>` instance.

```java
  Map<String, List<Integer>> map = Tuple.mapAll(
      Tuple.of("foo", 1),
      Tuple.of("foo", 2)
  );
  assertThat(map).containsOnlyKeys("foo");
  assertThat(map.get("foo")).contains(1, 2);
``` 

#### `Tuples.zip` Static Method

The `zip` method combines two arrays/Streams/Iterables into a single List of Tuples. The two arguments to the method
need not be of the same length and can use different data types.

```java
  List<Tuple<Integer, Integer>> tuples = Tuples.zip(
          IntStream.range(0, 1000).boxed(),
          IntStream.range(1, 1001).boxed()
  );

  assertThat(tuples).hasSize(1000);
  int value = 0;
  for (Tuple<Integer, Integer> tuple : tuples) {
      assertThat(tuple).isEqualTo(Tuple.of(value, value + 1));
      ++value;
  }
```
