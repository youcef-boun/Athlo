package com.youcef_bounaas.athlo.Stats.data

import kotlinx.serialization.Serializable



@Serializable
data class Run(
    val id: String? = null,
    val user_id: String,
    val date: String,
    val distance_km: Double,
    val duration_secs: Long,
    val avg_pace: Double,
    val gpx_url: String,
    val city: String? = null
)