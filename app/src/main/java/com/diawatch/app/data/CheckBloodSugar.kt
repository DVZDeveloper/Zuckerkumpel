package com.diawatch.app.data

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.wear.tiles.TileService
import com.diawatch.app.R
import com.diawatch.app.data.DataStoreManager.getEncryptedValue
import com.diawatch.app.data.DataStoreManager.getValue
import com.diawatch.app.data.DataStoreManager.setValue
import com.diawatch.app.tile.MainTileService
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class CheckBloodSugar {

    companion object {
        suspend fun check(context: Context) {

            Debugger.print(
                context.applicationContext,
                "CHECK_BLOOD_SUGAR_SERVICE",
                "Sync ausgeführt!"
            )


            val client = OkHttpClient()

            DataStoreManager.init(context.applicationContext)

            val patientID = getEncryptedValue(DataStoreKey.PATIENT_ID)
            val userID = getEncryptedValue(DataStoreKey.USER_ID)
            val token = getEncryptedValue(DataStoreKey.TOKEN)

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
                return
            }


            val graphJSON = JSONObject(graphResponse.body.string())

            if (!graphJSON.has("data")) {
                return
            }

            val glucoseValue = graphJSON.getJSONObject("data").getJSONObject("connection")
                .getJSONObject("glucoseMeasurement").getDouble("Value")
            Debugger.print(
                context.applicationContext,
                "CHECK_BLOOD_SUGAR_SERVICE",
                "retrieved glucoseValue: $glucoseValue"
            )

            //1 -> ⬆, 2 = ↗️, 3 = ➡, 4 =↘️, 5 = ⬇️
            val trendArrow = graphJSON.getJSONObject("data").getJSONObject("connection")
                .getJSONObject("glucoseMeasurement").getInt("TrendArrow")


            //Speichere neue Sensordaten und fordere Tile dazu auf, sich upzudaten

            setValue(DataStoreKey.LAST_VALUE, glucoseValue)
            setValue(DataStoreKey.TREND_ARROW, trendArrow)

            Debugger.print(
                context.applicationContext, "CHECK_BLOOD_SUGAR_SERVICE", "Tile update requested"
            )
            TileService.getUpdater(context.applicationContext)
                .requestUpdate(MainTileService::class.java)

            val highLimit = getValue(DataStoreKey.HIGH_BLOOD_SUGAR_LIMIT)
            val lowLimit = getValue(DataStoreKey.LOW_BLOOD_SUGAR_LIMIT)
            //DataStore ist sehr rechenaufwändig, so mus nur nötigste Arbeit ausgeführt werden
            if (glucoseValue < highLimit && glucoseValue > lowLimit) {
                return
            }


            val timeoutTimestamp = getValue(DataStoreKey.TIMEOUT_TIMESTAMP)

            val highEnabled = getValue(DataStoreKey.HIGH_BLOOD_SUGAR_ALARM_ENABLED)
            val highTimeout = getValue(DataStoreKey.HIGH_BLOOD_SUGAR_TIMEOUT)


            val action = NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher_round,
                context.applicationContext.getString(R.string.action_sugar_alarm),
                PendingIntent.getBroadcast(
                    context,
                    173,
                    Intent(
                        context,
                        SnoozeAlarmReceiver::class.java
                    ),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            ).build()


            if (glucoseValue > highLimit && highEnabled && (highTimeout.minutes + timeoutTimestamp.minutes < System.currentTimeMillis().milliseconds)) {
                try {
                    NotificationManagerCompat.from(context.applicationContext).notify(
                        173,
                        NotificationCompat.Builder(context.applicationContext, "alarm_channel")
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(
                                context.applicationContext.getString(R.string.title_sugar_alarm_low, glucoseValue)
                            )
                            .setContentText(
                                context.applicationContext.getString(R.string.body_sugar_alarm_high)
                            )
                            .setPriority(NotificationManager.IMPORTANCE_HIGH)
                            .addAction(action)
                            .build()
                    )
                } catch (e: SecurityException) {
                    Log.e("CHECK_BLOOD_SUGAR_SERVICE", "Notification permission missing", e)
                }
                return
            }

            val lowEnabled = getValue(DataStoreKey.LOW_BLOOD_SUGAR_ALARM_ENABLED)
            val lowTimeout = getValue(DataStoreKey.LOW_BLOOD_SUGAR_TIMEOUT)


            if (glucoseValue < lowLimit && lowEnabled && (lowTimeout.minutes + timeoutTimestamp.milliseconds < System.currentTimeMillis().milliseconds)) {

                try {
                    NotificationManagerCompat.from(context.applicationContext).notify(
                        173,
                        NotificationCompat.Builder(context.applicationContext, "alarm_channel")
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(
                                context.applicationContext.getString(
                                    R.string.title_sugar_alarm_low,
                                    glucoseValue
                                )
                            )
                            .setContentText(
                                context.applicationContext.getString(R.string.body_sugar_alarm_low)
                            )
                            .setPriority(NotificationManager.IMPORTANCE_HIGH)
                            .addAction(action)
                            .build()
                    )
                } catch (e: SecurityException) {
                    Log.e("CHECK_BLOOD_SUGAR_SERVICE", "Notification permission missing", e)
                }
                return
            }
            return

        }

        fun sha256Digest(string: String): String {
            return MessageDigest.getInstance("SHA-256")
                .digest(string.toByteArray())
                .joinToString("") { "%02x".format(it) }
            //kotlin nutzt eine spezielle form für lambda syntax. das {} ist ein argument für die funktion jointostring.
            //"%02x" bedeutet konvertiere jedes byte (it) zu hex (x), nutze immer 2 symbole (2) und fülle mit 0 (0). % bedeutet einfach dass eine Formatierungsangabe beginnt

        }
    }

}