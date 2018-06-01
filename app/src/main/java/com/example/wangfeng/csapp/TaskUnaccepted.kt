package com.example.wangfeng.csapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.example.wangfeng.csapp.dummy.DummyContent
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.util.ArrayList

class TaskUnaccepted : AppCompatActivity(), View.OnClickListener {

    private val tag = "task unaccepted"
    private var id = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.task_detail_unaccepted)
        setContentView(R.layout.task_detail_unaccepted)
        val bundle = this.intent.extras
        if (bundle != null) {
            val title = bundle.getString("title")
            val desc = bundle.getString("desc")
            val deadline = bundle.getString("deadline")
            id = bundle.getString("id")
            Log.v(tag, title)
            Log.v(tag, desc)
            val titleView = findViewById<TextView>(R.id.taskTitle)
            val descView = findViewById<TextView>(R.id.description)
            val deadLineView = findViewById<TextView>(R.id.deadLine)
            titleView.text = title
            descView.text = desc
            deadLineView.text = deadline
            val btnAccept : Button = findViewById(R.id.btnAccept)
            val btnRefuse : Button = findViewById(R.id.btnRefuse)
            btnAccept.setOnClickListener(this)
            btnRefuse.setOnClickListener(this)
        }
    }

    override fun onClick(view: View) {
        var nextStatus = 0
        when (view.id) {
            R.id.btnAccept -> {
                Log.i(tag, "accept")
                nextStatus = 2
            }
            R.id.btnRefuse -> {
                Log.i(tag, "refuse")
                nextStatus = 1
            }
        }
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(MainAppliaction.context))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val formBody = FormBody.Builder()
                        .add("status", nextStatus.toString())
                        .add("taskId", id)
                        .build()
                val request = Request.Builder()
                        .url("http://www.sudowind.com:8000/user/task/modify_status")
                        .post(formBody)
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
                        Log.i(tag, res)
                        if (response.code() == 200) {
                            finish()
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }
}
