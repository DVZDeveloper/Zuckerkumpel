package com.diawatch.app.data

import android.content.Context
import android.content.Intent

class ServiceLauncher {
    companion object {
        fun launch(context: Context){
            val intent = Intent(context, CheckBloodSugarService::class.java)
            context.startService(intent)
        }
    }
}