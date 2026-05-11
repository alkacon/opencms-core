# JUnit Jupiter Usage Notes

This is a quick reference for writing and maintaining OpenCms tests with JUnit Jupiter.

## Class-level annotations

### `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`

**This is already inherited from `OpenCmsTestRunner`.** Do not repeat this annotation on subclasses.

By default JUnit Jupiter creates a new test class instance per test method. `PER_CLASS` changes this to one shared instance per class. This allows `@BeforeAll` to be a non-static method, which is required here because `$openCmsSetUp(TestInfo)` calls instance methods such as `setupOpenCms()` on `this`.

Classes that extend `OpenCmsTestRunner` inherit this lifecycle. Pure unit tests that do not need `OpenCmsTestRunner` can use the default Jupiter lifecycle.

### `@TestMethodOrder`

Controls the order in which test methods run.

Use method ordering only when a migrated legacy test class really depends on a fixed execution sequence. Some OpenCms-backed tests may still contain ordering because their former JUnit 3 `suite()` methods listed test methods in a fixed sequence. Because booting OpenCms is expensive, related test methods shared one initialized OpenCms instance. Later methods may depend on state created by earlier methods. This is legacy behavior, not a pattern for new tests.

- `MethodOrderer.OrderAnnotation.class`: explicit ordering via `@Order(n)` on each method; use only for existing OpenCms-backed tests where legacy execution order must be preserved.

Do not use method-name ordering. Enforcing alphabetical execution can hide or create unwanted dependencies between test methods.

Do not add `@TestMethodOrder` to new tests. New tests should avoid shared state dependencies and should pass independently of execution order. Ordering from migrated classes where it had no effect has been removed, for example for a class with only one `@Test` or a class that does not use an OpenCms database instance.

## Method-level annotations

### `@BeforeAll` on `$openCmsSetUp(TestInfo testInfo)`

Boots OpenCms before the test methods run. Must be non-static, using the `PER_CLASS` lifecycle inherited from `OpenCmsTestRunner`. Always override the base-class declaration:

```java
@Override
@BeforeAll
public void $openCmsSetUp(TestInfo testInfo) {
    setupOpenCms(testInfo, "simpletest", "/");
}
```

### `@Test` and `@Order(n)`

Mark test methods with `@Test`. Avoid `@Order(n)` unless a migrated legacy OpenCms-backed test must preserve its original execution order.

### Assertion parameter order

JUnit 3: `assertTrue(message, condition)`
JUnit Jupiter: `assertTrue(condition, message)`; message is always last.

Jupiter assertions also support lazy message suppliers, for example `assertTrue(condition, () -> message)`.

## On-demand tests

Some legacy test classes were kept as on-demand tests. If a test should not run as part of the normal suite, do not comment out the `@Test` annotation. Keep the test discoverable and mark it with `@Disabled` instead.

```java
@Test
@Disabled("On-demand test")
public void testSomethingOnDemand() {
    // ...
}
```

## Current test shape

OpenCms tests now follow these conventions:

- OpenCms-backed test classes extend `OpenCmsTestRunner`.
- OpenCms-backed tests initialize OpenCms once per class through `$openCmsSetUp(TestInfo)` and `@BeforeAll`.
- OpenCms-backed tests inherit `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` from `OpenCmsTestRunner`; subclasses should not repeat the annotation.
- New tests should not depend on execution order.
- Use Jupiter's message-last assertion parameter order.
- Keep helper classes out of the `Test...` naming pattern unless they are real test classes.
