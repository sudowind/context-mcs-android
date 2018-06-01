package com.example.wangfeng.csapp.receiver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
//        TODO("BluetoothReceiver.onReceive() is not implemented")
        val action = intent.action
        if (action == BluetoothDevice.ACTION_FOUND) {

        } else if (action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {

        }
    }
}
