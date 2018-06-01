package com.example.wangfeng.csapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class Registration : AppCompatActivity(), View.OnClickListener {

    private val tag = "register"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        val btnSubmit: Button = findViewById(R.id.registerButton)
        btnSubmit.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.registerButton -> {
                val username = findViewById<EditText>(R.id.username).text.toString()
                val password = findViewById<EditText>(R.id.password).text.toString()
                val confirmPassword = findViewById<EditText>(R.id.confirmPassword).text.toString()
                if (username.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                    if (password != confirmPassword) {
                        Toast.makeText(this@Registration,
                                "请确认两次密码输入一致", Toast.LENGTH_SHORT).show()
                    } else {
                        register(username, password)
                    }
                } else {
                    Toast.makeText(this@Registration,
                            "请输入用户名和密码", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun register (p0 : String, p1: String) {
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val formBody = FormBody.Builder()
                        .add("username", p0)
                        .add("password", p1)
                        .build()
                val request = Request.Builder()
                        .url("http://www.sudowind.com:8000/user/create")
                        .post(formBody)
                        .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        println("something wrong")
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val res = response!!.body()!!.string()
                        Log.i(tag, res)
                        if (response.code() == 200) {
                            login(p0, p1)
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun login(p0:String, p1:String) {
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val formBody = FormBody.Builder()
                        .add("username", p0)
                        .add("password", p1)
                        .build()
                val request = Request.Builder()
                        .url("http://www.sudowind.com:8000/user/login")
                        .post(formBody)
                        .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.v("login", "fail")
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        if (response.code() == 200) {
                            val user = getSharedPreferences("user", 0)
                            val editor = user.edit()
                            val result = response.body()!!.string()
                            val obj = JSONObject(result)
                            val name =  obj.getString("name")
                            val id = obj.getInt("id")
                            Log.i(tag,name)
                            Log.i(tag, id.toString())
                            Log.i(tag, result)
                            editor.putString("name", name)
                            editor.putInt("id", id)
                            editor.apply()
                            Log.i("login", result)
                            val intent = Intent(this@Registration, MainActivity::class.java)
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
