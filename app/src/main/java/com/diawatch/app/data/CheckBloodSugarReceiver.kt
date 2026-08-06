package com.diawatch.app.data


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class CheckBloodSugarReceiver: BroadcastReceiver() {

    private  val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onReceive(context: Context, intent: Intent) {
        AlarmScheduler.schedule(context)
        Debugger.print(context, "DOWNLOAD_ALARM_RECEIVER", "Alarm abgelaufen")
        val pendingResult = goAsync()
        scope.launch{
            CheckBloodSugar.check(context)
            pendingResult.finish()
        }



    }
}