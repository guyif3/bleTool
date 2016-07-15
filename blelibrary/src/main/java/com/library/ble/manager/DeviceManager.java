package com.library.ble.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.library.ble.exception.BleException;
import com.library.ble.exception.ConnectException;
import com.library.ble.impl.callback.BleGattCallback;
import com.library.ble.impl.scan.PeriodMacScanCallback;
import com.library.ble.impl.scan.PeriodScanCallback;
import com.library.ble.service.BleService;
import com.library.ble.utils.LogUtil;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by gongjianghua on 16/7/5.
 */

public class DeviceManager {

    private static final String TAG =DeviceManager.class.getSimpleName() ;

    /**
     * 正在扫描
     */
    public static final int STATE_SCANNING = -1;
    /**
     * 已断开连接
     */
    public static final int STATE_DISCONNECTED = 0;
    /**
     * 正在断开连接中
     */
    public static final int STATE_DISCONNECTING = 1;
    /**
     * 正在连接中
     */
    public static final int STATE_CONNECTING = 2;
    /**
     * 已连接
     */
    public static final int STATE_CONNECTED = 3;
    /**
     * 服务已经搜索
     */
    public static final int STATE_SERVICES_DISCOVERED = 4;



    /**
     * 硬件是否支持蓝牙 false为不支持，true为支持
     */
    private boolean hardwareSupport = true;
    /**
     * 系统是否支持蓝牙4.0 false为不支持，true为支持
     */
    private boolean systemSupport = true;


    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothManager bluetoothManager;
    private Set<BluetoothGattCallback> callbackList = new CopyOnWriteArraySet<>();
    private int connectionState = STATE_DISCONNECTED;
    private Context context;
    private BleService.MyHanlder mHanlder = null;
    /**
     * 当前正在扫描的蓝牙回调
     */
    private BluetoothAdapter.LeScanCallback scanCallBack = null;


    public DeviceManager(Context context,BleService.MyHanlder mHanlder) {
        this.mHanlder = mHanlder;
        this.mHanlder.setDeviceManager(this);
        this.context=context;
        this.connectionState = DeviceManager.STATE_DISCONNECTED;
        checkBleEnable();
    }

