# Collection of Java containers

Optimized for **size** efficiency:

* `ArrayMap` and `SortedArrayMap` extend `AbstractArrayMap` which is a `Map` implementation that makes use of a single mutable _cursor_ instead of the classic multiple _entries_ for each mapping. It is based on a single array of `Object` that grows as needed. It's very compact but also slow. The sorted implementation `SortedArrayMap` might improve access time in case of many elements (O(log n)). They are very fast to clone. It's especially well suited if a lot of small maps (few elements) are needed or if it should be very fast to (shallow) clone.
* `ArraySet` is a very compact `Set` implementation backed by an array. Every insertion must scan the entire array for unicity so it's not very efficient. `SortedArraySet` is awaiting to be done ;-).

Optimized for **speed** of access:

* `AbstactEntryMap` is not dependent upon a specific `Entry` implementation which, togeher with many extension points, makes it very flexible. It's performances are comparable with JDK's `HashMap`.
* `KeyOnlyMap` is an `AbstractEntryMap` implementation that manages only keys. It's `keySet` is a very usable Set implementation which uses less memory than the standard JDK `HashSet`.
* `SetWrapper` uses different types of internal representation depending on the size of the data. It aims for a good balance between space efficiency and speed. (_It's waiting for improvements_.)

Different kind of containers:

* `ImmutableList` is an explicictly immutable list that can be safely shared between objects.
* `ImmutableSet` is an explicitly immutable linked hash set that can be safely shared between objects. `ArraySet` also has an immutable constructor but it's performances are poor.
* `BiMap` is a compliant `Map` implementation based on `AbstractEntryMap` where value unicity is enforced in addition to usual key unicity. It's symmetrical and its mapping can be reversed on both ways.
* `Matrix` it's a multi-associative map, can be created from an existing map, with a builder or just by adding values to the bi-dimensional matrix. It's aimed towards space efficiency and not speed of access.
* `MultiMap` it's a map that allows multiple keys for each value. It can generate tree structures that reflect the associations between keys and offers useful methods to inspect and extract data out of it.
* `Holder` contains a mutable value to be used inside non concurrent lambdas.
* `Counter` contains a counter that can be used inside non concurrent lambdas.

