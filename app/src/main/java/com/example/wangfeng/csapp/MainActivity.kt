package com.example.wangfeng.csapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.ServiceCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import com.example.wangfeng.csapp.dummy.DummyContent
import com.example.wangfeng.csapp.receiver.ScreenBroadcastReceiver
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_item.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.HashMap

class MainActivity : AppCompatActivity(), BlankFragment.OnFragmentInteractionListener, ItemFragment.OnListFragmentInteractionListener {

    private var content: FrameLayout? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
//                Log.i("main", "list")
                val fragment = ItemFragment.Companion.newInstance(1)
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
//                Log.i("main", "home")
                val fragment = BlankFragment.Companion.newInstance("test1", "test2")
                addFragment(fragment)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    @SuppressLint("PrivateResource")
    private fun addFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out)
                .replace(R.id.content, fragment, fragment.javaClass.simpleName)
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
        Log.i("main", "created")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        content = findViewById<FrameLayout>(R.id.content)
        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        val fragment = ItemFragment.Companion.newInstance(1)
        addFragment(fragment)
        val intent = Intent(this@MainActivity, DataLogger::class.java)
        startService(intent)

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_SCREEN_ON)
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        intentFilter.addAction(Intent.ACTION_USER_PRESENT)
        applicationContext.registerReceiver(ScreenBroadcastReceiver(), intentFilter)
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onListFragmentInteraction(item: DummyContent.DummyItem) {
        Log.i("main", item.id + item.title + item.details)
        val id : String = item.taskId
        val title : String = item.title
        val content : String = item.details
        val deadline : String = item.deadline
//            Toast.makeText(applicationContext,
//                    "你选择了第" + i + "个Item\nitemTitle的值是：" + title + "\nitemContent的值是:" + content,
//                    Toast.LENGTH_SHORT).show()

        Thread(Runnable {
            try {
                val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
                val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                val request = Request.Builder()
                        .url("http://192.168.255.14:8000/user/task/$id")
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
                        if (response.code() == 200) {
                            val status = JSONObject(res)["status"] as Int
                            Log.i("task status", status.toString())
                            var intent : Intent? = null
                            when (status) {
                                0 -> {
                                    intent = Intent(this@MainActivity, TaskUnaccepted::class.java)
                                }
                                2 -> {
                                    intent = Intent(this@MainActivity, TaskDetail::class.java)
                                }
                            }

                            /* 通过Bundle对象存储需要传递的数据 */
                            val bundle = Bundle()
                            /*字符、字符串、布尔、字节数组、浮点数等等，都可以传*/
                            bundle.putString("title", title)
                            bundle.putString("desc", content)
                            bundle.putString("deadline", deadline)

                            /*把bundle对象assign给Intent*/
                            intent?.putExtras(bundle)

                            startActivity(intent)
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    companion object {

        fun <T> cast(obj: Any): T {
            return obj as T
        }
    }

}
