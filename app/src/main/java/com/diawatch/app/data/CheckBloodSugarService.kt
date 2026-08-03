package com.diawatch.app.data

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.edit
import androidx.wear.tiles.TileService
import com.diawatch.app.R
import com.diawatch.app.tile.MainTileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.security.MessageDigest

class CheckBloodSugarService : Service() {

    private var syncJob: Job? = null

    private val serviceScope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        /*val notification = NotificationCompat.Builder(applicationContext, "alarm_channel")
            .build()

        ServiceCompat.startForeground(
            this,
            67,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )*/

        if (syncJob?.isActive == true) {
            //verhindert, dass mehrere Kopien der Coroutine entstehen können
            return START_STICKY
        }

        //Coroutine die den Code für das Syncen der Daten enthält (nutzt wieder die seltsame Lambda schreibweise von kotlin)
        syncJob = serviceScope.launch {

            Debugger.print(applicationContext, "CHECK_BLOOD_SUGAR_SERVICE", "Sync ausgeführt!")


            //sehr schlechta code tuan niamols es token exposed in code inna, nur für testzweck
            val email = "***REMOVED***"
            val password = "***REMOVED***"

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
                cancel("connection failed")

            }

            val loginJSON = JSONObject(loginResponse.body.string())

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
                cancel("connection failed")

            }

            val connectionsJSON = JSONObject(connectionsResponse.body.string())

            val patientID =
                connectionsJSON.getJSONArray("data").getJSONObject(0).getString("patientId")


            //Zuckerdaten via GET /llu/connections/{patientID}/graph
            val graphResponse = client.newCall(
                Request.Builder()
                    .url("https://api-eu.libreview.io/llu/connections/$patientID/graph")
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

            if (!graphResponse.isSuccessful) {
                cancel("connection failed")
            }

            val graphJSON = JSONObject(graphResponse.body.string())

            val glucoseValue = graphJSON.getJSONObject("data").getJSONObject("connection")
                .getJSONObject("glucoseMeasurement").getDouble("Value")
            Debugger.print(
                applicationContext,
                "CHECK_BLOOD_SUGAR_SERVICE",
                "retrieved glucoseValue: $glucoseValue"
            )

            //1 -> ⬆, 2 = ↗️, 3 = ➡, 4 =↘️, 5 = ⬇️
            val trendArrow = graphJSON.getJSONObject("data").getJSONObject("connection")
                .getJSONObject("glucoseMeasurement").getInt("TrendArrow")


            //Speichere neue Sensordaten und fordere Tile dazu auf, sich upzudaten
            applicationContext.sensorDataStore.edit { preferences ->
                preferences[SensorKeys.LAST_VALUE] = glucoseValue
                preferences[SensorKeys.TREND_ARROW] = trendArrow
            }


            if (glucoseValue !in 4.0..15.0) {
                try {
                    NotificationManagerCompat.from(applicationContext).notify(
                        173,
                        NotificationCompat.Builder(applicationContext, "alarm_channel")
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(
                                applicationContext.getString(R.string.sugar_alarm_title)
                            )
                            .setContentText(
                                applicationContext.getString(R.string.sugar_alarm_body)
                            )
                            .setPriority(NotificationManager.IMPORTANCE_HIGH)
                            .build()
                    )
                } catch (e: SecurityException) {
                    Log.e("CHECK_BLOOD_SUGAR_SERVICE", "Notification permission missing", e)
                }
            }

            Debugger.print(applicationContext, "CHECK_BLOOD_SUGAR_SERVICE", "Updated tile"
            )
            TileService.getUpdater(applicationContext).requestUpdate(MainTileService::class.java)

            stopSelf()

        }


        return START_STICKY
    }

    override fun onDestroy() {
        //wenn der Service abgebrochen wird, sollen auch alle coroutinen sofort beendet Werden
        serviceScope.cancel()
        super.onDestroy()

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