package co.com.jikanle.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Organization(
    val id: String,
    val name: String,
    val slug: String,
    @SerialName("logo_url") val logoUrl: String? = null,
    @SerialName("community_url") val communityUrl: String? = null,
)
