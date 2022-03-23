package com.bitmart.demo

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitCompat {

    fun getRetrofit(): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(1000L, TimeUnit.MILLISECONDS)
            .readTimeout(1000L, TimeUnit.MILLISECONDS)
            .writeTimeout(1000L, TimeUnit.MILLISECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply { this.level = HttpLoggingInterceptor.Level.BODY }) //添加日志拦截器
            .build()
        return Retrofit.Builder().client(okHttpClient).baseUrl("https://dapi.binance.com").addConverterFactory(GsonConverterFactory.create()).build()
    }
}