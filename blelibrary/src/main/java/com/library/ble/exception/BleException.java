package com.library.ble.exception;

import java.io.Serializable;

/**
 * ble 异常   基类
 */
public abstract class BleException implements Serializable {

    public static final int ERROR_CODE_GATT = 201;
    public static final int ERROR_CODE_INITIAL = 101;
    public static final int ERROR_CODE_TIMEOUT = 1;
    public static final int GATT_CODE_OTHER = 301;
    public static final int GATT_UUID_ERROR = 401;
    public static final TimeoutException TIMEOUT_EXCEPTION = new TimeoutException();
    private static final long serialVersionUID = 8004414918500865564L;
    private int code;
    private String description;

    public BleException(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return this.code;
    }

    public BleException setCode(int code) {
        this.code = code;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public BleException setDescription(String description) {
        this.description = description;
        return this;
    }

    public String toString() {
        return "BleException{code=" + this.code + ", description='" + this.description + '\'' + '}';
    }
}
