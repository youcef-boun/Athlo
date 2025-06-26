package com.youcef_bounaas.athlo.Navigation.presentation

import androidx.lifecycle.SavedStateHandle

class DetailArgs(val id: Int) {
    companion object {
        const val ID_ARG = "id"
    }
}

// Extension function to get typed arguments from SavedStateHandle
fun SavedStateHandle.detailArgs() = DetailArgs(
    id = checkNotNull(get<Int>(DetailArgs.ID_ARG)) { "Detail ID is required" }
)

class SettingsArgs(val theme: String?) {
    companion object {
        const val THEME_ARG = "theme"
    }
}

fun SavedStateHandle.settingsArgs() = SettingsArgs(
    theme = get<String>(SettingsArgs.THEME_ARG)
)