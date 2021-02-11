# Collection of Java containers

### Optimized for size efficiency

* `AbstractArrayMap` is an abstract `Map` backed by a growing array of interleaved keys and values. It's very compact but also slow to access. Useful to pass few elements around or if many maps are needed (it doesn't use `Entry` so it's footprint is really small). It's very fast to clone. It uses _cursor_ instead of _entries_.
* `ArrayMap` is a simple `AbstractArrayMap` implementation.
* `SortedArrayMap` is a `Map` implementation that maintains it's keys sorted so obtaining access time of O(log N). Its keys need to implement `Comparable`.
* `ArraySet` is a very compact `Set` implementation backed by an array. Every insertion must scan the entire array for unicity so it's not very efficient O(N).

### Optimized for speed of access

* `AbstractEntryMap` is a very extendable `Map` implementation based on hash table.
* `TableMap` is a compliant `Map` implementation based on hash table.
* `VieweableMap` is a `Map` where `Entry` cannot set values. It provides very fast to obtain _immutable_ and normal _views_.

### Immutable containers

* `ImmutableList` is an explicictly immutable `List` implementation.
* `ImmutableLinkedSet` is a marker interface that states that the implementing class is immutable and maintains input order.
* `ImmutableArraySet` implements `ImmutableLinkedSet` using as little memory as possible (it's slow to access with O(N)).
* `ImmutableHashSet` implements `ImmutableLinkedSet` using an hash table with performances of O(1).
* `ImmutableMap` is an explicitly immutable hash `Map` implementation.


### Different kind of containers

* `BiMap` is a compliant `Map` implementation where value unicity is enforced in addition to usual key unicity. It's symmetrical and its mapping can be reversed on both ways.
* `Matrix` it's a multi-associative map where keys can be associated with multiple values (kind of column headers) but also values can be associated between themselves (by rows). It can be accessed like a bi-dimensional array and by key and column index.
* `MultiMap` it's a map that allows multiple keys for each value. It can generate tree structures that reflect the associations between keys and offers useful methods to inspect and extract data out of it.

### Lambdas helpers

* `Holder` contains a mutable value to be used inside non concurrent lambdas.
* `Counter` contains a counter that can be used inside non concurrent lambdas.

