package com.diawatch.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.sensorDataStore: DataStore<Preferences> by preferencesDataStore(name = "sensor")

object SensorKeys {
    val LAST_VALUE = doublePreferencesKey("last_value")
    val TREND_ARROW = intPreferencesKey("trend_arrow")

}

val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

object UserPreferencesKeys {
    val HIGH_BLOOD_SUGAR_ALARM_ENABLED = booleanPreferencesKey("high_blood_sugar_alarm_enabled")
    val HIGH_BLOOD_SUGAR_LIMIT = doublePreferencesKey("high_blood_sugar_limit")
    val HIGH_BLOOD_SUGAR_TIMEOUT = intPreferencesKey("high_blood_sugar_timeout")

    val LOW_BLOOD_SUGAR_ALARM_ENABLED = booleanPreferencesKey("low_blood_sugar_alarm_enabled")
    val LOW_BLOOD_SUGAR_LIMIT = doublePreferencesKey("low_blood_sugar_limit")
    val LOW_BLOOD_SUGAR_TIMEOUT = intPreferencesKey("low_blood_sugar_timeout")

}

val Context.loginDataStore: DataStore<Preferences> by preferencesDataStore(name = "login")

object LoginDataKeys {
    val EMAIL = stringPreferencesKey("email")
    val PASSWORD = stringPreferencesKey("password")
}

val Context.protocolDataStore: DataStore<Preferences> by preferencesDataStore(name = "protocol")

object ProtocolKeys {
    val EMAIL = stringPreferencesKey("email")
    val PASSWORD = stringPreferencesKey("password")
}

val Context.timeoutDataStore: DataStore<Preferences> by preferencesDataStore(name = "timeout")

object TimeoutKeys {
    val TIMESTAMP = longPreferencesKey("TIMESTAMP")
}
