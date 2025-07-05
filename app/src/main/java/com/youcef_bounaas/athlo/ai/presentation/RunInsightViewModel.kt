package com.youcef_bounaas.athlo.ai.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youcef_bounaas.athlo.Stats.data.Run
import com.youcef_bounaas.athlo.UserInfo.data.Profile
import com.youcef_bounaas.athlo.ai.data.OpenAIRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed class InsightState {
    object Idle : InsightState()
    object Loading : InsightState()
    data class Success(val insight: String) : InsightState()
    data class Error(val message: String) : InsightState()
}

class RunInsightViewModel(
    private val openAIRepository: OpenAIRepository
) : ViewModel() {
    private val _insightState = MutableStateFlow<InsightState>(InsightState.Idle)
    val insightState: StateFlow<InsightState> = _insightState.asStateFlow()

    fun generateInsight(run: Run, profile: Profile) {
        viewModelScope.launch {
            _insightState.value = InsightState.Loading
            try {
                val result = openAIRepository.generateRunInsight(run, profile)
                result.onSuccess { insight ->
                    _insightState.value = InsightState.Success(insight)
                }.onFailure { exception ->
                    _insightState.value = InsightState.Error(exception.message ?: "Unknown error occurred")
                }
            } catch (e: Exception) {
                _insightState.value = InsightState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun resetState() {
        _insightState.value = InsightState.Idle
    }
}
