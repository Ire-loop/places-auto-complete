package com.example.places.core.network

import com.example.places.core.network.api.RoutesApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object
NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)  // Match HttpURLConnection timeout
            .readTimeout(10, TimeUnit.SECONDS)     // Match HttpURLConnection timeout
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }



    @Provides
    @Singleton
    @Named("RoutesClient")
    fun provideGoogleRetrofitClient(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.ROUTES_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // For JSON response
            .build()
    }

    @Provides
    @Singleton
    fun providesRoutesApiService(
        @Named("RoutesClient") retrofit: Retrofit
    ): RoutesApi {
        return retrofit.create(RoutesApi::class.java)
    }
}
