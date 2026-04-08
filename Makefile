.PHONY: build run gui debug test test-all lint format icon dmg deps sec clean help

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

GRADLE := ./gradlew

# Fast build: host binary only, no linting
build:
	$(GRADLE) linkDebugExecutable$(TARGET_SUFFIX)

# Run (pass ARGS="..." for tool args)
run:
	$(GRADLE) runDebugExecutable$(TARGET_SUFFIX) -Pargs="$(ARGS)"

# GUI mode
gui: icon
	$(GRADLE) runDebugExecutable$(TARGET_SUFFIX) -Pargs="--gui"

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
icon:
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

# DMG Packaging (macOS only)
dmg: icon
	@if [ "$(OS)" != "Darwin" ]; then echo "Error: DMG creation only supported on macOS"; exit 1; fi
	$(GRADLE) linkReleaseExecutable$(TARGET_SUFFIX)
	@mkdir -p build/dmg-staging
	@mkdir -p build/dmg-staging/Ofis.app/Contents/MacOS
	@mkdir -p build/dmg-staging/Ofis.app/Contents/Resources
	@cp build/bin/$(shell echo $(TARGET_SUFFIX) | sed 's/M/m/')/releaseExecutable/Ofis.kexe build/dmg-staging/Ofis.app/Contents/MacOS/Ofis
	@cp src/macosMain/resources/Info.plist build/dmg-staging/Ofis.app/Contents/Info.plist
	@cp build/AppIcon.icns build/dmg-staging/Ofis.app/Contents/Resources/AppIcon.icns
	@ln -s /Applications build/dmg-staging/Applications
	@echo "Creating DMG..."
	@rm -f build/Ofis.dmg
	@hdiutil create -volname "Ofis" -srcfolder build/dmg-staging -ov -format UDZO build/Ofis.dmg
	@rm -rf build/dmg-staging
	@echo "DMG created: build/Ofis.dmg"

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

help:
	@echo "Usage: make <target> [ARGS=\"...\"]"
	@echo ""
	@echo "  build      — compile host binary (fast, no lint)"
	@echo "  run        — build + run  (ARGS='pdf-compress input.pdf')"
	@echo "  gui        — launch GUI"
	@echo "  debug      — run with --debug flag"
	@echo "  test       — run tests for host platform"
	@echo "  test-all   — run tests for all targets"
	@echo "  lint       — static analysis + style (detekt + ktlint)"
	@echo "  format     — auto-fix style issues"
	@echo "  icon       — convert SVG icon to PNG and ICNS"
	@echo "  dmg        — package app as DMG (macOS only)"
	@echo "  deps       — check for outdated dependencies"
	@echo "  sec        — OWASP vulnerability scan"
	@echo "  clean      — remove build artifacts"
