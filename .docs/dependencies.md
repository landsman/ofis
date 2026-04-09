# Dependencies

## Kotlin

- **Kotlin Multiplatform** `2.1.10` — https://github.com/JetBrains/kotlin
- **Compose Multiplatform** `1.7.3` — https://github.com/JetBrains/compose-multiplatform
- **Okio** `3.9.1` — https://github.com/square/okio
- **kotlinx.coroutines** `1.9.0` — https://github.com/Kotlin/kotlinx.coroutines
- **detekt** `1.23.8` — https://github.com/detekt/detekt
- **ktlint** `1.5.0` — https://github.com/pinterest/ktlint

## Binary dependencies (PDF compression)

Must be installed on the host for development (`make dev`) and are bundled inside the `.app` for distribution (`make macos_dmg`).

- **qpdf** — lossless PDF stream recompression, always runs as the first pass
  - https://github.com/qpdf/qpdf
  - Releases: https://github.com/qpdf/qpdf/releases
  - Install: `brew install qpdf`

- **Ghostscript (gs)** — lossy image downsampling, runs for BALANCED and MAXIMUM profiles
  - https://www.ghostscript.com
  - Source: https://github.com/ArtifexSoftware/ghostpdl
  - Releases: https://github.com/ArtifexSoftware/ghostpdl/releases
  - Install: `brew install ghostscript`

## macOS packaging tools (dev machine only)

- **create-dmg** — builds the `.dmg` installer with custom layout — https://github.com/create-dmg/create-dmg — `brew install create-dmg`
- **dylibbundler** — copies `.dylib` dependencies into the app bundle and rewrites rpaths — https://github.com/auriamg/macdylibbundler — `brew install dylibbundler`
- **librsvg** (`rsvg-convert`) — converts the SVG icon to PNG/ICNS — https://gitlab.gnome.org/GNOME/librsvg — `brew install librsvg`
