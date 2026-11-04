package it.edon.lifeos.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import it.edon.lifeos.core.database.LifeOSDatabase
import it.edon.lifeos.core.database.MIGRATION_1_2
import it.edon.lifeos.core.database.dao.CategoryDao
import it.edon.lifeos.core.database.dao.TaskDao
import javax.inject.Singleton

private const val DATABASE_NAME = "lifeos.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LifeOSDatabase =
        Room.databaseBuilder(
            context,
            LifeOSDatabase::class.java,
            DATABASE_NAME
        ).addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideCategoryDao(database: LifeOSDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTaskDao(database: LifeOSDatabase): TaskDao = database.taskDao()
}
