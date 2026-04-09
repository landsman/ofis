.PHONY: build run gui cli debug test test-all lint format macos_icon macos_dmg macos_sign macos_notarize macos_release linux_package linux_release deps sec clean help

# Detect OS and Architecture
OS   := $(shell uname -s)
ARCH := $(shell uname -m)

ifeq ($(OS),Darwin)
    ifeq ($(ARCH),arm64)
        TARGET_SUFFIX  := MacosArm64
        HOST_TEST_TASK := macosArm64Test
    else
        TARGET_SUFFIX  := MacosX64
        HOST_TEST_TASK := macosX64Test
    endif
else ifeq ($(OS),Linux)
    TARGET_SUFFIX  := LinuxX64
    HOST_TEST_TASK := linuxX64Test
else
    TARGET_SUFFIX  := MingwX64
    HOST_TEST_TASK := allTests
endif

GRADLE  := ./gradlew
VERSION := $(shell git describe --tags --abbrev=0 2>/dev/null | sed 's/^v//' || echo "1.0.0")

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

# Tests: host platform only
test:
	$(GRADLE) $(HOST_TEST_TASK)

# Tests: all targets
test-all:
	$(GRADLE) allTests

# Linting (separate from build — run in CI or on demand)
lint:
	$(GRADLE) detekt ktlintCheck

# Code formatting
format:
	$(GRADLE) ktlintFormat

# Icon generation (requires rsvg-convert: brew install librsvg)
macos_icon:
	@mkdir -p build/icon.iconset
	@rsvg-convert -w 16 -h 16 src/macosMain/resources/icon.svg -o build/icon.iconset/icon_16x16.png
	@rsvg-convert -w 32 -h 32 src/macosMain/resources/icon.svg -o build/icon.iconset/icon_16x16@2x.png
	@rsvg-convert -w 32 -h 32 src/macosMain/resources/icon.svg -o build/icon.iconset/icon_32x32.png
	@rsvg-convert -w 64 -h 64 src/macosMain/resources/icon.svg -o build/icon.iconset/icon_32x32@2x.png
	@rsvg-convert -w 128 -h 128 src/macosMain/resources/icon.svg -o build/icon.iconset/icon_128x128.png
	@rsvg-convert -w 256 -h 256 src/macosMain/resources/icon.svg -o build/icon.iconset/icon_128x128@2x.png
	@rsvg-convert -w 256 -h 256 src/macosMain/resources/icon.svg -o build/icon.iconset/icon_256x256.png
	@rsvg-convert -w 512 -h 512 src/macosMain/resources/icon.svg -o build/icon.iconset/icon_256x256@2x.png
	@rsvg-convert -w 512 -h 512 src/macosMain/resources/icon.svg -o build/icon.iconset/icon_512x512.png
	@rsvg-convert -w 1024 -h 1024 src/macosMain/resources/icon.svg -o build/icon.iconset/icon_512x512@2x.png
	@iconutil -c icns build/icon.iconset -o build/AppIcon.icns
	@rsvg-convert -w 1024 -h 1024 src/macosMain/resources/icon.svg -o build/mac_os_app_icon.png \
		|| echo "Warning: rsvg-convert not found (brew install librsvg)"

# DMG packaging using create-dmg (brew install create-dmg)
macos_dmg: macos_icon
	@if [ "$(OS)" != "Darwin" ]; then echo "Error: DMG creation only supported on macOS"; exit 1; fi
	@command -v create-dmg >/dev/null || { echo "Error: install create-dmg: brew install create-dmg"; exit 1; }
	$(GRADLE) linkReleaseExecutable$(TARGET_SUFFIX)
	@rm -rf build/Ofis.app
	@mkdir -p build/Ofis.app/Contents/MacOS build/Ofis.app/Contents/Resources
	@cp build/bin/$(shell echo $(TARGET_SUFFIX) | sed 's/M/m/')/releaseExecutable/Ofis.kexe \
		build/Ofis.app/Contents/MacOS/Ofis
	@cp src/macosMain/resources/Info.plist build/Ofis.app/Contents/Info.plist
	@cp build/AppIcon.icns build/Ofis.app/Contents/Resources/AppIcon.icns
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

# Dependency updates report
deps:
	$(GRADLE) dependencyUpdates

# Security scan (OWASP)
sec:
	$(GRADLE) dependencyCheckAnalyze
	@echo "Report: build/reports/dependency-check-report.html"

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
	@echo "  build            — compile host binary (fast, no lint)"
	@echo "  run              — build + run  (ARGS='pdf-compress input.pdf')"
	@echo "  gui              — launch GUI
  cli              — launch CLI  (ARGS='pdf-compress input.pdf')"
	@echo "  debug            — run with --debug flag"
	@echo "  test             — run tests for host platform"
	@echo "  test-all         — run tests for all targets"
	@echo "  lint             — static analysis + style (detekt + ktlint)"
	@echo "  format           — auto-fix style issues"
	@echo ""
	@echo "  macos_icon       — convert SVG icon to PNG and ICNS"
	@echo "  macos_dmg        — package app as DMG (macOS only, requires create-dmg)"
	@echo "  macos_sign       — sign app with Developer ID (requires SIGN_ID)"
	@echo "  macos_notarize   — notarize + staple DMG (requires APPLE_ID, APPLE_TEAM_ID, APPLE_APP_PASSWORD)"
	@echo "  macos_release    — publish DMG to GitHub Releases (requires gh)"
	@echo ""
	@echo "  linux_package    — create tar.gz archive of binary (Linux only)"
	@echo "  linux_release    — publish tar.gz to GitHub Releases (requires gh)"
	@echo ""
	@echo "  deps             — check for outdated dependencies"
	@echo "  sec              — OWASP vulnerability scan"
	@echo "  clean            — remove build artifacts"
