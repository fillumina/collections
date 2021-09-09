# Collection of Java containers

### Optimized for size efficiency

* `BaseArrayMap` is an abstract `Map` backed by a growing array of interleaved keys and values. It's very compact but also slow to access. Useful to pass few elements around or if many maps are needed (it doesn't use `Entry` so it's footprint is really small). It's very fast to clone. It uses a _cursor_ instead of _entries_. It doesn't implement `put()` which is left to implement to its extending classes.
* `ArrayMap` is a simple `AbstractArrayMap` implementation good only for very few entries.
* `SortedArrayMap` is a `AbstractArrayMap` implementation that maintains it's keys sorted to obtain access time of O(log N). Its keys need to implement `Comparable`. It's very compact and its performances are decent.
* `SmallSet` is a very compact `Set` implementation backed by an object that would eventually be an array. Every insertion must scan the entire array for unicity so its performances are linear. Because its internal state is an object that can be either the only item or an array it plays badly with reflective tools such as `Kryo` but it really takes the memory efficiency at the extreme.
* `ArraySet` is a very compact `Set` implementation backed by an array. Every insertion must scan the entire array for unicity so its performances are linear. It uses an array instead of an `Object` like `SmallSet` and because of that it plays nicer with reflective tools such as `Kryo`.
* `SmallList` is a compact array list that grows and shrink as required: its array is sized exactly to contain the actual items. This means that it's slower to add than `ArrayList`.

### Optimized for speed of access

* `AbstractEntryMap` is a very extendable, compliant, `Map` implementation based on hash table with performances of O(1).
* `TableMap` is an `AbstractEntryMap` implementation useful to extend (otherwise just prefer JDK's `HashMap`) or to use because of it's builder.
* `VieweableMap` is `AbstractEntryMap` implementation where `Entry` cannot set values. It provides an _unmodifiable view_ that share its internal data so it's very fast to create and occupies no extra memory useful to pass internal data to clients in a safe way.

### Immutable containers

The Java JDK library uses defensive object copying extensively to avoid having an object changed unexpectedly by another actor provoking all sort of _side effect_ type of bugs. These can be avoided by passing _immutable_ objects when needed. The JDK offers several ways to create immutable containers but all of them fail to explicitly present them as such. So even an immutable container must be defensively copied over and over when being passed to other objects because they have no way to verify that. The containers listed here are _guaranteed_ immutable and can be used and passed directly without the need of an expensive defensive copy.

* `ImmutableList` is an immutable `List` implementation.
* `ImmutableSmallSet` is an immutable set using as little memory as possible with linear access time.
* `ImmutableArraySet` is an immutable set backed by an array with linear access time.
* `ImmutableLinkedHashSet` is an immutable set using an hash table with performances of O(1).
* `ImmutableHashMap` is an immutable hash `Map` implementation with performances of O(1).
* `ImmutableArrayMap` is an immutable array backed `Map` implementation with performances of O(N) but very tight memory requirements.
* `ImmutableSortedArrayMap` is an immutable sorted array backed `Map` implementation with performances of O(log N) and very tight memory requirements. Its keys must implement `Comparable`. Prefer `ImmutableArrayMap` for very few elements (i.e. less than 7) because of the overhead in managing bisections.
* `ImmutableSmallList` is the immutable version of `SmallList`.

Note that all these immutable containers can be used as _viewers_ to mutable ones and in that case their content could be changed by them. This is made by design so the immutable container could be easily built and exported (exactly like the JDK `Collections.unmodifiable...()`).

### Different kind of containers

* `BiMap` is a compliant `Map` implementation where value unicity is enforced in addition to usual key unicity. It's symmetrical and its mapping can be reversed on both ways.
* `Matrix` it's a multi-associative map where keys can be associated with multiple values (kind of column headers) but also values can be associated between themselves (by rows). It can be accessed like a bi-dimensional array and by key and column index.
* `MultiMap` it's a map that allows multiple keys for each value. It can generate different `Tree` structures that reflect the different possible associations between keys.
* `Tree` is a multi node tree. Each branch is selected via usual key mapping. Each node of the Tree can contain a value. A Tree can be created from a `MultiMap` following different criteria. A Tree can be flattened by different criteria and can be converted into a single level `Map`.

### Lambdas helpers

* `Holder` contains a mutable value to be used inside non concurrent lambdas (use `AtomicReference` in concurrent lambdas).
* `Counter` contains a counter that can be used inside non concurrent lambdas (use `AtomicInteger` in concurrent lambdas).

