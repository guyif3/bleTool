package com.library.ble.conn;

import android.bluetooth.BluetoothGattCallback;

import com.library.ble.exception.BleException;


public abstract class BleCallback {
    private BluetoothGattCallback bluetoothGattCallback;

    public BleCallback setBluetoothGattCallback(BluetoothGattCallback bluetoothGattCallback) {
        this.bluetoothGattCallback = bluetoothGattCallback;
        return this;
    }

    public BluetoothGattCallback getBluetoothGattCallback() {
        return this.bluetoothGattCallback;
    }


    /**
     * 发送成功
     */
    public  void onInitiatedSuccess(){

    }

    /**
     * 发送失败 当数据操作中出现异常的时候
     * @param bleException
     */
    public abstract void onFailure(BleException bleException);

}
