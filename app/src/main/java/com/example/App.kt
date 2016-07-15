package com.example

import android.app.Application
import com.library.ble.BleServiceController

import com.library.ble.utils.LogUtil
import com.zhy.autolayout.config.AutoLayoutConifg


/**
 * Created by gongjianghua on 16/7/5.
 */

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        LogUtil.init(BuildConfig.isDebug)
        AutoLayoutConifg.getInstance().init(this);
        AutoLayoutConifg.getInstance().useDeviceSize();
    }

    companion object{
        var bleController:BleServiceController ?=null
    }
}
