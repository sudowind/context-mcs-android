package com.example.wangfeng.csapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.support.v4.content.ContextCompat
import android.util.Log
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import java.io.IOException
import android.Manifest
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.SensorManager
import android.os.*
import com.example.wangfeng.csapp.receiver.AlarmReceiver
import java.util.*
import android.app.ActivityManager
import android.content.ComponentName
import android.app.ActivityManager.RunningTaskInfo
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ContentValues.TAG
import android.hardware.Sensor
import android.net.ConnectivityManager


class DataLogger : Service() {

    private val tag : String = "DataLogger"
    private var locationManager : LocationManager? = null
    private var sensorManager : SensorManager? = null
    private var powerManager : PowerManager? = null
    private var provider : String = ""
//    private var alarmTime = 10 * 1000
    private var alarmTime = 6 * 10 * 1000
    private val ifilter : IntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    private var batteryStatus : Intent? = null
    private var mediaRecorder: MediaRecorderDemo? = null


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun getNetworkInfo() : Int {
        val netInfo = 0
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo ?: return netInfo
        val nType = networkInfo.type
        when (nType) {
            ConnectivityManager.TYPE_WIFI -> return 1
            ConnectivityManager.TYPE_MOBILE -> return 2
        }
        return netInfo
    }

    private fun getTopApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val m = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            if (m != null) {
                val now = System.currentTimeMillis()
                //获取60秒之内的应用数据
                val stats : List<UsageStats> = m.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 60 * 1000, now)
                Log.i(tag, "Running app number in last 60 seconds : " + stats.size)

                var topActivity = ""

                //取得最近运行的一个app，即当前运行的app，赋值于j
                if ((stats != null) && (!stats.isEmpty())) {
                    var j : UsageStats = stats[0]
                    for (i in stats) {
                        if (i.lastTimeUsed > j.lastTimeUsed) {
                            j = i
                        }
                    }
                    topActivity = j.packageName
                }
                Log.i(tag, "top running app is : $topActivity")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        batteryStatus = applicationContext.registerReceiver(null, ifilter)
        val level = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        var charging = 0
        val netInfo = getNetworkInfo()
        if (isCharging) {
            charging = 1
        }
        Log.i(tag, (level * 1.0 / scale).toString())
        Log.i(tag, (isCharging).toString())
        Log.i(tag, (netInfo).toString())

        getTopApp()

        Thread(Runnable {
            Looper.prepare()
            mediaRecorder = MediaRecorderDemo()
            mediaRecorder?.updateMicStatus()
            mediaRecorder?.startRecord()
            Looper.loop()
        }).start()
//        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
//        val runningTaskInfos = activityManager.getRunningTasks(30)
//        // 判断集合不为空且 size>0
//        if (runningTaskInfos != null && runningTaskInfos.size > 0) {
//            // 集合中的第一个正是前台运行的应用程序
//            val info = runningTaskInfos[0]
//            // 获得正在前台运行的应用
//            val componentName = info.topActivity
//            // 返回正在运行的应用名
//            Log.i(tag, "app name " + componentName.className)
//        }

        Thread(Runnable {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val list : List<String> = locationManager!!.getProviders(true)
            when {
                list.contains(LocationManager.GPS_PROVIDER) -> provider = LocationManager.GPS_PROVIDER
                list.contains(LocationManager.NETWORK_PROVIDER) -> provider = LocationManager.NETWORK_PROVIDER
                else -> Log.i(tag, "请打开GPS或网络")
            }
            if (provider !== "") {

            }
            if (ContextCompat.checkSelfPermission(this@DataLogger, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                var location : Location? = null
                for (p in list) {
                    val l = locationManager!!.getLastKnownLocation(p)
                    if (l == null) {
                        continue
                    } else {
                        location = l
                        break
                    }
                }

//                val location : Location = locationManager!!.getLastKnownLocation(provider)
                val currentTime = Date()
                Log.i(tag, "纬度为${location!!.latitude}，经度为${location.longitude}，时间 ${currentTime.time}")

                try {
                    val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
                    val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                    val formBody = FormBody.Builder()
                            .add("longitude", "${location.longitude}")
                            .add("latitude", "${location.latitude}")
                            .add("level", "${level * 1.0 / scale}")
                            .add("charging", "$charging")
                            .add("netInfo", "$netInfo")
                            .build()
                    val request = Request.Builder()
                            .url(Utils().baseUrl + "/task/periodic_record")
                            .post(formBody)
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

            }

        }).start()
        val manager : AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAtTime = SystemClock.elapsedRealtime() + alarmTime
        val i = Intent(this@DataLogger, AlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(this@DataLogger, 0, i, 0)
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi)
//        return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        val service = Intent(this, DataLogger::class.java)
        this.startService(service)
        Log.i(tag, "service restart")
        super.onDestroy()
    }
}
