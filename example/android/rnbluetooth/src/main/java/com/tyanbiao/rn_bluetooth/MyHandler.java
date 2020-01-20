package com.tyanbiao.rn_bluetooth;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;

import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;


public class MyHandler extends Handler {
    Promise connectPromise;
    DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;

    MyHandler(Looper looper) {
        super(looper);
    }
    public void setConnectPromise(Promise connectPromise) {
        this.connectPromise = connectPromise;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.MESSAGE_CONNECT_OK:
                connectPromise.resolve(true);
                Logger.i(RNBluetoothModule.TAG, "连接成功");
                break;

            case Constants.MESSAGE_CONNECT_FAIL:
                connectPromise.reject("1001", "连接失败");
                Logger.i(RNBluetoothModule.TAG, "连接失败");
                break;

            case Constants.MESSAGE_CONNECTION_LOST:
                Logger.i(RNBluetoothModule.TAG, "连接丢失");
                eventEmitter.emit(Constants.onConnectionLoast, "连接丢失");
                break;
            case Constants.MESSAGE_DATA_RECEIVED:
                byte[] buffer = (byte[]) msg.obj;
                String b64 = Base64.encodeToString(buffer, Base64.DEFAULT);
                eventEmitter.emit(Constants.onDataReceived, b64);
                break;
            case Constants.MESSAGE_ERROR:
                Exception e = (Exception) msg.obj;
                Logger.e(RNBluetoothModule.TAG, e.toString());
                break;

        }
    }
    public void setEventEmitter(DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter) {
        this.eventEmitter = eventEmitter;
    }
}
