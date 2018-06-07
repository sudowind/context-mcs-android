package com.example.wangfeng.csapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import com.example.wangfeng.csapp.dummy.QuestionContent
import com.example.wangfeng.csapp.receiver.BluetoothReceiver
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
import java.util.*

class MainActivity : AppCompatActivity(), SensorEventListener, BlankFragment.OnFragmentInteractionListener, ItemFragment.OnListFragmentInteractionListener, QuestionFragment.OnListFragmentInteractionListener {

    private var content: FrameLayout? = null
    private var sm: SensorManager? = null
    private var accSensor: Sensor? = null
    var ax = 0.0
    var ay = 0.0
    var az = 0.0
    var ox = 0.0
    var oy = 0.0
    var oz = 0.0
    var axp = 0.0
    var ayp = 0.0
    var g = 10.0
    var gp = 0.0
    var simpleRate: SimpleRate? = null
    var tmp1 = 0.0
    var tmp2 = 0.0
    var tmp3 = 0.0
    var tmp4 = 0.0
    var lastTime = 0L

    private val MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1101

    fun getAccelerationX(): Double {
        return axp
    }

    fun getAccelerationY(): Double {
        return ayp
    }

    fun getOrientation(): Double {
        return Math.asin(tmp4) / Math.PI * 180.0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS) {
            if (!hasPermission()) {
                //若用户未开启权限，则引导用户开启“Apps with usage access”权限
                startActivityForResult(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        init()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun init() {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
        } else if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 3)
        } else {
            Log.i("main", "created")

            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_SCREEN_ON)
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
            intentFilter.addAction(Intent.ACTION_USER_PRESENT)
            applicationContext.registerReceiver(ScreenBroadcastReceiver(), intentFilter)
            var bluetoothFilter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            applicationContext.registerReceiver(BluetoothReceiver(), bluetoothFilter)
            bluetoothFilter = IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            applicationContext.registerReceiver(BluetoothReceiver(), bluetoothFilter)
            simpleRate = SimpleRate()
            sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            accSensor = sm!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sm!!.registerListener(this@MainActivity, accSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    //检测用户是否对本app开启了“Apps with usage access”权限
    private fun hasPermission() : Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        var mode = 0
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

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
            R.id.navigation_questionnaire -> {
                val fragment = QuestionFragment.Companion.newInstance(1)
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
        checkLoginStatus()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!hasPermission()) {
                startActivityForResult(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                        MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS)
            }
        }

        init()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        content = findViewById<FrameLayout>(R.id.content)
        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        val fragment = ItemFragment.Companion.newInstance(1)
        addFragment(fragment)
        val intent = Intent(this@MainActivity, DataLogger::class.java)
        startService(intent)
    }


    override fun onSensorChanged(event : SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val now = Date()
            if (now.time - lastTime > 60 * 1000) {
                ax = event.values[0].toDouble()
                ay = event.values[1].toDouble()
                az = event.values[2].toDouble()
                Log.i("main", "$ax $ay $az")
                lastTime = now.time
                Thread(Runnable {
                    try {
                        val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
                        val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                        val formBody = FormBody.Builder()
                                .add("ax", "$ax")
                                .add("ay", "$ay")
                                .add("az", "$az")
                                .build()
                        val request = Request.Builder()
                                .url(Utils().baseUrl + "/task/moving_record")
                                .post(formBody)
                                .build()
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call?, e: IOException?) {
                                println("something wrong")
                            }

                            override fun onResponse(call: Call?, response: Response?) {
                                val res = response!!.body()!!.string()

                                if (response.code() == 200) {
                                    Log.i("send acc data", res)
                                }
                            }
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }).start()
            }
        }
//        synchronized(this) {
//            if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
//                oy = event.values[1].toDouble()
//                oz = event.values[2].toDouble()
//            }
//            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//                ax = event.values[0].toDouble()
//                ay = event.values[1].toDouble()
//                az = event.values[2].toDouble()
//                Log.i("main", "$ax $ay $az")
//            }
//            tmp1 = Math.sin(oz * Math.PI / 180.0)
//            tmp2 = Math.sin(Math.abs(oy) * Math.PI / 180.0)
//            tmp3 = Math.sqrt(tmp1 * tmp1 + tmp2 * tmp2)
//            tmp4 = tmp1 / tmp3
//
//            gp = 10 * tmp3
//            axp = ax * Math.cos(tmp4) + ay * Math.sin(tmp4)
//            ayp = -ax * Math.sin(tmp4) + ay * Math.cos(tmp4) + gp
//            Log.i("main", "$axp $ayp")

//            Log.i("acc", "$oy $oz $ax $ay")
//            acx.setText("a X: " + getAccelerationX())
//            acy.setText("a Y: " + getAccelerationY())
//            o.setText("Orientation : " + getOrientation())
            // mDataListX.add(getAccelerationX() + "\n");
            // mDataListY.add(getAccelerationY() + "\n");
            // mDataListZ.add(getOrientation() + "\n");
//        }
    }

    override fun onAccuracyChanged(p0: Sensor, p1: Int) {
    }

    private fun checkLoginStatus() {
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

                        } else {
                            val intent = Intent(this@MainActivity, Login::class.java)
                            startActivity(intent)
                        }
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onListFragmentInteraction(item: QuestionContent.QuestionItem) {
        Log.i("main", item.id + item.name)
        val id : String = item.uqId
        val name: String = item.name
        val deadline : String = item.deadline
        val bundle = Bundle()
        /*字符、字符串、布尔、字节数组、浮点数等等，都可以传*/
        bundle.putString("name", name)
        bundle.putString("deadline", deadline)
        bundle.putString("id", id)
        val intent = Intent(this@MainActivity, Questionnaire::class.java)
        intent.putExtras(bundle)
        startActivity(intent)
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
                        .url("http://www.sudowind.com:8000/user/task/$id")
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
                            bundle.putString("id", id)

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

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
//        sm!!.unregisterListener(this@MainActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
        sm!!.unregisterListener(this@MainActivity)
    }

}
