package com.diawatch.app.data

import android.content.Context
import android.util.Log

class Debugger {
    companion object {
        fun print(context: Context, tag: String, msg: String) {

            Log.d(tag, msg)

            /*
            val logFile = File(context.filesDir, "debug.log")
            logFile.appendText("$tag, $msg, ${System.currentTimeMillis()}")
            */

        }
    }
}