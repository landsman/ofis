package ofis

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import ofis.platform.fileSystem
import ofis.tool.Tool
import ofis.tool.ToolRegistry
import ofis.ui.system.DotGridBackground
import ofis.ui.system.DropOverlay
import ofis.ui.system.FileDropBus
import ofis.ui.system.LocalLogController
import ofis.ui.system.LogController
import ofis.ui.system.LogOverlay
import ofis.ui.system.toast.LocalToastController
import ofis.ui.system.toast.ToastController
import ofis.ui.system.toast.ToastHost
import ofis.ui.view.filesuggestion.FileSuggestView
import ofis.ui.view.settings.SettingsView
import ofis.ui.view.toolselection.ToolSelectionView
import okio.Path.Companion.toPath

private enum class Screen { Tools, Settings, FileSuggestion }

@Composable
fun App() {
    var currentTool by remember { mutableStateOf<Tool?>(null) }
    var screen by remember { mutableStateOf(Screen.Tools) }
    var droppedFilePath by remember { mutableStateOf<String?>(null) }
    var droppedFileSize by remember { mutableStateOf<Long?>(null) }
    var droppedFileTools by remember { mutableStateOf<List<Tool>>(emptyList()) }
    val toastController = remember { ToastController() }
    val logController = remember { LogController() }
    var selectedLanguage by remember { mutableStateOf(loadLanguagePreference()) }

    // Intercept global file drops when no tool screen is active.
    LaunchedEffect(FileDropBus.pendingFilePath) {
        val path = FileDropBus.pendingFilePath ?: return@LaunchedEffect
        if (currentTool != null) return@LaunchedEffect
        val ext = path.substringAfterLast('.', "").lowercase()
        val tools = ToolRegistry.findForExtension(ext)
        droppedFilePath = path
        droppedFileSize = fileSystem.metadataOrNull(path.toPath())?.size
        droppedFileTools = tools
        screen = Screen.FileSuggestion
    }

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
                        screen == Screen.FileSuggestion && droppedFilePath != null -> {
                            FileSuggestView(
                                filePath = droppedFilePath!!,
                                fileSize = droppedFileSize,
                                matchingTools = droppedFileTools,
                                onToolSelect = { tool ->
                                    Logger.debug("navigate → ${tool.name} via drop")
                                    droppedFilePath = null
                                    droppedFileSize = null
                                    droppedFileTools = emptyList()
                                    screen = Screen.Tools
                                    currentTool = tool
                                },
                                onDismiss = {
                                    FileDropBus.consume()
                                    droppedFilePath = null
                                    droppedFileSize = null
                                    droppedFileTools = emptyList()
                                    screen = Screen.Tools
                                },
                            )
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

                if (FileDropBus.isDragging) {
                    DropOverlay()
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
