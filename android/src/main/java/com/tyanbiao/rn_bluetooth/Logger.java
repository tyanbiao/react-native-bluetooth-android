package com.tyanbiao.rn_bluetooth;

import android.util.Log;

public class Logger {
    static private int level = Log.DEBUG;
    static public void setLevel(int level) {
        Logger.level = level;
    }
    static public int getLevel() {
        return level;
    }

    static public void d (String TAG, String msg) {
        if (level <= Log.DEBUG) {
            Log.d(TAG, msg);
        }
    }
    static public void i(String TAG, String msg) {
        if (level <= Log.INFO) {
            Log.i(TAG, msg);
        }
    }
    static public void w(String TAG, String msg) {
        if (level <= Log.WARN) Log.w(TAG, msg);
    }
    static public void e(String TAG, String msg) {
        if (level <= Log.ERROR) Log.e(TAG, msg);
    }
}
