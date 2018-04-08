package com.example.wangfeng.csapp

import android.content.Intent
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor

import java.io.IOException

import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * Created by wangfeng on 2018/3/21.
 */

class Login : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
    }

    @Throws(Exception::class)
    fun onClick(v: View) {
        // check user name and password
        val userName = findViewById<EditText>(R.id.userName)
        val passWord = findViewById<EditText>(R.id.passWord)
        Log.i("login", userName.text.toString() + " " + passWord.text.toString())
        if (userName.text.toString().isEmpty() || passWord.text.toString().isEmpty()) {
            Toast.makeText(applicationContext, "请输入用户名和密码", Toast.LENGTH_SHORT).show()
            return
        }
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val formBody = FormBody.Builder()
                        .add("username", userName.text.toString())
                        .add("password", passWord.text.toString())
                        .build()
                val request = Request.Builder()
                        .url("http://192.168.255.14:8000/user/login")
                        .post(formBody)
                        .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.v("login", "fail")
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.code() == 200) {
                            val result = response.body()!!.string()
                            Log.i("login", result)
                            val intent = Intent(this@Login, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Looper.prepare()
                            Toast.makeText(applicationContext,
                                    "请输入正确的用户名和密码",
                                    Toast.LENGTH_SHORT).show()
                            Looper.loop()
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

}
