package com.diawatch.app.data

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.compose.ui.res.stringResource
import com.diawatch.app.R
import com.diawatch.app.data.DataStoreManager.setEncryptedValue
import com.diawatch.app.data.DataStoreManager.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest

class LoginService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val email: String = intent?.getStringExtra("EMAIL") ?: ""
        val password: String = intent?.getStringExtra("PASSWORD") ?: ""

        Debugger.print(applicationContext, "LOGIN_SERVICE", "Email: $email, Password: $password")

        serviceScope.launch {

            val client = OkHttpClient()


            //token & uid bekommen via POST /llu/auth/login
            val loginResponse = client.newCall(
                Request.Builder()
                    .url("https://api-eu.libreview.io/llu/auth/login")
                    .addHeader("version", "4.16.0")
                    .addHeader("User-Agent", "DiaWatch")
                    .addHeader("connection", "Keep-Alive")
                    .addHeader("product", "llu.android")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .post(
                        """{ 
                    |"email": "$email",
                    |"password": "$password"
                    |}""".trimMargin()
                            .toRequestBody("application/json".toMediaTypeOrNull())
                    )
                    .build()
            ).execute()

            if (!loginResponse.isSuccessful) {
                LoginStateHolder.set(LoginState.Error(
                    applicationContext.getString(R.string.error_login_no_internet
                    )))
                stopSelf()
                return@launch
            }

            val loginJSON = JSONObject(loginResponse.body.string())

            Debugger.print(applicationContext, "LOGIN_ERVICE", "$loginJSON")
            when (loginJSON.getInt("status")){
                0-> Unit
                2 -> {
                    LoginStateHolder.set(LoginState.Error(
                    applicationContext.getString(R.string.error_login_incorrect_credentials
                    )))
                    stopSelf()
                    return@launch
                }
                4 -> {
                    LoginStateHolder.set(LoginState.Error(
                        applicationContext.getString(R.string.error_login_reaccept_tou
                        )))
                    stopSelf()
                    return@launch
                }
                else -> {LoginStateHolder.set(LoginState.Error(
                    "Unknown error"
                ))
                stopSelf()
                    return@launch
                }
            }

            val token =
                loginJSON.getJSONObject("data").getJSONObject("authTicket").getString("token")

            val userID = loginJSON.getJSONObject("data").getJSONObject("user").getString("id")


            //patientID bekommen wir mit GET /llu/connections
            val connectionsResponse = client.newCall(
                Request.Builder()
                    .url("https://api-eu.libreview.io/llu/connections")
                    .addHeader("version", "4.16.0")
                    .addHeader("User-Agent", "DiaWatch")
                    .addHeader("connection", "Keep-Alive")
                    .addHeader("product", "llu.android")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("Account-Id", sha256Digest(userID))
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
            ).execute()

            if (!connectionsResponse.isSuccessful) {
                LoginStateHolder.set(LoginState.Error(
                    applicationContext.getString(R.string.error_login_no_internet
                    )))

                stopSelf()
                return@launch
            }

            val connectionsJSON = JSONObject(connectionsResponse.body.string())

            val patientID =
                connectionsJSON.getJSONArray("data").getJSONObject(0).getString("patientId")

            DataStoreManager.init(applicationContext)

            setEncryptedValue(DataStoreKey.PATIENT_ID, patientID)
            setEncryptedValue(DataStoreKey.TOKEN, token)
            setEncryptedValue(DataStoreKey.USER_ID, userID)
            Log.d("LOGIN_SERVICE", "Success!")
            LoginStateHolder.set(LoginState.Success)
            stopSelf()
            return@launch
        }
        return START_STICKY
    }


    companion object {
        fun login(context: Context, email: String, password: String) {
            LoginStateHolder.set(LoginState.Loading)
            context.applicationContext.startService(
                Intent(
                    context,
                    LoginService::class.java
                ).apply {
                    putExtra("EMAIL", email)
                    putExtra("PASSWORD", password)
                }
            )
        }
    }

    //hashing funktion
    fun sha256Digest(string: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(string.toByteArray())
            .joinToString("") { "%02x".format(it) }
        //kotlin nutzt eine spezielle form für lambda syntax. das {} ist ein argument für die funktion jointostring.
        //"%02x" bedeutet konvertiere jedes byte (it) zu hex (x), nutze immer 2 symbole (2) und fülle mit 0 (0). % bedeutet einfach dass eine Formatierungsangabe beginnt

    }
}

sealed interface LoginState {
    data object Idle : LoginState
    data object Loading : LoginState
    data object Success : LoginState
    data class Error(val message: String? = null) : LoginState
}

object LoginStateHolder {
    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun set(newState: LoginState) { _state.value = newState }
    fun reset() { _state.value = LoginState.Idle }
}