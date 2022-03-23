package com.bitmart.demo.di

import com.bitmart.data.remote.api.ContractService
import com.bitmart.data.remote.repository.ContractRemoteRepository
import com.bitmart.demo.RetrofitCompat
import org.koin.dsl.module
import retrofit2.Retrofit

val remoteDataSourceModule = module {
    single {
        provideRetrofit()
    }

    single {
        provideContractService(get())
    }

    single {
        ContractRemoteRepository(get())
    }
}

internal fun provideRetrofit(): Retrofit = RetrofitCompat.getRetrofit()

internal fun provideContractService(retrofit: Retrofit): ContractService = retrofit.create(ContractService::class.java)