
package com.tyanbiao;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;


import java.util.Map;

public class RNBluetoothModule extends ReactContextBaseJavaModule {
    static public final String TAG = "react-native-bluetoth-android";
    private final ReactApplicationContext reactContext;

    public RNBluetoothModule(ReactApplicationContext reactContext) {
      super(reactContext);
      this.reactContext = reactContext;
    }

    @Override
    public String getName() {
      return "RNBluetooth";
    }
    @ReactMethod
    public void openBluetoothAdapter() {}

    @ReactMethod
    public void closeBluetoothAdapter() {}

    @ReactMethod
    public void startDevicesDiscovery() {}

    @ReactMethod
    public void stopDevicesDiscovery() {}

    @ReactMethod
    public void listBoundDdevices() {}

    @ReactMethod
    public void createConnection() {}

    @ReactMethod
    public void closeConnection() {}

    @ReactMethod
    public void writeBuffer() {}

    private void eventEmit(String eventName, Object payload) {
        reactContext.getJSModule(RCTDeviceEventEmitter.class).emit(eventName, payload);
    }

}