package com.example.wangfeng.csapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.content.IntentFilter
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class ScreenBroadcastReceiver : BroadcastReceiver() {

    private var action : String? = null
    private var tag : String = "screen"

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        action = intent.action
        var actionCode = -1
        when (action) {
            Intent.ACTION_SCREEN_ON -> {
                Log.i(tag, "screen on")
                actionCode = 1
            }
            Intent.ACTION_SCREEN_OFF -> {
                Log.i(tag, "screen off")
                actionCode = 0
            }
            Intent.ACTION_USER_PRESENT -> {
                Log.i(tag, "user unlock")
                actionCode = 2
            }
        }
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
//                val jsonObj = JSONObject()
//                jsonObj.put("action", actionCode)
                val formBody = FormBody.Builder()
                        .add("action", actionCode.toString())
                        .build()
//                val requestBody = RequestBody.create(okhttp3.MediaType.parse("application/json"), jsonObj.toString())
                val request = Request.Builder()
                        .post(formBody)
                        .url("http://192.168.255.14:8000/task/lock_record")
                        .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        println("something wrong")
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val res = response!!.body()!!.string()

                        if (response.code() == 200) {
                            Log.i(tag, res)
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).run()
    }
}
