package com.tangyb.ReactNativeBluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.tangyb.ReactNativeBluetooth.utils.FileUtil;

import java.util.Set;


public class BluetoothModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    public static final String TAG = "tangyb:BluetoothModule";
    private ReactApplicationContext context;
    private RCTDeviceEventEmitter eventEmitter;
    private ActivityEventListener mActivityEventListener = new MyActivityEventListener();
    public final static int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_EXTERNAL_STORAGE = 2;
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 0xb01;
    private final int REQUEST_PERMISSION_CODE = 0x02;
    private final MyBroadcastReceiver receiver;
    private BluetoothService bluetoothService;
    private BluetoothAdapter bluetoothAdapter;
    private FileUtil fileUtil;
    private MyHandler handler = new MyHandler(Looper.getMainLooper());

    public BluetoothModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        context.addActivityEventListener(mActivityEventListener);
        context.addLifecycleEventListener(this);
        receiver = new MyBroadcastReceiver();
        fileUtil = new FileUtil(context);
//        verifyStoragePermissions(context.getCurrentActivity());

    }

    @Override
    public String getName() {
        return "BluetoothModule";
    }

    @ReactMethod
    public void test(Promise promise) {
        promise.resolve("react native module test");
    }

    @ReactMethod
    public void init(Promise promise) {
        bluetoothService = new BluetoothService(handler);
        bluetoothService.setFileUtil(fileUtil);
        bluetoothAdapter = bluetoothService.getBluetoothAdapter();

        this.eventEmitter = context.getJSModule(RCTDeviceEventEmitter.class);
        if (bluetoothService.getBluetoothAdapter() == null) {
            promise.reject("1001", "设备不支持蓝牙");
            return;
        }

        if (!bluetoothService.getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.context.getCurrentActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            promise.reject("1001", "蓝牙未打开");
            return;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context.getCurrentActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context.getCurrentActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
            } else {
                Log.i(TAG, "已申请存储权限");
            }
        }

        // 注入事件发送对象
        receiver.setEventEmitter(eventEmitter);
        handler.setEventEmitter(eventEmitter);

        // 注册广播监听
        // 开始发现
        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        context.registerReceiver(receiver, filterStart);

        // 发现设备
        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);

        context.registerReceiver(receiver, filterFound);

        // 发现结束
        IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(receiver, filterEnd);


        promise.resolve(true);
    }

    @ReactMethod
    public void getBoundDevices(Promise promise) {
        BluetoothAdapter bluetoothAdapter = this.bluetoothService.getBluetoothAdapter();
        if (bluetoothAdapter == null) {
            promise.reject("1002", "蓝牙未初始化");
            return;
        }
        WritableArray resList = new WritableNativeArray();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                WritableMap map = new WritableNativeMap();
                map.putString("name", device.getName());
                map.putString("address", device.getAddress());
                resList.pushMap(map);
            }
        }
        promise.resolve(resList);
    }

    @ReactMethod
    public void startDiscovery(Promise promise) {
        // 动态申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context.getCurrentActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context.getCurrentActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            } else {
                Log.i(TAG, "已申请蓝牙权限");
            }
        }

        boolean res = this.bluetoothService.getBluetoothAdapter().startDiscovery();
        promise.resolve(true);
    }

    @ReactMethod
    public void stopDiscovery(Promise promise) {
        bluetoothService.getBluetoothAdapter().cancelDiscovery();
        promise.resolve(true);
    }

    @ReactMethod
    public void connect(String address, Promise promise) {
        synchronized (this) {
            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                if (device == null) {
                    throw new Exception("Cannot find device:" + address);
                }
                bluetoothService.connect(device, true);
                handler.setConnectPromise(promise);
            } catch (Exception e) {
                Log.e(TAG, "连接错误");
                promise.reject("1002", e);
            }
        }
    }

    @ReactMethod
    public void disconnect(Promise promise) {
        synchronized (this) {
            try {
                bluetoothService.stop();
                promise.resolve(true);
            } catch (Exception e) {
                promise.reject("10002", e);
            }
        }

    }

    @ReactMethod
    public void write(String base64Text, Promise promise) {
        byte[] bytes = Base64.decode(base64Text, Base64.DEFAULT);
        boolean res = bluetoothService.write(bytes);
        promise.resolve(res);
    }

    @ReactMethod
    public void setTaskMode(int mode, Promise promise) {
        bluetoothService.setTaskMode(mode);
        promise.resolve(true);
    }

    @ReactMethod
    public void setTaskState(boolean state, Promise promise) {
        bluetoothService.setTaskState(state);
        promise.resolve(true);
    }

    @ReactMethod
    public void setFileName(String fileName, Promise promise) {
        fileUtil.setFileName(fileName);
    }


    // 监听activity生命周期
    @Override
    public void onHostResume() {
        // Activity `onResume`
    }

    @Override
    public void onHostPause() {
        // Activity `onPause`
    }

    @Override
    public void onHostDestroy() {
        context.unregisterReceiver(receiver);
    }

    // 动态权限申请
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }

                break;
            case REQUEST_PERMISSION_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
                }
                break;
            default:
                break;
        }
    }
}

// 处理 startActivityForResult回调
class MyActivityEventListener extends BaseActivityEventListener {
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case BluetoothModule.REQUEST_ENABLE_BT:
                break;
            case BluetoothModule.REQUEST_EXTERNAL_STORAGE:
            default:
                break;
        }
    }
}

// 监听广播
class MyBroadcastReceiver extends BroadcastReceiver {
    private RCTDeviceEventEmitter eventEmitter;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) return;

            WritableMap map = new WritableNativeMap();
            map.putString("name", device.getName());
            map.putString("address", device.getAddress());
            if (eventEmitter == null) {
                Log.e(BluetoothModule.TAG, "eventEmitter is null");
                return;
            }
            eventEmitter.emit("onDeviceFound", map);
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Log.i(BluetoothModule.TAG, "discovery finished");
            eventEmitter.emit("onDiscoveryFinshed", true);
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            Log.i(BluetoothModule.TAG, "扫描开始");
        }
        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d("aaa", device.getName() + " ACTION_ACL_CONNECTED");
        }
        // 连接断开
        if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(BluetoothModule.TAG, " 连接断开");
            eventEmitter.emit("onDisconnected", true);
        }
    }

    public void setEventEmitter(RCTDeviceEventEmitter eventEmitter) {
        this.eventEmitter = eventEmitter;
    }
}