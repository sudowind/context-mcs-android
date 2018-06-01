package com.example.wangfeng.csapp

import android.media.MediaRecorder
import android.os.Environment
import android.os.Handler
import android.util.Log
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.*
import java.io.File
import java.io.IOException

/**
 * amr音频处理
 *
 * @author hongfa.yy
 * @version 创建时间2012-11-21 下午4:33:28
 */
class MediaRecorderDemo {
    private val TAG = "MediaRecord"
    private var mMediaRecorder: MediaRecorder? = null
    private var filePath: String? = null

    private var startTime: Long = 0
    private var endTime: Long = 0
    private var soundFile: File? = null

    private val mHandler = Handler()
    private val mUpdateMicStatusTimer = Runnable { updateMicStatus() }
    private val mStopRecord = Runnable { stopRecord() }

    /**
     * 更新话筒状态
     *
     */
    private val BASE = 1
    private val SPACE = 5 * 1000// 间隔取样时间5s

    constructor() {
        this.filePath = "/dev/null"
    }

    constructor(file: File) {
        this.filePath = file.absolutePath
    }

    /**
     * 开始录音 使用amr格式
     *
     * 录音文件
     * @return
     */
    fun startRecord() {
        // 开始录音
        /* ①Initial：实例化MediaRecorder对象 */
        if (mMediaRecorder == null)
            mMediaRecorder = MediaRecorder()
        try {
            /* ②setAudioSource/setVedioSource */
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)// 设置麦克风
            /* ②设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default 声音的（波形）的采样 */
            mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
            /*
             * ②设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default THREE_GPP(3gp格式
             * ，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
             */
            mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            /* ③准备 */
            soundFile = File(Environment.getExternalStorageDirectory().canonicalFile.toString() + "/sound.amr")
            mMediaRecorder!!.setOutputFile(soundFile!!.absolutePath)
            mMediaRecorder!!.setMaxDuration(MAX_LENGTH)
            mMediaRecorder!!.prepare()
            /* ④开始 */
            mMediaRecorder!!.start()
            // AudioRecord audioRecord.
            /* 获取开始时间* */
            startTime = System.currentTimeMillis()
            mHandler.postDelayed(mStopRecord, (10 * 1000).toLong())     //30s后自动停止录音，并等待一个新的Service周期开始

            Log.i("ACTION_START", "startTime$startTime")
        } catch (e: IllegalStateException) {
            Log.i(TAG,
                    "Illegal call startAmr(File mRecAudioFile) failed!" + e.message)
            e.printStackTrace()
        } catch (e: IOException) {
            Log.i(TAG,
                    "call startAmr(File mRecAudioFile) failed!" + e.message)
            e.printStackTrace()
        }

    }

    /**
     * 停止录音
     *
     */
    fun stopRecord(): Long {
        if (mMediaRecorder == null)
            return 0L
        endTime = System.currentTimeMillis()
        Log.i("ACTION_END", "endTime$endTime")
        mMediaRecorder!!.stop()
        mMediaRecorder!!.reset()
        mMediaRecorder!!.release()
        mMediaRecorder = null
        Log.i("ACTION_LENGTH", "Time" + (endTime - startTime))
        return endTime - startTime
    }

    fun updateMicStatus() {
        if (mMediaRecorder != null) {
            val ratio = mMediaRecorder!!.maxAmplitude.toDouble() / BASE
            var db = 0.0// 分贝
            if (ratio > 1)
                db = 20 * Math.log10(ratio)
            if (db != 0.0) {
                Thread(Runnable {
                    try {
                        val cookieJar: ClearableCookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(MainAppliaction.context))
                        val client = OkHttpClient.Builder().cookieJar(cookieJar).build()
                        val formBody = FormBody.Builder()
                                .add("score", "$db")
                                .build()
                        val request = Request.Builder()
                                .url(Utils().baseUrl + "/task/voice_record")
                                .post(formBody)
                                .build()
                        client.newCall(request).enqueue(object : Callback {
                            override fun onFailure(call: Call?, e: IOException?) {
                                println("something wrong")
                            }

                            override fun onResponse(call: Call?, response: Response?) {
                                val res = response!!.body()!!.string()

                                if (response.code() == 200) {
                                    Log.i(TAG, res)
                                }
                            }
                        })
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }).start()
            }
            Log.i(TAG, "分贝值：$db")
        }
        mHandler.postDelayed(mUpdateMicStatusTimer, SPACE.toLong())
    }

    companion object {
        val MAX_LENGTH = 1000 * 30// 最大录音时长30秒;
    }
}
