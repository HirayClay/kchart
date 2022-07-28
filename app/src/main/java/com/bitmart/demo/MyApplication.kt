package com.bitmart.demo

import android.app.Application
import com.bitmart.demo.di.localDataSourceModule
import com.bitmart.demo.di.remoteDataSourceModule
import com.bitmart.demo.di.useCaseModule
import com.bitmart.demo.di.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApplication : Application() {

    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            modules(
                useCaseModule,
                viewModelsModule,
                localDataSourceModule,
                remoteDataSourceModule,
            )
        }
    }
}