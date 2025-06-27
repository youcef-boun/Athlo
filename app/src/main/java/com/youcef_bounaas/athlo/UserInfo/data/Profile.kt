package com.youcef_bounaas.athlo.UserInfo.data

import kotlinx.serialization.Contextual
import java.util.UUID

import kotlinx.serialization.Serializable

@Serializable
data class Profile(

  //  @Contextual val id: UUID,
    val id: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val birthday: String,
    val gender: String,
    val avatar_url : String
)
