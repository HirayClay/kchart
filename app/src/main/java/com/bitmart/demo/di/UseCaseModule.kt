package com.bitmart.demo.di

import com.bitmart.data.domain.usecase.GetKLineListUseCase
import com.bitmart.data.remote.repository.ContractRemoteRepository
import org.koin.dsl.module

val useCaseModule = module {

    single {
        GetKLineListUseCase(get<ContractRemoteRepository>())
    }
}
