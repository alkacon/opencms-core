# JUnit Jupiter Migration Notes

## Class-level annotations

### `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`

Required on any test class that boots OpenCms (i.e. calls `setupOpenCms()` in `$openCmsSetUp`).

By default JUnit Jupiter creates a new test class instance per test method. `PER_CLASS` changes this to one shared instance per class, which allows `@BeforeAll` to be a non-static method — a requirement for `$openCmsSetUp` to call `setupOpenCms()` on `this`.

Classes that do not boot OpenCms (pure unit tests or config-only tests) omit this annotation and use the default lifecycle.

### `@TestMethodOrder`

Controls the order in which test methods run.

- `MethodOrderer.OrderAnnotation.class` — explicit ordering via `@Order(n)` on each method; use when the legacy suite listed tests in a specific sequence.
- `MethodOrderer.MethodName.class` — alphabetical ordering; use when the legacy suite used `TestSuite(Class)` or `generateTestSuite(Class)`, both of which order by method name.

## Method-level annotations

### `@BeforeAll` on `$openCmsSetUp(TestInfo testInfo)`

Replaces the JUnit 3 `TestSetup.setUp()` wrapper. Must be non-static (requires `PER_CLASS`). Always override the base-class declaration:

```java
@Override
@BeforeAll
public void $openCmsSetUp(TestInfo testInfo) {
    setupOpenCms(testInfo, "simpletest", "/");
}
```

### `@Test` and `@Order(n)`

Each test method that was included in the legacy suite gets `@Test`. Methods that were excluded from the legacy suite (commented out in `AllTests.java`) are kept but left unannotated — no `@Test`, no `@Disabled`.

### Assertion parameter order

JUnit 3: `assertTrue(message, condition)`
JUnit Jupiter: `assertTrue(condition, message)` — message is always last.

## On-demand / excluded tests

Test classes not included in any legacy `AllTests` suite (run only on demand) have all `@Test` annotations commented out (`// @Test`). The methods and the class itself are preserved so they can be run manually.
