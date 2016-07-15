package com.library.ble.data;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

/**
 * 扫描到的蓝牙设备信息
 * Created by gongjianghua on 16/7/6.
 */

public class SearchData implements Serializable {


    public int ressi;

    public BluetoothDevice device;


    public long update_time;


    public String mac;

    @Override
    public boolean equals(Object obj) {
         if(obj instanceof SearchData){
           return ((SearchData) obj).mac.equals(mac);
         }
        return false;
    }
}
