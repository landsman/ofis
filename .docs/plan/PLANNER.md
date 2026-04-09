# Ofis Project Planner & Guidelines

This document serves as a persistent planner and guideline for the "Ofis" project.
LLMs and developers should use this as a source of truth for the project's architecture, roadmap, and technical decisions.

## Project Vision
"Ofis" is a Kotlin Multiplatform (KMP) toolset for office-related tasks (PDF compression, image/video transformation, etc.) that runs locally on Windows, macOS, and Linux as independent native binaries and in the browser via WebAssembly (Wasm).

## Tech Stack
- **Language:** Kotlin 2.x
- **Multiplatform:** Kotlin Multiplatform (KMP)
- **Targets:**
  - `macosArm64`, `macosX64` (primary development targets)
  - `linuxX64`, `mingwX64` (native binaries)
  - *Future:* `ios`, `android`
- **UI Framework:** Compose Multiplatform (experimental macOS native)
- **File I/O:** Okio (`FileSystem`, `Path`) — shared across all targets
- **Dependencies:** Avoid JVM-dependent libraries. Use `expect`/`actual` for platform ops.

## Architecture

### Build
- `make build` — compiles host binary only (`linkDebugExecutableMacosArm64`), no lint, ~7s incremental
- `make gui` — launches GUI via Compose
- `make lint` — detekt + ktlint (separate from build, run in CI)
- `make test` — host-platform tests only
- Gradle: configuration cache + build cache enabled, daemon on, 4GB heap

### PDF Compression engine
- **qpdf** — always runs (lossless stream recompression)
- **Ghostscript (gs)** — BALANCED (`/ebook`, 144 DPI) and MAXIMUM (`/screen`, 96 DPI) profiles only
- Both must be installed on system PATH (`brew install qpdf ghostscript`)
- Binary bundling deferred to v2
- Process execution: **NSTask** on macOS, `fork+execvp+pipe+waitpid` on Linux, stub on Windows

#### Process runner — per-platform notes
| Target  | Implementation                                                                    | File                                   |
|---------|-----------------------------------------------------------------------------------|----------------------------------------|
| macOS   | `NSTask` (Foundation) — avoids fork() malloc deadlocks in multi-threaded GUI apps | `macosMain/.../NativeProcessRunner.kt` |
| Linux   | POSIX `fork+execvp+pipe+waitpid`                                                  | `linuxMain/.../NativeProcessRunner.kt` |
| Windows | **TODO** — `CreateProcess` + anonymous pipes                                      | `mingwMain/.../NativeProcessRunner.kt` |

**macOS NSTask lessons learned:**
- Use `executableURL = NSURL.fileURLWithPath(path)` + `launchAndReturnError(errorPtr.ptr)` — the old `launchPath`/`launch()` APIs are deprecated and stripped from Kotlin/Native 2.x bindings
- `readDataToEndOfFile()` is deprecated; use `readDataToEndOfFileAndReturnError(null)`
- Always check `launchAndReturnError` return value **before** reading pipes — if it returns false, the pipe write-end stays open and the read blocks forever
- `waitUntilExit()` (NSTaskConveniences category) is unresolved in Kotlin/Native 2.x; poll `task.running` instead
- Capture `NSError` from `launchAndReturnError(errorPtr.ptr)` for diagnostics — `null` swallows the failure reason

**`findHelperBinary` popen bug (fixed):**
- `fgets` fills only part of the 256-byte buffer; the rest stays as `\0` bytes
- `buf.decodeToString()` decodes all 256 bytes → string ends with `…path\n\0\0\0…`
- `.trim()` stops at `\0` (not whitespace) so the `\n` is never removed
- Fix: `.substringBefore('\u0000').trim()` — cut at null terminator first

**Linux `runProcess`** uses the same POSIX approach as the original but with argv built **before** `fork()` (malloc is not async-signal-safe in a forked child of a multi-threaded process).

**Windows `runProcess`** — currently a stub returning exit code 1. Needs `CreateProcess` with `STARTUPINFO` pipe redirection. The `mingwMain/NativeProcessRunner.kt` file exists but is unimplemented.

### UI flow (PDF Compressor)
1. File drop zone → select PDF, shows filename + size (e.g. "259.8 KB")
2. Profile selector: HIGH_QUALITY / BALANCED (recommended) / MAXIMUM
3. "Compress PDF" button → progress spinner (spinner visible immediately; "Almost there!" text after 3 s)
4. Result card: original → compressed size, % saved, "already optimized" warning if no gain
5. "Save compressed PDF" (CTA blue) → NSSavePanel with suggested name `{base}-ofis-compressed-{level}.pdf`
6. "Compress another file" (secondary grey)

### Platform abstraction pattern
All platform ops are `expect`/`actual`. Common code never calls platform APIs directly.
- `runProcess` — POSIX on Unix, Win32 stub on Windows, will need JNI/NDK on Android
- `pickFile` / `saveFile` — NSSavePanel/NSOpenPanel on macOS, stubs elsewhere
- Android: when added, `actual` implementations swap in without touching common code

## Roadmap

