# JVM target (test-only)

This source set provides `actual` implementations for all `expect` declarations so the project compiles against a JVM target.

**The JVM target exists solely to enable `jvmTest`**, where `kotlin-reflect` is available. Reflection is used in `LocaleTest` to enumerate all `AppStrings` properties automatically — Kotlin/Native does not support `KClass.memberProperties`.

All actuals here throw `UnsupportedOperationException`. Nothing in this source set is intended to run in production.

## Why not put the locale test in `commonTest`?

`commonTest` runs on native targets (macosArm64, linuxX64, mingwX64). Kotlin/Native reflection is too limited to enumerate interface properties at runtime, so the test would have to maintain a manual list of every key — defeating the purpose.

## Adding a new platform function

If a new `expect fun` is added to `commonMain`, add a corresponding stub here:

```kotlin
actual fun myNewFunction(): Unit = throw UnsupportedOperationException("JVM target is test-only")
```
