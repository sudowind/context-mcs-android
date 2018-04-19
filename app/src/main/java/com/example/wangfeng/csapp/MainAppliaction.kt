package com.example.wangfeng.csapp

import android.app.Application
import android.content.Context
import cn.jpush.android.api.JPushInterface

class MainAppliaction : Application() {

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        JPushInterface.setDebugMode(true)
        JPushInterface.init(this)
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
