.PHONY: build test clean run-native run-wasm help debug gui generate-icon prepare-macos-bin lint format

# Default target
build: lint
	@./gradlew --console=plain build

# Debug mode for macOS
debug: generate-icon
	@./gradlew --console=plain -q runReleaseExecutableMacosArm64 -Pargs="--debug $(ARGS)"

# GUI mode for macOS
gui: generate-icon prepare-macos-bin
	./build/bin/macosArm64/releaseExecutable/Ofis --gui

# Icon generation
generate-icon:
	@mkdir -p build
	@rsvg-convert -w 1024 -h 1024 src/macosMain/resources/icon.svg -o build/mac_os_app_icon.png || echo "Warning: rsvg-convert not found. Application will lack an icon."

# Prepare macOS executable name (symlink without .kexe for proper Dock title)
prepare-macos-bin:
	@./gradlew --console=plain -q linkReleaseExecutableMacosArm64
	@ln -sf Ofis.kexe build/bin/macosArm64/releaseExecutable/Ofis

# Testing
test:
	@./gradlew --console=plain allTests

# Linting
lint:
	@./gradlew --console=plain detekt ktlintCheck

# Formatting
format:
	@./gradlew --console=plain ktlintFormat

# Cleanup
clean:
	@./gradlew --console=plain clean
	@rm -f build/mac_os_app_icon.png

# Run native (assuming macosArm64 as primary on Apple Silicon)
run-native:
	@./gradlew --console=plain -q runReleaseExecutableMacosArm64 -Pargs="$(ARGS)"

# Run wasm (node.js required for console run)
run-wasm:
	@./gradlew --console=plain wasmJsBrowserRun

# Help message
help:
	@echo "Available commands:"
	@echo "  make build       - Build the project"
	@echo "  make test        - Run all tests"
	@echo "  make clean       - Clean build artifacts"
	@echo "  make run-native  - Run the native desktop tool (usage: make run-native ARGS='pdf-compress input.pdf')"
	@echo "  make debug [ARGS=\"...\"]  - Run the native macOS tool with verbose logging"
	@echo "  make gui                  - Run the native macOS tool with a minimalistic GUI"
	@echo "  make generate-icon        - Convert SVG icon to PNG for the application"
	@echo "  make lint                 - Run static analysis and style checks"
	@echo "  make format               - Automatically fix code style issues"
	@echo "  make run-wasm             - Run the Wasm browser version"
	@echo "  make help                 - Show this help message"
