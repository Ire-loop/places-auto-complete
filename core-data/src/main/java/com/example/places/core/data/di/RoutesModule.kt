package com.example.places.core.data.di

import com.example.places.core.data.repository.RoutesRepositoryImpl
import com.example.places.core.domain.repository.RoutesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutesModule {

    @Binds
    @Singleton
    abstract fun bindsRoutesRepository(
        routesRepositoryImpl: RoutesRepositoryImpl
    ): RoutesRepository
}