package com.syrous.imageloadinglib.di

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.syrous.imageloadinglib.data.ApiService
import com.syrous.imageloadinglib.data.ApiServiceImpl
import com.syrous.imageloadinglib.data.okhttp.OkHttpService
import com.syrous.imageloadinglib.data.okhttp.OkHttpServiceImpl
import com.syrous.imageloadinglib.imageLoader.ImageLoader
import com.syrous.imageloadinglib.imageLoader.ImageLoaderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(okHttpService: OkHttpService): ApiService = ApiServiceImpl(okHttpService)

    @Provides
    @Singleton
    fun provideOkHttpService(okHttpClient: OkHttpClient, moshi: Moshi): OkHttpService {
        return OkHttpServiceImpl.Builder()
            .addOkHttpClient(okHttpClient)
            .addBaseUrl("https://api.unsplash.com/")
            .addMoshi(moshi)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    fun provideMoshiClient(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader = ImageLoaderImpl.
    Builder(context).build()

}