package com.youcef_bounaas.athlo.Authentication.di

import com.youcef_bounaas.athlo.Authentication.data.AuthRepositoryImpl
import com.youcef_bounaas.athlo.Authentication.domain.AuthRepository
import com.youcef_bounaas.athlo.Authentication.domain.use_case.SignOutUseCase
import com.youcef_bounaas.athlo.Authentication.domain.use_case.SignInUseCase
import com.youcef_bounaas.athlo.Authentication.domain.use_case.SignUpUseCase
import com.youcef_bounaas.athlo.Authentication.presentation.AuthViewModel
import io.github.jan.supabase.SupabaseClient
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModel



val AuthModule = module {

    // Repo binding
    single<AuthRepository> { AuthRepositoryImpl(get<SupabaseClient>()) }


    // UseCase bindings
    single { SignInUseCase(get()) }
    single { SignUpUseCase(get()) }
    single { SignOutUseCase(get()) }


    // ViewModel
    viewModel {
        AuthViewModel(
            signInUseCase = get(),
            signUpUseCase = get(),
            signOutUseCase = get()
        )
    }

}