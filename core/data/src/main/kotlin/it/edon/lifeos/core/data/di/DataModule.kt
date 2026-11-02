package it.edon.lifeos.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.edon.lifeos.core.data.repository.CategoryRepositoryImpl
import it.edon.lifeos.core.domain.repository.CategoryRepository
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    companion object {
        @Provides
        fun provideClock(): Clock = Clock.systemUTC()
    }
}
