# Snapshot based test setup

Fast OpenCms setup for tests. Instead of running the full OpenCms import for every setup, the
import runs **once per fixture per JVM run**, the resulting database is dumped to disk together
with the post-import config and search index, and each later setup boots OpenCms from a cheap
copy of that dump.

This document is the record of what the project achieved and the guidance for when and how to
use it.

## TL;DR — when to use it

- **Use it for new tests that want a fresh, isolated instance per test method**
  (`@BeforeEach`). This is where it pays off: the expensive import is paid once, and every
  method gets a clean instance for the price of a restore (~0.5 s for `simpletest`, ~0.65 s for
  the `alkacon.mercury.test` fixture). Without it, per-method setup would cost the full import
  (seconds to ~16 s) every method and be unusable.
- **Use it for module-based fixtures** (e.g. `alkacon.mercury.test`): the one-time build is
  expensive (~16 s) but amortizes across every method and every class that shares the fixture.
- **Do not bother converting existing setup-light, test-heavy suites.** Measured on
  `org.opencms.file` (42 classes, mostly sharing `simpletest`): the whole package ran ~6m27s
  traditional vs ~5m53s with snapshots — only ~9% faster. The per-setup speedup is real (~4x),
  but setup is only ~20% of that package's runtime; the VFS/publish test bodies dominate, and
  those classes already share one `@BeforeAll` instance across many methods. See
  [Lessons learned](#lessons-learned-paths-already-tried).

## Architecture

- `OpenCmsTestRunner` owns the full setup chain. All `setupOpenCms(...)` overloads funnel into a
  single **terminal** with this signature:

  ```java
  protected CmsObject setupOpenCms(
      TestInfo testInfo, String importFolder, String targetFolder,
      String configFolder, String specialConfigFolder, String servletMapping,
      String defaultWebAppName, boolean publish, List<String> modules)
  ```

  It does the regular import based setup and then, if `modules != null`, imports them via
  `importModules(cms, modules)`. There is also a convenience overload
  `setupOpenCms(testInfo, importFolder, targetFolder, List<String> modules)`.
- `OpenCmsTestSnapRunner extends OpenCmsTestRunner` and **overrides only that terminal** to
  build/restore a snapshot instead of importing. Because every overload funnels into the
  terminal, a test becomes snapshot-backed simply by **changing its parent class** from
  `OpenCmsTestRunner` to `OpenCmsTestSnapRunner` — its own setup code is unchanged. The build
  itself calls `super.setupOpenCms(...)`, so the override cannot recurse.
- **HSQLDB only, with graceful fallback.** The snapshot uses HSQLDB file-database mechanics
  (file copy, `SHUTDOWN`/`CHECKPOINT`, rewriting the jdbc URL). For any other `db.product`, or
  when the build switch disables it, the override transparently falls back to
  `super.setupOpenCms(...)` (the traditional full import). Tests still run on MySQL/Oracle/etc.,
  just without the speedup.

## Usage

Extend `OpenCmsTestSnapRunner` and call `setupOpenCms(...)` as usual.

**Per-method isolation (the recommended pattern for new tests):**

```java
public class TestMyFeature extends OpenCmsTestSnapRunner {

    @BeforeEach
    public void perTest(TestInfo testInfo) {
        setupOpenCms(testInfo, "systemtest", "/", List.of(MODULE_OPENCMS_BASE, "alkacon.mercury.test"));
    }
}
```

Each call returns a fresh, fully isolated instance: changes made by one method never leak into
the next (verified by `org.opencms.test.performance.TestSnapshotSetup`). Per-method teardown is
self-managing — each restore shuts down the previous shell and working database before copying
the fresh one, so no explicit `@AfterEach` cleanup is needed between methods.

**Simple fixtures** use the same calls as the regular runner:

```java
setupOpenCms(testInfo, "simpletest", "/");   // resource folder import
setupOpenCms(testInfo);                       // no fixture import
```

### Importing modules

Pass the module names as the last argument (the `List<String> modules`). They are imported in
order after the base setup. `importModules(...)` handles ordering and the special base module:

- `org.opencms.base` (constant `MODULE_OPENCMS_BASE`) is **special**: its content is not part of
  the VFS import, it is zipped on the fly from `modules/org.opencms.base/resources`, registered,
  and the ADE configuration cache is settled (`waitForCacheUpdate`) before continuing.
- all other modules are imported from the package path (`WEB-INF/packages/<name>.zip`) via
  `importModule`.

**Most modules need `org.opencms.base` first.** Modules that rely on ADE / formatter
configuration (most do) deadlock on import without it — the import blocks forever inside
`CmsImportVersion10.parseLinks` waiting for the formatter configuration cache. So list it first:

```java
setupOpenCms(testInfo, "systemtest", "/", List.of(MODULE_OPENCMS_BASE, "alkacon.mercury.test"));
```

The heavy base + module import logs errors; the runner already suppresses break-on-error during
setup, so no extra handling is needed in the test.

## Disabling the optimization from Gradle

A build switch forces the traditional full import on every setup (the same path as a non-HSQLDB
product), without changing any test class — useful for A/B comparison of the two designs.

```
./gradlew test                                            # optimized (default)
./gradlew test -PnoSnapshot=true                          # traditional, snapshot skipped
./gradlew testSingle --tests "org.opencms.file.*"
./gradlew testSingle --tests "org.opencms.file.*" -PnoSnapshot=true
```

Mechanism: `OpenCmsTestSnapRunner` reads the system property
`opencms.test.snapshot.disabled` (constant `PROP_SNAPSHOT_DISABLED`) and falls back to
`super.setupOpenCms(...)` when it is `true`. The shared `coreTestConfig` in `build.gradle`
forwards it from `-PnoSnapshot` (or `-Dopencms.test.snapshot.disabled`).

## Measured impact (hsqldb)

Per-setup times measured in the test harness (`SnapshotSetupBenchmark`,
`MercurySnapshotBenchmark`):

| Fixture | Traditional setup | Snapshot restore | One-time build | Per-setup speedup |
|---------|-------------------|------------------|----------------|-------------------|
| `simpletest` | ~2.0 s | ~0.48 s | ~4.6 s | ~4.2x |
| `systemtest` + `alkacon.mercury.test` (+ `org.opencms.base`) | ~16.5 s (import) | ~0.65 s | ~16.5 s | ~25x |

Total setup cost for `N` setups of one fixture drops from `N x import` to `build + N x restore`.
The heavier the import and the more setups share it, the bigger the win — which is why the
sweet spot is per-method tests on a module fixture.

The realized speedup of a whole test run is capped by how much of the run is setup. For
setup-heavy work (a module fixture rebuilt per method) that is most of the time; for
setup-light, test-body-heavy suites it is a minority (see Lessons learned).

## How it works

- **Build** (first call per distinct fixture): the connections and OpenCms runtime are pointed
  at a file based hsqldb (`jdbc:hsqldb:file:<dir>/db`) tuned for fast import writes
  (`SET FILES LOG FALSE` + `WRITE DELAY 2000 MILLIS`, safe because the build DB is discarded
  after checkpoint). The regular slow `super.setupOpenCms(...)` runs once (import + module
  import), the build waits for background offline indexing to finish, and `CHECKPOINT DEFRAG` +
  `SHUTDOWN` leave a consistent on-disk database. The post-import RFS folders
  (`WEB-INF/config`, `WEB-INF/index`, `WEB-INF/solr`) are copied into the template. The template
  is cached in a per-fixture map.
- **Restore** (every call): the previous working database is shut down, its directory is
  replaced with a plain file copy of the template, the captured RFS folders are restored
  verbatim, `opencms.properties` is rewritten so the connection points at the working copy, and
  OpenCms boots against it and logs in Admin in the Offline project. No setup scripts, no
  import, no publish.

Databases live under a unique per-JVM temp directory
(`<java.io.tmpdir>/opencms-test-snapshot-<id>`) deleted by a JVM shutdown hook.

### Cache key

The template cache is keyed by all setup arguments (`importFolder`, `targetFolder`,
`configFolder`, `specialConfigFolder`, `servletMapping`, `defaultWebAppName`, `publish`) plus
the module names appended in order. A `null` module list contributes nothing. Distinct argument
combinations therefore build distinct templates; identical ones (e.g. many classes calling
`setupOpenCms(testInfo, "simpletest", "/")`) share one template across the whole JVM run.

### RFS restore (config + search index)

OpenCms config XML files are **mutated during import** (module imports rewrite
opencms-modules.xml, opencms-search.xml, resource types, etc.), and the Lucene/Solr indexes are
built on disk under `WEB-INF/index` and `WEB-INF/solr`. A database-only restore would miss both,
so the RFS folders are always captured after the one-time import and restored verbatim per
setup. This keeps module-importing and search-dependent fixtures faithful. Offline indexing runs
on a background thread, so the build waits for it (`updateOfflineIndexes(...)`) before capturing,
making the captured index complete.

### Why an explicit shutdown before each restore

hsqldb keeps a file database instance registered in the JVM even after all its connections are
closed. Without `shutdownDatabaseQuietly(...)` before replacing the files, a later boot against
the same path would reattach to the stale instance instead of loading the freshly copied
snapshot, silently breaking test isolation. **This is the single most important correctness
detail; do not remove it.**

### Test configuration is assembled from `WEB-INF/base`

`setupOpenCms` rebuilds the live `test/data/WEB-INF/config` folder on every run by copying
`test/data/WEB-INF/base/` (then the db specific `config.<product>/`) into it. Edits made
directly to `WEB-INF/config/*.xml` are overwritten on the next setup. To register a widget or
schema type for tests (for example a `<schematype>` a module's content relies on), edit
`WEB-INF/base/opencms-vfs.xml`. The older test config template lagged behind production; the
`CmsCodeWidget`, `CmsFilterSelectWidget` and `CmsAccessRestrictionWidget` /
`CmsXmlAccessRestrictionValue` entries were added there to match `webapp/WEB-INF/config`.

## Limitations

- **HSQLDB only.** Other `db.product` values fall back to the traditional import (graceful, no
  test changes needed).
- **Serial execution.** A single working directory and the shared live `WEB-INF` RFS folders are
  reused across setups, which assumes tests in a JVM run sequentially (the current model).
  Parallel test execution within one JVM would need per-thread working and RFS directories.
- **The cold build is more expensive than a single traditional setup** (import + index wait +
  RFS capture). A fixture used by only **one** setup is therefore a net loss. The benefit comes
  from reuse — many methods/classes sharing a fixture.

## Lessons learned (paths already tried)

Recorded so they are not re-attempted.

- **Converting existing setup-light suites is not worth it.** `org.opencms.file` (42 classes):
  ~6m27s traditional vs ~5m53s snapshot, ~9% faster. Per-setup is ~4x faster, but setup is only
  ~20% of that suite's runtime and the classes already amortize one `@BeforeAll` instance across
  many methods. The snapshot helps where setup dominates, not where test bodies do.
- **hsqldb `MEMORY` tables instead of `CACHED`:** measured only ~7% faster restore (459 -> 427 ms
  over 5 restores), because restore is dominated by OpenCms boot, not table access. Not worth the
  build-time conversion; the DDL default `CACHED` is kept.
- **Skipping the teardown database drop:** ~15 ms for the file based work DB, far below any
  meaningful share of a class runtime. Left as-is.
- **Database-only restore (skip RFS):** an opt-out was prototyped and removed. RFS restore was
  only ~9-10% slower (~40 ms/setup) and is required for correctness with modules and search, so
  it is now always on.

## Benchmarks

All have no `Test` prefix (except the isolation test) so the slow ones are excluded from the
normal suite; run them manually via `testSingle`. Add `-PnoSnapshot=true` to any of them to get
the traditional baseline.

- **Correctness + isolation** (`org.opencms.test.performance.TestSnapshotSetup`): runs several
  setups, asserts the fixture data is present and that each restore is fresh.

  ```
  ./gradlew testSingle --tests "org.opencms.test.performance.TestSnapshotSetup"
  ```

- **Cold vs warm, `simpletest`** (`org.opencms.test.performance.SnapshotSetupBenchmark`): times
  the one-time build against N warm restores.

  ```
  ./gradlew testSingle --tests "org.opencms.test.performance.SnapshotSetupBenchmark"
  ```

- **Cold vs warm, module fixture** (`org.opencms.test.performance.MercurySnapshotBenchmark`):
  `systemtest` + `org.opencms.base` + `alkacon.mercury.test`.

  ```
  ./gradlew testSingle --tests "org.opencms.test.performance.MercurySnapshotBenchmark"
  ```
