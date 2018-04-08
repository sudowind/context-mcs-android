package com.example.wangfeng.csapp

import android.app.Application
import android.content.Context

class MainAppliaction : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    companion object {
        /**
         * 获取context
         * @return
         */
        var context: Context? = null
            private set
    }
}
