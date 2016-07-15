package com.library.ble.conn;

import android.bluetooth.BluetoothGattCharacteristic;

public abstract class BleCharactCallback extends BleCallback {
    public abstract void onSuccess(BluetoothGattCharacteristic bluetoothGattCharacteristic);
}
