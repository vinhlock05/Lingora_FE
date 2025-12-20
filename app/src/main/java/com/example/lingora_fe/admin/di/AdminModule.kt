package com.example.lingora_fe.admin.di

import com.example.lingora_fe.admin.category.data.remote.api.CategoryApiService
import com.example.lingora_fe.admin.category.data.repository.CategoryRepositoryImpl
import com.example.lingora_fe.admin.category.domain.repository.CategoryRepository
import com.example.lingora_fe.admin.topic.data.remote.api.TopicApiService
import com.example.lingora_fe.admin.topic.data.repository.TopicRepositoryImpl
import com.example.lingora_fe.admin.topic.domain.repository.TopicRepository
import com.example.lingora_fe.admin.word.data.remote.api.WordApiService
import com.example.lingora_fe.admin.word.data.repository.WordRepositoryImpl
import com.example.lingora_fe.admin.word.domain.repository.WordRepository
import com.example.lingora_fe.admin.user.data.remote.api.UserManagementApiService
import com.example.lingora_fe.admin.user.data.repository.UserManagementRepositoryImpl
import com.example.lingora_fe.admin.user.domain.repository.UserManagementRepository
import com.example.lingora_fe.admin.report.data.remote.api.ReportApiService
import com.example.lingora_fe.admin.report.data.repository.ReportRepositoryImpl
import com.example.lingora_fe.admin.report.domain.repository.ReportRepository
import com.example.lingora_fe.admin.withdrawal.data.remote.api.AdminWithdrawalApiService
import com.example.lingora_fe.admin.withdrawal.data.repository.AdminWithdrawalRepositoryImpl
import com.example.lingora_fe.admin.withdrawal.domain.repository.AdminWithdrawalRepository
import com.example.lingora_fe.admin.dashboard.data.remote.api.DashboardApiService
import com.example.lingora_fe.admin.dashboard.data.repository.DashboardRepositoryImpl
import com.example.lingora_fe.admin.dashboard.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdminRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserManagementRepository(
        userManagementRepositoryImpl: UserManagementRepositoryImpl
    ): UserManagementRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTopicRepository(
        topicRepositoryImpl: TopicRepositoryImpl
    ): TopicRepository

    @Binds
    @Singleton
    abstract fun bindWordRepository(
        wordRepositoryImpl: WordRepositoryImpl
    ): WordRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        reportRepositoryImpl: ReportRepositoryImpl
    ): ReportRepository

    @Binds
    @Singleton
    abstract fun bindAdminWithdrawalRepository(
        impl: AdminWithdrawalRepositoryImpl
    ): AdminWithdrawalRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        impl: DashboardRepositoryImpl
    ): DashboardRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AdminNetworkModule {

    // User Management
    @Provides
    @Singleton
    fun provideUserManagementApiService(retrofit: Retrofit): UserManagementApiService {
        return retrofit.create(UserManagementApiService::class.java)
    }

    // Category Management
    @Provides
    @Singleton
    fun provideCategoryApiService(retrofit: Retrofit): CategoryApiService {
        return retrofit.create(CategoryApiService::class.java)
    }

    // Topic Management
    @Provides
    @Singleton
    fun provideTopicApiService(retrofit: Retrofit): TopicApiService {
        return retrofit.create(TopicApiService::class.java)
    }

    // Word Management
    @Provides
    @Singleton
    fun provideWordApiService(retrofit: Retrofit): WordApiService {
        return retrofit.create(WordApiService::class.java)
    }

    // Report Management
    @Provides
    @Singleton
    fun provideReportApiService(retrofit: Retrofit): ReportApiService {
        return retrofit.create(ReportApiService::class.java)
    }

    // Withdrawal Management
    @Provides
    @Singleton
    fun provideAdminWithdrawalApiService(retrofit: Retrofit): AdminWithdrawalApiService {
        return retrofit.create(AdminWithdrawalApiService::class.java)
    }

    // Dashboard
    @Provides
    @Singleton
    fun provideDashboardApiService(retrofit: Retrofit): DashboardApiService {
        return retrofit.create(DashboardApiService::class.java)
    }
}

