package com.diawatch.app.data


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File


class DownloadAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        Debugger.print(context, "DOWNLOAD_ALARM_RECEIVER", "Alarm abgelaufen")
        ServiceLauncher.launch(context)
        AlarmScheduler.schedule(context)

    }
}