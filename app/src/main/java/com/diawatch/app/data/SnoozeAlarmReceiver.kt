package com.diawatch.app.data

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SnoozeAlarmReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        val dataStoreManager = DataStoreManager.getInstance(context.applicationContext)
        val pendingResult = goAsync()
        receiverScope.launch {
            dataStoreManager.setValue(DataStoreKeys.TIMEOUT_TIMESTAMP, System.currentTimeMillis())
            pendingResult.finish()
        }

    }
    companion object {
        fun getPendingIntent(context : Context) : PendingIntent{
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