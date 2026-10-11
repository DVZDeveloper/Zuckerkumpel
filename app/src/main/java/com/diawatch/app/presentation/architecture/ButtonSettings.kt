package com.diawatch.app.presentation.architecture

import com.diawatch.app.R
import com.diawatch.app.data.DataStoreKey
import com.diawatch.app.data.DataStorePreference

data class ButtonSettings(
    val titleID: String,
    val titleResource: Int,
    val limit: DataStorePreference<Double>,
    val enabled: DataStorePreference<Boolean>,
    val timeout: DataStorePreference<Int>
)

enum class ButtonSettingsType(val settings: ButtonSettings) {
    HIGH(
        ButtonSettings(
            "high",
            R.string.setting_title_high,
            DataStoreKey.HIGH_BLOOD_SUGAR_LIMIT,
            DataStoreKey.HIGH_BLOOD_SUGAR_ALARM_ENABLED,
            DataStoreKey.HIGH_BLOOD_SUGAR_TIMEOUT
        )
    ),
    LOW(
        ButtonSettings(
            "low",
            R.string.setting_title_low,
            DataStoreKey.LOW_BLOOD_SUGAR_LIMIT,
            DataStoreKey.LOW_BLOOD_SUGAR_ALARM_ENABLED,
            DataStoreKey.LOW_BLOOD_SUGAR_TIMEOUT
        )
    );

    companion object {
        fun fromId(titleID: String): ButtonSettingsType =
            entries.find { it.settings.titleID == titleID }
                ?: throw IllegalArgumentException("Unknown ButtonSettings id: $titleID")
    }
}