package com.youcef_bounaas.athlo.Authentication.domain.use_case

import com.youcef_bounaas.athlo.Authentication.domain.AuthRepository

class SignUpUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<Unit> {
        return repository.signUp(email, password)
    }
}