package ofis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ofis.config.Logger
import ofis.tool.Tool
import ofis.ui.system.DotGridBackground
import ofis.ui.system.LocalLogController
import ofis.ui.system.LogController
import ofis.ui.system.LogOverlay
import ofis.ui.system.toast.LocalToastController
import ofis.ui.system.toast.ToastController
import ofis.ui.system.toast.ToastHost
import ofis.ui.view.toolselection.ToolSelectionView

@Composable
fun App() {
    var currentTool by remember { mutableStateOf<Tool?>(null) }
    val toastController = remember { ToastController() }
    val logController = remember { LogController() }

    DisposableEffect(Unit) {
        Logger.onLog = { msg -> logController.append(msg) }
        onDispose { Logger.onLog = null }
    }

    MaterialTheme {
        CompositionLocalProvider(
            LocalToastController provides toastController,
            LocalLogController provides logController,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                DotGridBackground()

                Column(modifier = Modifier.fillMaxSize()) {
                    if (currentTool == null) {
                        ToolSelectionView(onToolSelect = {
                            Logger.debug("navigate → ${it.name}")
                            currentTool = it
                        })
                    } else {
                        currentTool!!.Screen(onBack = {
                            Logger.debug("navigate → home")
                            currentTool = null
                        })
                    }
                }

                if (logController.isVisible) {
                    LogOverlay(
                        logs = logController.logs,
                        onClose = { logController.hide() },
                    )
                }

                ToastHost(
                    toast = toastController.current,
                    onDismiss = { toastController.dismiss() },
                )
            }
        }
    }
}
