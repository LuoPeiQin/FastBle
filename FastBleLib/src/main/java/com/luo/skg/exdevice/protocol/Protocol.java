package com.luo.skg.exdevice.protocol;

import com.luo.skg.exdevice.packet.Packet;

import java.util.UUID;

/**
 * Created by Administrator on 2016/11/14.
 */

public abstract class Protocol<E extends Packet, T extends OnEventListener> {
    public final static int BLE_MAX_SEND_INTERVAL = 500;

    private T mEventListener;

    private Object mData;

    private int mMaxBleSendInterval = BLE_MAX_SEND_INTERVAL;

    /**
     * 协议所特有的主动事件监听
     */
    protected Protocol(T listener) {
        mEventListener = listener;
    }

    /**
     * 初始化
     */
    public void initialize() {

    }

    /**
     * 销毁
     */
    public void destroy() {
        mEventListener = null;
    }

    /**
     * 发送包处理成最终要发送的字节数据
     */
    public abstract byte[] packetToBytes(E packet);

    /**
     * 解析收到的字节处理成结果
     */
    public abstract ParseResult parse(byte[] data);

    /**
     * 获取协议类型
     */
    public abstract int getType();

    /**
     * 是否设置了主动事件监听
     */
    protected boolean haveSetEventListener() {
        return mEventListener != null;
    }

    public String getServiceUUIDString() {
        return "0000fff0-0000-1000-8000-00805f9b34fb";
    }

    public String getSendTunnelUUIDString() {
        return "0000fff6-0000-1000-8000-00805f9b34fb";
    }

    public String getRecvTunnelUUIDString() {
        return "0000fff7-0000-1000-8000-00805f9b34fb";
    }

    public String getDescriptorUUIDString() {
        return "00002902-0000-1000-8000-00805f9b34fb";
    }

    protected UUID getServiceUUID() {
        return UUID.fromString(getServiceUUIDString());
    }

    protected UUID getSendTunnelUUID() {
        return UUID.fromString(getSendTunnelUUIDString());
    }

    protected UUID getRecvTunnelUUID() {
        return UUID.fromString(getRecvTunnelUUIDString());
    }

    protected UUID getDescriptorUUID() {
        return UUID.fromString(getDescriptorUUIDString());
    }

    /**
     * 建议最大发送间隔
     */
    public void setMaxBleSendInterval(int interval) {
        mMaxBleSendInterval = interval;
    }

    /**
     * 有的协议对应目标硬件接收间隔有限制，例如生久
     */
    public int getMaxBleSendInterval() {
        return mMaxBleSendInterval;
    }

    public T getEventListener() {
        return mEventListener;
    }

    public void setEventListener(T eventListener) {
        this.mEventListener = eventListener;
    }

    public Object getData() {
        return mData;
    }

    public void setData(Object data) {
        this.mData = data;
    }
}
