# Claude Code — Project Rules

## Source of truth
Architecture, roadmap, and technical decisions live in `.docs/plan/PLANNER.md`. Read it at the start of any non-trivial task.

## After every file operation
- `git add` every newly created file immediately after writing it — never leave new files untracked
- Do not commit unless explicitly asked

## Quick commands to run after refactoring
```
make check   # compile only — catches missing imports and type errors in seconds
make lint    # full detekt + ktlint — run before PRs, not after every edit
```

## Code style
- Packages must match directory structure — no `@file:Suppress("PackageDirectoryMismatch")` workarounds
- `expect`/`actual` is the only platform abstraction mechanism — no `if (platform == ...)` in common code
- No inline platform-specific paths (e.g. `/opt/homebrew`) in `commonMain` — belongs in platform `actual`
- Extract business logic out of `@Composable` functions into services/functions that can be tested independently
- One responsibility per file — don't grow files, split them

## File organisation
```
platform/
    <Platform>Platform.kt       # defaultToGui, platform config
    view/<Platform>View.kt      # platformGui, pickFile, saveFile
    service/<Platform>Service.kt # findHelperBinary, availableDiskSpace
```

## Answers to recurring questions
- **Missing imports after refactor?** → run `make check`
- **App closes instantly on macOS?** → check if `--gui` flag or `defaultToGui` is wired correctly in `Main.kt`
- **Compress button does nothing?** → `qpdf`/`gs` not on PATH and not bundled; check `findHelperBinary` resolution order
- **DMG Finder styling fails (-1743)?** → `create-dmg` needs `--skip-jenkins` or Finder automation permission in System Settings

## What NOT to do
- Don't add `@file:Suppress` to paper over package/directory mismatches — fix the package
- Don't put POSIX-specific code (`which`, `/bin/test`, `/opt/homebrew`) in `commonMain`
- Don't run `make lint` as a blocking step during normal development — it's slow
- Don't skip `git add` for new files
