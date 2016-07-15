package com.library.ble.impl.callback;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;


import com.library.ble.exception.BleException;


public  abstract class BleGattCallback extends BluetoothGattCallback {

    /**
     * 连接失败
     * @param bleException
     */
    public abstract  void onConnectFailure(BleException bleException);

    /**
     * 连接成功
     * @param bluetoothGatt
     */
    public abstract  void onConnectSuccess(BluetoothGatt bluetoothGatt);

    /**
     *  扫描通道的结果
     * @param bluetoothGatt 操作对象
     * @param isDiscovered  是否扫描成功 true 成功  false 失败
     */
    public abstract void onServicesDiscovered(BluetoothGatt bluetoothGatt, boolean  isDiscovered);


}
