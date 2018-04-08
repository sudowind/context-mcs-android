package com.example.wangfeng.csapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.content.IntentFilter



class ScreenBroadcastReceiver : BroadcastReceiver() {

    private var action : String? = null
    private var tag : String = "screen"

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        action = intent.action
        when (action) {
            Intent.ACTION_SCREEN_ON -> Log.i(tag, "screen on")
            Intent.ACTION_SCREEN_OFF -> Log.i(tag, "screen off")
            Intent.ACTION_USER_PRESENT -> Log.i(tag, "user unlock")
        }
    }
}
