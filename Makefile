.PHONY: build run gui debug test test-all lint format icon deps sec clean help

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
	@mkdir -p build
	@rsvg-convert -w 1024 -h 1024 src/macosMain/resources/icon.svg -o build/mac_os_app_icon.png \
		|| echo "Warning: rsvg-convert not found (brew install librsvg)"

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
	@rm -f build/mac_os_app_icon.png

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
	@echo "  icon       — convert SVG icon to PNG"
	@echo "  deps       — check for outdated dependencies"
	@echo "  sec        — OWASP vulnerability scan"
	@echo "  clean      — remove build artifacts"
