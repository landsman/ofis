package ofis.ui.system

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Suppress("unused")
class LogController {
    var logs by mutableStateOf("")
        private set
    var isVisible by mutableStateOf(false)
        private set

    private val observers = mutableListOf<(String) -> Unit>()

    fun append(msg: String) {
        logs += "$msg\n"
        observers.forEach { it(msg) }
    }

    fun addObserver(listener: (String) -> Unit) {
        observers.add(listener)
    }

    fun removeObserver(listener: (String) -> Unit) {
        observers.remove(listener)
    }

    fun clear() {
        logs = ""
    }

    fun toggle() {
        isVisible = !isVisible
    }

    fun hide() {
        isVisible = false
    }
}

val LocalLogController = compositionLocalOf<LogController> { error("No LogController provided") }
