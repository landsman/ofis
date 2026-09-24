package ofis.tool.pdf.compress.model

import okio.Path

// ── Command model ─────────────────────────────────────────────────────────────

data class NativeCommand(
    val executable: String,
    val arguments: List<String>,
)

data class ProcessResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
) {
    companion object {
        /** Shell convention: the command could not be found or launched. */
        const val EXIT_COMMAND_NOT_FOUND = 127
    }
}

// ── Compression I/O ───────────────────────────────────────────────────────────

data class CompressionRequest(
    val inputPath: Path,
    val outputPath: Path,
    val profile: CompressionProfile,
    val keepSmallerOnly: Boolean = true,
)

sealed interface CompressionResult {
    data class Success(
        val inputPath: Path,
        val outputPath: Path,
        val originalBytes: Long,
        val compressedBytes: Long,
        val profile: CompressionProfile,
        val warnings: List<String> = emptyList(),
    ) : CompressionResult {
        val savedBytes: Long get() = originalBytes - compressedBytes
        val ratio: Double get() = if (originalBytes > 0) compressedBytes.toDouble() / originalBytes else 1.0
        val savedPercent: Int get() = ((1.0 - ratio) * 100).toInt()
        val alreadyOptimal: Boolean get() = compressedBytes >= originalBytes
    }

    data class Failure(
        val reason: CompressionError,
    ) : CompressionResult
}

// ── Error model ───────────────────────────────────────────────────────────────

sealed interface CompressionError {
    data class BinaryNotFound(
        val name: String,
    ) : CompressionError {
        override fun toString() = "$name is not installed. Run: brew install $name"
    }

    data class ProcessFailed(
        val tool: String,
        val exitCode: Int,
        val stderr: String,
    ) : CompressionError {
        override fun toString() =
            when {
                exitCode == ProcessResult.EXIT_COMMAND_NOT_FOUND ->
                    "$tool is not installed or could not be launched. Run: brew install $tool"

                stderr.isNotBlank() -> "$tool failed: ${stderr.trim()}"

                else -> "$tool failed with exit code $exitCode."
            }
    }

    data class InvalidInput(
        val message: String,
    ) : CompressionError {
        override fun toString() = message
    }

    data class FileSystemError(
        val message: String,
    ) : CompressionError {
        override fun toString() = message
    }
}
