package co.com.jikanle.core.di

import co.com.jikanle.core.data.repository.AuthRepositoryImpl
import co.com.jikanle.core.data.repository.BundledTranslatedSongRepository
import co.com.jikanle.core.data.repository.LessonRepositoryImpl
import co.com.jikanle.core.data.repository.ProfileRepositoryImpl
import co.com.jikanle.core.data.repository.RoomRepositoryImpl
import co.com.jikanle.core.data.repository.SongRepositoryImpl
import co.com.jikanle.core.data.repository.VocabularyRepositoryImpl
import co.com.jikanle.core.domain.repository.AuthRepository
import co.com.jikanle.core.domain.repository.LessonRepository
import co.com.jikanle.core.domain.repository.ProfileRepository
import co.com.jikanle.core.domain.repository.RoomRepository
import co.com.jikanle.core.domain.repository.SongRepository
import co.com.jikanle.core.domain.repository.TranslatedSongRepository
import co.com.jikanle.core.domain.repository.VocabularyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSongRepository(impl: SongRepositoryImpl): SongRepository

    @Binds
    @Singleton
    abstract fun bindTranslatedSongRepository(
        impl: BundledTranslatedSongRepository,
    ): TranslatedSongRepository

    @Binds
    @Singleton
    abstract fun bindLessonRepository(impl: LessonRepositoryImpl): LessonRepository

    @Binds
    @Singleton
    abstract fun bindVocabularyRepository(impl: VocabularyRepositoryImpl): VocabularyRepository

    @Binds
    @Singleton
    abstract fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
}
