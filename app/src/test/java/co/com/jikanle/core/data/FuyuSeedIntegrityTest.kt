package co.com.jikanle.core.data

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class FuyuSeedIntegrityTest {

    private val seed: JsonObject by lazy {
        val seedFile = listOf(
            File("data/seed/fuyu_no_hanashi.json"),
            File("../data/seed/fuyu_no_hanashi.json"),
        ).first(File::exists)
        AppJson.parseToJsonElement(seedFile.readText()).jsonObject
    }

    @Test
    fun `seed lesson keeps the expected identity and language contract`() {
        assertEquals("seed-fuyu-no-hanashi", seed.string("id"))
        assertEquals("ja", seed.string("language_target"))
        assertEquals("es", seed.string("language_explanation"))
        assertEquals("intermediate", seed.string("level"))

        val song = seed.objectValue("song")
        assertEquals("冬のはなし", song.string("title_original"))
        assertEquals("Fuyu no Hanashi", song.string("title_romanized"))
        assertEquals("Centimillimental", song.string("artist"))
        assertEquals("ja", song.string("language"))
        assertEquals(260000L, song.primitive("duration_ms").content.toLong())
    }

    @Test
    fun `seed lesson contains every required slide type in stable order`() {
        val slides = seed.objectValue("slide_deck").arrayValue("slides")
        val types = slides.map { it.jsonObject.string("type") }

        assertEquals(
            listOf(
                "intro",
                "listen_first",
                "vocabulary",
                "grammar",
                "cultural",
                "discussion",
                "listen_second",
                "outro",
            ),
            types,
        )
    }

    @Test
    fun `seed lesson vocabulary has eight complete CJK picks`() {
        val vocabulary = seed.objectValue("slide_deck")
            .arrayValue("slides")
            .first { it.jsonObject.string("type") == "vocabulary" }
            .jsonObject
            .arrayValue("items")

        assertEquals(8, vocabulary.size)
        vocabulary.forEach { element ->
            val item = element.jsonObject
            assertTrue(item.string("surface").isNotBlank())
            assertTrue(item.string("reading").isNotBlank())
            assertTrue(item.string("meaning_es").isNotBlank())
            assertTrue(item.string("meaning_en").isNotBlank())
        }
    }
}

private fun JsonObject.objectValue(key: String): JsonObject = getValue(key).jsonObject
private fun JsonObject.arrayValue(key: String): JsonArray = getValue(key).jsonArray
private fun JsonObject.primitive(key: String): JsonPrimitive = getValue(key).jsonPrimitive
private fun JsonObject.string(key: String): String = primitive(key).content
