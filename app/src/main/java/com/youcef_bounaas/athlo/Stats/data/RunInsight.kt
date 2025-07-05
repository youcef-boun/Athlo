package com.youcef_bounaas.athlo.Stats.data

import kotlinx.serialization.Serializable

@Serializable
data class RunInsight(
    val id: String? = null,
    val runId: String,
    val insight: String,
    val createdAt: String = java.time.LocalDateTime.now().toString()
)
