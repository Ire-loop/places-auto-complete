package com.example.places.core.data.di

import com.example.places.core.data.repository.SharedShipmentRepositoryImpl
import com.example.places.core.domain.repository.SharedShipmentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SharedShipmentModule {

    @Binds
    @Singleton
    abstract fun bindsSharedShipmentRepository(
        sharedShipmentRepositoryImpl: SharedShipmentRepositoryImpl
    ): SharedShipmentRepository
}