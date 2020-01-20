package com.tyanbiao.rn_bluetooth;

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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

import java.util.Set;


public class RNBluetoothModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    static public final String TAG = "react-native-bluetoth-android";
    private final ReactApplicationContext reactContext;
    private BluetoothService bluetoothService;
    private MyHandler handler = new MyHandler(Looper.getMainLooper());
    private BluetoothAdapter bluetoothAdapter;
    public final static int REQUEST_ENABLE_BT = 1;
    public static final int REQUEST_EXTERNAL_STORAGE = 2;
    private final int PERMISSION_REQUEST_COARSE_LOCATION = 0xb01;
    private final int REQUEST_PERMISSION_CODE = 0x02;
    private ActivityEventListener mActivityEventListener = new MyActivityEventListener();
    private final MyBroadcastReceiver receiver;


    public RNBluetoothModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(mActivityEventListener);
        reactContext.addLifecycleEventListener(this);
        receiver = new MyBroadcastReceiver();
        receiver.setReactApplicationContext(reactContext);
    }

    @Override
    public String getName() {
      return "RNBluetoothModule";
    }
    @ReactMethod
    public void openBluetoothAdapter(Promise promise) {
        handler.setEventEmitter(reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class));
        bluetoothService = new BluetoothService(handler);
        bluetoothAdapter = bluetoothService.getBluetoothAdapter();

        if (bluetoothService.getBluetoothAdapter() == null) {
            promise.reject("1001", "设备不支持蓝牙");
            return;
        }
        if (!bluetoothService.getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.reactContext.getCurrentActivity().startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            promise.reject("1001", "蓝牙未打开");
            return;
        }

        // 注册广播监听
        // 开始发现
        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        reactContext.registerReceiver(receiver, filterStart);

        // 发现设备
        IntentFilter filterFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        reactContext.registerReceiver(receiver, filterFound);

        // 发现结束
        IntentFilter filterEnd = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        reactContext.registerReceiver(receiver, filterEnd);
        promise.resolve(true);
    }

    @ReactMethod
    public void closeBluetoothAdapter(Promise promise) {
       promise.resolve(true);
    }

    @ReactMethod
    public void startDevicesDiscovery(Promise promise) {
        // 动态申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(reactContext.getCurrentActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(reactContext.getCurrentActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            } else {
                Logger.i(TAG, "已申请蓝牙权限");
            }
        }

        boolean res = bluetoothService.getBluetoothAdapter().startDiscovery();
        promise.resolve(true);
    }

    @ReactMethod
    public void stopDevicesDiscovery(Promise promise) {
        bluetoothService.getBluetoothAdapter().cancelDiscovery();
        promise.resolve(true);
    }

    @ReactMethod
    public void listBoundDevices(Promise promise) {
        BluetoothAdapter bluetoothAdapter = bluetoothService.getBluetoothAdapter();
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
    public void createConnection(String address, Promise promise) {
        synchronized (this) {
            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                if (device == null) {
                    throw new Exception("Cannot find device:" + address);
                }
                bluetoothService.connect(device);
                handler.setConnectPromise(promise);
            } catch (Exception e) {
                Logger.e(TAG, "连接错误");
                promise.reject("1002", e);
            }
        }
    }

    @ReactMethod
    public void closeConnection(Promise promise) {
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
    public void writeBuffer(String b64, Promise promise) {
        byte[] bytes = Base64.decode(b64, Base64.NO_WRAP);
        promise.resolve(bluetoothService.write(bytes));
    }

    private void eventEmit(String eventName, Object payload) {
        reactContext.getJSModule(RCTDeviceEventEmitter.class).emit(eventName, payload);
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
        reactContext.unregisterReceiver(receiver);
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
                    Logger.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
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
            case RNBluetoothModule.REQUEST_ENABLE_BT:
                break;
            case RNBluetoothModule.REQUEST_EXTERNAL_STORAGE:
            default:
                break;
        }
    }
}

// 监听广播
class MyBroadcastReceiver extends BroadcastReceiver {
    ReactApplicationContext reactApplicationContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device == null) return;

            WritableMap map = new WritableNativeMap();
            map.putString("name", device.getName());
            map.putString("address", device.getAddress());

            eventEmit(Constants.onDeviceFound, map);
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            Logger.i(RNBluetoothModule.TAG, "discovery finished");
            eventEmit("onDiscoveryFinshed", true);
        }

        if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            Logger.i(RNBluetoothModule.TAG, "扫描开始");
        }
        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Logger.d("aaa", device.getName() + " ACTION_ACL_CONNECTED");
        }
        // 连接断开
        if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Logger.d(RNBluetoothModule.TAG, " 连接断开");
            eventEmit("onDisconnected", true);
        }
    }

    public void eventEmit(String eventName, Object payload) {
        if (reactApplicationContext == null) return;
        reactApplicationContext.getJSModule(RCTDeviceEventEmitter.class).emit(eventName, payload);
    }
    public void setReactApplicationContext(ReactApplicationContext reactContext) {
        this.reactApplicationContext = reactContext;
    }
}