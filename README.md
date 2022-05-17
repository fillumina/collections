# Collection of Java containers

JDK collections are very well done and optimized but they have made some design choices that might have left some interesting corner cases apart:
 - The use of map **entries** forces every mapping to use a dedicated object. Java is fast to create and manipulate objects but using arrays by interleaving keys and values might offer some advantages in performances (because of locality) and cloning (copying an array is fast)
 - JDK containers are usually slow to **clone**
 - **Immutable instances** are not declared as such explicitly forcing to use defensive cloning extensively (a consumer producer pattern should be used instead but it's not really common).
 - To create an **immutable view** JDK wraps a container into an unmodifiable class that uses the original mutable container as delegate. This approach uses a lot of method overriding and must create new entry every time an immutable one is required. This is very inefficient both on performances and on memory usage. The containers here all use an internal state that can be passed to _immutable_ containers in a very efficient way. The immutable container is an implementation of the original one with only one method overridden: `readOnlyCheck()` that could eventually throw an exception if the container is read-only. That's all.

### Optimized for size efficiency

Containers optimized for space versus speed efficiency. They are well suited to contain very few elements (the speed of linear searching of elements is impacted heavily by memory locality and cache size) and perform many more readings than writings. They are fast to create and clone.

* **`BaseArrayMap`** is an abstract `Map` backed by a growing array of interleaved keys and values. It's very compact but also slow to access O(N). It uses a _cursor_ instead of _entries_: a cursor is a mutable entry that is also an iterator and that changes when iterating over it. It's very fast to iterate on it (especially if entries are not used by the implementation) but not 100% compliant with the specification. All element access times are O(N).
* **`ArrayMap`** is a simple `BaseArrayMap` implementation.
* **`SortedArrayMap`** is a `BaseArrayMap` implementation with sorted keys and access time of O(log N). Keys need to implement `Comparable`. It's very compact and its performances are decent and can be cloned really fast.
* **`SmallSet`** is a very compact `Set` implementation backed by an object that would eventually be an array. Every insertion must scan the entire array for unicity so its performances are linear. Because its internal state is an object that can be either the only item or an array it plays badly with reflective tools such as `Kryo` but it really takes the memory efficiency at the extreme.
* **`ArraySet`** is a very compact `Set` implementation backed by an array. Every insertion must scan the entire array for unicity so its performances are linear O(N). It uses an array instead of an `Object` like `SmallSet` to play nicer with reflective tools such as `Kryo`.
* **`SmallList`** is a compact array list that grows and shrink as required: its array is sized exactly to contain the actual items. This means that it's slower to add than `ArrayList` but 50% more memory efficient (but JDK `ArrayList` has `trimToSize()`).

### Optimized for speed of access

JDK maps aren't very friendly towards extension, these maps offer a lot of extension points and utilities that made them very flexible and quite performant at the same time. Being internally based on array they are very **fast to clone** (which is a distinctive advantage over JDK maps).

* **`AbstractEntryMap`** is a very _extendable_, _compliant_, `Map` implementation based on hash table with performances of O(1).
* **`TableMap`** is an `AbstractEntryMap` implementation.
* **`VieweableMap`** is `AbstractEntryMap` implementation where `Entry` cannot set values. It provides an _unmodifiable view_ that share its internal data.
* **`CopyOnWriteMap`** a concurrent `VieweableMap` that allows fast access for frequent readings with an efficient use of space.

### Immutable containers

The JDK library uses defensive object copying extensively to avoid having an object changed unexpectedly by another actor provoking all sort of _side effect_ type of bugs. These can be avoided by passing _immutable_ objects when needed. Immutable objects have also the advantage of being usable in a multi threading environment. The JDK offers several ways to create immutable containers but all of them fail to explicitly present them as such. So even an immutable container must be defensively copied over and over when being passed to other objects because they have no way to know that. The containers listed here are _guaranteed_ immutable and can be used and passed directly without the need of an expensive defensive copy.

* **`ImmutableList`** is an immutable `List` implementation.
* **`ImmutableSmallSet`** is an immutable set using as little memory as possible with linear access time.
* **`ImmutableArraySet`** is an immutable set backed by an array with linear access time.
* **`ImmutableLinkedHashSet`** is an immutable set using an hash table with performances of O(1).
* **`ImmutableHashMap`** is an immutable hash `Map` implementation with performances of O(1).
* **`ImmutableArrayMap`** is an immutable array backed `Map` implementation with performances of O(N) but very tight memory requirements.
* **`ImmutableSortedArrayMap`** is an immutable sorted array backed `Map` implementation with performances of O(log N) and very tight memory requirements. Its keys must implement `Comparable`. Prefer `ImmutableArrayMap` for very few elements (i.e. less than 7) because of the overhead in managing bisections.
* **`ImmutableSmallList`** is the immutable version of `SmallList`.

Note that all these immutable containers can be used as _viewers_ to mutable ones by calling `immutable()` (they share the same internal state but only the mutable instance can change it).

### Different kind of containers

## BiMap

Is a compliant `Map` implementation where *values unicity* is enforced in addition to usual *keys unicity*. Its mappings are therefore symmetrical and can be reversed on both ways. Each of the two symmetrical maps is a compliant `Map` implementation.

## Matrix

It's a multi-associative map where keys can be associated to multiple values. It can _translate_ (key1,value) into key2 giving back value2:
```java
        Matrix<String, String> mtx = Matrix.<String, String>rowBuilder()
                .keys("IT", "EN", "FR")
                .row("uno", "one", "une")
                .row("due", "two", "deux")
                .row("tre", "three", "trois")
                .buildImmutable();

        assertEquals("une", mtx.getRelationValue("IT", "FR", "uno"));
```
or by column:
```java
        Matrix<String, String> mtx = Matrix.<String, String>columnBuilder()
                .col("IT", "uno", "due", "tre")
                .col("EN", "one", "two", "three")
                .col("FR", "une", "deux", "trois")
                .buildImmutable();

        assertEquals("une", mtx.getRelationValue("IT", "FR", "uno"));
```

## MultiMap

It's a map that uses multiple keys for each value. It can then be queried to retrieve the content pointed by specific keys. It can generate different `Tree` structures that reflect the different possible associations between keys.

## Tree

Is a composition of maps where each node is a `Map` implementation and can also contain a value. A Tree can be created from a `MultiMap` following different criteria. A Tree can be flattened by different criteria and can be converted into a single level `Map`.

### Lambdas helpers

* **`Holder`** contains a mutable value to be used inside non concurrent lambdas (use `AtomicReference` in concurrent lambdas).
* **`Counter`** contains a counter that can be used inside non concurrent lambdas (use `AtomicInteger` in concurrent lambdas).

### Improvements

 - Many classes of this project should implement `Serializable` with a sensible implementation.
