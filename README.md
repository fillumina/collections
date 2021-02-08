# Collection of Java containers

All containers offer static creator, builder and immutable implementation. 

Generally speaking JDK's collections are very well balanced between space and speed and the containers offered here are useful in cases where different features are needed or to be extremely tight in space. One thing I would like to put forward though is the choice they have made to build maps around the concept of _entries_ (a JDK `Map` is a basically a set of entries, just look at `AbstractMap`). Entries are rarely used by themselves and pose limitations on the possible range of optimizations available to the most used case (which is `get(K)` and iterations).

Optimized for **size** efficiency:

* `ArrayMap` and `SortedArrayMap` extend `AbstractArrayMap` which is a `Map` implementation that makes use of a single mutable _cursor_ instead of the classic multiple _entries_ for each mapping. It is based on a single array of `Object` that grows as needed. It's very compact but also slow. The sorted implementation `SortedArrayMap` might improve access time in case of many elements (O(log n)). They are very fast to clone. It's especially well suited if a lot of small maps (few elements) are needed or if it should be very fast to (shallow) clone.
* `ArraySet` is a very compact `Set` implementation backed by an array. Every insertion must scan the entire array for unicity so it's not very efficient. `SortedArraySet` is awaiting to be done ;-).

Optimized for **speed** of access:

* `AbstactEntryMap` is not dependent upon a specific `Entry` implementation which, togeher with many extension points, makes it very flexible. It's performances are comparable with JDK's `HashMap`.
* `KeyOnlyMap` is an `AbstractEntryMap` implementation that manages only keys. It's `keySet` is a very usable Set implementation which uses less memory than the standard JDK `HashSet`.
* `SetWrapper` uses different types of internal representation depending on the size of the data. It aims for a good balance between space efficiency and speed. (_It's waiting for improvements_.)

Different kind of containers:

* `BiMap` is a compliant `Map` implementation based on `AbstractEntryMap` where value unicity is enforced in addition to usual key unicity. It's symmetrical and its mapping can be reversed on both ways.
* `Matrix` allows an easy creation (it resizes automatically), manipulation and visualization of bi-dimensional matrices of objects.
* `MultiMap` it's a map that allows multiple keys for each value. It can generate tree structures that reflect the associations between keys and offers useful methods to inspect and extract data out of it.
* `Holder` contains a mutable value to be used inside non concurrent lambdas.
* `Counter` contains a counter that can be used inside non concurrent lambdas.

