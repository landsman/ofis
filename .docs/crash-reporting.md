# Crash Reporting & Remote Error Observability

## Goal

Capture crashes and unhandled errors on end-user machines and surface them in a dashboard so bugs can be diagnosed and fixed without requiring user action.

---

## What to capture

| Signal | Examples |
|--------|----------|
| **Hard crashes** | SIGSEGV, SIGABRT, SIGILL — process killed by OS |
| **Uncaught Kotlin exceptions** | `NullPointerException`, `IllegalStateException` thrown outside a try/catch |
| **Handled errors logged as events** | Binary not found, compression failed, file save failed |
| **App lifecycle** | Launch, first run, version, locale, OS version |
| **Breadcrumbs** | Last N user actions before the crash (navigate, drop file, compress) |

---

## Recommended stack: Sentry

**Why Sentry:**
- First-class Kotlin Multiplatform SDK (`io.sentry:sentry-kotlin-multiplatform`)
- Native crash capturing (SIGSEGV etc.) via Sentry Native under the hood
- Source-context, breadcrumbs, releases, user impact counts — all built in
- Self-hostable (GDPR-friendly) or cloud
- Free tier covers small user bases

**Alternatives considered:**
- Firebase Crashlytics — mobile-first, weak native desktop support
- Bugsnag — good but more expensive, no KMP SDK
- Custom backend — viable long-term but requires building a pipeline from scratch

---

## Architecture

```
App process
  ├── SentryKMP.init()          ← runs at startup, registers signal + exception handlers
  ├── Logger.kt                 ← existing; bridge to Sentry breadcrumbs
  ├── CrashContext.kt           ← attaches OS, locale, app version, tool name to every event
  └── On crash / unhandled ex   → Sentry SDK serialises to disk
                                 → uploaded on next launch (or immediately if network ok)

Sentry cloud / self-hosted
  ├── Issues list with stack traces + symbolicated frames
  ├── Release tracking (which version introduced the crash)
  └── Alerts → email / Slack when new issue or regression
```

---

## Implementation plan

### Phase 1 — Basic crash capture (1–2 days)

1. Add dependency:
   ```toml
   # gradle/libs.versions.toml
   sentry-kmp = "0.9.0"   # check latest
   ```
   ```kotlin
   // commonMain
   implementation("io.sentry:sentry-kotlin-multiplatform:$sentryKmpVersion")
   // macosArm64 / macosX64 / linuxX64 / mingwX64
   implementation("io.sentry:sentry-kotlin-multiplatform-cocoa:$sentryKmpVersion")
   ```

2. Create `src/common/kotlin/ofis/crash/CrashReporting.kt`:
   ```kotlin
   object CrashReporting {
       fun init(dsn: String, release: String, environment: String) {
           SentryKMP.start {
               it.dsn = dsn
               it.release = release           // "ofis@1.2.3"
               it.environment = environment   // "production" | "debug"
               it.attachStacktrace = true
               it.beforeSend = { event ->
                   // Strip any file paths containing home dir
                   event.sanitisePaths()
                   event
               }
           }
       }

       fun captureError(message: String, extras: Map<String, Any> = emptyMap()) {
           SentryKMP.captureMessage(message, SentryLevel.ERROR) { scope ->
               extras.forEach { (k, v) -> scope.setExtra(k, v.toString()) }
           }
       }

       fun addBreadcrumb(message: String, category: String) {
           SentryKMP.addBreadcrumb(message) { it.category = category }
       }
   }
   ```

3. Call `CrashReporting.init(...)` early in `commonMain()` before any UI.

4. Bridge `Logger.kt` so every `Logger.error(...)` also calls `CrashReporting.addBreadcrumb(...)`.

---

### Phase 2 — Context enrichment (half day)

Attach useful context to every event so you can reproduce:

