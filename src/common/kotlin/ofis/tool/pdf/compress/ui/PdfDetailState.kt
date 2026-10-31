package ofis.tool.pdf.compress.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ofis.tool.pdf.compress.model.CompressionFailureKind
import ofis.tool.pdf.compress.model.CompressionProfile

/**
 * Screen state for [PdfDetailScreen], driven by the structured lines the tool logs
 * (`RESIZE_INFO:`, `OUTPUT_PATH:`, `SUGGESTED_NAME:`, `FAILURE_KIND:`).
 */
class PdfDetailState {
    var selectedFilePath by mutableStateOf<String?>(null)
        private set
    var selectedFileSize by mutableStateOf<Long?>(null)
        private set
    var resizeInfo by mutableStateOf<String?>(null)
        private set
    var completedProfile by mutableStateOf<CompressionProfile?>(null)
        private set
    var outputFilePath by mutableStateOf<String?>(null)
    var suggestedSaveName by mutableStateOf<String?>(null)
        private set
    var isRunning by mutableStateOf(false)
        private set
    var selectedProfile by mutableStateOf(CompressionProfile.BALANCED)

    /** Parses the saved percentage out of "259.8 KB → 180.2 KB (31%)". */
    val isAlreadyOptimal: Boolean
        get() =
            resizeInfo
                ?.substringAfter("(")
                ?.substringBefore("%")
                ?.toIntOrNull()
                ?.let { it <= 0 } ?: false

    val showProfileSelector: Boolean
        get() = selectedFilePath != null && resizeInfo == null && !isRunning

    fun selectFile(
        path: String,
        size: Long?,
    ) {
        selectedFilePath = path
        selectedFileSize = size
        clearResult()
    }

    fun clearFile() {
        selectedFilePath = null
        selectedFileSize = null
        clearResult()
    }

    fun startCompression(profile: CompressionProfile) {
        selectedProfile = profile
        clearResult()
        isRunning = true
    }

    /** Applies one log line; returns the failure to show, if the line reports one. */
    fun onLogMessage(msg: String): CompressionFailureKind? {
        when {
            msg.startsWith(RESIZE_INFO) -> {
                resizeInfo = msg.substringAfter(RESIZE_INFO)
                completedProfile = selectedProfile
                isRunning = false
            }

            msg.startsWith(OUTPUT_PATH) -> outputFilePath = msg.substringAfter(OUTPUT_PATH)

            msg.startsWith(SUGGESTED_NAME) -> suggestedSaveName = msg.substringAfter(SUGGESTED_NAME)

            msg.startsWith(FAILURE_KIND) -> {
                isRunning = false
                val name = msg.substringAfter(FAILURE_KIND)
                return CompressionFailureKind.entries.firstOrNull { it.name == name }
                    ?: CompressionFailureKind.UNEXPECTED
            }
        }
        return null
    }

    private fun clearResult() {
        resizeInfo = null
        outputFilePath = null
        suggestedSaveName = null
    }

    private companion object {
        const val RESIZE_INFO = "RESIZE_INFO: "
        const val OUTPUT_PATH = "OUTPUT_PATH: "
        const val SUGGESTED_NAME = "SUGGESTED_NAME: "
        const val FAILURE_KIND = "FAILURE_KIND: "
    }
}
