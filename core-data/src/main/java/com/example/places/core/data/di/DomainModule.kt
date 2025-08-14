package com.example.places.core.data.di

import com.example.places.core.data.repository.GeocodingRepositoryImpl
import com.example.places.core.domain.repository.GeocodingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    @Singleton
    abstract fun bindGeocodingRepository(
        geocodingRepositoryImpl: GeocodingRepositoryImpl
    ): GeocodingRepository
}