package com.youcef_bounaas.athlo.Maps.di

import com.youcef_bounaas.athlo.Maps.data.MapsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val MapsModule = module {
    viewModel { MapsViewModel() }
} 