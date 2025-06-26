package com.youcef_bounaas.athlo

import android.os.Build
import androidx.annotation.RequiresApi
import com.youcef_bounaas.athlo.Record.presentation.RecordViewModel
import com.youcef_bounaas.athlo.UserInfo.data.UserInfoRepository
import com.youcef_bounaas.athlo.UserInfo.presentation.UserInfoViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
val appModule = module {
    viewModel { RecordViewModel(get()) }

    factory { UserInfoRepository(get()) }

    viewModel { UserInfoViewModel(get()) }



}