package ofis

import okio.*

actual val fileSystem: FileSystem = object : FileSystem() {
    override fun atomicMove(source: Path, target: Path) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun canonicalize(path: Path) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun createDirectory(dir: Path, mustCreate: Boolean) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun createSymlink(source: Path, target: Path) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun delete(path: Path, mustExist: Boolean) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun list(dir: Path) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun listRecursively(dir: Path, followSymlinks: Boolean) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun metadataOrNull(path: Path) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun openReadOnly(file: Path) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun sink(file: Path, append: Boolean) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun source(file: Path) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun appendingSink(file: Path, mustExist: Boolean) = throw UnsupportedOperationException("Wasm filesystem not supported")
    override fun listOrNull(dir: Path) = throw UnsupportedOperationException("Wasm filesystem not supported")
}

actual fun platformMain(args: Array<String>) {
    commonMain(args)
}

actual fun platformGui() {
    println("GUI is not supported on Wasm/Browser yet.")
}

actual fun exitProcess(status: Int) {
    // In browser, we cannot exit a process.
    // We could throw an exception or just log.
    println("Exiting with status $status")
}

fun main() {
    // In wasmJs, args are usually not available from command line in browser.
    // However, if we run in Node.js, we can get them.
    // For now, let's just call commonMain with empty args or simulate.
    platformMain(emptyArray())
}
