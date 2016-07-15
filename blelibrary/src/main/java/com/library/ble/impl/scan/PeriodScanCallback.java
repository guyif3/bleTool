package com.library.ble.impl.scan;

import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.text.TextUtils;

import com.library.ble.manager.DeviceManager;


public abstract class PeriodScanCallback implements LeScanCallback {
    protected Handler handler=null;
    protected DeviceManager bleDeviceManager;
    /** 扫描时间*/
    protected long timeoutMillis;
    /** 过滤条件*/
    protected String filterName;

    /**
     * 扫描完毕
     */
    public abstract void onScanTimeout();

    /**
     * 扫描结果
     * @param device 设备
     * @param rssi 信号
     * @param scanRecord
     * @param scan_time
     */
    protected void onScanResults(BluetoothDevice device, int rssi, byte[] scanRecord, long scan_time){

    }


    public PeriodScanCallback(DeviceManager bleDeviceManager, long timeoutMillis) {
        this.bleDeviceManager=bleDeviceManager;
        this.handler=bleDeviceManager.getMainHandler();
        this.timeoutMillis = timeoutMillis;
        this.filterName=null;
    }

    public PeriodScanCallback(DeviceManager bleDeviceManager,String filterName, long timeoutMillis) {
        this.bleDeviceManager=bleDeviceManager;
        this.handler=bleDeviceManager.getMainHandler();
        this.filterName = filterName;
        this.timeoutMillis = timeoutMillis;
    }


    public void notifyScanStarted() {
        if (this.timeoutMillis > 0) {
            removeHandlerMsg();
            this.handler.postDelayed(new Runnable() {
                public void run() {
                    PeriodScanCallback.this.bleDeviceManager.stopScan(PeriodScanCallback.this);
                    PeriodScanCallback.this.onScanTimeout();
                }
            }, this.timeoutMillis);
        }
    }

    public void removeHandlerMsg() {
        this.handler.removeCallbacksAndMessages(null);
    }




    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if(TextUtils.isEmpty(filterName)){
            onScanResults(device,rssi,scanRecord, System.currentTimeMillis());
        }else{
            if(device.getName().contains(filterName)){
                onScanResults(device,rssi,scanRecord, System.currentTimeMillis());
            }
        }
    }

}
