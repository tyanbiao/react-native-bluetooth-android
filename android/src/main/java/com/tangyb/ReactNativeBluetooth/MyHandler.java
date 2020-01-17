package com.tangyb.ReactNativeBluetooth;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.tangyb.ReactNativeBluetooth.utils.Utility;



public class MyHandler extends Handler {
    private Promise connectPromise;
    private Promise writePromise;
    private DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;

    MyHandler(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Constants.MESSAGE_CONNECT_OK:
                handleConnect(true);
                Log.i(BluetoothModule.TAG, "连接成功");
                break;

            case Constants.MESSAGE_CONNECT_FAIL:
                handleConnect(false);
                Log.i(BluetoothModule.TAG, "连接失败");
                break;

            case Constants.MESSAGE_CONNECT_LOST:
                Log.i(BluetoothModule.TAG, "连接丢失");
                onConnectionLost();
                break;
            case Constants.MESSAGE_WRITE_FAIL:
                Log.e(BluetoothModule.TAG, "写入失败");
                handleWrite(false);
                break;
            case Constants.MESSAGE_READ: {
                byte[] buffer = (byte[]) msg.obj;
//                Log.i(BluetoothModule.TAG, "收到数据" + buffer.length);
                break;
            }
            case Constants.MESSAGE_WRITE_OK:
//                Log.i(BluetoothModule.TAG, "写入成功");
                handleWrite(true);
                break;
            case Constants.MESSAGE_READ_COMMAND: {
                byte[] buffer = (byte[]) msg.obj;
//                Log.i(BluetoothModule.TAG, "读取指令"+ Utility.toHexString1(buffer));
                onCommandReceived(buffer);
                break;
            }
            case Constants.MESSAGE_READ_DATA: {
                byte[] buffer = (byte[]) msg.obj;
//                Log.i(BluetoothModule.TAG, "读取数据" + Utility.toHexString1(buffer));
                onDataReceived(buffer);
                break;
            }

        }
    }

    public void setConnectPromise(Promise connectPromise) {
        this.connectPromise = connectPromise;
    }

    public void setWritePromise(Promise writePromise) {
        this.writePromise = writePromise;
    }

    private void handleConnect(boolean state) {
        if (connectPromise == null) return;
        if (state) {
            connectPromise.resolve(state);
        } else {
            connectPromise.reject("1001", "连接失败");
        }
        connectPromise = null;
    }

    private void handleWrite(boolean state) {
        if (writePromise == null) return;
        if (state) {
            writePromise.resolve(true);
        } else {
            writePromise.reject("1002", "写入失败");
        }
        writePromise = null;
    }

    public void setEventEmitter(DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter) {
        this.eventEmitter = eventEmitter;
    }

    private void onConnectionLost() {
        eventEmitter.emit("onConnectionLost", "连接丢失");
    }

    private void onCommandReceived(byte[] resBuffer) {
        if (eventEmitter == null) return;

        if (resBuffer[1] == 0x04 && resBuffer[2] == 0x14) {
            motionDataHandler(resBuffer);
            return;
        }
        WritableArray arr = new WritableNativeArray();
        for (int i = 0; i < resBuffer.length; i++) {
            arr.pushInt(Utility.byteToInt(resBuffer[i]));
        }
        eventEmitter.emit("onCommandReceived", arr);
    }

    private void motionDataHandler(byte[] buffer) {
        WritableArray arr = new WritableNativeArray();
        int index = 0;
        while (index < buffer.length) {
            if (index < 3) {
                arr.pushInt(Utility.byteToInt(buffer[index]));
            }
            if (index == 3) {
                arr.pushInt(8);
            }
            if (index == buffer.length - 1) {
                arr.pushInt(Utility.byteToInt(buffer[index]));
            }
            if (index > 3 && index < buffer.length - 1) {
                arr.pushInt(twoByteToInt(buffer[index], buffer[index + 1]));
                index += 2;
                continue;
            }
            index ++;
        }
        eventEmitter.emit("onCommandReceived", arr);
    }

    private void onDataReceived(byte[] resBuffer) {
        if (eventEmitter == null) return;
        if (resBuffer.length < resBuffer[1] + 2) return;
        WritableArray arr = new WritableNativeArray();
        int index = 5;
        byte[] imuArr = new byte[8];

        while (index < resBuffer.length - 3) {
            arr.pushInt(Utility.byteToInt(resBuffer[index]));
            index++;
            System.arraycopy(resBuffer, index, imuArr, 0, 8);
            imuHandler(imuArr, arr);
            index+= 8;
        }
        eventEmitter.emit("onDataReceived", arr);
    }

    private void imuHandler (byte[] imudata, WritableArray arr) {
        arr.pushInt(twoByteToInt(imudata[0], imudata[1]));
        arr.pushInt(twoByteToInt(imudata[2], imudata[3]));
        arr.pushInt(twoByteToInt(imudata[4], imudata[5]));
        arr.pushInt(twoByteToInt(imudata[6], imudata[7]));
    }

    private short twoByteToInt(byte a, byte b) {
        // 小端模式，低字节在前
        return (short)(a & 0xFF | (b & 0xFF) << 8);
    }
}
