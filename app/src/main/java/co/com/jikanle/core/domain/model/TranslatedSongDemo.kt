package co.com.jikanle.core.domain.model

import kotlinx.serialization.Serializable

@Serializable data class TranslatedSongDemo(val id: String, val titleOriginal: String, val titleRomanized: String? = null, val artist: String, val sourceLanguage: String, val lyrics: List<DemoLyricLine>, val translations: List<DemoTranslation>, val vocabulary: List<DemoVocabularyItem>)
@Serializable data class DemoLyricLine(val lineIndex: Int, val text: String, val transliteration: String? = null)
@Serializable data class DemoTranslation(val targetLanguage: String, val alignmentNote: String? = null, val lines: List<DemoTranslationLine>)
@Serializable data class DemoTranslationLine(val lineIndex: Int, val text: String, val sourceUnits: Int? = null, val targetUnits: Int? = null, val note: String? = null)
@Serializable data class DemoVocabularyItem(val lineIndex: Int? = null, val term: String, val reading: String? = null, val meaning: String, val explanation: String? = null)
