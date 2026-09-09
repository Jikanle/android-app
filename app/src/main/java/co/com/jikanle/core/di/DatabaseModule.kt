package co.com.jikanle.core.di

import android.content.Context
import androidx.room.Room
import co.com.jikanle.core.data.local.JikanleDatabase
import co.com.jikanle.core.data.local.BetaDatabase
import co.com.jikanle.core.data.local.BetaDao
import co.com.jikanle.core.data.local.dao.LessonDao
import co.com.jikanle.core.data.local.dao.EventDao
import co.com.jikanle.core.data.local.dao.ProfileDao
import co.com.jikanle.core.data.local.dao.RoomDao
import co.com.jikanle.core.data.local.dao.SongDao
import co.com.jikanle.core.data.local.dao.VocabularyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBetaDatabase(@ApplicationContext context: Context): BetaDatabase =
        Room.databaseBuilder(context, BetaDatabase::class.java, "jikanle-beta.db").build()

    @Provides
    fun provideBetaDao(database: BetaDatabase): BetaDao = database.betaDao()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): JikanleDatabase =
        Room.databaseBuilder(context, JikanleDatabase::class.java, "jikanle.db")
            // Pre-1.0: the cache is disposable; wipe and re-fetch on schema bumps.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideSongDao(database: JikanleDatabase): SongDao = database.songDao()

    @Provides
    fun provideLessonDao(database: JikanleDatabase): LessonDao = database.lessonDao()

    @Provides
    fun provideVocabularyDao(database: JikanleDatabase): VocabularyDao = database.vocabularyDao()

    @Provides
    fun provideRoomDao(database: JikanleDatabase): RoomDao = database.roomDao()

    @Provides
    fun provideProfileDao(database: JikanleDatabase): ProfileDao = database.profileDao()

    @Provides
    fun provideEventDao(database: JikanleDatabase): EventDao = database.eventDao()
}
