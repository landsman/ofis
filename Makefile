.PHONY: build test test-all clean run-native run-wasm help debug gui generate-icon lint format

# Detect OS and Architecture for fast testing
OS := $(shell uname -s)
ARCH := $(shell uname -m)

# Architecture specific paths and suffixes
ifeq ($(OS),Darwin)
    ifeq ($(ARCH),arm64)
        BIN_PATH := macosArm64
        BIN_PATH_SUFFIX := MacosArm64
    else
        BIN_PATH := macosX64
        BIN_PATH_SUFFIX := MacosX64
    endif
endif

# Shortcut for gradlew
GRADLE := ./gradlew

ifeq ($(OS),Darwin)
    ifeq ($(ARCH),arm64)
        HOST_TEST_TASK := macosArm64Test
    else
        HOST_TEST_TASK := macosX64Test
    endif
else ifeq ($(OS),Linux)
    HOST_TEST_TASK := linuxX64Test
else
    # Fallback to allTests if OS is not detected or supported for specific task
    HOST_TEST_TASK := allTests
endif

# Default target
build: lint
	$(GRADLE) build

# Debug mode for macOS
debug: generate-icon
	$(GRADLE) -q runDebugExecutable$(BIN_PATH_SUFFIX) -Pargs="--debug $(ARGS)"

# GUI mode for macOS (Debug, fast)
gui: generate-icon
	@echo "Starting GUI application..."
	$(GRADLE) -q runDebugExecutable$(BIN_PATH_SUFFIX) -Pargs="--gui" \
		-x convertXmlValueResourcesForCommonMain \
		-x convertXmlValueResourcesForMacosArm64Main \
		-x convertXmlValueResourcesForMacosX64Main \
		-x convertXmlValueResourcesForMacosMain \
		-x convertXmlValueResourcesForNativeMain \
		-x copyNonXmlValueResourcesForCommonMain \
		-x copyNonXmlValueResourcesForMacosArm64Main \
		-x copyNonXmlValueResourcesForMacosX64Main \
		-x copyNonXmlValueResourcesForMacosMain \
		-x copyNonXmlValueResourcesForNativeMain \
		-x generateExpectResourceCollectorsForCommonMain \
		-x generateComposeResClass \
		-x prepareComposeResourcesTaskForCommonMain \
		-x prepareComposeResourcesTaskForMacosArm64Main \
		-x prepareComposeResourcesTaskForMacosX64Main \
		-x prepareComposeResourcesTaskForMacosMain \
		-x prepareComposeResourcesTaskForNativeMain \
		-x generateResourceAccessorsForCommonMain \
		-x generateResourceAccessorsForMacosArm64Main \
		-x generateResourceAccessorsForMacosX64Main \
		-x generateResourceAccessorsForMacosMain \
		-x generateResourceAccessorsForNativeMain \
		-x generateActualResourceCollectorsForMacosArm64Main \
		-x generateActualResourceCollectorsForMacosX64Main

# Icon generation
generate-icon:
	@mkdir -p build
	@rsvg-convert -w 1024 -h 1024 src/macosMain/resources/icon.svg -o build/mac_os_app_icon.png || echo "Warning: rsvg-convert not found. Application will lack an icon."

# Testing (host platform only for speed)
test:
	$(GRADLE) $(HOST_TEST_TASK)

# Run tests for all targets
test-all:
	$(GRADLE) allTests

# Linting
lint:
	$(GRADLE) detekt ktlintCheck

# Formatting
format:
	$(GRADLE) ktlintFormat

# Cleanup
clean:
	$(GRADLE) clean
	@rm -f build/mac_os_app_icon.png

# Run native (assuming host as primary)
run-native:
	$(GRADLE) -q runDebugExecutable$(BIN_PATH_SUFFIX) -Pargs="$(ARGS)"

# Run wasm (node.js required for console run)
run-wasm:
	$(GRADLE) wasmJsBrowserRun

# Help message
help:
	@echo "Available commands:"
	@echo "  make build       - Build the project"
	@echo "  make test        - Run tests for the current host platform (fastest)"
	@echo "  make test-all    - Run tests for all targets (multiplatform)"
	@echo "  make clean       - Clean build artifacts"
	@echo "  make run-native  - Run the native desktop tool (usage: make run-native ARGS='pdf-compress input.pdf')"
	@echo "  make debug [ARGS=\"...\"]  - Run the native macOS tool with verbose logging"
	@echo "  make gui                  - Run the native macOS tool with a minimalistic GUI (fast)"
	@echo "  make generate-icon        - Convert SVG icon to PNG for the application"
	@echo "  make lint                 - Run static analysis and style checks"
	@echo "  make format               - Automatically fix code style issues"
	@echo "  make run-wasm             - Run the Wasm browser version"
	@echo "  make help                 - Show this help message"
