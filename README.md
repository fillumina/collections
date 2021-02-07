# Collection of Java containers

All containers offer static creator, builder and immutable implementation.

Optimized for **size** efficiency:

* `ArrayMap` and `SortedArrayMap` extend `AbstractArrayMap` which is a standard `Map` implementation apart from the use of a single mutable _cursor_ instead of the classic multiple _entries_ for each mapping. They are based on a single array of `Object`s that grows as needed. It's very compact. The sorted implementation `SortedArrayMap` might improve access time in case of many elements (O(log n)). They are both compact and very fast to clone.
* `ArraySet` is a very compact `Set` implementation backed by an array. Every insertion must scan the entire array for unicity so it's not very efficient.

Optimized for **speed** of access:

* `AbstactEntryMap` has many extension points that makes it extremely flexible. It is slightly slower than `HashMap`.
* `KeyOnlyMap` is an `AbstractEntryMap` implementation that manages only keys. It's `keySet` is a very usable Set implementation which uses less memory than the standard JDK `HashSet`.
* `SetWrapper` uses different types of internal representation depending on the size of the data. It aims for a good balance between space efficiency and speed.

Different kind of containers:

* `BiMap` is a compliant `Map` implementation based on `AbstractEntryMap` where value unicity is enforced in addition to usual key unicity. It's symmetrical and its mapping can be reversed on both ways.
* `Matrix` allows an easy creation (it resizes automatically), manipulation and visualization of bi-dimensional matrices of objects.
* `MultiMap` it's a map that allows multiple keys for each value. It can generate tree structures that reflect the associations between keys and offers useful methods to inspect and extract data out of it.
* `Holder` contains a mutable value to be used inside non concurrent lambdas.
* `Counter` contains a counter that can be used inside non concurrent lambdas.

