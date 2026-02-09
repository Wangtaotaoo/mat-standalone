# MAT Standalone — Eclipse MAT Heap Analysis Engine (OSGi-Free)

A standalone, zero-dependency Java library extracted from [Eclipse Memory Analyzer (MAT)](http://eclipse.dev/mat/). All OSGi and Eclipse Runtime dependencies have been stripped, producing a single JAR that can be embedded in any JVM project.

## Features

- Zero external dependencies, single JAR (~1.8 MB), Java 8+
- Full HPROF parsing (Android and JVM heap dumps)
- Dominator tree computation
- Retained size / shallow size calculation
- GC roots analysis
- Shortest paths to GC roots (with weak/soft/phantom reference exclusion)
- OQL (Object Query Language) support
- Class histogram
- Inbound / outbound reference traversal

## Build & Publish

```bash
./gradlew publishToMavenLocal
```

Published coordinates:

```
org.eclipse.mat:mat-standalone:1.17.0-standalone
```

## Integration

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenLocal()
}

dependencies {
    implementation("org.eclipse.mat:mat-standalone:1.17.0-standalone")
}
```

### Gradle (Groovy)

```groovy
repositories {
    mavenLocal()
}

dependencies {
    implementation 'org.eclipse.mat:mat-standalone:1.17.0-standalone'
}
```

### Maven

```xml
<dependency>
    <groupId>org.eclipse.mat</groupId>
    <artifactId>mat-standalone</artifactId>
    <version>1.17.0-standalone</version>
</dependency>
```

Ensure your local Maven repository is configured.

## Quick Start

### Open an HPROF and Get Basic Info

```java
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.eclipse.mat.util.IProgressListener;
import java.io.File;

File hprofFile = new File("/path/to/dump.hprof");
IProgressListener listener = new ConsoleProgressListener();

// Opens the hprof (generates index files on first run, reuses them afterwards)
ISnapshot snapshot = SnapshotFactory.openSnapshot(hprofFile, listener);

try {
    System.out.println("Objects:  " + snapshot.getSnapshotInfo().getNumberOfObjects());
    System.out.println("Heap:     " + snapshot.getSnapshotInfo().getUsedHeapSize());
    System.out.println("Classes:  " + snapshot.getClasses().size());
    System.out.println("GC Roots: " + snapshot.getGCRoots().length);
} finally {
    SnapshotFactory.dispose(snapshot);
}
```

### Class Histogram Sorted by Retained Size

```java
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.util.VoidProgressListener;

IProgressListener voidListener = new VoidProgressListener();

for (IClass cls : snapshot.getClasses()) {
    int[] objectIds = cls.getObjectIds();
    if (objectIds.length == 0) continue;

    long shallowSize = snapshot.getHeapSize(objectIds);
    // Deduplicated retained size — matches MAT GUI's ">=" values
    long retainedSize = snapshot.getMinRetainedSize(objectIds, voidListener);

    System.out.printf("%-50s | %,8d | %,12d | >= %,12d%n",
        cls.getName(), objectIds.length, shallowSize, retainedSize);
}
```

### Single Object Retained Size

```java
long retained = snapshot.getRetainedHeapSize(objectId);
long shallow  = snapshot.getHeapSize(objectId);
```

### Shortest Paths to GC Roots (Excluding Weak References)

```java
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.IPathsFromGCRootsComputer;
import java.util.*;

// Build exclude map: skip "referent" field on Reference subclasses
Map<IClass, Set<String>> excludeMap = new HashMap<>();
String[] refClasses = {
    "java.lang.ref.WeakReference",
    "java.lang.ref.SoftReference",
    "java.lang.ref.PhantomReference",
    "java.lang.ref.Reference",
    "java.lang.ref.FinalizerReference",
    "sun.misc.Cleaner"
};
for (String name : refClasses) {
    Collection<IClass> classes = snapshot.getClassesByName(name, true);
    if (classes != null) {
        for (IClass cls : classes) {
            excludeMap.put(cls, new HashSet<>(Arrays.asList("referent")));
        }
    }
}

// Compute shortest path from an object to its GC root
IPathsFromGCRootsComputer computer = snapshot.getPathsFromGCRoots(objectId, excludeMap);
int[] path = computer.getNextShortestPath(); // [target, ..., gcRoot]
```

### Dominator Tree Traversal

```java
// Direct dominatees
int[] dominated = snapshot.getImmediateDominatedIds(objectId);

