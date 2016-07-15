package com.library.ble.exception.hanlder;

import android.content.Context;
import android.widget.Toast;

import com.library.ble.exception.ConnectException;
import com.library.ble.exception.GattException;
import com.library.ble.exception.InitiatedException;
import com.library.ble.exception.OtherException;
import com.library.ble.exception.TimeoutException;


public class DefaultBleExceptionHandler extends BleExceptionHandler {
    private Context context;

    public DefaultBleExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    protected void onConnectException(ConnectException e) {
        Toast.makeText(this.context, e.getDescription(), Toast.LENGTH_SHORT).show();
    }

    protected void onGattException(GattException e) {
        Toast.makeText(this.context, e.getDescription(), Toast.LENGTH_SHORT).show();
    }

    protected void onTimeoutException(TimeoutException e) {
        Toast.makeText(this.context, e.getDescription(), Toast.LENGTH_SHORT).show();
    }

    protected void onInitiatedException(InitiatedException e) {
        Toast.makeText(this.context, e.getDescription(), Toast.LENGTH_SHORT).show();
    }

    protected void onOtherException(OtherException e) {
        Toast.makeText(this.context, e.getDescription(), Toast.LENGTH_SHORT).show();
    }
}
