package com.allsoftdroid.audiobook.feature_mini_player.di

import com.allsoftdroid.audiobook.feature_mini_player.presentation.viewModel.MiniPlayerViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

fun injectFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(listOf(
        miniPlayerViewModelModule
    ))
}

val miniPlayerViewModelModule: Module = module{
    viewModel {
        MiniPlayerViewModel(get())
    }
}