// Immediate dominator
int dominatorId = snapshot.getImmediateDominatorId(objectId);

// Top-level ancestors (for correct retained size summation without double-counting)
int[] topAncestors = snapshot.getTopAncestorsInDominatorTree(objectIds, listener);
```

### Reading Object Fields

```java
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;

IObject obj = snapshot.getObject(objectId);

Object value = obj.resolveValue("fieldName");

List<NamedReference> refs = obj.getOutboundReferences();

String className = obj.getClazz().getName();
long address = obj.getObjectAddress();
```

### OQL Query

```java
import org.eclipse.mat.snapshot.IOQLQuery;

IOQLQuery query = SnapshotFactory.createQuery("SELECT * FROM java.lang.String");
```

## Core API Reference

| Class / Interface | Description |
|---|---|
| `SnapshotFactory` | Entry point — open/close hprof, create OQL queries |
| `ISnapshot` | Central interface for all analysis operations |
| `IObject` | Abstraction of a heap object — read fields, references |
| `IClass` | Abstraction of a Java class — list instances, field definitions |
| `GCRootInfo` | GC root metadata (type, context) |
| `IPathsFromGCRootsComputer` | Computes shortest paths to GC roots |
| `ConsoleProgressListener` | Progress listener that prints to stdout |
| `VoidProgressListener` | Silent progress listener |
| `PrettyPrinter` | Formats object values (String, Date, etc.) |

## ISnapshot Method Cheat Sheet

| Method | Description | Performance |
|---|---|---|
| `getClasses()` | All classes | Fast (in-memory) |
| `getClassesByName(name, includeSub)` | Find classes by name | Fast (in-memory) |
| `getObject(objectId)` | Get object abstraction | Fast (index) |
| `getHeapSize(objectId)` | Shallow size | Fast |
| `getRetainedHeapSize(objectId)` | Retained size (single object, exact) | Fast (index) |
| `getMinRetainedSize(ids, listener)` | Min retained size (deduplicated, approximate) | Fast |
| `getGCRoots()` | All GC root IDs | Fast (in-memory) |
| `getGCRootInfo(objectId)` | GC root info for an object | Fast (in-memory) |
| `getPathsFromGCRoots(id, excludeMap)` | Shortest paths to GC roots | Medium |
| `getInboundRefererIds(objectId)` | Objects referencing this object | Fast (index) |
| `getOutboundReferentIds(objectId)` | Objects referenced by this object | Fast (index) |
| `getImmediateDominatedIds(objectId)` | Dominator tree children | Fast |
| `getImmediateDominatorId(objectId)` | Dominator tree parent | Fast |
| `getTopAncestorsInDominatorTree(ids, listener)` | Top ancestors (for dedup) | Fast |
| `getRetainedSet(ids, listener)` | Full retained set | Slow |
| `getHistogram(listener)` | Full histogram | Fast (in-memory) |

## Notes

- On first open, index files (`.index`, `.threads`, etc.) are generated alongside the hprof file. Subsequent opens reuse them and are near-instant.
- `getMinRetainedSize` returns an approximate lower bound (MAT labels it `>=`), but is much faster than computing the full retained set.
- `getRetainedHeapSize` is exact per object, but summing across multiple objects can double-count due to dominator nesting — use `getMinRetainedSize` for class-level aggregation.
- For Android hprof files, `String.value` is a `byte[]` (UTF-8). `PrettyPrinter` defaults to UTF-16 decoding which may produce garbled output. Read the raw `byte[]` and decode with `new String(bytes, "UTF-8")` instead.
- Based on Eclipse MAT 1.17.0 source. Licensed under [Eclipse Public License 2.0](LICENSE).

## Comparison with Original MAT

| | Original MAT | mat-standalone |
|---|---|---|
| Runtime | Eclipse IDE / RCP | Any JVM project |
| Dependencies | OSGi, Eclipse Runtime, SWT | None |
| Packaging | Multiple Eclipse plugin bundles | Single JAR |
| GUI | Eclipse plugin UI | None (API only) |
| Analysis capabilities | Full | Core analysis fully preserved |
| Report generation | Built-in HTML reports | Bring your own |
