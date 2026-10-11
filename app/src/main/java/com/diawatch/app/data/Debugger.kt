package com.diawatch.app.data

import android.content.Context
import android.os.SystemClock
import android.util.Log
import java.io.File

class Debugger {
    companion object {
        fun print(context: Context, tag: String, msg: String, saveToFile: Boolean = false) {

            Log.d(tag, msg)

            if(saveToFile) {
                val logFile = File(context.filesDir, "debug.log")
                logFile.appendText("$tag, $msg, ${SystemClock.elapsedRealtime()} \n")
            }

        }
    }
}