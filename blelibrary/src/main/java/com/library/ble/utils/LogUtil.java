package com.library.ble.utils;

import android.util.Log;

/**
 * Created by baixiaokang on 16/4/28.
 */
public class LogUtil {

    private static boolean isDEBUG=false;
    public static void init(boolean debug){
        isDEBUG=debug;
    }

    public static void d(String tag, String data) {
        if (!isDEBUG) {
            return;
        }
        Log.d(tag, data);
    }
    public static void d(String data) {
        if (!isDEBUG) {
            return;
        }
        Log.d("debug", data);
    }
}
