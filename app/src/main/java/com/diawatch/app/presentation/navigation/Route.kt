package com.diawatch.app.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Home : NavKey
    @Serializable
    data object AlarmSettings : NavKey
}