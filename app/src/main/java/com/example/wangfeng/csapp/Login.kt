package com.example.wangfeng.csapp

import android.content.Intent
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
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
import org.json.JSONObject

/**
 * Created by wangfeng on 2018/3/21.
 */

class Login : AppCompatActivity(), View.OnClickListener {

    private val tag = "Login"

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val loginBtn = findViewById<Button>(R.id.logInBtn)
        val registerBtn = findViewById<Button>(R.id.logOnBtn)
        loginBtn.setOnClickListener(this)
        registerBtn.setOnClickListener(this)
        checkLoginStatus()
    }

    private fun checkLoginStatus () {
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val request = Request.Builder()
                        .url("http://www.sudowind.com:8000/user/hello")
                        .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        println("something wrong")
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val res = response!!.body()!!.string()
                        if (response.code() == 200) {
                            val intent = Intent(this@Login, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.logInBtn -> {
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
                                .url(Utils().baseUrl + "/user/login")
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
            R.id.logOnBtn -> {
                val intent = Intent(this@Login, Registration::class.java)
                startActivity(intent)
            }
        }
    }

}
