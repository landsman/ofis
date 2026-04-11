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
import ofis.i18n.LocalAppStrings
import ofis.i18n.loadLanguagePreference
import ofis.i18n.saveLanguagePreference
import ofis.i18n.toAppStrings
import ofis.tool.Tool
import ofis.ui.system.DotGridBackground
import ofis.ui.system.LocalLogController
import ofis.ui.system.LogController
import ofis.ui.system.LogOverlay
import ofis.ui.system.toast.LocalToastController
import ofis.ui.system.toast.ToastController
import ofis.ui.system.toast.ToastHost
import ofis.ui.view.settings.SettingsView
import ofis.ui.view.toolselection.ToolSelectionView

private enum class Screen { Tools, Settings }

@Composable
fun App() {
    var currentTool by remember { mutableStateOf<Tool?>(null) }
    var screen by remember { mutableStateOf(Screen.Tools) }
    val toastController = remember { ToastController() }
    val logController = remember { LogController() }
    var selectedLanguage by remember { mutableStateOf(loadLanguagePreference()) }

    DisposableEffect(Unit) {
        Logger.onLog = { msg -> logController.append(msg) }
        onDispose { Logger.onLog = null }
    }

    MaterialTheme {
        CompositionLocalProvider(
            LocalToastController provides toastController,
            LocalLogController provides logController,
            LocalAppStrings provides selectedLanguage.toAppStrings(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                DotGridBackground()

                Column(modifier = Modifier.fillMaxSize()) {
                    when {
                        currentTool != null -> {
                            currentTool!!.Screen(onBack = {
                                Logger.debug("navigate → home")
                                currentTool = null
                            })
                        }
                        screen == Screen.Settings -> {
                            SettingsView(
                                selectedLanguage = selectedLanguage,
                                onLanguageSelect = { lang ->
                                    selectedLanguage = lang
                                    saveLanguagePreference(lang)
                                },
                                onBack = { screen = Screen.Tools },
                            )
                        }
                        else -> {
                            ToolSelectionView(
                                onToolSelect = {
                                    Logger.debug("navigate → ${it.name}")
                                    currentTool = it
                                },
                                onSettingsClick = { screen = Screen.Settings },
                            )
                        }
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
