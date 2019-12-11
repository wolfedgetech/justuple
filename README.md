# justuple
Purposefully tiny library providing Tuples for Java 8+.

## Basic Features

* Immutable
* Convertable to and from Java collections
* Parameterized member types
* Comparable with like-typed Tuples
* Serializable, provided member types are serializable

## Tuple Creation

The primary way to create a new Tuple is using the `of` factory method:

```java
  Tuple<Integer, String> tuple = Tuple.of(1, "foo");
  
  assertThat(tuple.getFirst()).isEqualTo(1);
  assertThat(tuple.getSecond()).isEqualTo("foo");
```

Tulpes are immutable, but you can create new Tuples from existing ones:

```java
  Tuple<Integer, String> anotherTuple = tuple.withFirst(100);
  assertThat(anotherTuple.getFirst()).isEqualTo(100);
  assertThat(anotherTuple.getSecond()).isEqualTo(tuple.getSecond());
  
  Tuple<Integer, String> yetAnotherTuple = tuple.withSecond("bar");  
  assertThat(yetAnotherTuple.getFirst()).isEqualTo(tuple.getFirst());
  assertThat(anotherTuple.getSecond()).isEqualTo("bar");
```

Or you can create individual Tuples from Map.Entry or List intances:

```java
  Map<String, LocalDate> map = ...
  Tuple<String, LocalDate> tupleFromEntry = Tuple.of(map.entrySet().iterator.first());
  
  List<Long> list = ...
  Tuple<Long, Long> tupleFromList = Tuple.of(list);
```
