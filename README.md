# Collection of Java containers

In general JDK collections are very well done and optimized but they have made some choices that might have left some interesting corner cases apart. In particular their preference towards **entries** (enforced by the `Map` API) which forces every mapping to use a dedicated object. Java is fast to create and manipulate objects but nonetheless using arrays (i.e. interleaving keys and values) might offer some advantages in performances (because of locality) and cloning (copying an array is very fast). One other thing is that JDK's are usually slow to **clone** and that immutable instances are not declared as such. For this reason defensive (slow) cloning/copying must be repeated over and over. Probably they relied on passing them as parameters rather than as returning values but this isn't always practical or convenient.

Another problem is with **unmodifiable** containers: to create an immutable view JDK wraps a container into an unmodifiable class that uses the original mutable container as delegate. This approach uses a lot of method overriding and must create new entry, keys and value collections and also a new entry every time one is requested. This is very inefficient both on performances and memory usage. The containers here all use an internal state that can be passed to _immutable_ containers in a very efficient way. The immutable container is an implementation of the original one with only one method overridden: `readOnlyCheck()` that could eventually throw an exception if the container should be read-only. That's all.

### Optimized for size efficiency

All those containers are well suited to contain very few elements (say less than 5 and really no more than 10 but I still have to do real performance tests) and perform many more readings than writings (ideally they should be read-only). They are fast to create and clone and very memory efficient. 

* `BaseArrayMap` is an abstract `Map` backed by a growing array of interleaved keys and values. It's very compact but also slow to access O(N). It uses a _cursor_ instead of _entries_: a cursor is a mutable entry that is also an iterator and that changes when iterating over it. It's very fast to iterate on (especially if entries are not used by the implementation) but not 100% compliant with the specification. It doesn't support putting elements which is left to implementing classes.
* `ArrayMap` is a simple `BaseArrayMap` implementation good only for very few entries.
* `SortedArrayMap` is a `BaseArrayMap` implementation that maintains it's keys sorted to obtain access time of O(log N). Its keys need to implement `Comparable`. It's very compact and its performances are decent and can be cloned really fast.
* `SmallSet` is a very compact `Set` implementation backed by an object that would eventually be an array. Every insertion must scan the entire array for unicity so its performances are linear. Because its internal state is an object that can be either the only item or an array it plays badly with reflective tools such as `Kryo` but it really takes the memory efficiency at the extreme.
* `ArraySet` is a very compact `Set` implementation backed by an array. Every insertion must scan the entire array for unicity so its performances are linear. It uses an array instead of an `Object` like `SmallSet` does and because of that it plays nicer with reflective tools such as `Kryo`.
* `SmallList` is a compact array list that grows and shrink as required: its array is sized exactly to contain the actual items. This means that it's slower to add than `ArrayList`.

### Optimized for speed of access

JDK maps aren't very friendly towards extending classes, these maps offer a lot of extending points and utilities that made them very flexible and quite performant at the same time.

* `AbstractEntryMap` is a very _extendable_, _compliant_, `Map` implementation based on hash table with performances of O(1).
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

* `BiMap` is a compliant `Map` implementation where value unicity is enforced in addition to usual key unicity. Its mappings are symmetrical and can be reversed on both ways.
* `Matrix` it's a multi-associative map where keys can be associated to multiple values (like the column headers of a spreadsheet) but also values can be associated between themselves (by rows) so that one can jump from a value in a column (identified by its key/header) to another value on the same row using the target key/header (or index). It can also be accessed like a bi-dimensional array (by row and column) and will grow automatically when a cell is set outside the current dimensions.
* `MultiMap` it's a map that uses multiple keys for each value. It can then be queried to retrieve the content pointed by specific keys. It can generate different `Tree` structures that reflect the different possible associations between keys. 
* `Tree` is a composition of maps where each node is a `Map` implementation and can also contain a value. A Tree can be created from a `MultiMap` following different criteria. A Tree can be flattened by different criteria and can be converted into a single level `Map`.

### Lambdas helpers

* `Holder` contains a mutable value to be used inside non concurrent lambdas (use `AtomicReference` in concurrent lambdas).
* `Counter` contains a counter that can be used inside non concurrent lambdas (use `AtomicInteger` in concurrent lambdas).

### Improvements

One of the good things about having fast cloning collections is that it allows to implement a copy on write algorithm which is very efficient and fast in case of concurrent access (of course with many more readings than writings). In the future I might choose to add some implementations of this. Of course I would gladly accept suggestions and contributions.
