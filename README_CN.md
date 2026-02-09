# MAT Standalone — Eclipse MAT 堆分析引擎（去 OSGi 独立版）

[English](README.md) | 中文

基于 [Eclipse Memory Analyzer (MAT)](http://eclipse.dev/mat/) 源码，剥离所有 OSGi / Eclipse Runtime 依赖，打包为一个纯 Java 的独立 JAR，可直接嵌入任何 JVM 项目中使用。

## 特性

- 零外部依赖，单 JAR（~1.8MB），Java 8+
- 完整的 HPROF 解析能力（Android / JVM 均支持）
- Dominator Tree 计算
- Retained Size / Shallow Size 计算
- GC Roots 分析
- Shortest Paths to GC Roots（支持排除 weak/soft/phantom reference）
- OQL 查询
- Class Histogram
- Inbound / Outbound 引用遍历

## 构建 & 发布到 mavenLocal

```bash
./gradlew publishToMavenLocal
```

发布后的坐标：

```
org.eclipse.mat:mat-standalone:1.17.0-standalone
```

## 接入方式

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

需要在 `settings.xml` 或 `pom.xml` 中配置 `mavenLocal` 仓库。

## 快速上手

### 打开 HPROF 并获取基本信息

```java
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.eclipse.mat.util.IProgressListener;
import java.io.File;

File hprofFile = new File("/path/to/dump.hprof");
IProgressListener listener = new ConsoleProgressListener();

// 打开 hprof（首次会生成索引文件，后续打开直接复用）
ISnapshot snapshot = SnapshotFactory.openSnapshot(hprofFile, listener);

try {
    System.out.println("对象数: " + snapshot.getSnapshotInfo().getNumberOfObjects());
    System.out.println("堆大小: " + snapshot.getSnapshotInfo().getUsedHeapSize());
    System.out.println("类数量: " + snapshot.getClasses().size());
    System.out.println("GC Roots: " + snapshot.getGCRoots().length);
} finally {
    SnapshotFactory.dispose(snapshot);
}
```

### 按 Retained Size 排序的 Class Histogram

```java
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.util.VoidProgressListener;

IProgressListener voidListener = new VoidProgressListener();

for (IClass cls : snapshot.getClasses()) {
    int[] objectIds = cls.getObjectIds();
    if (objectIds.length == 0) continue;

    long shallowSize = snapshot.getHeapSize(objectIds);
    // getMinRetainedSize: 去重后的 retained size，与 MAT GUI 的 ">=" 值一致
    long retainedSize = snapshot.getMinRetainedSize(objectIds, voidListener);

    System.out.printf("%-50s | %,8d | %,12d | >= %,12d%n",
        cls.getName(), objectIds.length, shallowSize, retainedSize);
}
```

### 获取单个对象的 Retained Size

```java
long retained = snapshot.getRetainedHeapSize(objectId);
long shallow  = snapshot.getHeapSize(objectId);
```

### Shortest Paths to GC Roots（排除弱引用）

```java
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.IPathsFromGCRootsComputer;
import java.util.*;

// 构建排除 map：排除 Reference 子类的 referent 字段
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

// 获取某个对象到 GC Root 的最短路径
IPathsFromGCRootsComputer computer = snapshot.getPathsFromGCRoots(objectId, excludeMap);
int[] path = computer.getNextShortestPath(); // [target, ..., gcRoot]
```

### Dominator Tree 遍历

```java
// 获取直接支配的对象
int[] dominated = snapshot.getImmediateDominatedIds(objectId);

// 获取直接支配者
int dominatorId = snapshot.getImmediateDominatorId(objectId);

// 获取 top-level 祖先（去重用，避免 retained size 重复计算）
int[] topAncestors = snapshot.getTopAncestorsInDominatorTree(objectIds, listener);
```

### 读取对象字段

```java
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.NamedReference;

IObject obj = snapshot.getObject(objectId);

Object value = obj.resolveValue("fieldName");

List<NamedReference> refs = obj.getOutboundReferences();

String className = obj.getClazz().getName();
long address = obj.getObjectAddress();
```

### OQL 查询

```java
import org.eclipse.mat.snapshot.IOQLQuery;

IOQLQuery query = SnapshotFactory.createQuery("SELECT * FROM java.lang.String");
```

## 核心 API 一览

| 类 / 接口 | 说明 |
|---|---|
| `SnapshotFactory` | 入口类，打开/关闭 hprof，创建 OQL 查询 |
| `ISnapshot` | 核心接口，所有分析操作的入口 |
| `IObject` | 堆中对象的抽象，可读取字段、引用 |
| `IClass` | 类的抽象，可获取实例列表、字段定义 |
| `GCRootInfo` | GC Root 信息（类型、上下文） |
| `IPathsFromGCRootsComputer` | 计算到 GC Root 的最短路径 |
| `ConsoleProgressListener` | 控制台进度监听器（开箱即用） |
| `VoidProgressListener` | 静默进度监听器（不输出任何信息） |
| `PrettyPrinter` | 对象值格式化（String、Date 等） |

## ISnapshot 常用方法速查

| 方法 | 说明 | 性能 |
|---|---|---|
| `getClasses()` | 获取所有类 | 快（内存） |
| `getClassesByName(name, includeSub)` | 按名称查找类 | 快（内存） |
| `getObject(objectId)` | 获取对象抽象 | 快（索引） |
| `getHeapSize(objectId)` | Shallow Size | 快（内存/索引） |
| `getRetainedHeapSize(objectId)` | Retained Size（单对象） | 快（索引） |
| `getMinRetainedSize(objectIds, listener)` | 最小 Retained Size（去重） | 快 |
| `getGCRoots()` | 所有 GC Root | 快（内存） |
| `getGCRootInfo(objectId)` | 对象的 GC Root 信息 | 快（内存） |
| `getPathsFromGCRoots(id, excludeMap)` | 到 GC Root 的最短路径 | 中等 |
| `getInboundRefererIds(objectId)` | 入引用 | 快（索引） |
| `getOutboundReferentIds(objectId)` | 出引用 | 快（索引） |
| `getImmediateDominatedIds(objectId)` | Dominator Tree 子节点 | 快 |
| `getImmediateDominatorId(objectId)` | Dominator Tree 父节点 | 快 |
| `getTopAncestorsInDominatorTree(ids, listener)` | 顶层祖先（去重） | 快 |
| `getRetainedSet(objectIds, listener)` | 完整 Retained Set | 慢 |
| `getHistogram(listener)` | 全量 Histogram | 快（内存） |

## 注意事项

- 首次打开 hprof 文件会在同目录生成索引文件（`.index`、`.threads` 等），后续打开直接复用，速度极快
- `getMinRetainedSize` 返回的是近似值（MAT 也标注 `>=`），但速度远快于 `getRetainedSet` + 求和
- `getRetainedHeapSize` 是单对象精确值，但多对象求和会因 dominator 嵌套而重复计算
- 对于 Android hprof，`String.value` 是 `byte[]`（UTF-8），`PrettyPrinter` 默认按 UTF-16 解码可能乱码，建议直接读取 `byte[]` 后用 `new String(bytes, "UTF-8")` 解码
- 本项目基于 Eclipse MAT 1.17.0 源码修改，遵循 [Eclipse Public License 2.0](LICENSE)

## 与原版 MAT 的区别

| | 原版 MAT | mat-standalone |
|---|---|---|
| 运行环境 | Eclipse IDE / RCP | 任意 JVM 项目 |
| 依赖 | OSGi、Eclipse Runtime、SWT | 零依赖 |
| 打包 | 多个 Eclipse 插件 bundle | 单 JAR |
| GUI | 有（Eclipse 插件界面） | 无（纯 API） |
| 分析能力 | 完整 | 核心分析能力完整保留 |
| 报告生成 | 内置 HTML 报告 | 需自行实现 |
