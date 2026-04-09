package ofis.ui.system.toast

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ToastController {
    var current by mutableStateOf<ToastData?>(null)
        private set

    fun show(
        message: String,
        isSuccess: Boolean,
    ) {
        current = ToastData(message, isSuccess)
    }

    fun show(data: ToastData) {
        current = data
    }

    fun dismiss() {
        current = null
    }
}

val LocalToastController = compositionLocalOf<ToastController> { error("No ToastController provided") }
