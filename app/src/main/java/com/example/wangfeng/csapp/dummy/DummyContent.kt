package com.example.wangfeng.csapp.dummy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.example.wangfeng.csapp.MainActivity
import com.example.wangfeng.csapp.MainAppliaction
import com.example.wangfeng.csapp.R
import com.example.wangfeng.csapp.Utils
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    var ITEMS:MutableList<DummyItem> = ArrayList<DummyItem>()

    /**
     * A map of sample (dummy) items, by ID.
     */
    var ITEM_MAP:MutableMap<String, DummyItem> = HashMap<String, DummyItem>()

    init{
        // Add some sample items.
    }

    fun loadTasks() {
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(MainAppliaction.context))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val request = Request.Builder()
                        .url("http://www.sudowind.com:8000/user/task/task_list")
                        .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        println("something wrong")
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val res = response!!.body()!!.string()
//                            runOnUiThread({
//                                Toast.makeText(applicationContext, res, Toast.LENGTH_SHORT).show()
//                            })
                        Log.i("load content", res)
                        if (response.code() == 200) {
                            val obj = JSONArray(res)
                            ITEMS = ArrayList<DummyItem>()
                            for (i in 0..(obj.length() - 1))
                            {
                                val status : Int = obj.getJSONObject(i).get("status") as Int
                                var statusString : String = ""
                                when (status) {
                                    0 -> { statusString = "待接受"}
                                    1 -> { statusString = "已拒绝"}
                                    2 -> { statusString = "进行中"}
                                    3 -> { statusString = "已完成"}
                                    4 -> { statusString = "未完成"}
                                }
                                addItem(DummyItem(
                                        (i + 1).toString(),
                                        obj.getJSONObject(i).getJSONObject("task").getString("title"),
                                        obj.getJSONObject(i).getJSONObject("task").getString("content"),
                                        "截止时间：" + Utils().getTimeStr((obj.getJSONObject(i).get("end_time")).toString().toLong()),
                                        obj.getJSONObject(i).get("id").toString(),
                                        statusString,
                                        "task"
                                ))
                            }
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    fun addItem(item:DummyItem) {
        ITEMS.add(item)
        ITEM_MAP[item.id] = item
    }

    /**
     * A dummy item representing a piece of content.
     */
    class DummyItem( val id:String,  val title:String,  val details:String, val deadline:String,
                     val taskId:String, val status:String, val type:String) {

        override fun toString():String {
            return details
        }
    }

}
