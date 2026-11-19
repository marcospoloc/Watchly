package com.marcospolo.watchly.di

import com.marcospolo.watchly.BuildConfig
import com.marcospolo.watchly.data.createShowRepository
import com.marcospolo.watchly.domain.repository.ShowRepository
import com.marcospolo.watchly.domain.usecase.GetShowDetailUseCase
import com.marcospolo.watchly.domain.usecase.SearchShowsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Composition root. Domain and data are framework-free Kotlin modules, so
 * they are wired here instead of being annotated with Hilt themselves.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    /** Singleton so the in-memory cache lives as long as the app process. */
    @Provides
    @Singleton
    fun provideShowRepository(httpClient: OkHttpClient): ShowRepository = createShowRepository(httpClient)

    @Provides
    fun provideSearchShowsUseCase(repository: ShowRepository) = SearchShowsUseCase(repository)

    @Provides
    fun provideGetShowDetailUseCase(repository: ShowRepository) = GetShowDetailUseCase(repository)
}
