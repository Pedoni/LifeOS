package it.edon.lifeos.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import it.edon.lifeos.core.data.repository.AuthRepositoryImpl
import it.edon.lifeos.core.data.repository.CategoryRepositoryImpl
import it.edon.lifeos.core.data.repository.UserDataRepositoryImpl
import it.edon.lifeos.core.domain.repository.AuthRepository
import it.edon.lifeos.core.domain.repository.CategoryRepository
import it.edon.lifeos.core.domain.repository.UserDataRepository
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindUserDataRepository(impl: UserDataRepositoryImpl): UserDataRepository

    companion object {
        @Provides
        fun provideClock(): Clock = Clock.systemUTC()
    }
}
