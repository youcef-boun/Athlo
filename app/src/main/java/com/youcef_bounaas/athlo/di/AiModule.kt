package com.youcef_bounaas.athlo.di

import android.content.Context
import com.youcef_bounaas.athlo.ai.data.OpenAIRepository
import com.youcef_bounaas.athlo.ai.data.OpenAIRepositoryImpl
import com.youcef_bounaas.athlo.ai.presentation.RunInsightViewModel
import com.youcef_bounaas.athlo.utils.ApiKeyManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val aiModule = module {
    single { ApiKeyManager(androidContext()) }
    single<OpenAIRepository> { OpenAIRepositoryImpl(androidContext()) }
    viewModel { RunInsightViewModel(get()) }
}
