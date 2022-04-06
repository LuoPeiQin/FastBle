package com.luo.skg.exdevice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.luo.skg.exdevice.config.ExDeviceEnum
import com.luo.skg.exdevice.listener.OnBluetoothStateChangeListener
import com.luo.skg.exdevice.protocol.Protocol

/**
 * 蓝牙总控制
 * Created by LPQ on 2017/11/14.
 */
class BleExDeviceController private constructor(context: Context) {
    private val bleManager = BleManager()

    private val mContext: Context = context.applicationContext

    // 监听手机蓝牙状态
    private val bluetoothStateChangeListenerList = mutableListOf<OnBluetoothStateChangeListener>()

    // 已连接设备列表
    private val connectedDeviceMap = hashMapOf<String, BaseExDevice>()

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: BleExDeviceController? = null
        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: BleExDeviceController(context).also { instance = it }
        }
    }

    init {
        registerBluetoothBroadcast()
    }

    /**
     * 判断手机设备是否支持低功耗蓝牙
     */
    fun isSupportBle() = bleManager.isSupportBle

    /**
     * 判断手机蓝牙是否支持低功耗蓝牙
     */
    fun isBlueEnable() = bleManager.isBlueEnable

    /**
     * 开启手机蓝牙
     */
    fun enableBluetooth() {
        bleManager.enableBluetooth()
    }

    /**
     * 连接设备
     */
    fun connect(address: String, protocol: Protocol<*, *>) {
        bleManager.connect(address, object : BleGattCallback() {
            override fun onStartConnect() {
                Log.i("lpq", "onStartConnect: 开始连接")
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException?) {
                Log.i("lpq", "onConnectFail: 连接失败")
                connectedDeviceMap.remove(bleDevice.mac)
            }

            override fun onConnectSuccess(
                bleDevice: BleDevice,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                Log.i("lpq", "onConnectSuccess: 连接成功")
                val baseExDevice = BaseExDevice(address, bleDevice, protocol)
                connectedDeviceMap[bleDevice.mac] = baseExDevice
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                Log.i("lpq", "onDisConnected: 连接断开")
                val exDevice = connectedDeviceMap.remove(device?.mac)
                exDevice?.destory()
            }

        })
    }

    /**
     * 直接发送数据
     */
    fun sendDataDirect(address: String, data: ByteArray) {
        val exDevice = connectedDeviceMap[address]
        exDevice?.sendDataDirect(data)
    }

    /**
     * 发送数据任务
     */
    fun sendTask(address: String, task: BluetoothTask<*>) {
        val exDevice = connectedDeviceMap[address]
        exDevice?.sendTask(task)
    }

    /**
     * 断开设备
     */
    fun disconnect(macAddress: String) {
        val deviceList = bleManager.multipleBluetoothController.deviceList
        val device = deviceList.find { it.mac == macAddress }
        bleManager.disconnect(device)
    }

    /**
     * 断开所有外部设备
     */
    fun disconnectAllExDevice() {
        val deviceList = bleManager.multipleBluetoothController.deviceList
        deviceList.forEach {
            if (isExDevice(it.name)) {
                bleManager.disconnect(it)
            }
        }
    }

    /**
     * 断开所有设备
     */
    fun disconnectAll() {
        bleManager.disconnectAllDevice()
    }

    /**
     * 添加系统蓝牙状态监听
     */
    fun registerBluetoothStatusChangeListener(listener: OnBluetoothStateChangeListener) {
        if (!bluetoothStateChangeListenerList.contains(listener)) {
            bluetoothStateChangeListenerList.add(listener)
        }
    }

    fun unregisterBluetoothStatusChangeListener(listener: OnBluetoothStateChangeListener) {
        bluetoothStateChangeListenerList.remove(listener)
    }

    /**
     * 监听系统蓝牙开关状态
     */
    private fun registerBluetoothBroadcast() {
        val filter = IntentFilter()
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        mContext.registerReceiver(mReceiver, filter)
    }

    private fun unregisterBluetoothBroadcast() {
        mReceiver?.let {
            mContext.unregisterReceiver(it)
        }
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)
                    when (blueState) {
                        BluetoothAdapter.STATE_ON -> {
                            bluetoothStateChangeListenerList.forEach {
                                it.onBluetoothOpen()
                            }
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            bluetoothStateChangeListenerList.forEach {
                                it.onBluetoothClose()
                            }
                        }
                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * 判断是否外部设备
     */
    fun isExDevice(bleName: String): Boolean {
        ExDeviceEnum.values().forEach {
            if (it.bleName == bleName) {
                return true
            }
        }
        return false
    }


}