package com.library.ble.exception;

public class OtherException extends BleException {
    public OtherException(String description) {
        super(BleException.GATT_CODE_OTHER, description);
    }
}
