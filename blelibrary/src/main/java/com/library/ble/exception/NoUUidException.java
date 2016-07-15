package com.library.ble.exception;

public class NoUUidException extends BleException {
    public NoUUidException() {
        super(BleException.GATT_UUID_ERROR, "Initiated Exception Occurred! ");
    }
}
