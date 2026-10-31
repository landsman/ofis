# Detect OS and Architecture
OS   := $(shell uname -s)
ARCH := $(shell uname -m)

ifeq ($(OS),Darwin)
    TARGET_SUFFIX  := MacosArm64
    HOST_TEST_TASK := macosArm64Test jvmTest
    BIN_DIR        := macosArm64
else ifeq ($(OS),Linux)
    TARGET_SUFFIX  := LinuxX64
    HOST_TEST_TASK := linuxX64Test jvmTest
    BIN_DIR        := linuxX64
else
    TARGET_SUFFIX  := MingwX64
    HOST_TEST_TASK := allTests
    BIN_DIR        := mingwX64
endif

GRADLE  := ./gradlew

# Not in Ubuntu's archive, so Linux takes the upstream package. Its digest is the one GitHub lists.
OXIPNG_DEB        := oxipng_10.2.1-1_amd64.deb
OXIPNG_DEB_URL    := https://github.com/oxipng/oxipng/releases/download/v10.2.1/$(OXIPNG_DEB)
OXIPNG_DEB_SHA256 := 4c3e7440c3ebf94b0d5a67d3add25b9a40897c7f14678ecdb5db3c7b77ff816f
VERSION := $(shell git describe --tags --abbrev=0 2>/dev/null | sed 's/^v//' || echo "1.0.0")

# ── Setup ─────────────────────────────────────────────────────────────────────
.PHONY: install install-bins uninstall-bins lock

# Download all Kotlin/Gradle dependencies into the local cache
install:
	$(GRADLE) dependencies --configuration commonMainImplementation

# Rewrite gradle.lockfile after changing a dependency by hand
lock:
	$(GRADLE) dependencies --configuration jvmRuntimeClasspath --write-locks

# Install binary dependencies (macOS: Homebrew — Ubuntu/Debian: apt)
install-bins:
ifeq ($(OS),Darwin)
	@command -v brew >/dev/null || { echo "Error: Homebrew not found - install from https://brew.sh"; exit 1; }
	# pdf compression
	brew install qpdf ghostscript librsvg
	# png optimization
	brew install oxipng
	# bundler
	brew install create-dmg dylibbundler
else ifeq ($(OS),Linux)
	@command -v apt-get >/dev/null || { echo "Error: apt-get not found - only Ubuntu/Debian is supported"; exit 1; }
	@[ "$$(id -u)" = "0" ] || { echo "Error: run with sudo: sudo make install-bins"; exit 1; }
	apt-get update -qq
	# pdf compression
	apt-get install -y qpdf ghostscript librsvg2-bin
	# png optimization
	curl -fsSL -o /tmp/$(OXIPNG_DEB) $(OXIPNG_DEB_URL)
	echo "$(OXIPNG_DEB_SHA256)  /tmp/$(OXIPNG_DEB)" | sha256sum -c -
	apt-get install -y /tmp/$(OXIPNG_DEB)
else
	@echo "Error: install-bins is not supported on $(OS)"; exit 1
endif

# Uninstall binary dependencies installed by install-bins
uninstall-bins:
ifeq ($(OS),Darwin)
	@command -v brew >/dev/null || { echo "Error: Homebrew not found"; exit 1; }
	brew uninstall --ignore-dependencies qpdf ghostscript librsvg oxipng create-dmg dylibbundler || true
else ifeq ($(OS),Linux)
	@command -v apt-get >/dev/null || { echo "Error: apt-get not found - only Ubuntu/Debian is supported"; exit 1; }
	@[ "$$(id -u)" = "0" ] || { echo "Error: run with sudo: sudo make uninstall-bins"; exit 1; }
	apt-get remove -y qpdf ghostscript librsvg2-bin oxipng || true
else
	@echo "Error: uninstall-bins is not supported on $(OS)"; exit 1
endif

# ── Development ───────────────────────────────────────────────────────────────
.PHONY: check build run gui cli debug

# Fast compile check: catches missing imports and type errors without linking (~seconds with config cache)
check:
	$(GRADLE) compileKotlin$(TARGET_SUFFIX)

# Fast build: host binary only, no linting
build:
	$(GRADLE) linkDebugExecutable$(TARGET_SUFFIX)

# Run (pass ARGS="..." for tool args)
run:
	$(GRADLE) runDebugExecutable$(TARGET_SUFFIX) -Pargs="$(ARGS)"

