package co.com.jikanle.core.domain.model

/** Keep line identity intact before a renderer or future remote adapter consumes content. */
fun TranslatedSongDemo.normalized(): TranslatedSongDemo {
    require(id.isNotBlank() && titleOriginal.isNotBlank() && artist.isNotBlank() && sourceLanguage.isNotBlank())
    require(lyrics.isNotEmpty())
    val source = lyrics.sortedBy { it.lineIndex }
    val indices = source.indices.toList()
    require(source.map { it.lineIndex } == indices) { "Source lines must be unique and contiguous from zero" }
    require(source.all { it.text.isNotBlank() })
    require(translations.isNotEmpty())
    require(translations.map { it.targetLanguage }.distinct().size == translations.size)
    val ordered = translations.map { translation ->
        require(translation.targetLanguage.isNotBlank() && translation.targetLanguage != sourceLanguage)
        val lines = translation.lines.sortedBy { it.lineIndex }
        require(lines.map { it.lineIndex } == indices) { "Translation must match every source line exactly once" }
        require(lines.all { it.text.isNotBlank() && (it.sourceUnits == null || it.sourceUnits >= 0) && (it.targetUnits == null || it.targetUnits >= 0) })
        translation.copy(lines = lines)
    }
    (vocabulary + ordered.flatMap { it.vocabulary.orEmpty() }).forEach { item ->
        require(item.term.isNotBlank() && item.meaning.isNotBlank())
        require(item.lineIndex == null || item.lineIndex in indices)
    }
    return copy(lyrics = source, translations = ordered)
}
