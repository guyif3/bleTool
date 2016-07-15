package com.library.ble.manager;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.text.TextUtils;

import com.library.ble.conn.BleCallback;
import com.library.ble.conn.BleCharactCallback;
import com.library.ble.conn.BleDescriptorCallback;
import com.library.ble.conn.BleRssiCallback;
import com.library.ble.exception.BleException;
import com.library.ble.exception.ConnectException;
import com.library.ble.exception.GattException;
import com.library.ble.exception.InitiatedException;
import com.library.ble.exception.NoUUidException;
import com.library.ble.exception.OtherException;
import com.library.ble.utils.HexUtil;
import com.library.ble.utils.LogUtil;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 蓝牙操作的控制器
 * Created by gongjianghua on 16/7/5.
 */
public class BleDataController {
    private static final String TAG = BleDataController.class.getSimpleName();
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "0000-1000-8000-00805f9b34fb";
    public static final UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(HEART_RATE_MEASUREMENT);
    /** 开启信道通知 */
    private static final int MSG_NOTIY_CHA = 6;
    /**  */
    private static final int MSG_NOTIY_DES = 7;
    /**  */
    private static final int MSG_READ_CHA = 3;
    /**  */
    private static final int MSG_READ_DES = 4;
    /**  */
    private static final int MSG_READ_RSSI = 5;
    /** 写入数据进入设备 */
    private static final int MSG_WRIATE_CHA = 1;
    /**  */
    private static final int MSG_WRIATE_DES = 2;

    /**
     * 当前操作的蓝牙对象
     */
    private BluetoothGatt bluetoothGatt;

    /**
     * 异常处理handler
     */
    private Handler handler;
    /**
     *
     */
    private DeviceManager bleDeviceManager;

    /**
     *
     */
    private int timeOutMillis;

    public BleDataController(DeviceManager bleDeviceManager) {
        this.bleDeviceManager = bleDeviceManager;
        this.timeOutMillis = 10*1000;
        this.handler = bleDeviceManager.getMainHandler();
        this.bluetoothGatt = bleDeviceManager.getBluetoothGatt();
    }

    /**
     * 关闭所有蓝牙操作
     */
    public void close(){
        if(this.handler!=null){
            this.handler.removeCallbacksAndMessages(null);
            this.handler=null;
        }
        this.bluetoothGatt=null;
        this.bleDeviceManager=null;
    }


    /**
     * 返回uuid
     * @param uuid
     * @return
     */
    private UUID formUUID(String uuid) {
        return uuid == null ? null : UUID.fromString(uuid);
    }

    /**
     * 通过uuid的头获取uuid 字符串
     * @param head  头内容
     * @return
     */
    public String returnUUID(String head){
        if(TextUtils.isEmpty(head)) return null;
        StringBuffer uuid=new StringBuffer();
        uuid.append(head);
        uuid.append(CLIENT_CHARACTERISTIC_CONFIG);
        return uuid.toString().trim();
    }


