package co.com.jikanle.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Light theme: paper background, ink text, violet primary (CLAUDE.md §4.1).
 */
private val LightColors = lightColorScheme(
    primary = JikanleColors.Primary,
    onPrimary = JikanleColors.White,
    secondary = JikanleColors.Blue,
    onSecondary = JikanleColors.White,
    tertiary = JikanleColors.Pink,
    onTertiary = JikanleColors.White,
    background = JikanleColors.Paper,
    onBackground = JikanleColors.Ink,
    surface = JikanleColors.Paper,
    onSurface = JikanleColors.Ink,
    surfaceVariant = JikanleColors.PaperDeep,
    onSurfaceVariant = JikanleColors.InkSoft,
    outline = JikanleColors.InkFaint,
    outlineVariant = JikanleColors.PaperRule,
)

/**
 * Dark theme: ink background, paper as primary text (CLAUDE.md §4.1).
 */
private val DarkColors = darkColorScheme(
    primary = JikanleColors.Primary,
    onPrimary = JikanleColors.White,
    secondary = JikanleColors.Blue,
    onSecondary = JikanleColors.White,
    tertiary = JikanleColors.Pink,
    onTertiary = JikanleColors.White,
    background = JikanleColors.Ink,
    onBackground = JikanleColors.Paper,
    surface = JikanleColors.Ink,
    onSurface = JikanleColors.Paper,
    surfaceVariant = JikanleColors.InkSoft,
    onSurfaceVariant = JikanleColors.PaperDeep,
    outline = JikanleColors.InkFaint,
    outlineVariant = JikanleColors.InkSoft,
)

/**
 * Jikanle theme. Dynamic color is intentionally disabled — the brand palette is locked
 * and must not be overridden by Material You wallpaper extraction (CLAUDE.md §4.4).
 */
@Composable
fun JikanleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = JikanleM3Typography,
        content = content,
    )
}
