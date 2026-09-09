package co.com.jikanle.core.domain.model

data class DiscoverableEvent(
    val event: Event,
    val organizationName: String? = null,
    val communityUrl: String? = null,
)
