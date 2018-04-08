package com.example.wangfeng.csapp

import java.text.SimpleDateFormat
import java.util.*

class Utils {
    init {}
    public fun getTimeStr(second: Long): String {
        val date = Date(second * 1000)
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return format.format(date)
    }
}