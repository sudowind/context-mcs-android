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

class DataLogger : Service() {

    private val Tag : String = "DataLogger"
    private var locationManager : LocationManager? = null
    private var sensorManager : SensorManager? = null
    private var powerManager : PowerManager? = null
    private var provider : String = ""
    private var alarmTime = 5 * 1000
    private val ifilter : IntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    private var batteryStatus : Intent? = null


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        batteryStatus = applicationContext.registerReceiver(null, ifilter)
        val level = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = batteryStatus!!.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        Log.i(Tag, (level / scale).toString())
        Thread(Runnable {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val list : List<String> = locationManager!!.getProviders(true)
            when {
                list.contains(LocationManager.GPS_PROVIDER) -> provider = LocationManager.GPS_PROVIDER
                list.contains(LocationManager.NETWORK_PROVIDER) -> provider = LocationManager.NETWORK_PROVIDER
                else -> Log.i(Tag, "请打开GPS或网络")
            }
            if (ContextCompat.checkSelfPermission(this@DataLogger, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                val location : Location = locationManager!!.getLastKnownLocation(provider)
                val currentTime = Date()
                Log.i(Tag, "纬度为${location.latitude}，经度为${location.longitude}，时间 ${currentTime.time}")

                try {
                    val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(applicationContext))
                    val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                    val request = Request.Builder()
                            .url("http://192.168.255.14:8000/user/hello")
                            .build()
                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            println("something wrong")
                        }

                        override fun onResponse(call: Call?, response: Response?) {
                            val res = response!!.body()!!.string()

                            if (response.code() == 200) {
                                Log.i(Tag, res)
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
        return super.onStartCommand(intent, flags, startId)
    }
}
