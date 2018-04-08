package com.example.wangfeng.csapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.wangfeng.csapp.DataLogger

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val i = Intent(context, DataLogger::class.java)
        context.startService(i)
    }
}
