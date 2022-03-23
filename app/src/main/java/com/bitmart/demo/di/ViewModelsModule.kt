package com.bitmart.demo.di

import com.bitmart.demo.viewmodel.MainActivityViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel {
        MainActivityViewModel(get())
    }
}