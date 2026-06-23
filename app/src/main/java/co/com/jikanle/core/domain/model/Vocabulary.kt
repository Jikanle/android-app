package co.com.jikanle.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A vocabulary pick a Creator chose to emphasize for a Song/Lesson (CLAUDE.md §6/§7).
 * [term] is in the target language (may be CJK); [reading] is the romanization/kana.
 */
@Serializable
data class Vocabulary(
    val id: String? = null,
    val term: String,
    val reading: String? = null,
    val meaning: String,
    @SerialName("example") val example: String? = null,
)
