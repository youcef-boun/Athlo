package com.youcef_bounaas.athlo.Authentication.domain

import io.github.jan.supabase.auth.user.UserSession


import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signOut(): Result<Unit>
    fun observeSession(): Flow<com.youcef_bounaas.athlo.Authentication.domain.model.UserSession?>
}
