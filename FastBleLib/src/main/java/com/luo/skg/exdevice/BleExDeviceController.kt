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
import com.luo.skg.exdevice.helper.BluetoothHelper
import com.luo.skg.exdevice.listener.OnBluetoothConnectStateChangeListener
import com.luo.skg.exdevice.listener.OnBluetoothStateChangeListener
import com.luo.skg.exdevice.listener.OnBluetoothTransmitListener
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

    //当前controller连接状态监听
    private val mCurrentConnectListener: OnBluetoothConnectStateChangeListener? = null

    //传进来的连接状态监听
    var connectStateChangeListener: OnBluetoothConnectStateChangeListener? = null

    //监听手机蓝牙状态
    var bluetoothStateChangeListener: OnBluetoothStateChangeListener? = null

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
    fun connect(address: String, bleGattCallback: BleGattCallback) {
        mProtocol = protocol
        mHelper!!.connect(address)
        bleManager.connect(address, object : BleGattCallback() {
            override fun onStartConnect() {
                bleGattCallback.onStartConnect()
            }

            override fun onConnectFail(bleDevice: BleDevice, exception: BleException?) {
                bleGattCallback.onConnectFail(bleDevice, exception)
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
            }

            override fun onDisConnected(
                isActiveDisConnected: Boolean,
                device: BleDevice?,
                gatt: BluetoothGatt?,
                status: Int
            ) {

            }

        })
    }

    fun registerBluetoothStateChangeListener(listener: OnBluetoothStateChangeListener?) {
        bluetoothStateChangeListener = listener
    }

    fun unregisterBluetoothStateChangeListener() {
        registerBluetoothStateChangeListener(null)
    }

    val transmitListener: OnBluetoothTransmitListener?
        get() = mHelper?.transmitListener

    fun registerTransmitListener(transmitListener: OnBluetoothTransmitListener?) {
//        BleHelper.getInstance(mContext).setTransmitListener(transmitListener);
//        TraditionHelper.getInstance(mContext).setTransmitListener(transmitListener);
        mHelper!!.transmitListener = transmitListener
    }

    fun unregisterTransmitListener() {
        registerTransmitListener(null)
    }

    var protocol: Protocol<*, *>?
        get() = mProtocol
        set(protocol) {
            if (mProtocol != null) {
                mProtocol!!.destroy()
            }
            mProtocol = protocol
            if (mProtocol != null) mProtocol!!.initialize()
            mHelper?.setProtocol(protocol)
            BluetoothTransfer.getInstance().protocol = mProtocol
        }


    fun sendData(data: ByteArray) {
        mHelper?.send(data)
    }

    /**
     * 断开设备
     */
    fun disconnect() {
        mHelper!!.disconnect()
        unregisterBluetoothBroadcast()
        BluetoothTransfer.getInstance().stop()
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
                            bluetoothStateChangeListener?.let {
                                it.onBluetoothOpen()
                            }
                        }
                        BluetoothAdapter.STATE_OFF -> {
                            bluetoothStateChangeListener?.let {
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


}