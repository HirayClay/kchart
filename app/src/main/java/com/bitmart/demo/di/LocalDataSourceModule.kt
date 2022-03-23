package com.bitmart.demo.di

import android.content.Context
import androidx.room.Room
import com.bitmart.data.local.ContractDatabase
import com.bitmart.data.local.repository.ContractLocalRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localDataSourceModule = module {
    single {
        provideDatabase(androidContext())
    }

    single {
        ContractLocalRepository(androidContext())
    }
}

internal fun provideDatabase(context: Context): ContractDatabase = Room.databaseBuilder(context, ContractDatabase::class.java, "contract_db").build()