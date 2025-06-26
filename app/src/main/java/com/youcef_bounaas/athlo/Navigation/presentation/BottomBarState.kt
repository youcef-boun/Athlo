package com.youcef_bounaas.athlo.Navigation.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class BottomBarState {
    var visible by mutableStateOf(true)
        private set

    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }
}

val LocalBottomBarState = staticCompositionLocalOf { BottomBarState() }