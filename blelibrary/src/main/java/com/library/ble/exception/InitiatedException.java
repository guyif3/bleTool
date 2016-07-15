package com.library.ble.exception;

public class InitiatedException extends BleException {
    public InitiatedException() {
        super(BleException.ERROR_CODE_INITIAL, "Initiated Exception Occurred! ");
    }
}