# GUI mode
gui: macos_icon
	$(GRADLE) runDebugExecutable$(TARGET_SUFFIX) -Pargs="--gui"

# CLI mode
cli:
	$(GRADLE) runDebugExecutable$(TARGET_SUFFIX) -Pargs="--cli $(ARGS)"

# Debug mode (verbose logging)
debug:
	$(GRADLE) runDebugExecutable$(TARGET_SUFFIX) -Pargs="--debug $(ARGS)"

# ── Testing & quality ─────────────────────────────────────────────────────────
.PHONY: test scripts-test lint format locale locale-test

# Tests: host platform only
test:
	$(GRADLE) $(HOST_TEST_TASK) --rerun

# Every tracked *.test.sh, found by git rather than listed, so a new test needs no new target.
# rc rather than `|| exit 1`, so one run reports every failing test, not just the first.
scripts-test:
	mise exec shellcheck -- shellcheck $$(git ls-files '*.sh')
	@rc=0; for t in $$(git ls-files '*.test.sh'); do echo "== $$t"; "./$$t" || { echo "FAIL $$t"; rc=1; }; done; exit $$rc

# Linting (separate from build — run in CI or on demand)
lint:
	$(GRADLE) detekt ktlintCheck

# Code formatting
format:
	$(GRADLE) ktlintFormat

# ── Localisation ──────────────────────────────────────────────────────────────
I18N_DIR := src/common/kotlin/ofis/i18n

