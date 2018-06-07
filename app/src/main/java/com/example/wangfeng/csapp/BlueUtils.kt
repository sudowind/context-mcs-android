package com.example.wangfeng.csapp

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import java.util.ArrayList


/**
 * 蓝牙扫描
 * Created by huitao on 2017/12/27.
 */

class BlueUtils {
    //蓝牙适配器
    private var mBluetoothAdapter: BluetoothAdapter? = null
    //搜索状态的标示
    private var mScanning = true
    //蓝牙适配器List
    private var mBlueList: MutableList<BluetoothDevice>? = null
    //上下文
    private var context: Context? = null
    //蓝牙的回调地址
    private var mLesanCall: BluetoothAdapter.LeScanCallback? = null
    //扫描执行回调
    private var callback: Callbacks? = null

    /**
     * 判断是否支持蓝牙
     * @return
     */
    val isSupportBlue: Boolean
        get() = !context!!.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    /**
     * 返回蓝牙对象
     * @return
     */
    fun getmBluetoothAdapter(): BluetoothAdapter? {
        return mBluetoothAdapter
    }


    /***
     * 初始化蓝牙的一些信息
     */
    fun getInitialization(context: Context) {
        this.context = context
        //初始化蓝牙适配器
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        //初始化蓝牙
        mBluetoothAdapter = bluetoothManager.adapter
        //初始化List
        mBlueList = ArrayList()
        //实例化蓝牙回调
        mLesanCall = BluetoothAdapter.LeScanCallback { bluetoothDevice, i, bytes ->
            //返回三个对象 分类别是 蓝牙对象 蓝牙信号强度 以及蓝牙的广播包
            if (!mBlueList!!.contains(bluetoothDevice)) {//重复的则不添加
                mBlueList!!.add(bluetoothDevice)
                //接口回调
                callback!!.CallbackList(mBlueList)
            }
        }
    }

    /**
     * 开启蓝牙
     */
    fun startBlue() {
        if (mScanning) {
            mScanning = false
            //开始扫描并设置回调
            mBluetoothAdapter!!.startLeScan(mLesanCall)
        }
    }

    /**
     * 停止蓝牙扫描
     */
    fun stopBlue() {
        if (!mScanning) {
            //结束蓝牙扫描
            mBluetoothAdapter!!.stopLeScan(mLesanCall)
            Log.i("blue tooth", mBlueList!!.size.toString())
        }
    }

    /**
     * 接口回调
     */
    public interface Callbacks {
        fun CallbackList(mBlueLis: List<BluetoothDevice>?)
    }

    /**
     * 设置接口回调
     * @param callback 自身
     */
    fun setCallback(callback: Callbacks) {
        this.callback = callback
    }

    companion object {
//        //单例模式
//        private var blueUtils: BlueUtils? = null
//
//        //单例模式
//        fun getBlueUtils(): BlueUtils {
//            if (blueUtils == null) {
//                blueUtils = BlueUtils()
//            }
//            return blueUtils
//        }
    }
}
