# JUnit Jupiter Tests in OpenCms

This document describes the Java test cases in OpenCms core from version 22 forward.
Starting with this version, core tests are written and maintained as JUnit Jupiter tests on the JUnit 6 platform.
It is intended as a quick reference for developers familiar with older JUnit 3 OpenCms tests and as a brief overview of the completed migration.

## Cheat Sheet

### OpenCms test architecture

- `OpenCmsTestRunner` is the base class for OpenCms core Jupiter tests.
- It extends `Assertions`, so `assertEquals(...)`, `assertTrue(...)`, `assertThrows(...)`, `fail(...)` and the other Jupiter assertions can be called directly.
- It provides OpenCms setup helpers such as `setupOpenCms(...)`, `getCmsObject()` and OpenCms-specific assertions such as `assertFilter(...)` and `assertXmlEquals(...)`.
- It is annotated with `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`. Subclasses inherit this automatically.
- It has class lifecycle methods:
  - `@BeforeAll $openCmsSetUp(TestInfo)`: override when the class needs an OpenCms setup. The override would usually call `setupOpenCms(...)` (see example below).
  - `@AfterAll $openCmsTearDown(TestInfo)`: normally inherited; override only for extra class cleanup.
- It has method lifecycle methods:
  - `@BeforeEach $testStart(TestInfo)`: inherited logging.
  - `@AfterEach $testEnd(TestInfo)`: available for cleanup, but normally not overridden.
- The `TestInfo` parameter is JUnit Jupiter's per-test descriptor and provided by default; passing it through to `setupOpenCms(...)` is how the test name reaches `OpenCmsTestRunner`.
- Tests that do not need an OpenCms database setup should not override `$openCmsSetUp(...)`.

### Test with OpenCms setup

```java
public class TestFoo extends OpenCmsTestRunner {

    @Override
    @BeforeAll
    public void $openCmsSetUp(TestInfo testInfo) {

        setupOpenCms(testInfo, "simpletest", "/");
    }

    @Test
    public void testSomething() throws Exception {

        CmsObject cms = getCmsObject();
        assertNotNull(cms);
    }
}
```

Other setup options exist for special configuration folders, custom configuration folders, publish control and servlet mapping / webapp overrides.
See the `setupOpenCms(...)` overloads in `OpenCmsTestRunner` and follow nearby tests that use a similar fixture.

### Test without OpenCms setup

```java
public class TestFooUtil extends OpenCmsTestRunner {

    @Test
    public void testSomething() {

        assertEquals("foo", normalize(" foo "));
    }
}
```

Do not add `$openCmsSetUp(...)` if no OpenCms instance is needed.
A plain Jupiter test class without `OpenCmsTestRunner` is also fine if it does not need OpenCms helpers.

### Common annotations

- `@Test`: Marks a test method. Keep the existing `public void test...()` style.
- `@Disabled("reason")`: Skips an on-demand or temporarily disabled test. Use this instead of commenting out tests.
- `@BeforeAll`: Class setup. Inherited from `OpenCmsTestRunner`, overwrite if specific setup steps are needed.
- `@AfterAll`: Class cleanup. Usually not required. Use this for extra cleanup beyond the runner.
- `@BeforeEach`: Per-test setup, for example mock objects or fresh per-method state.
- `@AfterEach`: Per-test cleanup, for example clearing mock state.
- `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)`: Preserves a required fixed method order.
- `@Order(n)`: Gives the method position when `@TestMethodOrder(...)` uses order annotations.
- `@DisplayName("...")`: Changes the name shown in test reports.
- `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`: Already inherited from `OpenCmsTestRunner`; do not repeat it on subclasses.

Other useful Jupiter annotations:

- `@Tag("...")`: Tags tests for filtering or grouping.
- `@Timeout(...)`: Fails a test or lifecycle method if it runs too long.
- `@TempDir`: Injects a temporary directory.
- `@RepeatedTest(n)`: Runs the same test multiple times.
- `@Nested`: Groups related tests in an inner class.
- `@ParameterizedTest`: Runs the same test with multiple input values.
- `@TestFactory`: Creates dynamic tests at runtime.
- `@ExtendWith(...)`: Registers a Jupiter extension.

These annotations are not used in the current OpenCms tests, but they are normal Jupiter features and can be used when they fit the test.

### Assertions, failures and skips

JUnit Jupiter puts the message last:

```java
assertTrue(condition, "message");
assertEquals(expected, actual, "message");
```

