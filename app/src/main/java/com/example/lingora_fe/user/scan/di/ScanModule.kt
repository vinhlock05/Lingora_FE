package com.example.lingora_fe.user.scan.di

import com.example.lingora_fe.user.scan.data.ml.ObjectDetectorHelper
import com.example.lingora_fe.user.scan.data.repository.ScanRepositoryImpl
import com.example.lingora_fe.user.scan.domain.repository.ScanRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ScanRepositoryModule {

    @Binds
    abstract fun bindScanRepository(
        impl: ScanRepositoryImpl
    ): ScanRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ScanMLModule {

    @Provides
    fun provideDetector(): ObjectDetectorHelper {
        return ObjectDetectorHelper()
    }
}