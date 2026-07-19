package com.diawatch.app.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File

class AlarmScheduler {

    companion object {
        fun schedule(context: Context) {
            val delayInMinutes = 5
            Log.d("ALARM_SCHEDULER", "Alarm für $delayInMinutes gesetzt!")
            val logFile = File(context.filesDir, "debug.log")
            logFile.appendText("Alram started at ${System.currentTimeMillis()} \n")
            val alarmManager =
                context.getSystemService(
                    Context.ALARM_SERVICE
                ) as AlarmManager

            if(alarmManager.canScheduleExactAlarms()) {

                Log.d("ALARM_SCHEDULER", "Exakter Alarm gesetzt!")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1000L * 60 * delayInMinutes,
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(
                            context,
                            DownloadAlarmReceiver::class.java
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }else{

                logFile.appendText("Failed to schedule exact alarm! \n")

                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + 1000L * 60 * delayInMinutes,
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(
                            context,
                            DownloadAlarmReceiver::class.java
                        ),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                )
            }
        }
    }
}
