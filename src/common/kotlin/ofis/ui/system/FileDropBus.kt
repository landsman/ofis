package ofis.ui.system

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Process-global state bus for file drag-and-drop events from the platform window layer.
 * Platform code (AppKit on macOS) writes to this; Compose screens read it.
 */
object FileDropBus {
    var isDragging by mutableStateOf(false)
        private set

    var pendingFilePath by mutableStateOf<String?>(null)
        private set

    fun onDragEnter() {
        isDragging = true
    }

    fun onDragExit() {
        isDragging = false
    }

    fun onDrop(path: String) {
        isDragging = false
        pendingFilePath = path
    }

    fun consume() {
        pendingFilePath = null
    }
}
