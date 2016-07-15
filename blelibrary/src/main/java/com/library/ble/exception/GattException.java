package com.library.ble.exception;

public class GattException extends BleException {
    private int gattStatus;

    public GattException(int gattStatus) {
        super(BleException.ERROR_CODE_GATT, "Gatt Exception Occurred! ");
        this.gattStatus = gattStatus;
    }

    public int getGattStatus() {
        return this.gattStatus;
    }

    public GattException setGattStatus(int gattStatus) {
        this.gattStatus = gattStatus;
        return this;
    }

    public String toString() {
        return "GattException{gattStatus=" + this.gattStatus + "} " + super.toString();
    }
}
