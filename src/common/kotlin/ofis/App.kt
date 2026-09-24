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
import ofis.i18n.AppLanguage
import ofis.i18n.LocalAppStrings
import ofis.i18n.loadLanguagePreference
import ofis.i18n.saveLanguagePreference
import ofis.i18n.toAppStrings
import ofis.platform.fileSystem
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

@Composable
fun App() {
    val nav = remember { AppNavigator() }
    val toastController = remember { ToastController() }
    val logController = remember { LogController() }
    var selectedLanguage by remember { mutableStateOf(loadLanguagePreference()) }

    // Intercept global file drops when no tool screen is active.
    LaunchedEffect(FileDropBus.pendingFilePath) {
        val path = FileDropBus.pendingFilePath ?: return@LaunchedEffect
        if (nav.currentTool != null) return@LaunchedEffect
        val ext = path.substringAfterLast('.', "").lowercase()
        nav.showDroppedFile(
            DroppedFile(
                path = path,
                size = fileSystem.metadataOrNull(path.toPath())?.size,
                tools = ToolRegistry.findForExtension(ext),
            ),
        )
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
                    CurrentScreen(
                        nav = nav,
                        selectedLanguage = selectedLanguage,
                        onLanguageSelect = { lang ->
                            selectedLanguage = lang
                            saveLanguagePreference(lang)
                        },
                    )
                }
                Overlays(logController, toastController)
            }
        }
    }
}

@Composable
private fun CurrentScreen(
    nav: AppNavigator,
    selectedLanguage: AppLanguage,
    onLanguageSelect: (AppLanguage) -> Unit,
) {
    val tool = nav.currentTool
    val dropped = nav.droppedFile
    when {
        tool != null -> tool.Screen(onBack = { nav.closeTool() })

        nav.screen == Screen.FileSuggestion && dropped != null ->
            FileSuggestView(
                filePath = dropped.path,
                fileSize = dropped.size,
                matchingTools = dropped.tools,
                onToolSelect = { nav.openTool(it, via = " via drop") },
                onDismiss = {
                    FileDropBus.consume()
                    nav.dismissDroppedFile()
                },
            )

        nav.screen == Screen.Settings ->
            SettingsView(
                selectedLanguage = selectedLanguage,
                onLanguageSelect = onLanguageSelect,
                onBack = { nav.home() },
            )

        else ->
            ToolSelectionView(
                onToolSelect = { nav.openTool(it) },
                onSettingsClick = { nav.openSettings() },
            )
    }
}

@Composable
private fun Overlays(
    logController: LogController,
    toastController: ToastController,
) {
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
