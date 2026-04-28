# Gradle 9 Migration

## Current State

The `opencms-core` build now runs on Gradle `9.4.1` and the main build surface has been restored for normal development use.

- `./gradlew help`, `./gradlew tasks --all`, `./gradlew jar` and `./gradlew tlddocZip` run successfully.
- `./gradlew test` runs the OpenCms test suite again under Gradle 9.
- `./gradlew testSingle --tests ...` works again for normal single-test execution.
- The Java 25 native-access warnings emitted by Gradle and test workers have been removed by granting the required native access explicitly.
- The migration is not fully complete from a test-quality perspective: the full suite still has two known failing tests.
- Configuration cache support was investigated but is intentionally not part of the retained migration changes at this stage.

Known remaining issues:

- The full suite is not clean yet. The current known failures are `org.opencms.jsp.util.TestCmsStringTemplateResolver#testRenderDate` and `org.opencms.importexport.TestCmsImportExport#testExportType`.
- `testSingle --tests class.method` is still not method-precise for some legacy JUnit 3 style test classes. In those cases Gradle runs the whole class even though a single method was requested.
- The shared `Test` configuration still uses `ignoreFailures = true`, so the build can report `BUILD SUCCESSFUL` even when individual tests fail. This behavior already existed before the migration and was not changed.

## Changes

### 1. Wrapper Upgrade to Gradle 9.4.1

The wrapper in `gradle/wrapper/gradle-wrapper.properties` was updated to Gradle `9.4.1` and pinned with the matching SHA-256 checksum.

This was necessary because the previous wrapper level was not sufficient for the target Java runtime in this migration. The wrapper upgrade is the foundation for every other build-script change; without it the rest of the port is irrelevant because the build does not start in the intended runtime.

Verification:

- Run `./gradlew --version` and confirm `Gradle 9.4.1`.
- Confirm that the distribution URL points to `gradle-9.4.1-bin.zip`.

### 2. Explicit Native Access Grant for Java 25

Java 25 requires native access to be granted explicitly. Gradle itself uses native libraries through `net.rubygrapefruit.platform`, and the test runtime also exercises libraries that trigger the same check. Without an explicit grant, every build emitted restricted-method warnings before the test task even started.

This was not fixed by hiding output. The build now authorizes the required native access in the places where the JVM is actually started:

- `gradle.properties` sets `org.gradle.jvmargs=--enable-native-access=ALL-UNNAMED` for the Gradle daemon / launcher JVM.
- `gradlew` and `gradlew.bat` add the same flag to `DEFAULT_JVM_OPTS` so the wrapper bootstrap JVM is also covered.
- `build.gradle` adds the same flag to all `Test` tasks so forked test worker JVMs are covered as well.

This combination is important. Adding the flag only to test tasks is not enough, because the first warning came from the Gradle JVM itself before any test worker was forked.

Verification:

- Run `./gradlew --no-daemon testSingle --tests org.opencms.util.TestCmsDateUtil.testHttpDateTimeZoneUsage`.
- Confirm that the old `WARNING: A restricted method in java.lang.System has been called` message no longer appears.

### 3. Plugin Resolution and Repository Setup for Gradle 9

`settings.gradle` now defines `pluginManagement` repositories explicitly and includes the Liferay repository used by the build's plugin resolution path.

This was necessary because Gradle 9 is less forgiving about implicit plugin resolution. The build uses custom plugin resolution logic for `org.opencms.modules`, and the plugin artifacts involved are not all available from the default plugin portal alone. Without explicit repository and resolution setup, a clean environment can fail very early during settings and plugin resolution.

What was done:

- Added `pluginManagement.repositories` to `settings.gradle`.
- Included the Liferay repository before `mavenCentral()` and `gradlePluginPortal()`.
- Kept the existing `resolutionStrategy` that maps `org.opencms.modules*` plugin IDs to `org.opencms:opencms-gradle-plugin:3.+`.

Verification:

- Run `./gradlew help` in a clean environment.
- The build should configure successfully without manual repository tweaks.

### 4. Repository Property Handling Was Hardened

The repository setup in `build.gradle` now guards the `additional_repositories` property before splitting it.

