package co.com.jikanle.core.data.mapper

import co.com.jikanle.core.data.local.entity.EventEntity
import co.com.jikanle.core.domain.model.DiscoverableEvent
import co.com.jikanle.core.domain.model.Event
import co.com.jikanle.core.domain.model.Organization

fun Event.toEntity(organization: Organization?) = EventEntity(
    id = id,
    title = title,
    description = description,
    startsAt = startsAt,
    endsAt = endsAt,
    venueName = venueName,
    venueLocation = venueLocation,
    lumaUrl = lumaUrl,
    coverImageUrl = coverImageUrl,
    ticketPriceCop = ticketPriceCop,
    organizationId = organizationId,
    organizationName = organization?.name,
    communityUrl = organization?.communityUrl,
    language = language,
    level = level,
    summary = summary,
    tags = tags,
    timezone = timezone,
    lat = lat,
    lng = lng,
)

fun EventEntity.toDiscoverableEvent() = DiscoverableEvent(
    event = Event(
        id = id,
        title = title,
        description = description,
        startsAt = startsAt,
        endsAt = endsAt,
        venueName = venueName,
        venueLocation = venueLocation,
        lumaUrl = lumaUrl,
        coverImageUrl = coverImageUrl,
        ticketPriceCop = ticketPriceCop,
        organizationId = organizationId,
        language = language,
        level = level,
        summary = summary,
        tags = tags,
        timezone = timezone,
        lat = lat,
        lng = lng,
    ),
    organizationName = organizationName,
    communityUrl = communityUrl,
)
