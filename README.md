# Ofis

Multiplatform toolset for office tasks (PDF compression, and more). 
Runs as a native binary on macOS, Linux, and Windows, with a Compose GUI on desktop.

## Quick start

```sh
make gui       # launch GUI
make cli ARGS="pdf-compress input.pdf"  # run a tool from CLI
make help      # list all commands
```

## Docs

- [Dependencies](.docs/dependencies.md) — all libraries and binary tools (qpdf, ghostscript, etc.)
- [Releasing for macOS](.docs/release-macos.md) — unsigned and signed/notarized DMG release guide
- [Architecture & roadmap](.docs/plan/PLANNER.md) — project vision, tech decisions, and feature roadmap
