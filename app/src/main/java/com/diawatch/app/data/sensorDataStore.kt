package com.diawatch.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore


val Context.sensorDataStore: DataStore<Preferences> by preferencesDataStore(name = "sensor_data")

object SensorKeys {
    val LAST_VALUE = doublePreferencesKey("last_value")
    val TIMESTAMP = doublePreferencesKey("timestamp")
    val OLD_TIMESTAMP = doublePreferencesKey("old_timestamp")
}