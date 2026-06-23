package co.com.jikanle.core.design.theme

import androidx.compose.ui.graphics.Color

/**
 * Jikanle brand palette — locked. These mirror the web app's CSS variables exactly
 * (see CLAUDE.md §4.1). Do not modify the hex values.
 */
object JikanleColors {
    val Ink = Color(0xFF1F1438)        // Deep aubergine — primary text on light / surface in dark
    val InkSoft = Color(0xFF5B4B7A)    // Secondary text
    val InkFaint = Color(0xFF9C8FB8)   // Tertiary text, dividers

    val Primary = Color(0xFF6D2DD3)    // Vivid violet — principal brand color
    val Pink = Color(0xFFE63B96)       // Vivid magenta — energy accent, "now playing", live
    val Blue = Color(0xFF3B5BDB)       // Secondary — non-primary actions, inline links

    val Paper = Color(0xFFFBF7F8)      // Default background, light theme
    val PaperDeep = Color(0xFFF2EAF5)  // Card backgrounds, light theme
    val PaperRule = Color(0xFFE5D6E5)  // Hairline dividers

    val White = Color(0xFFFFFFFF)
}
