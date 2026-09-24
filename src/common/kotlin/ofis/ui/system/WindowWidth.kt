package ofis.ui.system

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Mirrors Material 3 window size classes so we can migrate later with minimal friction.
 * https://m3.material.io/foundations/layout/applying-layout/window-size-classes
 */
enum class WindowWidth {
    /** < 480 dp — small or very narrow window */
    Compact,

    /** 480–720 dp — medium window */
    Medium,

    /** > 720 dp — large / maximised window */
    Expanded,
}

fun Dp.toWindowWidth(): WindowWidth =
    when {
        this < 480.dp -> WindowWidth.Compact
        this < 720.dp -> WindowWidth.Medium
        else -> WindowWidth.Expanded
    }

/** Provided by [ofis.ui.component.ScreenLayout]; defaults to Expanded so previews look right. */
val LocalWindowWidth = compositionLocalOf { WindowWidth.Expanded }
