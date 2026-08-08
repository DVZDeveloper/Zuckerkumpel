package com.diawatch.app.data

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.app.NotificationManager

class SnoozeAlarmReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {

        val dataStoreManager = DataStoreManager.getInstance(context.applicationContext)
        val pendingResult = goAsync()
        receiverScope.launch {
            dataStoreManager.setValue(DataStoreKey.TIMEOUT_TIMESTAMP, System.currentTimeMillis())
            pendingResult.finish()
        }

        //lösche die alarmbenachrichtigung falls diese existiert
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(173)

    }
    companion object {
        fun getActionPendingIntent(context : Context) : PendingIntent{
            return PendingIntent.getBroadcast(
                context,
                173,
                Intent(
                    context,
                    SnoozeAlarmReceiver::class.java
                ),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}