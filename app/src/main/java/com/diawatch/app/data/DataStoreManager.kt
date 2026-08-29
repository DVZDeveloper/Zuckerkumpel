package com.diawatch.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "DataStore")

object DataStoreKey {

    val LAST_VALUE = DataStorePreference(
        doublePreferencesKey("last_value"),
        0.0
    )
    val TREND_ARROW = DataStorePreference(
        intPreferencesKey("trend_arrow"),
        3
    )

    val HIGH_BLOOD_SUGAR_ALARM_ENABLED = DataStorePreference(
        booleanPreferencesKey("high_blood_sugar_alarm_enabled"),
        true
    )
    val HIGH_BLOOD_SUGAR_LIMIT = DataStorePreference(
        intPreferencesKey("high_blood_sugar_limit"),
        15
    )
    val HIGH_BLOOD_SUGAR_TIMEOUT = DataStorePreference(
        intPreferencesKey("high_blood_sugar_timeout"),
        45
    )


    val LOW_BLOOD_SUGAR_ALARM_ENABLED = DataStorePreference(
        booleanPreferencesKey("low_blood_sugar_alarm_enabled"),
        true
    )
    val LOW_BLOOD_SUGAR_LIMIT = DataStorePreference(
        intPreferencesKey("low_blood_sugar_limit"),
        4
    )
    val LOW_BLOOD_SUGAR_TIMEOUT = DataStorePreference(
        intPreferencesKey("low_blood_sugar_timeout"),
        15
    )

    val TOKEN = DataStorePreference(
        stringPreferencesKey("token"),
        ""
    )
    val USER_ID = DataStorePreference(
        stringPreferencesKey("UserID"),
        ""
    )
    val PATIENT_ID = DataStorePreference(
        stringPreferencesKey("PatientID"),
        ""
    )


    val TIMEOUT_TIMESTAMP = DataStorePreference(
        longPreferencesKey("timestamp"),
        0
    )

}

data class DataStorePreference<T>(
    val key: Preferences.Key<T>,
    val default: T
)


object DataStoreManager {

    private lateinit var dataStore: DataStore<Preferences>

    fun init(context: Context) {
        dataStore = context.applicationContext.dataStore
    }

    suspend fun <T> getValue(setting: DataStorePreference<T>): T {
        return dataStore.data.first()[setting.key] ?: setting.default
    }

    suspend fun <T> setValue(setting: DataStorePreference<T>, value: T) {
        dataStore.edit {
            it[setting.key] = value
        }
    }
}