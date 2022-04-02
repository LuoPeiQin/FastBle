package com.luo.skg.exdevice

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleGattCallback
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.luo.skg.exdevice.BluetoothTransfer.SendMethod
import com.luo.skg.exdevice.config.ExDeviceEnum
import com.luo.skg.exdevice.helper.BluetoothHelper
import com.luo.skg.exdevice.listener.OnBluetoothStateChangeListener
import com.luo.skg.exdevice.protocol.Protocol

/**
 * 蓝牙总控制
 * Created by LPQ on 2017/11/14.
 */
class BleExDeviceController private constructor(context: Context) {
    private val bleManager = BleManager()

    private val mHelper: BluetoothHelper? = null
    private var mProtocol: Protocol<*, *>? = null
    private val mContext: Context = context.applicationContext

    // 监听手机蓝牙状态
    private val bluetoothStateChangeListenerList = mutableListOf<OnBluetoothStateChangeListener>()

    // 已连接设备列表
    private val connectedDeviceMap = hashMapOf<String, BleDevice>()

    companion object {
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

    val exDeviceList = mutableListOf<ExDevice>()

    /**
     * 设置发送方法
     */
    private fun setSendMethod() {
        BluetoothTransfer.getInstance().send = SendMethod { data -> mHelper!!.send(data) }
    }

    /**
     * 连接设备
     */
    fun connect(address: String) {
        mProtocol = null
        mHelper!!.connect(address)
        bleManager.connect(address, object : BleGattCallback() {
            override fun onStartConnect() {
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException?) {
                connectedDeviceMap.remove(bleDevice.mac)
            }

            override fun onConnectSuccess(
                bleDevice: BleDevice,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                /*设置发送方法*/
                setSendMethod()
                /**启动蓝牙传输 */
                BluetoothTransfer.getInstance().start()
                bleManager.notify(
                    bleDevice,
                    mProtocol!!.serviceUUID.toString(),
                    mProtocol!!.recvTunnelUUID.toString(),
                    object :
                        BleNotifyCallback() {
                        override fun onNotifySuccess() {
                            // 打开通知操作成功
                        }

                        override fun onNotifyFailure(exception: BleException?) {
                            // 打开通知操作失败
                        }

                        override fun onCharacteristicChanged(data: ByteArray) {
                            // 打开通知后，设备发过来的数据将在这里出现
                        }

                    })
                connectedDeviceMap.put(bleDevice.mac, bleDevice)
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {
                connectedDeviceMap.remove(device?.mac)
            }

        })
    }

    fun sendData(data: ByteArray) {
        mHelper?.send(data)
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
     * 系统蓝牙状态监听
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