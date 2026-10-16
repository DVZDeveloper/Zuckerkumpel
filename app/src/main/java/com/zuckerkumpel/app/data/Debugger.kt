package com.zuckerkumpel.app.data

import android.content.Context
import android.util.Log

class Debugger {
    companion object {
        fun print(context: Context, tag: String, msg: String, saveToFile: Boolean = false) {

            Log.d(tag, msg)

            /*if(saveToFile) {
                val logFile = File(context.filesDir, "debug.log")
                logFile.appendText("$tag, $msg, ${SystemClock.elapsedRealtime()} \n")
            }
            */
        }
    }
}