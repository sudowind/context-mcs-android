package com.example.wangfeng.csapp

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.*
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.google.gson.JsonObject
import okhttp3.*
import org.json.JSONObject
import org.w3c.dom.Text
import java.io.IOException


class Questionnaire : AppCompatActivity(), View.OnClickListener {

    private var radioGroup : RadioGroup? = null
    private val tag : String = "questionnaire"
    private var uqId : String = ""
    private var option1 = -1
    private var option2 = -1
    private var option3 = -1
    private var option4 = -1
    private var option5 = -1
    private var option6 = -1
    private val option = arrayOf(-1, -1, -1, -1, -1, -1)

    @SuppressLint("SetTextI18n")
    var uiHandler = Handler(Handler.Callback { msg ->

//        Log.i(tag, desc)
        val ids = arrayOf(R.id.q1, R.id.q2, R.id.q3, R.id.q4, R.id.q5, R.id.q6)
        val rgs = arrayOf(R.id.rg1, R.id.rg2, R.id.rg3, R.id.rg4, R.id.rg5, R.id.rg6)
        for (idx in 0..5) {
            val desc = "${idx + 1}. " + msg.data.getString("desc" + idx.toString())
            val descView = findViewById<TextView>(ids[idx])
            radioGroup = findViewById(rgs[idx])
            descView.text = desc
            for (i in 0..4) {
                val radioButton = RadioButton(this@Questionnaire)
                radioButton.text = "${i + 1}"
                //设置radioButton的点击事件
                radioButton.setOnClickListener {
                    option[idx] = i + 1
//                Toast.makeText(this@Questionnaire, "this is radioButton  ${i + 1}", Toast.LENGTH_SHORT).show()
                }
                //将radioButton添加到radioGroup中
                radioGroup?.addView(radioButton)
            }
            radioGroup?.setOnCheckedChangeListener { p0, p1 ->
                val radioBtn = findViewById<RadioButton>(p1)
//            Toast.makeText(this@Questionnaire, radioBtn.text, Toast.LENGTH_SHORT).show()
            }
        }
        false
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questionnaire)
        val bundle = this.intent.extras
        if (bundle != null) {
            val name = bundle.getString("name")
            val id = bundle.getString("id")
            uqId = id
            val deadline = bundle.getString("deadline")
            Log.i(tag, name + id)
            val nameView = findViewById<TextView>(R.id.questionnaireName)
            nameView.text = name
            val ddlView = findViewById<TextView>(R.id.questionnaireDdl)
            ddlView.text = deadline
            loadQuestionnaire(id)
        }
        val submitBtn = findViewById<Button>(R.id.questionSubmitBtn)
        submitBtn.setOnClickListener(this@Questionnaire)
    }

    override fun onClick(p0: View?) {
        when(p0!!.id) {
            R.id.questionSubmitBtn -> {
                submitQuestionnaire()
                Log.i(tag, "submit questionnaire result")
            }
        }
    }

    private fun submitQuestionnaire() {
        var flag = true
        for (i in option) {
            if (i < 0) {
                flag = false
            }
        }
        if (!flag) {
            Toast.makeText(this@Questionnaire, "请完成所有选项", Toast.LENGTH_SHORT).show()
            return
        }
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val formBody = FormBody.Builder()
                        .add("uqId", uqId)
                        .add("option", option.joinToString())
                        .build()
                val request = Request.Builder()
                        .url(Utils().baseUrl + "/user/questionnaire/finish")
                        .post(formBody)
                        .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        println("something wrong")
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val res = response!!.body()!!.string()
                        if (response.code() == 200) {
//                            Log.i(tag, res)
                            finish()
                        }
                    }
                })
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    private fun loadQuestionnaire(id : String) {
        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val request = Request.Builder()
                        .url("http://www.sudowind.com:8000/user/questionnaire/$id")
                        .build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        println("something wrong")
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val res = response!!.body()!!.string()
                        if (response.code() == 200) {
//                            Log.i(tag, res)
                            val obj = JSONObject(res)
                            val msg = Message()
                            val bundle = Bundle()
                            for (i in 0..5) {
                                val question = obj.getJSONArray("questions").getJSONObject(i)
                                Log.i(tag, question.getString("description"))
//                            uiHandler.sendEmptyMessage(0)

                                bundle.putString("desc$i", question.getString("description"))
                            }
                            msg.data = bundle
                            uiHandler.sendMessage(msg)
                        }
                    }
                })
            } catch (e : Exception) {
                e.printStackTrace()
            }
        }).start()
    }
}
