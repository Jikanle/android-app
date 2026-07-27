package co.com.jikanle.core.data

import kotlinx.serialization.json.Json

/**
 * The single Json instance used everywhere rows cross a boundary:
 *  - supabase-kt's [io.github.jan.supabase.serializer.KotlinXSerializer] (Postgrest decode/encode)
 *  - Room [co.com.jikanle.core.data.local.Converters] (jsonb-shaped columns cached locally)
 *
 * Two settings are load-bearing:
 *  - [Json.ignoreUnknownKeys]: the shared web/Android schema may grow columns the client
 *    doesn't model yet; decoding must not break.
 *  - explicitNulls = false: our models carry nullable `id` / `created_at` whose columns are
 *    `not null default …` / generated. Emitting `"created_at": null` on insert would overwrite
 *    the DB default and fail the write, so null fields are simply omitted.
 *
 * Polymorphic [co.com.jikanle.core.domain.model.Slide] uses kotlinx's default class
 * discriminator ("type"), which matches the schema, so the sealed hierarchy auto-registers
 * with no SerializersModule.
 */
val AppJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
}