### Phase 1: Foundation ✅
- [x] KMP project structure with Native targets
- [x] Tool interface + ToolRegistry
- [x] Makefile with fast host-only build (~7s incremental, ~600ms no-change)
- [x] Compose Multiplatform GUI on macOS native
- [x] `--gui` flag wired to platform GUI

### Phase 2: PDF Compression ✅ (core done)
- [x] Compression profiles: HIGH_QUALITY / BALANCED / MAXIMUM
- [x] `expect`/`actual` process runner — NSTask on macOS, fork+execvp on Linux
- [x] `PdfCompressionService` orchestrating qpdf + Ghostscript
- [x] `PdfCompressionService` injectable (`binaryFinder`, `processRunner`) for unit testing
- [x] 11 unit tests in `commonTest` (binary-not-found, process failure, profile routing, metrics, already-optimal)
- [x] CLI tool with `--profile` flag
- [x] GUI: file picker, profile selector, progress spinner, result card
- [x] File size display after selection
- [x] Save dialog with suggested filename (`-ofis-compressed-{level}.pdf`)
- [x] User-friendly error toasts (exit 127 → "not installed" message)
- [x] Debug timing logs via `Logger` (`[timing] find qpdf`, `[timing] qpdf`, `[timing] gs`, `[timing] total`)
- [ ] Binary bundling (qpdf + gs per platform) — v2
- [ ] Windows `CreateProcess` runner (`mingwMain/NativeProcessRunner.kt` is a stub)
- [ ] Linux `findHelperBinary` — implement `apt`/`dpkg` path discovery (currently only checks fixed paths)
- [ ] Web UI (wasmJs target removed, revisit)

#### `findHelperBinary` — per-platform status
| Target  | Strategy                                                                                                   | Status                         |
|---------|------------------------------------------------------------------------------------------------------------|--------------------------------|
| macOS   | (1) app bundle `Contents/MacOS/`, (2) `popen("which $name")` + null-trim fix, (3) hardcoded Homebrew paths | ✅ working                      |
| Linux   | Fixed paths: `/usr/bin/$name`, `/usr/local/bin/$name`                                                      | ⚠️ needs `which`/`dpkg` lookup |
| Windows | Fixed paths: `C:\Program Files\...` stubs                                                                  | ❌ not implemented              |

### Phase 3: Image Transformations
- [ ] Image conversion (PNG ↔ JPEG ↔ WebP)
- [ ] Image resizing and optimization
- [ ] Batch processing

### Phase 4: Video Transformations
- [ ] Format conversion (native ffmpeg interop)

### Phase 5: Mobile & Packaging
- [ ] iOS and Android targets (Android needs JNI process runner or libqpdf.so)
- [ ] App bundle packaging with bundled binaries
- [ ] Auto-update

## Known pitfalls

- **`buf.decodeToString()` after `fgets`** — always `.substringBefore('\u0000').trim()`, never `.trim()` alone; the buffer tail is `\0` bytes which are not whitespace and block trim from reaching the `\n`.
- **NSTask pipe deadlock** — set pipes on task, then call `launchAndReturnError`. If you read from the pipe *before* checking the launch result and the task failed to start, `readDataToEndOfFileAndReturnError` blocks forever (write-end never closes). Check launch first.
- **Deprecated Foundation APIs in Kotlin/Native 2.x** — `NSTask.launchPath`, `NSTask.launch()`, `NSFileHandle.readDataToEndOfFile()`, `NSFileHandle.closeFile()` are all `API_DEPRECATED_WITH_REPLACEMENT` and absent from bindings. Use `executableURL`, `launchAndReturnError`, `readDataToEndOfFileAndReturnError`, `closeAndReturnError`.
- **`NSTask.waitUntilExit()` unresolved** — it's in the `NSTaskConveniences` ObjC category which Kotlin/Native 2.x doesn't expose as a direct method. Poll `task.running` instead.
- **fork() in multi-threaded process** — build argv (`allocArray`, `cstr.getPointer`) **before** `fork()`. Heap allocation in a forked child of a multi-threaded process is unsafe (malloc lock may be held by another thread).
- **ktlint `filter { exclude(...) }` uses relative paths** — `**/build/**` does NOT match generated files; use `**/generated/**` which matches the relative path `ofis/generated/resources/Res.kt`.

## Guidelines for LLM Interactions
1. **No JVM:** Never use `java.io.*`, `ProcessBuilder`, `Runtime.exec`, or JVM-only libraries in common or native code.
2. **Okio everywhere:** Use `FileSystem` and `Path` for all file operations in common code.
3. **expect/actual for platform ops:** Process execution, file dialogs, GUI entry points — all go through `expect`/`actual`.
4. **No shell strings:** Always pass executable + args as a `NativeCommand(executable, List<String>)` — never concatenate a shell command.
5. **Tool pattern:** New features implement the `Tool` interface and register via `ToolRegistry`.
6. **Build stays fast:** `make build` must stay under 15s on a warm incremental. Don't add plugins or tasks to the default build path.
7. **Lint is separate:** detekt + ktlint run via `make lint`, never as a dependency of `make build`.
8. **Host-only default:** `make build` compiles only the host target. Cross-compilation is explicit (`./gradlew build` or CI).