This change was needed because the default property value is empty. Under Gradle 9, blindly splitting and iterating that empty value can produce invalid or meaningless repository entries. The build now adds extra Maven repositories only when the property exists and contains non-whitespace content.

What was done:

- Added a trimmed-content check around `additional_repositories`.
- Filtered out empty segments before adding repositories.

Verification:

- Run `./gradlew help` with the default `gradle.properties`.
- Confirm the build does not fail due to malformed repository configuration.

### 5. Build Script APIs Were Moved to Gradle 9-Compatible Forms

Several parts of `build.gradle` were moved away from APIs that Gradle 9 has removed or tightened. The most important theme is that archive and output handling now uses current provider-based properties instead of legacy names and conventions.

This was required because Gradle 9 no longer accepts a number of legacy task properties and artifact wiring patterns that older Gradle versions tolerated.

What was done:

- The build output location is defined through `layout.buildDirectory.set(file(build_directory))` instead of relying on older `buildDir` handling.
- Archive-producing tasks consistently use modern properties such as `archiveBaseName`, `archiveFileName`, `archiveClassifier` and `destinationDirectory`.
- The old `archives` era behavior was preserved by explicitly collecting the legacy archive tasks and wiring them into `assemble` through `tasks.named('assemble') { dependsOn(...) }`.

The reason for the explicit `assemble` wiring is important. The build historically produced a large set of jars, zips and docs as part of its normal artifact surface. Gradle 9 no longer supports the removed `archives`-style integration in the same way, so `assemble` had to be reconnected deliberately in order to preserve the expected outputs.

Verification:

- Run `./gradlew assemble`.
- Confirm that the expected artifacts are still produced in `../BuildCms/libs`, `../BuildCms/distributions` and the docs output directories.
- Confirm that `opencms.jar` and `opencms.war` still retain their expected names.

### 6. TLDDoc Generation Was Reimplemented Without the Old External Plugin

The old TLDDoc plugin path was not kept as-is. Instead, TLDDoc generation was rebuilt locally in `build.gradle` using standard Gradle tasks.

This was necessary because the previous external TLDDoc builder path was not compatible with Gradle 9, but TLDDoc task names are part of the existing build surface and should remain available. Dropping TLDDoc entirely would have reduced functionality; keeping the old plugin would have blocked the migration.

What was done:

- Added a dedicated `tlddocTool` dependency in `dependencies.gradle` pointing to `taglibrarydoc:tlddoc:1.3`.
- Added `validateTLD` to parse and validate the TLD input with `XmlParser`.
- Added `copyTLDDocResources` to stage static documentation resources.
- Added `tlddoc` as a `JavaExec` task that runs `com.sun.tlddoc.TLDDoc` directly.
- Added `tlddocZip` to preserve the zipped documentation artifact.

This keeps the task names `validateTLD`, `copyTLDDocResources`, `tlddoc` and `tlddocZip` intact while removing the Gradle 9-incompatible plugin dependency.

Verification:

- Run `./gradlew tlddocZip`.
- Confirm that `../BuildCms/docs/tld.zip` is produced.
- Confirm that the generated docs are based on `webapp/WEB-INF/opencms.tld`.

### 7. Dependency Declarations Were Normalized for the Current DSL

`dependencies.gradle` was normalized to current dependency declaration forms that Gradle 9 accepts reliably.

This was needed because older dependency notation patterns, especially around classified artifacts and ad hoc tool dependencies, are more brittle under current Gradle versions. The build uses a large dependency set across multiple source sets, including source artifacts and tool jars, so the DSL had to be made explicit and uniform.

What was done:

- Kept the existing dependency graph and source-set structure.
- Rewrote declarations into the current method-call style used throughout the file.
- Used explicit `artifact { ... }` blocks where classifier-specific artifacts are required.
- Added the `tlddocTool` configuration entry used by the replacement TLDDoc task.

The goal here was compatibility, not dependency churn. This migration does not intentionally change the runtime library set beyond what was necessary to keep the existing build logic expressible under Gradle 9.

Verification:

