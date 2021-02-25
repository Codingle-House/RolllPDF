package id.co.rolllpdf.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.co.rolllpdf.core.DiffCallback
import javax.inject.Singleton

/**
 * Created by pertadima on 25,February,2021
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providesDiffCallback() = DiffCallback()
}