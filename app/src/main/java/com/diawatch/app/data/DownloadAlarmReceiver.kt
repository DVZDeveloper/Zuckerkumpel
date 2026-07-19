package com.diawatch.app.data


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.File


class DownloadAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val logFile = File(context.filesDir, "debug.log")
        logFile.appendText("Received alarm at ${System.currentTimeMillis()} \n" )

        Log.d("DOWNLOAD_ALARM_RECEIVER", "Alarm abgelaufen")
        //WorkScheduler.schedule(context)
        ServiceLauncher.launch(context)
        AlarmScheduler.schedule(context)

    }
}