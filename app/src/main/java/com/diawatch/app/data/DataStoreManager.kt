package com.diawatch.app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.diawatch.app.data.DataStoreManager.TRANSFORMATION
import kotlinx.coroutines.flow.first
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.util.stream.Stream
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

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
        doublePreferencesKey("high_blood_sugar_limit"),
        15.0
    )
    val HIGH_BLOOD_SUGAR_TIMEOUT = DataStorePreference(
        intPreferencesKey("high_blood_sugar_timeout"),
        45
    )
    val HIGH_BLOOD_SUGAR_CURRENTLY_ON = DataStorePreference(
        booleanPreferencesKey("high_blood_sugar_currently_on"),
        true
    )


    val LOW_BLOOD_SUGAR_ALARM_ENABLED = DataStorePreference(
        booleanPreferencesKey("low_blood_sugar_alarm_enabled"),
        true
    )
    val LOW_BLOOD_SUGAR_LIMIT = DataStorePreference(
        doublePreferencesKey("low_blood_sugar_limit"),
        4.0
    )
    val LOW_BLOOD_SUGAR_TIMEOUT = DataStorePreference(
        intPreferencesKey("low_blood_sugar_timeout"),
        15
    )
    val LOW_BLOOD_SUGAR_CURRENTLY_ON = DataStorePreference(
        booleanPreferencesKey("low_blood_sugar_currently_on"),
        true
    )

    val TOKEN = DataStorePreference(
        stringPreferencesKey("token"),
        "",
    )
    val USER_ID = DataStorePreference(
        stringPreferencesKey("UserID"),
        "",
    )
    val PATIENT_ID = DataStorePreference(
        stringPreferencesKey("PatientID"),
        "",
    )

    val TIMEOUT_TIMESTAMP = DataStorePreference(
        longPreferencesKey("timestamp"),
        0
    )

}

data class DataStorePreference<T>(
    val key: Preferences.Key<T>,
    val default: T,
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

    suspend fun getEncryptedValue(setting: DataStorePreference<String>): String{
        val data = dataStore.data.first()[setting.key] ?: setting.default
        if(data == ""){
            return ""
        }
        return try {decrypt(data)} catch (e: Exception) {""}
    }

    suspend fun setEncryptedValue(setting: DataStorePreference<String>, value: String) {
        dataStore.edit {
            it[setting.key] = encrypt(value)
        }
    }

    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    private fun createKey(): SecretKey {
        return KeyGenerator.getInstance(ALGORITHM).apply {
            init(
                KeyGenParameterSpec.Builder(
                    "secret",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(BLOCK_MODE)
                    .setEncryptionPaddings(PADDING)
                    .setUserAuthenticationRequired(false)
                    .setRandomizedEncryptionRequired(true)
                    .build()
            )
        }.generateKey()
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore.getEntry("secret", null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun encrypt(string: String): String {
        val encryptCipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, DataStoreManager.getKey())
        }
        val bytes = string.toByteArray(Charsets.UTF_8)
        val encryptedBytes = encryptCipher.doFinal(bytes)

        val byteStream = ByteArrayOutputStream()
        DataOutputStream(byteStream).use {
            it.writeInt(encryptCipher.iv.size)
            it.write(encryptCipher.iv)
            it.writeInt(encryptedBytes.size)
            it.write(encryptedBytes)
        }
        return Base64.encodeToString(byteStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun decrypt(stored: String): String{
        val bytes = Base64.decode(stored, Base64.NO_WRAP)
        DataInputStream(ByteArrayInputStream(bytes)).use {
            val ivSize = it.readInt()
            require(ivSize in 1..32) {"Invalid IV size: $ivSize"}
            val iv = ByteArray(ivSize)
            it.readFully(iv)

            val encryptedBytesSize = it.readInt()
            require(ivSize in 1..it.available()) {"Invalid data size: $encryptedBytesSize"}
            val encryptedBytes = ByteArray(encryptedBytesSize)
            it.readFully(encryptedBytes)

            return String(
                Cipher.getInstance(TRANSFORMATION).apply {
                    init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
                }.doFinal(encryptedBytes),
                Charsets.UTF_8
            )
        }
    }


}