package co.com.jikanle.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import co.com.jikanle.core.data.local.dao.LessonDao
import co.com.jikanle.core.data.local.dao.ProfileDao
import co.com.jikanle.core.data.local.dao.RoomDao
import co.com.jikanle.core.data.local.dao.SongDao
import co.com.jikanle.core.data.local.dao.VocabularyDao
import co.com.jikanle.core.data.local.entity.LessonEntity
import co.com.jikanle.core.data.local.entity.ProfileEntity
import co.com.jikanle.core.data.local.entity.RoomEntity
import co.com.jikanle.core.data.local.entity.SongEntity
import co.com.jikanle.core.data.local.entity.VocabularyEntity

/**
 * Local offline cache. `exportSchema = false` for now — the schema isn't stable pre-1.0 and
 * the DI builder uses destructive migration, so there are no migration files to export yet.
 */
@Database(
    entities = [
        SongEntity::class,
        LessonEntity::class,
        VocabularyEntity::class,
        RoomEntity::class,
        ProfileEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class JikanleDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun lessonDao(): LessonDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun roomDao(): RoomDao
    abstract fun profileDao(): ProfileDao
}