    /**
     * 蓝牙初始化，同时检查蓝牙情况
     */
    public void checkBleEnable() {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            hardwareSupport = false;
            return;
        }
        bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            systemSupport = false;
            return;
        }
    }
    /**
     * 获取检查系统的情况
     * @return  true为手机支持蓝牙4。0，false则为手机完全不支持
     */
    public boolean getSupprotForPhone(){
        return  hardwareSupport&&systemSupport;
    }


    /**
     * 返回当前蓝牙是否打开
     * @return
     */
    public boolean isOpenBle(){
        return this.bluetoothAdapter.isEnabled();
    }

    /**
     * 是否正在扫描
     * @return
     */
    public boolean isInScanning() {
        return this.connectionState == STATE_SCANNING;
    }

    /**
     * 是否正在连接或者已经连接上设备了
     * @return
     */
    public boolean isConnectingOrConnected() {
        return this.connectionState >= STATE_CONNECTING;
    }
    /**
     * 是否已经连接设备了
     * @return
     */
    public boolean isConnected() {
        return this.connectionState >= STATE_CONNECTED;
    }

    /**
     * 是否正在发现设备了
     * @return
     */
    public boolean isServiceDiscoered() {
        return this.connectionState == STATE_SERVICES_DISCOVERED;
    }
    /**
     * 公用handler
     * @return
     */
    public Handler getMainHandler() {
        return this.mHanlder;
    }

    public boolean addGattCallback(BluetoothGattCallback callback) {
        return this.callbackList.add(callback);
    }

    public boolean addGattCallback(BleGattCallback callback) {
        return this.callbackList.add(callback);
    }


    public boolean removeGattCallback(BluetoothGattCallback callback) {
        return this.callbackList.remove(callback);
    }

    /**
     * 获取当前连接gatt
     * @return
     */
    public BluetoothGatt getBluetoothGatt() {
        return this.bluetoothGatt;
    }

    /**
     * 初始化设备的服务
     */
    public void  discoverServices(){
        if(this.bluetoothGatt!=null) this.bluetoothGatt.discoverServices();
    }


    /**
     * 提供 据柄
     * @return
     */
    public Context getContext(){
        return context;
    }

    /**
     * 扫描设备 全部设备
     * @param callback
     * @return
     */
    public boolean startLeScan(PeriodScanCallback callback) {
        if(isInScanning()){
            return true;
        }
        callback.notifyScanStarted();
        boolean suc = this.bluetoothAdapter.startLeScan(callback);
        if (suc) {
            scanCallBack=callback;
            this.connectionState = STATE_SCANNING;
        } else {
            callback.removeHandlerMsg();
        }
        return suc;
    }
    /**
     * 停止扫描和连接操作
     */
    public void stopScan() {
        if(scanCallBack!=null){
            stopScan(scanCallBack);
        }
    }

    /**
     * 停止扫描
     * @param callback
     */
    public void stopScan(BluetoothAdapter.LeScanCallback callback) {
        if (callback instanceof PeriodScanCallback) {
            ((PeriodScanCallback) callback).removeHandlerMsg();
        }
        this.bluetoothAdapter.stopLeScan(callback);
        if (this.connectionState == STATE_SCANNING) {
            this.connectionState = STATE_DISCONNECTED;
            scanCallBack=null;
        }
    }

    /**
     * 连接设备
     * @param device 设备
     * @param autoConnect
     * @param callback
     * @return
     */
    public synchronized BluetoothGatt connect(BluetoothDevice device, boolean autoConnect, BleGattCallback callback) {
        LogUtil.d(TAG, "connect device\uff1a" + device.getName() + " mac:" + device.getAddress() + " autoConnect ------> " + autoConnect);
        this.callbackList.add(callback);
        return device.connectGatt(this.context, autoConnect, this.coreGattCallback);
    }


    /**
     * 关闭设备连接
     */
    public void closeBluetoothGatt() {
        mHanlder.removeCallbacks(null);
        if(this.bluetoothGatt != null) this.bluetoothGatt.disconnect();
        if(this.bluetoothGatt != null) this.bluetoothGatt.close();

        Log.i(TAG, "closed BluetoothGatt ");
    }

    /**
     *  扫描设备并且自动连接上去
     * @param mac 地址
     * @param autoConnect
     * @param callback
     * @return
     */
    public boolean scanAndConnect(String mac, boolean autoConnect, BleGattCallback callback) {
        if (mac == null || mac.split(":").length != 6) {
            throw new IllegalArgumentException("Illegal MAC ! ");
        }
        final BleGattCallback liteBleGattCallback = callback;
        final boolean z = autoConnect;
        startLeScan(new PeriodMacScanCallback(mac,this, 12000) {
            public void onScanTimeout() {
                if (liteBleGattCallback != null) {
                    liteBleGattCallback.onConnectFailure(BleException.TIMEOUT_EXCEPTION);
                }
            }

            public void onDeviceFound(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                DeviceManager.this.connect(device, z, liteBleGattCallback);
            }
        });
        return true;
    }

    /**
     * 总的蓝牙回调处理
     */
    public BluetoothGattCallback coreGattCallback=new BluetoothGattCallback() {


        /**
         *
         * 蓝牙连接变化
         * @param gatt
         * @param status
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtil.d(TAG, "onConnectionStateChange  status: " + status + " ,newState: " + newState + "  ,thread: " + Thread.currentThread().getId());
            if(status== BluetoothGatt.GATT_SUCCESS){//通信成功
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    DeviceManager.this.connectionState = STATE_CONNECTED;
                    DeviceManager.this.bluetoothGatt=bluetoothGatt;
                    for (BluetoothGattCallback callback: callbackList) {
                        if(callback instanceof BleGattCallback)
                            ((BleGattCallback)callback).onConnectSuccess(bluetoothGatt);
                    }
                } else if (newState == BluetoothGatt.STATE_CONNECTING) {
                    DeviceManager.this.connectionState = STATE_CONNECTING;
                } else {
                    mHanlder.removeCallbacks(null);
                    DeviceManager.this.bluetoothGatt=null;
                    DeviceManager.this.connectionState=STATE_DISCONNECTED;
                    for (BluetoothGattCallback callback: callbackList) {
                        if(callback instanceof BleGattCallback)
                            ((BleGattCallback)callback).onConnectFailure(new ConnectException(gatt, status));
                    }
                }
            }else{
                mHanlder.removeCallbacks(null);
                DeviceManager.this.bluetoothGatt=null;
                DeviceManager.this.connectionState=STATE_DISCONNECTED;
                for (BluetoothGattCallback callback: callbackList) {
                    if(callback instanceof BleGattCallback)
                        ((BleGattCallback)callback).onConnectFailure(new ConnectException(gatt, status));
                }
            }
        }

        /**
         * 扫描信道的结果
         * @param bluetoothGatt
         * @param state
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int state) {
            if(state== BluetoothGatt.GATT_SUCCESS) {//通信成功
                DeviceManager.this.connectionState = STATE_SERVICES_DISCOVERED;
                for (BluetoothGattCallback callback: callbackList) {
                    if(callback instanceof BleGattCallback)
                        ((BleGattCallback)callback).onServicesDiscovered(bluetoothGatt, true);
                }
            }else {
                for (BluetoothGattCallback callback: callbackList) {
                    if(callback instanceof BleGattCallback)
                        ((BleGattCallback)callback).onServicesDiscovered(bluetoothGatt, false);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if(DeviceManager.this.callbackList.size()>0) {
                for (BluetoothGattCallback call : DeviceManager.this.callbackList) {
                    call.onCharacteristicRead(gatt, characteristic, status);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if(DeviceManager.this.callbackList.size()>0) {
                for (BluetoothGattCallback call : DeviceManager.this.callbackList) {
                    call.onCharacteristicWrite(gatt, characteristic, status);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if(DeviceManager.this.callbackList.size()>0) {
                for (BluetoothGattCallback call : DeviceManager.this.callbackList) {
                    call.onCharacteristicChanged(gatt, characteristic);
                }
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if(DeviceManager.this.callbackList.size()>0) {
                for (BluetoothGattCallback call : DeviceManager.this.callbackList) {
                    call.onDescriptorRead(gatt, descriptor, status);
                }
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if(DeviceManager.this.callbackList.size()>0) {
                for (BluetoothGattCallback call : DeviceManager.this.callbackList) {
                    call.onDescriptorWrite(gatt, descriptor, status);
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if(DeviceManager.this.callbackList.size()>0) {
                for (BluetoothGattCallback call : DeviceManager.this.callbackList) {
                    call.onReadRemoteRssi(gatt, rssi, status);
                }
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            if(DeviceManager.this.callbackList.size()>0) {
                for (BluetoothGattCallback call : DeviceManager.this.callbackList) {
                    call.onReliableWriteCompleted(gatt, status);
                }
            }
        }
    };

}
