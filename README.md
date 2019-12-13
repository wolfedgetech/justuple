# justuple
Purposefully tiny library providing Tuples for Java 8+.

## Basic Features

* Immutable
* Supports `null`
* Conversion methods to and from Java collections
* Parameterized member types
* Comparable, provided member types are `Comparable`
* Serializable, provided member types are `Serializable`

## Usage

### Single-Instance Creation

#### `Tuple.of` Static Factory Method

The primary way to create a new Tuple is using the `of` factory method:

```java
  Tuple<Integer, String> tuple = Tuple.of(1, "foo");
  
  assertThat(tuple.getFirst()).isEqualTo(1);
  assertThat(tuple.getSecond()).isEqualTo("foo");
```

Instead of passing in the individual members, you can provide a `Map.Entry` argument

```java
  Map<String, LocalDate> map = ...
  Tuple<String, LocalDate> tupleFromEntry = Tuple.of(map.entrySet().iterator.first());
```

You can call `of` with an `Iterable` argument, such as `List`. The Iterable can emit any number of elements but only the first two will be present in the returned Tuple. Anything fewer than two elements results in a Tuple with one or more null members.

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

Tuples are immutable, but you can create new Tuples from existing ones using `withFirst` and `withSecond`

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

The `revsered` instance method returns a new Tuple with the first and second members swapped.

```java
    Tuple<String, Integer> tuple = Tuple.of("foo", 12);

    Tuple<Integer, String> swapped = tuple.swapped();
    assertThat(swapped.getFirst()).isEqualTo(12);
    assertThat(swapped.getSecond()).isEqualTo("foo");
```

### Multi-Instance Creation

#### `Tuple.toTuples` Static Factory Method

Use the `toTuples` factory method to create a List of Tuples from a List (or any `Iterable` implementation) of single elements. 

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

The `toTuples` method also accepts a `Map<K, V>` argument, returning a corresponding `Set<Tuple<K, V>>`.

```java
    Map<String, Integer> map = Map.of("foo", 1, "bar", 2, "baz", 3); // Java 9+
   
    Set<Tuple<String, Integer>> tuples = Tuple.tuplesFrom(map);
    assertThat(tuples).hasSize(3);
```

#### Creating Tuples from a `java.util.Stream`

The `Tuple.tupleCollector()` factory methods return a custom `Collector` that can generate a List of Tuples from a
Stream. Be aware that the collector does not accept `null` items, so they should be filtered out before collection.

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