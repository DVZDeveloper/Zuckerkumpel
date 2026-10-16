package com.zuckerkumpel.app.data

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zuckerkumpel.app.data.DataStoreManager.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SnoozeAlarmReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {

        val pendingResult = goAsync()
        Debugger.print(context, "SNOOZE_ALARM_RECEIVER", "Snoozed Alarm")
        receiverScope.launch {
            setValue(DataStoreKey.TIMEOUT_TIMESTAMP, System.currentTimeMillis())
            pendingResult.finish()
        }

        //lösche die alarmbenachrichtigung falls diese existiert
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(173)

    }
}