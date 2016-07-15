package com.library.ble.exception.hanlder;


import com.library.ble.exception.BleException;
import com.library.ble.exception.ConnectException;
import com.library.ble.exception.GattException;
import com.library.ble.exception.InitiatedException;
import com.library.ble.exception.OtherException;
import com.library.ble.exception.TimeoutException;

/**
 *ble异常处理
 */
abstract class BleExceptionHandler {
    protected abstract void onConnectException(ConnectException connectException);

    protected abstract void onGattException(GattException gattException);

    protected abstract void onInitiatedException(InitiatedException initiatedException);

    protected abstract void onOtherException(OtherException otherException);

    protected abstract void onTimeoutException(TimeoutException timeoutException);

    public BleExceptionHandler handleException(BleException exception) {
        if (exception != null) {
            if (exception instanceof ConnectException) {
                onConnectException((ConnectException) exception);
            } else if (exception instanceof GattException) {
                onGattException((GattException) exception);
            } else if (exception instanceof TimeoutException) {
                onTimeoutException((TimeoutException) exception);
            } else if (exception instanceof InitiatedException) {
                onInitiatedException((InitiatedException) exception);
            } else {
                onOtherException((OtherException) exception);
            }
        }
        return this;
    }
}
