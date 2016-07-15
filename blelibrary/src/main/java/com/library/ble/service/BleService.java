package com.library.ble.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.library.ble.conn.BleCallback;
import com.library.ble.exception.BleException;
import com.library.ble.manager.DeviceManager;

import java.util.LinkedList;

/**
 * Created by gongjianghua on 16/5/26.
 */

public  class BleService  extends Service {


    /**
     * 打开定位
     */
    public static final int TO_OPEN_GPS = 170778;

    /**
     * 打开蓝牙
     */
    public static final int TO_OPEN_BLE = 170214;

    /** 6.0中需要判断的权限 */
    public static final String[] SCAN_BLE_PERMISSIONS=new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    /** 蓝牙操作的管理类*/
    public static DeviceManager bleManager=null;
    /**
     * 预备工作的工作队列
     */
    private LinkedList<String> worklist=new LinkedList();


    private ServiceBinder MBleService=new ServiceBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return MBleService;
    }
    public class ServiceBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    private MyHanlder myHanlder;

    @Override
    public void onCreate() {
        super.onCreate();
        worklist=new LinkedList();
        myHanlder=new MyHanlder(getMainLooper());
        bleManager=new DeviceManager(this,myHanlder);

    }

    @Override
    public void onDestroy() {
        worklist.clear();
        super.onDestroy();
    }

    public class MyHanlder extends Handler {


        private  DeviceManager manager;

        public MyHanlder(Looper looper) {
            super(looper);
        }

        public void setDeviceManager(DeviceManager manager) {
            this.manager = manager;
        }

        public void handleMessage(Message msg) {
            BleCallback call = (BleCallback) msg.obj;
            if (call != null) {
                if(manager!=null)manager.removeGattCallback(call.getBluetoothGattCallback());
                call.onFailure(BleException.TIMEOUT_EXCEPTION);
            }
            msg.obj = null;
        }
    }


    public LinkedList<String> getWorklist() {
        return worklist;
    }

    public void addWorklist(String work) {
        this.worklist.add(work);
    }


}
