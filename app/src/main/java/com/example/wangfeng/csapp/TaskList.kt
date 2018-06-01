package com.example.wangfeng.csapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import java.io.IOException

import java.util.ArrayList
import java.util.HashMap

class TaskList : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.task_list)

        //要显示的数据
        val strs = arrayOf("任务1", "任务2", "任务3")
        val des = arrayOf("请拍摄一张农园的照片", "拍摄一张未名湖的照片", "请拍摄一张图书馆门前的照片")
        val deadline = arrayOf("2018-04-01 13:00", "2018-04-01 15:00", "2018-04-01 17:00")
        val tasks = ArrayList<HashMap<String, String>>()
        for (i in strs.indices) {
            val item = HashMap<String, String>()
            item["title"] = strs[i]
            item["desc"] = des[i]
            item["deadline"] = "截止时间：" + deadline[i]
            tasks.add(item)
        }
        //创建ArrayAdapter
        val myAdapter = SimpleAdapter(applicationContext, tasks, R.layout.list_item, arrayOf("title", "desc", "deadline"), intArrayOf(R.id.title, R.id.description, R.id.endTime))

        //获取ListView对象，通过调用setAdapter方法为ListView设置Adapter设置适配器
        val itemList = findViewById<View>(R.id.list_test) as ListView
        itemList.adapter = myAdapter
        itemList.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            val map = cast<HashMap<String, String>>(itemList.getItemAtPosition(i))
            val title = map["title"]
            val content = map["desc"]
//            Toast.makeText(applicationContext,
//                    "你选择了第" + i + "个Item\nitemTitle的值是：" + title + "\nitemContent的值是:" + content,
//                    Toast.LENGTH_SHORT).show()

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
//                            runOnUiThread({
//                                Toast.makeText(applicationContext, res, Toast.LENGTH_SHORT).show()
//                            })
                            Log.i("task list", res)
                            if (response.code() == 200) {
                                val intent = Intent(this@TaskList, MainActivity::class.java)

                                /* 通过Bundle对象存储需要传递的数据 */
                                val bundle = Bundle()
                                /*字符、字符串、布尔、字节数组、浮点数等等，都可以传*/
                                bundle.putString("title", title)
                                bundle.putString("desc", content)

                                /*把bundle对象assign给Intent*/
                                intent.putExtras(bundle)

                                startActivity(intent)
                            }
                        }
                    })
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }).start()

        }
    }

    companion object {

        fun <T> cast(obj: Any): T {
            return obj as T
        }
    }

}
