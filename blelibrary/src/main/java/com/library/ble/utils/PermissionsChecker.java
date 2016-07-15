package com.library.ble.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;


import com.library.R;


/**
 * 检查权限的工具类
 * <p/>
 * Created by wangchenlong on 16/1/26.
 */
public class PermissionsChecker {
    public static final int SYSTEM_PERMISSION_REQUEST_CODE=1;
    public static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案

    /**
     *定位
     */
    public static  String[] LOCAL_PERMISSIONS={
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private final Context mContext;
    private LocationManager lm;

    /**
     * 判断当前系统是否大于m系统
     * @return
     */
    public  boolean isCheckSystemVersionThanM(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public PermissionsChecker(Context context) {
        mContext = context.getApplicationContext();
    }


    /**
     * 判断权限集合
     * @param permissions  权限集合
     @return true为缺少，false为不缺少
     */
    public boolean lacksPermissions(@NonNull String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }


    /**
     *  判断是否缺少权限
     * @param permission 判断的权限
     * @return true为缺少，false为不缺少
     */
    private boolean lacksPermission(@NonNull String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }


    // 含有全部的权限

    /**
     * 判断请求权限获取的返回结果
     * @param grantResults  获取到的权限获取结果
     * @return  true为权限全部获取成功，false为权限获取失败
     */
    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }



    /**
     *请求权限兼容低版本
     * @param permissions
     */
    public void requestPermissionsForActiviy(Activity mActivity,String... permissions) {
        ActivityCompat.requestPermissions(mActivity, permissions, PERMISSION_REQUEST_CODE);

    }
    // 显示缺失权限提示
    public void showMissingPermissionDialog(final Activity mActivity,int msg_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("需要给予授权");
        builder.setMessage(msg_id);
        // 拒绝, 退出应用
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings(mActivity);
            }
        });
        builder.show();
    }


    // 启动应用的设置
    public void startAppSettings(Activity mActivity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + mActivity.getPackageName()));
        mActivity.startActivityForResult(intent, SYSTEM_PERMISSION_REQUEST_CODE);
    }


    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;


    private void showMessageOKCancel(Activity mActivity,String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void showMessageOKCancel(Activity mActivity,String message, DialogInterface.OnClickListener okListener,DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(mActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", cancelListener)
                .create()
                .show();
    }

    /**
     * 比较2次获取的权限是否对的
     * @param requestpermisson
     * @param resultpermissions
     * @return
     */
    public boolean compareto(String[] requestpermisson, String[] resultpermissions) {
        if(requestpermisson.length==resultpermissions.length){
            boolean is=true;
            for (int i = 0; i < requestpermisson.length; i++) {
                if(!requestpermisson[i].equals(resultpermissions[i])){
                    is=false;
                    break;
                }
            }
            return is;
        }else{
            return false;
        }
    }

    /**
     * 获取gps定位状态
     * @return
     */
    public boolean getGpsState(){
        if(lm==null)lm=(LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)||lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return true;
        }else{
            return false;
        }
    }
}