- Run `./gradlew compileJava compileTestJava gwtClasses setupClasses`.
- Run `./gradlew tlddoc`.
- The tasks should resolve dependencies and execute without dependency-notation errors.

### 8. XML Parser Import Was Made Explicit

`build.gradle` now imports `groovy.xml.XmlParser` explicitly.

This was necessary because the build script uses `XmlParser` in several places, including manifest parsing and TLD validation. Under the current Gradle / Groovy runtime, relying on implicit resolution is less robust. Making the import explicit removes ambiguity and keeps script compilation predictable.

Verification:

- Run `./gradlew help` or `./gradlew tlddocZip`.
- The build script should compile and configure without `XmlParser` resolution errors.

### 9. The Main Test Task Was Restored Through a JUnit Bridge

The main `test` task now targets `org/opencms/test/AllTestsBridge.class`, and a new file `test/org/opencms/test/AllTestsBridge.java` was added.

This was necessary because the OpenCms suite entry point is a legacy JUnit 3 `AllTests` class. Under Gradle 9, the old setup no longer resulted in reliable discovery of that suite entry. The observable symptom was that Gradle reported that no tests were found for the requested includes.

What was done:

- Added `AllTestsBridge.java` as a JUnit 4 bridge using `@RunWith(org.junit.runners.AllTests.class)`.
- The bridge delegates to the existing legacy suite through `org.opencms.test.AllTests.suite()`.
- The `test` task now includes `org/opencms/test/AllTestsBridge.class`.
- `scanForTestClasses = true` is enabled for the `test` task so Gradle discovers the bridge correctly.

This is a compatibility shim. It does not replace the legacy suite, and it does not change the intended suite composition. It only gives Gradle 9 a suite entry point that it can discover reliably.

Verification:

- Run `./gradlew --no-daemon cleanTest test`.
- Confirm that the suite executes instead of failing with `No tests found for given includes`.
- Confirm that `../BuildCms/test-results/test/TEST-org.opencms.test.AllTestsBridge.xml` is produced.

### 10. `testSingle` Was Repaired for Gradle 9 Execution

The `testSingle` task in `build.gradle` was adjusted so it works again as a dedicated single-test entry point under Gradle 9.

This was necessary because the previous task wiring was too loose for Gradle 9's task model. The task initially failed during execution and result handling instead of actually running the requested test.

What was done:

- `testSingle` now uses `classpath = sourceSets.test.runtimeClasspath`.
- `testSingle` now sets `testClassesDirs = files(sourceSets.test.java.classesDirectory)` explicitly.
- `doNotTrackState(...)` was added because this task is intentionally ad hoc: it depends on command-line filtering and produces transient result paths.

These changes make `testSingle` self-describing enough for Gradle 9 to execute it consistently in the normal non-configuration-cache case.

Verification:

- Run `./gradlew --no-daemon testSingle --tests org.opencms.util.TestCmsDateUtil.testHttpDateTimeZoneUsage`.
- Run `./gradlew --no-daemon testSingle --tests org.opencms.jsp.search.config.parser.simplesearch.preconfiguredrestrictions.TestRestrictionsBean.testValueHandling`.
- Both commands should complete successfully.

Important limitation:

- For some legacy JUnit 3 style classes, `--tests class.method` still causes the whole class to execute. That behavior is still present and should be treated as a separate legacy test-discovery issue, not as a Gradle 9 blocker.

## Verification Summary

The following commands were used to verify the migrated build surface:

- `./gradlew --version`
- `./gradlew --no-daemon help`
- `./gradlew --no-daemon tasks --all`
- `./gradlew --no-daemon jar`
- `./gradlew --no-daemon tlddocZip`
- `./gradlew --no-daemon cleanTest test`
- `./gradlew --no-daemon testSingle --tests org.opencms.util.TestCmsDateUtil.testHttpDateTimeZoneUsage`

The build is therefore in a usable Gradle 9 state, with the remaining work concentrated in test cleanup rather than in basic build compatibility.

## Deferred Items

Configuration cache was investigated and can be made to work for repeated identical `testSingle` invocations, but the supporting changes were intentionally not retained. The current recommendation is to leave configuration cache disabled until there is a concrete performance need and time to maintain the additional compatibility constraints.