Use Jupiter exception assertions:

```java
assertThrows(CmsException.class, () -> cms.readResource("/missing"));
```

Use assumptions for optional runtime requirements:

```java
Assumptions.assumeTrue(config != null, message);
```

This marks the test as skipped instead of failed.
Use this for tests that require external configuration that might not be always available, for example AI provider credentials.

### Run tests with Gradle

This section uses two environment variables for paths that vary per user.
Typical values (replace the prefix with your own workspace location):

```bash
export OPENCMS_WORKSPACE=~/workspace/opencms-core
export OPENCMS_BUILD_DIR=~/workspace/BuildCms
```

From the worktree:

```bash
cd ${OPENCMS_WORKSPACE}

TEST_FQCN=org.opencms.file.TestCopy
TEST_METHOD=testCopyFolderRecursive
TEST_PACKAGE=org.opencms.util

./gradlew --no-daemon testSingle --tests ${TEST_FQCN}
./gradlew --no-daemon testSingle --tests ${TEST_FQCN}.${TEST_METHOD}
./gradlew --no-daemon testSingle --tests "${TEST_PACKAGE}.*"
./gradlew --no-daemon testSingle --tests org.opencms.file.TestCopy --tests org.opencms.util.TestCmsStringUtil
```

Use `--no-daemon` for OpenCms test runs so the command uses a fresh Gradle process.

`testSingle` is configured to rerun by default and clears its previous XML and HTML reports before running.
This is equivalent to the task-local `--rerun` behavior for `testSingle`: the selected tests are executed again instead of relying on previous task outputs.
This is especially important for migrated legacy test classes with explicit `@Order(...)`:
if Gradle skips a previously successful ordered run, setup done by earlier ordered tests may be missing for later tests and the class can fail for the wrong reason.

Add `--rerun-tasks` when the whole task graph should be forced, for example to rerun compilation and resource processing as well.

Useful related commands:

```bash
./gradlew --no-daemon compileTestJava
./gradlew --no-daemon test
```

The Gradle `Test` tasks in OpenCms use `ignoreFailures = true`, so always check the test result, not only the Gradle exit code.
Reports are written below `${OPENCMS_BUILD_DIR}/reports/tests/`.

### Verbose test output

Only **errors** and **warnings** are written on stdout when running tests.
To restore the OpenCms JUnit 3 behavior with full **info** channel logging and `CmsShell` output on stdout,
pass `-Dopencms.test.verbose=true`, or set the `OPENCMS_TEST_VERBOSE=true` environment variable:

```bash
./gradlew --no-daemon testSingle --tests ${TEST_FQCN} -Dopencms.test.verbose=true
```

### Run tests in Eclipse

1. Import or refresh the project as a Gradle project.
2. Open a test class or method and run it with `Run As > JUnit Test`.
3. To run all tests in one package, select the package in Package Explorer and run it with `Run As > JUnit Test`.
4. To run several selected tests, select multiple classes or create a JUnit run configuration for the package or source folder.
5. For OpenCms setup tests, add these VM arguments to the JUnit run configuration:

```text
-Dtest.data.path=${OPENCMS_WORKSPACE}/test/data
-Dtest.webapp.path=${OPENCMS_WORKSPACE}/webapp
-Dtest.project.path=${OPENCMS_WORKSPACE}
-Dtest.build.folder=${OPENCMS_WORKSPACE}/bin/test
```

Add any test-specific properties in the same place, for example `-DLLM_PROVIDER=...` for AI tests.

## Migration Overview

The JUnit 3 to JUnit Jupiter migration for OpenCms core is complete in version 22.
The main migration step was replacing `OpenCmsTestCase` with `OpenCmsTestRunner`, which provides the OpenCms lifecycle, setup helpers and assertions without JUnit 3 inheritance.

- JUnit 3 `suite()` methods and string constructors were removed.
- Jupiter annotations now mark tests, lifecycle methods and any required execution order.
- `OpenCmsTestRunner` supplies `@TestInstance(PER_CLASS)` to all subclasses.
- OpenCms-backed tests initialize OpenCms once per class in `$openCmsSetUp(TestInfo)`.
- Static `suite()` ordering was replaced with `@TestMethodOrder(MethodOrderer.OrderAnnotation.class)` and `@Order(...)`.
- Legacy method order was preserved where the previous suites required it.
- Previous `AllTests` classes were removed.
- Gradle test discovery runs `Test*.class` tests on the JUnit Platform.
