package com.library.ble.exception;

public class TimeoutException extends BleException {
    public TimeoutException() {
        super(BleException.ERROR_CODE_TIMEOUT, "Timeout Exception Occurred! ");
    }
}