# List all supported locales and their string counts
locale:
	@echo "Supported locales:"
	@for f in $(I18N_DIR)/*Strings.kt; do \
		name=$$(basename $$f .kt); \
		count=$$(grep -c '^\s' $$f || true); \
		echo "  $$name  ($$count lines)"; \
	done
	@echo ""
	@echo "To add a new locale:"
	@echo "  1. Copy $(I18N_DIR)/EnStrings.kt → $(I18N_DIR)/XxStrings.kt"
	@echo "  2. Translate all string values"
	@echo "  3. Add LOCALE entry to $(I18N_DIR)/AppLanguage.kt"
	@echo "  4. Wire it in $(I18N_DIR)/LanguagePreference.kt (toAppStrings)"
	@echo "  5. Add label strings to AppStrings.kt and all *Strings.kt files"

# Run locale completeness test only (fast — JVM, no native compile)
locale-test:
	$(GRADLE) jvmTest --rerun --tests "ofis.i18n.LocaleTest"


# ── macOS release ─────────────────────────────────────────────────────────────
.PHONY: macos_icon macos_dmg macos_sign macos_notarize macos_release

# Icon generation (requires rsvg-convert: brew install librsvg)
macos_icon:
	@mkdir -p build/icon.iconset
	@rsvg-convert -w 16 -h 16 src/macos/resources/icon.svg -o build/icon.iconset/icon_16x16.png
	@rsvg-convert -w 32 -h 32 src/macos/resources/icon.svg -o build/icon.iconset/icon_16x16@2x.png
	@rsvg-convert -w 32 -h 32 src/macos/resources/icon.svg -o build/icon.iconset/icon_32x32.png
	@rsvg-convert -w 64 -h 64 src/macos/resources/icon.svg -o build/icon.iconset/icon_32x32@2x.png
	@rsvg-convert -w 128 -h 128 src/macos/resources/icon.svg -o build/icon.iconset/icon_128x128.png
	@rsvg-convert -w 256 -h 256 src/macos/resources/icon.svg -o build/icon.iconset/icon_128x128@2x.png
	@rsvg-convert -w 256 -h 256 src/macos/resources/icon.svg -o build/icon.iconset/icon_256x256.png
	@rsvg-convert -w 512 -h 512 src/macos/resources/icon.svg -o build/icon.iconset/icon_256x256@2x.png
	@rsvg-convert -w 512 -h 512 src/macos/resources/icon.svg -o build/icon.iconset/icon_512x512.png
	@rsvg-convert -w 1024 -h 1024 src/macos/resources/icon.svg -o build/icon.iconset/icon_512x512@2x.png
	@iconutil -c icns build/icon.iconset -o build/AppIcon.icns
	@rsvg-convert -w 1024 -h 1024 src/macos/resources/icon.svg -o build/mac_os_app_icon.png \
		|| echo "Warning: rsvg-convert not found (brew install librsvg)"

# DMG packaging using create-dmg (brew install create-dmg)
macos_dmg: macos_icon
	@if [ "$(OS)" != "Darwin" ]; then echo "Error: DMG creation only supported on macOS"; exit 1; fi
	@command -v create-dmg >/dev/null || { echo "Error: install create-dmg: brew install create-dmg"; exit 1; }
	@command -v dylibbundler >/dev/null || { echo "Error: install dylibbundler: brew install dylibbundler"; exit 1; }
	@command -v qpdf >/dev/null 2>&1 || { echo "Error: install qpdf: brew install qpdf"; exit 1; }
	@command -v gs >/dev/null 2>&1 || { echo "Error: install ghostscript: brew install ghostscript"; exit 1; }
	@[ -x "$$(brew --prefix)/bin/oxipng" ] || { echo "Error: install oxipng: brew install oxipng"; exit 1; }
	$(GRADLE) linkReleaseExecutable$(TARGET_SUFFIX)
	@rm -rf build/Ofis.app
	@mkdir -p build/Ofis.app/Contents/MacOS build/Ofis.app/Contents/Resources build/Ofis.app/Contents/Frameworks
	@cp build/bin/$(shell echo $(TARGET_SUFFIX) | sed 's/M/m/')/releaseExecutable/Ofis.kexe \
		build/Ofis.app/Contents/MacOS/Ofis
	@cp src/macos/resources/Info.plist build/Ofis.app/Contents/Info.plist
	@cp build/AppIcon.icns build/Ofis.app/Contents/Resources/AppIcon.icns
	@echo "Bundling helper binaries (qpdf, gs, oxipng)..."
	@cp $$(which qpdf) build/Ofis.app/Contents/MacOS/qpdf
	@cp $$(which gs) build/Ofis.app/Contents/MacOS/gs
# From Homebrew by path: with mise.toml pinning oxipng, `which` finds mise's shim, a script.
	@cp "$$(brew --prefix)/bin/oxipng" build/Ofis.app/Contents/MacOS/oxipng
	@echo "Bundling dylib dependencies..."
# qpdf links @rpath/libqpdf, whose rpath (@loader_path/../lib) points nowhere once copied, so
# Homebrew's lib/ is searched. A library still not found makes dylibbundler ask on stdin, and
# with no terminal it asks forever; `yes quit` answers, so it fails at once instead.
	yes quit | dylibbundler -od -b -s "$$(brew --prefix)/lib" \
		-x build/Ofis.app/Contents/MacOS/qpdf \
		-x build/Ofis.app/Contents/MacOS/gs \
		-x build/Ofis.app/Contents/MacOS/oxipng \
		-d build/Ofis.app/Contents/Frameworks/ \
		-p @executable_path/../Frameworks/
# Last, once nothing in the bundle changes. The linker's own signature seals no Info.plist and
# no resources, so a downloaded copy is "damaged" with no way past it; an ad-hoc one (no account)
# turns that into the ordinary not-notarized prompt. macos_sign replaces it with the real one.
	codesign --force --deep --sign - build/Ofis.app
	@rm -f build/Ofis.dmg
	create-dmg \
		--volname "Ofis" \
		--volicon build/AppIcon.icns \
		--window-pos 200 120 \
		--window-size 600 400 \
		--icon-size 100 \
		--icon "Ofis.app" 175 190 \
		--hide-extension "Ofis.app" \
		--app-drop-link 425 190 \
		--skip-jenkins \
		build/Ofis.dmg \
		build/Ofis.app
	@echo "DMG created: build/Ofis.dmg"

# Code signing — requires SIGN_ID='Developer ID Application: Your Name (TEAMID)'
macos_sign: macos_dmg
	@[ -n "$(SIGN_ID)" ] || { echo "Error: set SIGN_ID='Developer ID Application: Your Name (TEAMID)'"; exit 1; }
	codesign --deep --force --verify --verbose \
		--sign "$(SIGN_ID)" \
		--options runtime \
		build/Ofis.app
	codesign --verify --deep --strict build/Ofis.app
	@echo "Signed: build/Ofis.app"

# Notarize + staple — requires APPLE_ID, APPLE_TEAM_ID, APPLE_APP_PASSWORD
macos_notarize: macos_sign
	xcrun notarytool submit build/Ofis.dmg \
		--apple-id "$(APPLE_ID)" \
		--team-id "$(APPLE_TEAM_ID)" \
		--password "$(APPLE_APP_PASSWORD)" \
		--wait
	xcrun stapler staple build/Ofis.dmg
	@echo "Notarized and stapled: build/Ofis.dmg"

# Publish macOS DMG to GitHub Releases (requires gh: brew install gh)
macos_release:
	@command -v gh >/dev/null || { echo "Error: install gh: brew install gh"; exit 1; }
	gh release create "v$(VERSION)" build/Ofis.dmg \
		--title "Ofis v$(VERSION)" \
		--generate-notes
	@echo "Released v$(VERSION) to GitHub."

# ── Linux release ─────────────────────────────────────────────────────────────
.PHONY: linux_package linux_release

# Linux package: tar.gz archive of the binary
linux_package:
	@[ "$(OS)" = "Linux" ] || { echo "Error: linux_package is Linux-only; use macos_dmg on macOS"; exit 1; }
	$(GRADLE) linkReleaseExecutable$(TARGET_SUFFIX)
	@rm -f build/ofis-$(VERSION)-linux-x64.tar.gz
	tar -czf build/ofis-$(VERSION)-linux-x64.tar.gz \
		-C build/bin/linuxX64/releaseExecutable Ofis.kexe
	@echo "Package: build/ofis-$(VERSION)-linux-x64.tar.gz"

# Publish Linux package to GitHub Releases (requires gh: apt install gh)
linux_release:
	@command -v gh >/dev/null || { echo "Error: install gh: apt install gh"; exit 1; }
	gh release create "v$(VERSION)" build/ofis-$(VERSION)-linux-x64.tar.gz \
		--title "Ofis v$(VERSION)" \
		--generate-notes
	@echo "Released v$(VERSION) to GitHub."

# ── Maintenance ───────────────────────────────────────────────────────────────
.PHONY: clean help

# Clean
clean:
	$(GRADLE) clean
	@rm -rf build/icon.iconset
	@rm -f build/AppIcon.icns
	@rm -f build/mac_os_app_icon.png
	@rm -rf build/Ofis.app
	@rm -rf build/dmg-staging
	@rm -f build/Ofis.dmg
	@rm -f build/ofis-*-linux-x64.tar.gz

help:
	@echo "Usage: make <target> [ARGS=\"...\"]"
	@echo ""
	@echo "  install          — download all Kotlin/Gradle dependencies into local cache"
	@echo "  install-bins     — install binary deps: macOS via Homebrew, Ubuntu/Debian via apt"
	@echo "  uninstall-bins   — uninstall binary deps installed by install-bins"
	@echo "  check            — compile only, catches missing imports/type errors fast"
	@echo "  build            — compile host binary (fast, no lint)"
	@echo "  run              — build + run  (ARGS='pdf-compress input.pdf')"
	@echo "  gui              — launch GUI"
	@echo "  cli              — launch CLI  (ARGS='pdf-compress input.pdf')"
	@echo "  debug            — run with --debug flag"
	@echo "  test             — run tests for host platform"
	@echo "  scripts-test     — shellcheck every shell script, run every *.test.sh (no network)"
	@echo "  lint             — static analysis + style (detekt + ktlint)"
	@echo "  format           — auto-fix style issues"
	@echo "  locale           — list supported locales and instructions for adding new ones"
	@echo "  locale-test      — run locale completeness test only (fast)"
	@echo ""
	@echo "  macos_icon       — convert SVG icon to PNG and ICNS"
	@echo "  macos_dmg        — package app as DMG (macOS only, requires create-dmg, dylibbundler, qpdf, ghostscript)"
	@echo "  macos_sign       — sign app with Developer ID (requires SIGN_ID)"
	@echo "  macos_notarize   — notarize + staple DMG (requires APPLE_ID, APPLE_TEAM_ID, APPLE_APP_PASSWORD)"
	@echo "  macos_release    — publish DMG to GitHub Releases (requires gh)"
	@echo ""
	@echo "  linux_package    — create tar.gz archive of binary (Linux only)"
	@echo "  linux_release    — publish tar.gz to GitHub Releases (requires gh)"
	@echo ""
	@echo "  clean            — remove build artifacts"
