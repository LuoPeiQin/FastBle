package com.luo.skg.exdevice.protocol;

import android.content.Context;

import com.luo.skg.exdevice.packet.Packet;

import java.util.UUID;

/**
 * Created by Administrator on 2016/11/14.
 */

public abstract class Protocol<E extends Packet, T extends OnEventListener> {
    public final static int BLE_MAX_SEND_INTERVAL = 500;
    protected Context mContext;

    private T mEventListener;

    private Object mData;

    private int mMaxBleSendInterval = BLE_MAX_SEND_INTERVAL;

    /**
     * 协议所特有的主动事件监听
     */
    protected Protocol(Context context) {
        this(context, null);
    }

    /**
     * 协议所特有的主动事件监听
     */
    protected Protocol(Context context, T listener) {
        mContext = context.getApplicationContext();
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
        mContext = null;
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

    public UUID getServiceUUID() {
        return UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    }

    public UUID getSendTunnelUUID() {
        return UUID.fromString("0000fff6-0000-1000-8000-00805f9b34fb");
    }

    public UUID getRecvTunnelUUID() {
        return UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb");
    }

    public UUID getDescriptorUUID() {
        return UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
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
