package com.luo.skg.exdevice.protocol;

import com.luo.skg.exdevice.BluetoothDispatch;
import com.luo.skg.exdevice.packet.Packet;

/**
 * 蓝牙收到的数据解析结果
 */

public final class ParseResult {

    private ResultType type;

    private Packet packet;

    private BluetoothDispatch.Callback callback;    //主动事件才不为null

    public ParseResult() {

    }

    public ParseResult(ResultType type, Packet packet, BluetoothDispatch.Callback callback) {
        this.type = type;
        this.packet = packet;
        this.callback = callback;
    }

    public ResultType getType() {
        return type;
    }

    public void setType(ResultType type) {
        this.type = type;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public BluetoothDispatch.Callback getCallback() {
        return callback;
    }

    public void setCallback(BluetoothDispatch.Callback callback) {
        this.callback = callback;
    }
}
