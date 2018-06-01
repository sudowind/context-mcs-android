package com.example.wangfeng.csapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import org.json.JSONException
import org.json.JSONObject
import android.text.TextUtils
import cn.jpush.android.api.JPushInterface
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.app.NotificationCompat
import android.util.Log
import com.example.wangfeng.csapp.MainActivity
import com.example.wangfeng.csapp.R


class JPushReceiver : BroadcastReceiver() {

    private val tag = "JPush"

    override fun onReceive(context: Context, intent: Intent) {
        val bundle = intent.extras

        when {
            JPushInterface.ACTION_REGISTRATION_ID == intent.action -> {
                val regId = bundle!!.getString(JPushInterface.EXTRA_REGISTRATION_ID)
                Log.d(tag, "[MyReceiver] 接收Registration Id : " + regId!!)
                //send the Registration Id to your server...

            }
            JPushInterface.ACTION_MESSAGE_RECEIVED == intent.action -> {
                Log.d(tag, "[MyReceiver] 接收到推送下来的自定义消息: " + bundle!!.getString(JPushInterface.EXTRA_MESSAGE)!!)
                processCustomMessage(context, bundle)

            }
            JPushInterface.ACTION_NOTIFICATION_RECEIVED == intent.action -> {
                Log.d(tag, "[MyReceiver] 接收到推送下来的通知")
                val notifactionId = bundle!!.getInt(JPushInterface.EXTRA_NOTIFICATION_ID)
                Log.d(tag, "[MyReceiver] 接收到推送下来的通知的ID: $notifactionId")

            }
            JPushInterface.ACTION_NOTIFICATION_OPENED == intent.action -> {
                Log.d(tag, "[MyReceiver] 用户点击打开了通知")

                //打开自定义的Activity
                val i = Intent(context, MainActivity::class.java)
                i.putExtras(bundle!!)
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                context.startActivity(i)

            }
            JPushInterface.ACTION_RICHPUSH_CALLBACK == intent.action -> Log.d(tag, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle!!.getString(JPushInterface.EXTRA_EXTRA)!!)
        //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
            JPushInterface.ACTION_CONNECTION_CHANGE == intent.action -> {
                val connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false)
                Log.w(tag, "[MyReceiver]" + intent.action + " connected state change to " + connected)
            }
            else -> Log.d(tag, "[MyReceiver] Unhandled intent - " + intent.action!!)
        }
    }


    /**
     * 实现自定义推送声音
     * @param context
     * @param bundle
     */

    private fun processCustomMessage(context: Context, bundle: Bundle) {

        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager


        val notification = NotificationCompat.Builder(context)

        notification.setAutoCancel(true)
                .setContentText("自定义推送声音")
                .setContentTitle("极光测试")
                .setSmallIcon(R.mipmap.ic_launcher)

        val message = bundle.getString(JPushInterface.EXTRA_MESSAGE)
        val extras = bundle.getString(JPushInterface.EXTRA_EXTRA)
        if (!TextUtils.isEmpty(extras)) {
            try {
                val extraJson = JSONObject(extras)
                if (extraJson.length() > 0) {
                    val sound = extraJson.getString("sound")
                }
            } catch (e: JSONException) {

            }

        }
        val mIntent = Intent(context, MainActivity::class.java)

        mIntent.putExtras(bundle)
        val pendingIntent = PendingIntent.getActivity(context, 0, mIntent, 0)

        notification.setContentIntent(pendingIntent)

        notificationManager.notify(2, notification.build())
    }
}
