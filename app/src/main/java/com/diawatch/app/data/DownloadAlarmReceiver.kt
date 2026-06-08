package com.diawatch.app.data


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log


class DownloadAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Log.d("DOWNLOAD_ALARM_RECEIVER", "Alarm abgelaufen")
        WorkScheduler.schedule(context)
        AlarmScheduler.schedule(context)

    }
}