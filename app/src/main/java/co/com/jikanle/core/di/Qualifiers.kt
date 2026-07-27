package co.com.jikanle.core.di

import javax.inject.Qualifier

/** Marks the IO-bound [kotlinx.coroutines.CoroutineDispatcher] (DB/network). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Marks the CPU-bound [kotlinx.coroutines.CoroutineDispatcher] (parsing/sorting). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
