package id.co.rolllpdf.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import id.co.rolllpdf.core.DiffCallback
import id.co.rolllpdf.data.AppDatabase
import id.co.rolllpdf.data.local.preference.UserPreferenceManager
import id.co.rolllpdf.data.mapper.AppDataMapperDto
import id.co.rolllpdf.data.mapper.AppDataMapperEntity
import id.co.rolllpdf.domain.datasource.AppLocalDataSource
import id.co.rolllpdf.domain.repository.AppRepository
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

    @Singleton
    @Provides
    fun providesRoomDatabase(@ApplicationContext appContext: Context) =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "roll_db")
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun providesLocalDataBase(appDatabase: AppDatabase) = AppLocalDataSource(appDatabase)

    @Singleton
    @Provides
    fun providesDataMapperDto() = AppDataMapperDto

    @Singleton
    @Provides
    fun providesDataMapperEntity() = AppDataMapperEntity

    @Singleton
    @Provides
    fun providesAppRepository(
        appLocalDataSource: AppLocalDataSource,
        appDataMapperDto: AppDataMapperDto,
        appDataMapperEntity: AppDataMapperEntity
    ) = AppRepository(appLocalDataSource, appDataMapperDto, appDataMapperEntity)

    @Singleton
    @Provides
    fun providesUserPreferenceManager(
        @ApplicationContext appContext: Context,
    ) = UserPreferenceManager(appContext)
}