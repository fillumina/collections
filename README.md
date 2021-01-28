# Collection of Java containers

All containers offer static creator, builder, immutable implementation and are very efficient to clone.

Optimized for **size** efficiency:

* `ArrayMap` and `SortedArrayMap` are implementations of `AbstractArrayMap` which is a standard `Map` implementation apart from the use of a single mutable _cursor_ instead of the classic _entry_. They are based on a single array of `Object`s that grows as needed. It's very compact. The sorted implementation `SortedArrayMap` might improve access time in case of many elements (O(log n)). They are both compact and very fast to clone.
* `SmallSet` is a very small `Set` implementation backed by an array. Every insertion must scan the entire array for unicity so it's not very efficient.

Optimized for **speed** of access:

* `AbstactEntryMap` is designed to be quite fast on all operations. It embeds varous implementations. Differenlty from the default java `HashMap` it's very fast to clone and can use a customized `Entry` implementation that makes it extremely flexible.
* `BiMap` is a compliant `Map` implementation based on `AbstractEntryMap` where values unicity are enforced as well as keys. It's simmetrical and its mapping can be reversed on both ways.
* `KeyOnlyMap` is an `AbstractEntryMap` implementation that manages only keys. It's `keySet` is a very usable Set implementation which uses less memory than the standard JDK `HashSet`.
* `SetWrapper` uses different types of internal set represenation depending on the size of the data. It aims for a good balance between space efficiency and speed.

Different kind of containers:

* `Matrix` allows an easy management of a square matrix of objects.
* `IndexedTree` allows to capture a tree structure and rearrange it changing its internal structure much like what can be done with a SQL SELECT in a relational DB.

