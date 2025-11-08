package com.example.lingora_fe.di

import android.content.Context
import android.content.SharedPreferences
import com.example.lingora_fe.core.network.AuthInterceptor
import com.example.lingora_fe.core.network.PersistentCookieJar
import com.example.lingora_fe.core.network.SelectiveNullsAdapterFactory
import com.example.lingora_fe.core.network.TokenAuthenticator
import com.example.lingora_fe.core.network.TokenManager
import com.example.lingora_fe.di.qualifier.AuthApiClient
import com.example.lingora_fe.di.qualifier.RefreshTokenClient
import com.example.lingora_fe.util.Constant
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("lingora_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .registerTypeAdapterFactory(SelectiveNullsAdapterFactory())
            .create()
    }

    // OkHttpClient cho refresh token (KHÔNG có authenticator để tránh vòng lặp)
    // Có CookieJar để tự động lưu và gửi cookies (refreshToken)
    @Provides
    @Singleton
    @RefreshTokenClient
    fun provideRefreshTokenOkHttpClient(
        cookieJar: PersistentCookieJar
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .cookieJar(cookieJar)  // Tự động lưu và gửi cookies
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit cho refresh token
    @Provides
    @Singleton
    @RefreshTokenClient
    fun provideRefreshTokenRetrofit(
        @RefreshTokenClient okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // OkHttpClient cho các API thường (CÓ authenticator và auth interceptor)
    // Có CookieJar để tự động lưu và gửi cookies (refreshToken)
    @Provides
    @Singleton
    @AuthApiClient
    fun provideAuthOkHttpClient(
        @RefreshTokenClient retrofit: Retrofit,
        tokenManager: TokenManager,
        authInterceptor: AuthInterceptor,
        cookieJar: PersistentCookieJar
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Tạo AuthApiService từ retrofit không có authenticator
        val refreshAuthApiService = retrofit.create(com.example.lingora_fe.auth.data.remote.api.AuthApiService::class.java)
        
        // Tạo TokenAuthenticator với refreshAuthApiService và TokenManager
        val tokenAuthenticator = TokenAuthenticator(
            tokenManager,
            refreshAuthApiService
        )

        return OkHttpClient.Builder()
            .cookieJar(cookieJar)  // Tự động lưu và gửi cookies
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)  // Tự động thêm Authorization header
            .authenticator(tokenAuthenticator)  // Tự động refresh token khi 401
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit chính cho các API thường
    @Provides
    @Singleton
    fun provideRetrofit(
        @AuthApiClient okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constant.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
