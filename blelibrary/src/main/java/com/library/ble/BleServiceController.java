package com.library.ble;

import android.bluetooth.BluetoothDevice;

import com.library.ble.impl.scan.PeriodScanCallback;
import com.library.ble.inter.SearchDeviceInterface;
import com.library.ble.service.BleService;

/**
 * Created by gongjianghua on 16/7/6.
 */

public class BleServiceController {

    private BleService bleService;

    public BleServiceController(BleService bleService) {
        this.bleService = bleService;
    }


    /**
     * 扫描设备
     *
     * @return
     */
    public boolean startSearchDevice(SearchDeviceInterface listener) {
        if (bleService != null) {
            if (BleService.bleManager != null) {
               return BleService.bleManager.startLeScan(new PeriodScanCallback(BleService.bleManager,10*1000) {

                   @Override
                   protected void onScanResults(BluetoothDevice device, int rssi, byte[] scanRecord, long scan_time) {
                        listener.onScanResults(device,rssi,scanRecord,scan_time);
                   }

                   @Override
                   public void onScanTimeout() {
                       listener.onScanTimeout();
                   }
               });
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


}
