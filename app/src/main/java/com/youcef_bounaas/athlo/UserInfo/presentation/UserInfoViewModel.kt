package com.youcef_bounaas.athlo.UserInfo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.youcef_bounaas.athlo.UserInfo.data.UserInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserInfoViewModel(
    private val repository: UserInfoRepository
) : ViewModel() {

    private val _insertResult = MutableStateFlow<Result<Unit>?>(null)
    val insertResult: StateFlow<Result<Unit>?> = _insertResult.asStateFlow()

    fun insertProfile(first: String, last: String, birthday: String, gender: String) {
        viewModelScope.launch {
            _insertResult.value = repository.insertUserProfile(first, last, birthday, gender)
        }
    }

    fun clearResult() {
        _insertResult.value = null
    }
}