```kotlin
SentryKMP.configureScope { scope ->
    scope.setTag("os.version", platform.osVersion())
    scope.setTag("locale", currentLocale())
    scope.setTag("app.version", BuildConfig.VERSION)
    scope.setExtra("tool.active", currentTool?.name ?: "none")
}
```

Update context when the user navigates to a tool so the active tool appears in every crash.

---

### Phase 3 — Release tracking & symbolication (1 day)

Native binaries (`.kexe`) produce mangled stack frames. Sentry needs debug symbols to show readable function names.

1. Build with debug symbols preserved:
   ```kotlin
   // build.gradle.kts
   macosArm64 {
       binaries.executable {
           debuggable = true   // in release builds, strip separately after uploading symbols
       }
   }
   ```

2. Upload dSYM / debug info to Sentry as part of the release pipeline:
   ```bash
   # In Makefile / CI
   sentry-cli upload-dif --org <org> --project ofis build/bin/macosArm64/releaseExecutable/
   ```

3. Tag each build with a release version so Sentry can correlate crashes to releases:
   ```bash
   sentry-cli releases new "ofis@$(VERSION)"
   sentry-cli releases finalize "ofis@$(VERSION)"
   ```

---

### Phase 4 — Privacy & opt-in (half day)

Desktop app users expect control. Add an opt-in toggle in **Settings**.

- Default: **off** (conservative — avoid surprising users)
- If opted in: full Sentry reporting
- If opted out: log to local file only (`~/Library/Logs/Ofis/crash.log`)
- Store preference in `AppStorage` under key `crash_reporting_enabled`

```kotlin
if (AppStorage.get(StorageKeys.CRASH_REPORTING_ENABLED) == "true") {
    CrashReporting.init(dsn = BuildConfig.SENTRY_DSN, ...)
}
```

Never include: file contents, file paths outside the app sandbox, user-typed text.

---

### Phase 5 — Local fallback log (half day)

Even when Sentry is opted out, write a structured crash log to disk so users can attach it to a support email.

```
~/Library/Logs/Ofis/ofis-2026-04-11.log
```

Format: JSON Lines — one event per line with timestamp, level, message, thread.

Rotate on launch: keep last 5 files, delete older ones.

---

## DSN & secrets management

- `SENTRY_DSN` must not be hardcoded in source.
- Inject at build time via `BuildConfig` generated from an environment variable:
  ```kotlin
  // build.gradle.kts
  val sentryDsn = System.getenv("SENTRY_DSN") ?: ""
  buildConfigField("String", "SENTRY_DSN", "\"$sentryDsn\"")
  ```
- CI sets `SENTRY_DSN` as a secret. Local dev uses `.env.local` (gitignored).

---

## Alert setup in Sentry

Once live, configure:

| Alert | Condition | Action |
|-------|-----------|--------|
| New issue | First occurrence of unseen crash | Email immediately |
| Regression | Issue reappears after being resolved | Email + Slack |
| Volume spike | >10 events/hour for one issue | Slack |

---

## Files to create

```
src/common/kotlin/ofis/crash/CrashReporting.kt   ← init, captureError, breadcrumb
src/common/kotlin/ofis/crash/LocalCrashLog.kt     ← file-based fallback logger
src/common/kotlin/ofis/config/BuildConfig.kt      ← generated — DSN, version string
.docs/crash-reporting.md                          ← this file
```

## Files to modify

```
src/common/kotlin/ofis/Main.kt              ← call CrashReporting.init() early
src/common/kotlin/ofis/config/Logger.kt    ← forward errors to breadcrumbs
src/common/kotlin/ofis/storage/StorageKeys.kt  ← add CRASH_REPORTING_ENABLED
src/common/kotlin/ofis/ui/view/settings/SettingsView.kt  ← opt-in toggle
gradle/libs.versions.toml                   ← sentry-kmp version
build.gradle.kts                            ← dependency + BuildConfig generation
Makefile                                    ← sentry-cli upload-dif in release target
```
