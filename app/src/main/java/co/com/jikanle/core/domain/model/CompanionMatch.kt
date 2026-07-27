package co.com.jikanle.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * A proposed language partner (CLAUDE.md §6/§8). Produced by the `match_companion` RPC,
 * which scores candidates on complementary languages × shared hobbies × shared room.
 * The UI shows the *reasons* ([sharedHobbies], [complementaryLanguages]) — never the [score].
 */
@Serializable
data class CompanionMatch(
    val id: String,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("matched_id") val matchedId: String,
    val score: Double = 0.0,
    @SerialName("shared_hobbies") val sharedHobbies: List<String> = emptyList(),
    // jsonb, e.g. {"i_learn": ["ja"], "they_learn": ["es"]} — shape kept flexible.
    @SerialName("complementary_languages") val complementaryLanguages: JsonObject? = null,
    val status: MatchStatus = MatchStatus.PENDING,
    @SerialName("created_at") val createdAt: String? = null,
)
