package co.com.jikanle.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import co.com.jikanle.R

/**
 * Typography for Jikanle. Mirrors the web stack (CLAUDE.md §4.2):
 *  - Display: Fraunces (serif)
 *  - Body:    Instrument Sans
 *  - CJK:     Noto Serif JP
 *
 * Fonts are pulled via Google Play Services downloadable fonts. If the provider is
 * unavailable the system gracefully falls back to the platform default family, so
 * the app always renders.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private fun googleFamily(name: String, weights: List<FontWeight>): FontFamily =
    FontFamily(weights.map { Font(GoogleFont(name), provider, it) })

private val displayWeights = listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold)
private val bodyWeights = listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold)

val FrauncesFamily: FontFamily = googleFamily("Fraunces", displayWeights)
val InstrumentSansFamily: FontFamily = googleFamily("Instrument Sans", bodyWeights)
val NotoSerifJpFamily: FontFamily = googleFamily("Noto Serif JP", bodyWeights)

/**
 * Brand-level text styles. Lyric/section code reaches for [display], [body] or [cjk]
 * directly; [cjkBody] should be used for any line where [hasCJK] is true.
 */
object JikanleTypography {
    val display: TextStyle = TextStyle(
        fontFamily = FrauncesFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    )
    val body: TextStyle = TextStyle(
        fontFamily = InstrumentSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
    val cjk: TextStyle = TextStyle(
        fontFamily = NotoSerifJpFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 30.sp,
    )
}

/**
 * Detects whether [text] contains Japanese kana or CJK ideographs, so lyric lines can
 * switch to the CJK family automatically (CLAUDE.md §4.2).
 *  - U+3040–309F Hiragana
 *  - U+30A0–30FF Katakana
 *  - U+4E00–9FFF CJK Unified Ideographs
 */
fun hasCJK(text: String): Boolean = text.any { ch ->
    val code = ch.code
    code in 0x3040..0x309F || code in 0x30A0..0x30FF || code in 0x4E00..0x9FFF
}

/** Material 3 type scale wired to the Jikanle families. */
val JikanleM3Typography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = FrauncesFamily),
        displayMedium = displayMedium.copy(fontFamily = FrauncesFamily),
        displaySmall = displaySmall.copy(fontFamily = FrauncesFamily),
        headlineLarge = headlineLarge.copy(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold),
        headlineMedium = headlineMedium.copy(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold),
        headlineSmall = headlineSmall.copy(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold),
        titleLarge = titleLarge.copy(fontFamily = FrauncesFamily, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = InstrumentSansFamily, fontWeight = FontWeight.Medium),
        titleSmall = titleSmall.copy(fontFamily = InstrumentSansFamily, fontWeight = FontWeight.Medium),
        bodyLarge = bodyLarge.copy(fontFamily = InstrumentSansFamily),
        bodyMedium = bodyMedium.copy(fontFamily = InstrumentSansFamily),
        bodySmall = bodySmall.copy(fontFamily = InstrumentSansFamily),
        labelLarge = labelLarge.copy(fontFamily = InstrumentSansFamily, fontWeight = FontWeight.Medium),
        labelMedium = labelMedium.copy(fontFamily = InstrumentSansFamily, fontWeight = FontWeight.Medium),
        labelSmall = labelSmall.copy(fontFamily = InstrumentSansFamily, fontWeight = FontWeight.Medium),
    )
}
