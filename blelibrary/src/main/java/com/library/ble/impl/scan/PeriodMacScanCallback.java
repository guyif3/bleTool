package com.library.ble.impl.scan;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.library.ble.manager.DeviceManager;

import java.util.concurrent.atomic.AtomicBoolean;


public abstract class PeriodMacScanCallback extends PeriodScanCallback{
    private AtomicBoolean hasFound = new AtomicBoolean(false);
    private String mac;

    public abstract void onDeviceFound(BluetoothDevice bluetoothDevice, int ressi, byte[] bArr);

    public PeriodMacScanCallback(String mac, DeviceManager bleDeviceManager, long timeoutMillis) {
        super(bleDeviceManager,timeoutMillis);
        this.mac = mac;
        if (TextUtils.isEmpty(mac)) {
            throw new IllegalArgumentException("start scan, mac can not be null!");
        }
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (!this.hasFound.get() && this.mac.equalsIgnoreCase(device.getAddress())) {
            this.hasFound.set(true);
            this.bleDeviceManager.stopScan(this);
            onDeviceFound(device, rssi, scanRecord);
        }
    }
}
