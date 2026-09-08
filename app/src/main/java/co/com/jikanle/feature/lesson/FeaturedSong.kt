package co.com.jikanle.feature.lesson

data class FeaturedSong(
    val title: String,
    val artist: String,
    val language: String,
    val status: FeaturedSongStatus,
)

enum class FeaturedSongStatus {
    StandardLesson,
    SeasonalPreview,
}

val featuredSongs = listOf(
    FeaturedSong(
        title = "Fuyu no Hanashi",
        artist = "Centimillimental",
        language = "JA",
        status = FeaturedSongStatus.StandardLesson,
    ),
    FeaturedSong(
        title = "Seasonal K-pop feature",
        artist = "Stray Kids",
        language = "KO",
        status = FeaturedSongStatus.SeasonalPreview,
    ),
)
