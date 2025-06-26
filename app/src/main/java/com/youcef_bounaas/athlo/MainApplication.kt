package com.youcef_bounaas.athlo

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.youcef_bounaas.athlo.Authentication.di.AuthModule
import com.youcef_bounaas.athlo.Authentication.di.SupabaseModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                appModule,
                SupabaseModule,
                AuthModule

            )
        }
    }
}