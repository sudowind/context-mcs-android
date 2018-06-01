package com.example.wangfeng.csapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.wangfeng.csapp.DataLogger

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("AlarmReceiver", "alarm！")
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        val i = Intent(context, DataLogger::class.java)
        context.startService(i)
    }
}
