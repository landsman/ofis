# Ofis

Ofis is a set of multiplatform tooling for office tasks, including PDF compression and more. It is designed to be used consistently by both users and LLMs via a standardized command-line interface.

## Usage

For consistency, all common tasks should be performed using `make` commands. These commands are configured to use plain console output (non-interactive) by default.

### Common Commands

- **Build the project:**
  ```bash
  make build
  ```
  This will run linting and then compile the project.

- **Run tests:**
  ```bash
  make test
  ```
  Runs all unit tests across all targets.

- **Run the native tool (CLI):**
  ```bash
  make run-native ARGS="pdf-compress input.pdf"
  ```
  Pass arguments to the tool using the `ARGS` variable.

- **Run the GUI (macOS):**
  ```bash
  make gui
  ```

- **Debug mode (macOS):**
  ```bash
  make debug ARGS="pdf-compress input.pdf"
  ```

- **Run Wasm version (Browser):**
  ```bash
  make run-wasm
  ```

- **Code Quality:**
  ```bash
  make lint    # Run static analysis
  make format  # Automatically fix code style issues
  ```

- **Cleanup:**
  ```bash
  make clean
  ```

### Developer Notes

- The project uses Gradle for builds, but the `Makefile` wrappers ensure that `--console=plain` is used for predictable output in automated environments and LLM interactions.
- Native builds currently target `macosArm64` by default in the `Makefile`.
- To see all available commands, run `make help`.

## Tooling
- **PDF compression**: `pdf-compress <input.pdf> [output.pdf] [--level <1-9>]`