    /**
     * 写入数据
     * @param serviceUUID
     * @param charactUUID
     * @param data
     * @param notification
     * @param bleCallback
     * @return
     */
    public boolean writeCharacteristic(String serviceUUID, String charactUUID,byte[] data,boolean notification, BleCharactCallback bleCallback) {
        LogUtil.d(TAG, serviceUUID+","+charactUUID + " characteristic write bytes: " + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
        if (bleCallback == null)  return false;
        if(bluetoothGatt==null){
            bleCallback.onFailure(new ConnectException(bluetoothGatt, BluetoothProfile.STATE_DISCONNECTED));
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(formUUID(returnUUID(serviceUUID)));
        if(service==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattCharacteristic charact = service.getCharacteristic(formUUID(returnUUID(charactUUID)));
        if(charact==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        setCharacteristicNotification(bluetoothGatt,charact,notification);
        BluetoothGattCallback bluetoothGattCallback= new BluetoothGattCallback() {
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                BleDataController.this.handler.removeMessages(BleDataController.MSG_WRIATE_CHA, this);
                BleDataController.this.bleDeviceManager.removeGattCallback(bleCallback.getBluetoothGattCallback());
                if (status == 0) {
                    bleCallback.onSuccess(characteristic);
                } else {
                    bleCallback.onFailure(new GattException(status));
                }
            }
        };
        bleCallback.setBluetoothGattCallback(bluetoothGattCallback);
        listenAndTimer(bleCallback, MSG_WRIATE_CHA);
        charact.setValue(data);
        return handleAfterInitialed(bluetoothGatt.writeCharacteristic(charact), bleCallback);
    }


    /**
     * 都写入数据 信道 描述
     * @param serviceUUID
     * @param charactUUID
     * @param descriptorUUID
     * @param charactData
     * @param descriptorData
     * @param charactNotification
     * @param descriptorNotification
     * @param bleCallback
     * @return
     */
    public boolean writeCharacteristic(String serviceUUID, String charactUUID,String descriptorUUID,byte[] charactData,byte[] descriptorData,boolean charactNotification,boolean descriptorNotification, BleCharactCallback bleCallback) {
        LogUtil.d(TAG, serviceUUID+","+charactUUID + " characteristic write bytes: " + Arrays.toString(charactData) + " ,hex: " + HexUtil.encodeHexStr(charactData)+", descriptor write bytes:"+Arrays.toString(descriptorData)+",hex:"+HexUtil.encodeHexStr(descriptorData));
        if (bleCallback == null)  return false;
        if(bluetoothGatt==null){
            bleCallback.onFailure(new ConnectException(bluetoothGatt, BluetoothProfile.STATE_DISCONNECTED));
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(formUUID(returnUUID(serviceUUID)));
        if(service==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattCharacteristic charact = service.getCharacteristic(formUUID(returnUUID(charactUUID)));
        if(charact==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattDescriptor descriptor = charact.getDescriptor(formUUID(returnUUID(descriptorUUID)));
        if(descriptor==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        writeDescriptor(bluetoothGatt, descriptor, descriptorData, descriptorNotification, new BleDescriptorCallback() {
            @Override
            public void onSuccess(BluetoothGattDescriptor bluetoothGattDescriptor) {

            }

            @Override
            public void onFailure(BleException bleException) {

            }
        });
        setCharacteristicNotification(bluetoothGatt,charact,charactNotification);
        BluetoothGattCallback bluetoothGattCallback= new BluetoothGattCallback() {
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                BleDataController.this.handler.removeMessages(BleDataController.MSG_WRIATE_CHA, this);
                BleDataController.this.bleDeviceManager.removeGattCallback(bleCallback.getBluetoothGattCallback());
                if (status == 0) {
                    bleCallback.onSuccess(characteristic);
                } else {
                    bleCallback.onFailure(new GattException(status));
                }
            }
        };
        bleCallback.setBluetoothGattCallback(bluetoothGattCallback);
        listenAndTimer(bleCallback, MSG_WRIATE_CHA);
        charact.setValue(charactData);
        return handleAfterInitialed(bluetoothGatt.writeCharacteristic(charact), bleCallback);
    }


    /**
     * 写入描述数据
     * @param serviceUUID
     * @param charactUUID
     * @param descriptorUUID
     * @param data
     * @param isopen
     * @param bleCallback
     * @return
     */
    public boolean writeDescriptor(String serviceUUID, String charactUUID,String descriptorUUID, byte[] data, boolean isopen,BleDescriptorCallback bleCallback) {
         LogUtil.d(TAG, serviceUUID+","+charactUUID+","+descriptorUUID + " descriptor write bytes: " + Arrays.toString(data) + " ,hex: " + HexUtil.encodeHexStr(data));
        if (bleCallback == null)  return false;
        if(bluetoothGatt==null){
            bleCallback.onFailure(new ConnectException(bluetoothGatt, BluetoothProfile.STATE_DISCONNECTED));
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(formUUID(returnUUID(serviceUUID)));
        if(service==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattCharacteristic charact = service.getCharacteristic(formUUID(returnUUID(charactUUID)));
        if(charact==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattDescriptor descriptor = charact.getDescriptor(formUUID(returnUUID(descriptorUUID)));
        if(descriptor==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        setDescriptorNotification(bluetoothGatt,descriptor,isopen);
        BluetoothGattCallback bluetoothGattCallback  =new BluetoothGattCallback() {
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                BleDataController.this.handler.removeMessages(BleDataController.MSG_WRIATE_DES, this);
                BleDataController.this.bleDeviceManager.removeGattCallback(bleCallback.getBluetoothGattCallback());
                if (status == 0) {
                    bleCallback.onSuccess(descriptor);
                } else {
                    bleCallback.onFailure(new GattException(status));
                }
            }
        };
        bleCallback.setBluetoothGattCallback(bluetoothGattCallback);
        listenAndTimer(bleCallback, MSG_WRIATE_DES);
        descriptor.setValue(data);
        return handleAfterInitialed(bluetoothGatt.writeDescriptor(descriptor), bleCallback);
    }


    /**
     * 写入描述配置
     * @param bluetoothGatt
     * @param descriptor
     * @param data
     * @param isopen
     * @param bleCallback
     * @return
     */
    public boolean writeDescriptor(BluetoothGatt bluetoothGatt,BluetoothGattDescriptor descriptor,byte[] data,boolean isopen,BleDescriptorCallback bleCallback){
        setDescriptorNotification(bluetoothGatt,descriptor,isopen);
        BluetoothGattCallback bluetoothGattCallback  =new BluetoothGattCallback() {
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                BleDataController.this.handler.removeMessages(BleDataController.MSG_WRIATE_DES, this);
                BleDataController.this.bleDeviceManager.removeGattCallback(bleCallback.getBluetoothGattCallback());
                if (status == 0) {
                    bleCallback.onSuccess(descriptor);
                } else {
                    bleCallback.onFailure(new GattException(status));
                }
            }
        };
        bleCallback.setBluetoothGattCallback(bluetoothGattCallback);
        listenAndTimer(bleCallback, MSG_WRIATE_DES);
        descriptor.setValue(data);
        return handleAfterInitialed(bluetoothGatt.writeDescriptor(descriptor), bleCallback);
    }



    /**
     * 开起所有的通知
     * @param gatt
     * @param characteristic
     * @param descriptor
     * @param enable
     * @return
     */
    public boolean setNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, BluetoothGattDescriptor descriptor, boolean enable) {
        return setCharacteristicNotification(gatt, characteristic, enable) && setDescriptorNotification(gatt, descriptor, enable);
    }

    /**
     * 开起信道通知
     * @param serviceUUID
     * @param charactUUID
     * @param isopen
     * @param bleCallback
     * @return
     */
    public boolean setCharacteristicNotification(String serviceUUID, String charactUUID,boolean isopen, BleCharactCallback bleCallback){
        LogUtil.d(TAG, serviceUUID+","+charactUUID + " , state:" + isopen );
        if (bleCallback == null)  return false;
        if(bluetoothGatt==null){
            bleCallback.onFailure(new ConnectException(bluetoothGatt, BluetoothProfile.STATE_DISCONNECTED));
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(formUUID(returnUUID(serviceUUID)));
        if(service==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattCharacteristic charact = service.getCharacteristic(formUUID(returnUUID(charactUUID)));
        if(charact==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        if ((charact.getProperties() | 16) > 0) {
            BluetoothGattCallback bluetoothGattCallback=new BluetoothGattCallback() {
                AtomicBoolean msgRemoved = new AtomicBoolean(false);

                public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    if (!this.msgRemoved.getAndSet(true)) {
                        BleDataController.this.handler.removeMessages(BleDataController.MSG_NOTIY_CHA, this);
                    }
                    bleCallback.onSuccess(characteristic);
                }
            };
            bleCallback.setBluetoothGattCallback(bluetoothGattCallback);
            listenAndTimer(bleCallback, MSG_NOTIY_CHA);
            return handleAfterInitialed(setCharacteristicNotification(bluetoothGatt, charact, isopen),bleCallback);
        }
        bleCallback.onFailure(new OtherException("Characteristic [not supports] readable!"));
        return false;
    }




    /**
     * 开起信道通知
     * @param gatt
     * @param characteristic
     * @param enable
     * @return
     */
    public boolean setCharacteristicNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enable) {
        if (gatt == null || characteristic == null) {
            return false;
        }
       LogUtil.d(TAG, "Characteristic set notification value: " + enable);
        boolean success = gatt.setCharacteristicNotification(characteristic, enable);
        if (!UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            return success;
        }
        LogUtil.d(TAG, "Heart Rate Measurement set [descriptor] notification value: " + enable);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
        return success;
    }

    /**
     * 开起信道描述的通知
     * @param serviceUUID
     * @param charactUUID
     * @param descriptorUUID
     * @param isopen
     * @param bleCallback
     * @return
     */
    public boolean setDescriptorNotification(String serviceUUID, String charactUUID,String descriptorUUID,boolean isopen, BleDescriptorCallback bleCallback){
        LogUtil.d(TAG, serviceUUID+","+charactUUID +","+descriptorUUID+ " , state:" + isopen );
        if (bleCallback == null)  return false;
        if(bluetoothGatt==null){
            bleCallback.onFailure(new ConnectException(bluetoothGatt, BluetoothProfile.STATE_DISCONNECTED));
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(formUUID(returnUUID(serviceUUID)));
        if(service==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattCharacteristic charact = service.getCharacteristic(formUUID(returnUUID(charactUUID)));
        if(charact==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattDescriptor descriptor = charact.getDescriptor(formUUID(returnUUID(descriptorUUID)));
        if(descriptor==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattCallback bluetoothGattCallback= new BluetoothGattCallback() {
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                BleDataController.this.handler.removeMessages(BleDataController.MSG_NOTIY_DES, this);
                if (status == 0) {
                    bleCallback.onSuccess(descriptor);
                } else {
                    bleCallback.onFailure(new GattException(status));
                }
            }
        };
        bleCallback.setBluetoothGattCallback(bluetoothGattCallback);
        listenAndTimer(bleCallback, MSG_NOTIY_DES);
       return handleAfterInitialed(setDescriptorNotification(bluetoothGatt,descriptor,isopen),bleCallback);
    }
    /**
     * 开起配置通知
     * @param gatt
     * @param descriptor
     * @param enable
     * @return
     */
    public boolean setDescriptorNotification(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, boolean enable) {
        if (gatt == null || descriptor == null) {
            return false;
        }
        LogUtil.d(TAG, "Descriptor set notification value: " + enable);
        if (enable) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return gatt.writeDescriptor(descriptor);
    }

    /**
     * 启动操作定时器
     * @param bleCallback
     * @param what
     */
    private void listenAndTimer(BleCallback bleCallback, int what) {
        bleCallback.setBluetoothGattCallback(bleCallback.getBluetoothGattCallback());
        this.bleDeviceManager.addGattCallback(bleCallback.getBluetoothGattCallback());
        this.handler.sendMessageDelayed(this.handler.obtainMessage(what, bleCallback), (long) this.timeOutMillis);
    }

    /**
     * 判断最后操作师傅操作成功
     * @param initiated
     * @param bleCallback
     * @return
     */
    private boolean handleAfterInitialed(boolean initiated, BleCallback bleCallback) {
        if (bleCallback != null) {
            if (initiated) {
                bleCallback.onInitiatedSuccess();
            } else {
                bleCallback.onFailure(new InitiatedException());
            }
        }
        return initiated;
    }


    /**
     * 读取参数
     * @param serviceUUID
     * @param charactUUID
     * @param bleCallback
     * @return
     */
    public boolean readCharacteristic(String serviceUUID, String charactUUID,BleCharactCallback bleCallback) {
        LogUtil.d(TAG, serviceUUID+","+charactUUID  );
        if (bleCallback == null)  return false;
        if(bluetoothGatt==null){
            bleCallback.onFailure(new ConnectException(bluetoothGatt, BluetoothProfile.STATE_DISCONNECTED));
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(formUUID(returnUUID(serviceUUID)));
        if(service==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattCharacteristic charact = service.getCharacteristic(formUUID(returnUUID(charactUUID)));
        if(charact==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        if ((charact.getProperties() | MSG_WRIATE_DES) > 0) {
            setCharacteristicNotification(bluetoothGatt, charact, false);
            BluetoothGattCallback bluetoothGattCallback=  new BluetoothGattCallback() {
                AtomicBoolean msgRemoved = new AtomicBoolean(false);
                public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                    if (!this.msgRemoved.getAndSet(true)) {
                        BleDataController.this.handler.removeMessages(BleDataController.MSG_READ_CHA, this);
                    }
                    BleDataController.this.bleDeviceManager.removeGattCallback(bleCallback.getBluetoothGattCallback());
                    if (status == 0) {
                        bleCallback.onSuccess(characteristic);
                    } else {
                        bleCallback.onFailure(new GattException(status));
                    }
                }
            };
            bleCallback.setBluetoothGattCallback(bluetoothGattCallback);
            listenAndTimer(bleCallback, MSG_READ_CHA);
            return handleAfterInitialed(bluetoothGatt.readCharacteristic(charact), bleCallback);
        }
        bleCallback.onFailure(new OtherException("Characteristic [is not] readable!"));
        return false;
    }

    /**
     * 读取描述参数
     * @param serviceUUID
     * @param charactUUID
     * @param descriptorUUID
     * @param bleCallback
     * @return
     */
    public boolean readDescriptor(String serviceUUID, String charactUUID,String descriptorUUID,BleDescriptorCallback bleCallback) {
        LogUtil.d(TAG, serviceUUID+","+charactUUID+","+descriptorUUID);
        if (bleCallback == null)  return false;
        if(bluetoothGatt==null){
            bleCallback.onFailure(new ConnectException(bluetoothGatt, BluetoothProfile.STATE_DISCONNECTED));
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(formUUID(returnUUID(serviceUUID)));
        if(service==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattCharacteristic charact = service.getCharacteristic(formUUID(returnUUID(charactUUID)));
        if(charact==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattDescriptor descriptor = charact.getDescriptor(formUUID(returnUUID(descriptorUUID)));
        if(descriptor==null){
            bleCallback.onFailure(new NoUUidException());
            return false;
        }
        BluetoothGattCallback bluetoothGattCallback= new BluetoothGattCallback() {
            AtomicBoolean msgRemoved = new AtomicBoolean(false);

            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                if (!this.msgRemoved.getAndSet(true)) {
                    BleDataController.this.handler.removeMessages(BleDataController.MSG_READ_DES, this);
                }
                if (status == 0) {
                    bleCallback.onSuccess(descriptor);
                } else {
                    bleCallback.onFailure(new GattException(status));
                }
            }
        };
        bleCallback.setBluetoothGattCallback(bluetoothGattCallback);
        listenAndTimer(bleCallback, MSG_READ_DES);
        return handleAfterInitialed(bluetoothGatt.readDescriptor(descriptor), bleCallback);
    }


    /**
     * 读取信号强度
     * @param bleCallback
     * @return
     */
    public boolean readRemoteRssi(BleRssiCallback bleCallback) {
        if (bleCallback == null)  return false;
        if(bluetoothGatt==null){
            bleCallback.onFailure(new ConnectException(bluetoothGatt, BluetoothProfile.STATE_DISCONNECTED));
            return false;
        }
        BluetoothGattCallback bluetoothGattCallback=new BluetoothGattCallback() {
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                BleDataController.this.handler.removeMessages(BleDataController.MSG_READ_RSSI, this);
                if (status == 0) {
                    bleCallback.onSuccess(rssi);
                } else {
                    bleCallback.onFailure(new GattException(status));
                }
            }
        };
        bleCallback.setBluetoothGattCallback(bluetoothGattCallback);
        listenAndTimer(bleCallback, MSG_READ_RSSI);
        return handleAfterInitialed(bluetoothGatt.readRemoteRssi(), bleCallback);
    }


    public void setTimeOutMillis(int timeOutMillis) {
        this.timeOutMillis = timeOutMillis;
    }
}

