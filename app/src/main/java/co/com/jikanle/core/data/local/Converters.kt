package co.com.jikanle.core.data.local

import androidx.room.TypeConverter
import co.com.jikanle.core.data.AppJson
import co.com.jikanle.core.domain.model.Level
import co.com.jikanle.core.domain.model.LessonVisibility
import co.com.jikanle.core.domain.model.Role
import co.com.jikanle.core.domain.model.SlideDeck
import co.com.jikanle.core.domain.model.Vocabulary
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

/**
 * Room converters for the array / jsonb-shaped fields the domain models carry inline.
 * All go through [AppJson] so the local cache encodes identically to what Postgrest
 * produces — including the polymorphic [SlideDeck].
 */
class Converters {

    @TypeConverter
    fun stringListToJson(value: List<String>?): String =
        AppJson.encodeToString(ListSerializer(String.serializer()), value ?: emptyList())

    @TypeConverter
    fun jsonToStringList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList()
        else AppJson.decodeFromString(ListSerializer(String.serializer()), value)

    @TypeConverter
    fun slideDeckToJson(value: SlideDeck?): String =
        AppJson.encodeToString(SlideDeck.serializer(), value ?: SlideDeck())

    @TypeConverter
    fun jsonToSlideDeck(value: String?): SlideDeck =
        if (value.isNullOrEmpty()) SlideDeck()
        else AppJson.decodeFromString(SlideDeck.serializer(), value)

    @TypeConverter
    fun vocabularyListToJson(value: List<Vocabulary>?): String =
        AppJson.encodeToString(ListSerializer(Vocabulary.serializer()), value ?: emptyList())

    @TypeConverter
    fun jsonToVocabularyList(value: String?): List<Vocabulary> =
        if (value.isNullOrEmpty()) emptyList()
        else AppJson.decodeFromString(ListSerializer(Vocabulary.serializer()), value)

    // Enums are stored as their lowercase @SerialName (via the serializer descriptor),
    // so the cache column reads the same value as the Postgres CHECK/text column.
    @TypeConverter
    fun levelToDb(value: Level): String =
        Level.serializer().descriptor.getElementName(value.ordinal)

    @TypeConverter
    fun dbToLevel(value: String): Level =
        Level.entries[Level.serializer().descriptor.getElementIndex(value)]

    @TypeConverter
    fun visibilityToDb(value: LessonVisibility): String =
        LessonVisibility.serializer().descriptor.getElementName(value.ordinal)

    @TypeConverter
    fun dbToVisibility(value: String): LessonVisibility =
        LessonVisibility.entries[LessonVisibility.serializer().descriptor.getElementIndex(value)]

    @TypeConverter
    fun roleListToJson(value: List<Role>?): String =
        AppJson.encodeToString(ListSerializer(Role.serializer()), value ?: emptyList())

    @TypeConverter
    fun jsonToRoleList(value: String?): List<Role> =
        if (value.isNullOrEmpty()) emptyList()
        else AppJson.decodeFromString(ListSerializer(Role.serializer()), value)

    @TypeConverter
    fun levelMapToJson(value: Map<String, Level>?): String =
        AppJson.encodeToString(MapSerializer(String.serializer(), Level.serializer()), value ?: emptyMap())

    @TypeConverter
    fun jsonToLevelMap(value: String?): Map<String, Level> =
        if (value.isNullOrEmpty()) emptyMap()
        else AppJson.decodeFromString(MapSerializer(String.serializer(), Level.serializer()), value)
}
