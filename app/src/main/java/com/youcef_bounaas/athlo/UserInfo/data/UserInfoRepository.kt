package com.youcef_bounaas.athlo.UserInfo.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class UserInfoRepository(
    private val supabase: SupabaseClient
) {
    suspend fun insertUserProfile(
        firstName: String,
        lastName: String,
        birthday: String,
        gender: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = supabase.auth.currentUserOrNull()?.id
                ?: return@withContext Result.failure(Exception("User not logged in"))

            val profile = Profile(
              //  id = UUID.fromString(userId),
                id = userId,
                first_name = firstName,
                last_name = lastName,
                birthday = birthday,
                gender = gender
            )

            supabase.from("profiles").insert(profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
