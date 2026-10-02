package co.com.jikanle.core.data

import co.com.jikanle.core.domain.model.TranslatedSongDemo
import co.com.jikanle.core.domain.model.normalized
import java.io.File
import kotlinx.serialization.decodeFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TranslatedSongValidationTest {
    private fun seed(): TranslatedSongDemo {
        val path = "src/main/res/raw/songbridge_demo.json"
        val file = listOf(File(path), File("app/$path")).first { it.exists() }
        return AppJson.decodeFromString<TranslatedSongDemo>(file.readText())
    }

    @Test fun `Portuguese covers every line with localized vocabulary`() {
        val song = seed().normalized()
        val pt = song.translations.first { it.targetLanguage == "pt" }
        assertEquals(song.lyrics.indices.toList(), pt.lines.map { it.lineIndex })
        assertEquals("flor de cerejeira", pt.vocabulary!!.first().meaning)
        assertEquals(song.lyrics[0].text, song.lyrics[5].text)
    }

    @Test fun `normalizes shuffled rows without losing repeated text`() {
        val song = seed()
        assertEquals(song.normalized(), song.copy(lyrics = song.lyrics.reversed(), translations = song.translations.map {
            it.copy(lines = it.lines.reversed())
        }).normalized())
    }

    @Test fun `rejects missing duplicate and negative line data`() {
        val song = seed()
        val translation = song.translations.first()
        for (lines in listOf(translation.lines.dropLast(1), translation.lines + translation.lines.first(),
            translation.lines.map { it.copy(targetUnits = -1) })) {
            assertThrows(IllegalArgumentException::class.java) {
                song.copy(translations = listOf(translation.copy(lines = lines))).normalized()
            }
        }
    }

    @Test fun `rejects invalid source identity and vocabulary references`() {
        val song = seed()
        for (bad in listOf(song.copy(sourceLanguage = ""), song.copy(lyrics = song.lyrics + song.lyrics.first()),
            song.copy(vocabulary = listOf(song.vocabulary.first().copy(lineIndex = 99))))) {
            assertThrows(IllegalArgumentException::class.java) { bad.normalized() }
        }
    }
}
