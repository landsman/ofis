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
- Process execution: `fork+execvp+pipe+waitpid` on POSIX (no shell interpolation, args passed as argv array)

### UI flow (PDF Compressor)
1. File drop zone → select PDF, shows filename + size (e.g. "259.8 KB")
2. Profile selector: HIGH_QUALITY / BALANCED (recommended) / MAXIMUM
3. "Compress PDF" button → progress spinner ("Almost there!")
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
- [x] `expect`/`actual` process runner (POSIX fork+execvp)
- [x] `PdfCompressionService` orchestrating qpdf + Ghostscript
- [x] CLI tool with `--profile` flag
- [x] GUI: file picker, profile selector, progress, result card
- [x] File size display after selection
- [x] Save dialog with suggested filename (`-ofis-compressed-{level}.pdf`)
- [ ] Binary bundling (qpdf + gs per platform) — v2
- [ ] Windows CreateProcessW runner
- [ ] Web UI (wasmJs target removed, revisit)

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

## Guidelines for LLM Interactions
1. **No JVM:** Never use `java.io.*`, `ProcessBuilder`, `Runtime.exec`, or JVM-only libraries in common or native code.
2. **Okio everywhere:** Use `FileSystem` and `Path` for all file operations in common code.
3. **expect/actual for platform ops:** Process execution, file dialogs, GUI entry points — all go through `expect`/`actual`.
4. **No shell strings:** Always pass executable + args as a `NativeCommand(executable, List<String>)` — never concatenate a shell command.
5. **Tool pattern:** New features implement the `Tool` interface and register via `ToolRegistry`.
6. **Build stays fast:** `make build` must stay under 15s on a warm incremental. Don't add plugins or tasks to the default build path.
7. **Lint is separate:** detekt + ktlint run via `make lint`, never as a dependency of `make build`.
8. **Host-only default:** `make build` compiles only the host target. Cross-compilation is explicit (`./gradlew build` or CI).
