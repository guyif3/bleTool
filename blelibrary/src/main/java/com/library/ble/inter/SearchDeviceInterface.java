package com.library.ble.inter;

import android.bluetooth.BluetoothDevice;

/**
 * 扫描到设备的信息
 * Created by gongjianghua on 16/7/6.
 */
public interface SearchDeviceInterface {

    void onScanResults(BluetoothDevice device, int rssi, byte[] scanRecord, long scan_time);

    void onScanTimeout();
}
