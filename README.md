# justuple
Purposefully tiny library providing Tuples for Java 8+.

## Basic Features

* Immutable
* Supports `null`
* Convertable to and from Java collections
* Parameterized member types
* Comparable, provided member types are `Comparable`
* Serializable, provided member types are `Serializable`

## Usage Examples

### Single-Instance Creation

The primary way to create a new Tuple is using the `of` factory method:

```java
  Tuple<Integer, String> tuple = Tuple.of(1, "foo");
  
  assertThat(tuple.getFirst()).isEqualTo(1);
  assertThat(tuple.getSecond()).isEqualTo("foo");
```

Tulpes are immutable, but you can create new Tuples from existing ones:

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

Or you can create individual Tuples from `Map.Entry` or `Iterable` instances like `List`:

```java
  Map<String, LocalDate> map = ...
  Tuple<String, LocalDate> tupleFromEntry = Tuple.of(map.entrySet().iterator.first());
  
  List<Long> list = ... // may be of any length, elements after index = 1 ignored
  Tuple<Long, Long> tupleFromList = Tuple.of(list);
```

### Multi-Instance Creation

Use the `tuplesFrom` factory method to create a List of Tuples from a List (or any `Iterable` implementation) of single elements. 

| Input (`Iterable<T>`) | Output (`List<Tuple<T, T>>`) |
| ----------------------| ---------------------------- |
| []                    | []                           | 
| [1]                   | [(1, null)]                  |
| [1, 2, 3, 4]          | [(1, 2), (3, 4)]             |
| [1, 2, 3, 4, 5]       | [(1, 2), (3, 4), (5, null)]  |

```java
    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

    List<Tuple<Integer, Integer>> tuples = Tuple.tuplesFrom(list);

    assertThat(tuples).hasSize(3);
    assertThat(tuples.get(0)).isEqualTo(Tuple.of(1, 2));
    assertThat(tuples.get(2)).isEqualTo(Tuple.of(5, null));
```

To c
