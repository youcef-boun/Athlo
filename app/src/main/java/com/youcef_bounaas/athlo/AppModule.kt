package com.youcef_bounaas.athlo

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import com.youcef_bounaas.athlo.Record.presentation.RecordViewModel
import com.youcef_bounaas.athlo.UserInfo.data.UserInfoRepository
import com.youcef_bounaas.athlo.UserInfo.presentation.UserInfoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
val appModule = module {

    viewModel { (savedStateHandle: SavedStateHandle) ->
        RecordViewModel(get(), savedStateHandle)
    }

    factory { UserInfoRepository(get()) }

    viewModel { UserInfoViewModel(get()) }



}