/**
 * Copyright (C), 2007-2022, 未来穿戴有限公司
 * FileName: ExDevice
 * Author: lpq
 * Date: 2022/4/1 17:38
 * Description: 用一句话描述下
 */
package com.luo.skg.exdevice

import android.util.Log
import com.clj.fastble.BleManager
import com.clj.fastble.callback.BleNotifyCallback
import com.clj.fastble.data.BleDevice
import com.clj.fastble.exception.BleException
import com.clj.fastble.utils.HexUtil
import com.luo.skg.exdevice.protocol.Protocol

/**
 *
 * @ProjectName: FastBle
 * @Package: com.luo.skg.exdevice
 * @ClassName: ExDevice
 * @Description: 用一句话描述下
 * @Author: lpq
 * @CreateDate: 2022/4/1 17:38
 */
class BaseExDevice(
    // 当前设备地址
    val macAddress: String,
    // 连接设备
    val bleDevice: BleDevice,
    // 协议回调
    val protocol: Protocol<*, *>
) {
    private val bleManager = BleManager()

    val bluetoothTransfer by lazy { BluetoothTransfer(protocol) }

    // 开始运行后台任务
    init {
        bluetoothTransfer.setSend {
            bleManager.write(
                bleDevice,
                protocol.serviceUUIDString,
                protocol.sendTunnelUUIDString,
                it,
                null
            )
        }
        bluetoothTransfer.start()
        startRecvData()
    }

    /**
     * 开始接收数据
     */
    fun startRecvData() {
        bleManager.notify(
            bleDevice,
            protocol.serviceUUIDString,
            protocol.recvTunnelUUIDString,
            object :
                BleNotifyCallback() {
                override fun onNotifySuccess() {
                    // 打开通知操作成功
                    Log.i("lpq", "onNotifySuccess: 打开通知操作成功")
                }

                override fun onNotifyFailure(exception: BleException?) {
                    // 打开通知操作失败
                    Log.i("lpq", "onNotifyFailure: 打开通知操作失败: ${exception?.description}")
                }

                override fun onCharacteristicChanged(data: ByteArray) {
                    // 打开通知后，设备发过来的数据将在这里出现
                    Log.i(
                        "lpq",
                        "${bleDevice.name}-${bleDevice.mac} 接收到数据: ${HexUtil.formatHexString(data)}"
                    )
                    bluetoothTransfer.addRecvData(data)
                }

            })
    }

    /**
     * 发送数据
     */
    fun sendDataDirect(data: ByteArray) {
        bleDevice?.let {
            bleManager.write(
                it,
                protocol.serviceUUIDString,
                protocol.sendTunnelUUIDString,
                data,
                null
            )
        }
    }

    /**
     * 异步发送 -- 以任务形式发送
     */
    fun sendTask(task: BluetoothTask<*>) {
        bluetoothTransfer.addSendTask(task)
    }

    /**
     * 销毁
     */
    fun destory() {
        bluetoothTransfer.stop()
        protocol.destroy()
    }

}