package com.youcef_bounaas.athlo.Authentication.domain.use_case

import com.youcef_bounaas.athlo.Authentication.domain.AuthRepository

class SignOutUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.signOut()
    }
}