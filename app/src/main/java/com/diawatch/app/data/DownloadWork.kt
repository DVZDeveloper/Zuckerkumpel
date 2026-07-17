package com.diawatch.app.data

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.edit
import androidx.wear.tiles.TileService
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diawatch.app.R
import com.diawatch.app.data.SensorKeys.OLD_TIMESTAMP
import com.diawatch.app.tile.MainTileService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

class DownloadWork(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {

        Log.d("DOWNLOAD_WORK", "Work start yay.")
        val logFile = File(applicationContext.filesDir, "debug.log")
        logFile.appendText("Work start at ${System.currentTimeMillis()} \n")

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

        if(!loginResponse.isSuccessful){
            return Result.retry()
        }

        val loginJSON = JSONObject(loginResponse.body.string())

        val token = loginJSON.getJSONObject("data").getJSONObject("authTicket").getString("token")
        Log.d("DOWNLOAD_WORK", "retrieved token: $token")

        val userID = loginJSON.getJSONObject("data").getJSONObject("user").getString("id")
        Log.d("DOWNLOAD_WORK", "retrieved uid: $userID")

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

        if(!connectionsResponse.isSuccessful){
            return Result.retry()
        }

        val connectionsJSON = JSONObject(connectionsResponse.body.string())

        val patientID = connectionsJSON.getJSONArray("data").getJSONObject(0).getString("patientId")
        Log.d("DOWNLOAD_WORK", "retrieved patientid: $patientID")


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

        if(!graphResponse.isSuccessful){
            return Result.retry()
        }

        val graphJSON = JSONObject(graphResponse.body.string())

        val glucoseValue = graphJSON.getJSONObject("data").getJSONObject("connection").getJSONObject("glucoseMeasurement").getDouble("Value")
        Log.d("DOWNLOAD_WORK", "retrieved glucoseValue: $glucoseValue")
        logFile.appendText("Retieved value of $glucoseValue at ${System.currentTimeMillis()} \n")

        val time = graphJSON.getJSONObject("data").getJSONObject("connection").getJSONObject("glucoseMeasurement").getString("Timestamp")
        Log.d("DOWNLOAD_WORK", "retrieved Timestamp: $time")


        //Speichere neue Sensordaten und fordere Tile dazu auf, sich upzudaten
        applicationContext.sensorDataStore.edit {
            preferences -> preferences[SensorKeys.LAST_VALUE] = glucoseValue
            preferences[SensorKeys.TIMESTAMP] = System.currentTimeMillis().toDouble()
        }

        Log.d("DOWNLOAD_WORK", "current Time: ${System.currentTimeMillis()}, historic values: ${applicationContext.sensorDataStore.data.map {preferences -> preferences[OLD_TIMESTAMP] ?: ""}.first()}")


        if(glucoseValue !in 4.0..15.0) {
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
        }


        TileService.getUpdater(applicationContext).requestUpdate(MainTileService::class.java)
        logFile.appendText("Updated tile at ${System.currentTimeMillis()}")

        return Result.success()
    }




    //hashing funktion
    fun sha256Digest(string: String): String{
        return MessageDigest.getInstance("SHA-256")
            .digest(string.toByteArray())
            .joinToString("") {"%02x".format(it)}
        //kotlin nutzt eine spezielle form für lambda syntax. das {} ist ein argument für die funktion jointostring.
        //"%02x" bedeutet konvertiere jedes byte (it) zu hex (x), nutze immer 2 symbole (2) und fülle mit 0 (0). % bedeutet einfach dass eine Formatierungsangabe beginnt

    }
}