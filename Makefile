.PHONY: build test clean run-native run-wasm help debug gui generate-icon

# Default target
build:
	./gradlew build

# Debug mode for macOS
debug: generate-icon
	./gradlew runReleaseExecutableMacosArm64 -Pargs="--debug $(ARGS)"

# GUI mode for macOS
gui: generate-icon
	./gradlew runReleaseExecutableMacosArm64 -Pargs="--gui"

# Icon generation
generate-icon:
	@rsvg-convert -w 1024 -h 1024 src/macosMain/resources/icon.svg -o AppIcon.png || echo "Warning: rsvg-convert not found. Application will lack an icon."

# Testing
test:
	gradle allTests

# Cleanup
clean:
	gradle clean
	rm -f AppIcon.png

# Run native (assuming macosArm64 as primary on Apple Silicon)
run-native:
	./gradlew runReleaseExecutableMacosArm64 -Pargs="$(ARGS)"

# Run wasm (node.js required for console run)
run-wasm:
	gradle wasmJsBrowserRun

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
	@echo "  make run-wasm             - Run the Wasm browser version"
	@echo "  make help                 - Show this help message"
