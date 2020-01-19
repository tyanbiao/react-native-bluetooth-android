
# react-native-bluetooth-android

ReactNative 蓝牙库，适用于安卓平台

## 快速开始
`$ npm install react-native-bluetooth-android --save`  

使用 `yarn`  

`$ yarn add react-native-bluetooth-android`

### 自动链接

`$ react-native link react-native-bluetooth-android`

### 手动安装


#### Android

1. 打开 `android/app/src/main/java/[...]/MainActivity.java`
  -  `import com.reactlibrary.RNBluetoothPackage;` to the imports at the top of the file
  - Add `new RNBluetoothPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-react-native-bluetooth'
  	project(':react-native-react-native-bluetooth').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-react-native-bluetooth/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-react-native-bluetooth')
  	```


## 使用
```javascript
import * as RNBluetooth from 'react-native-bluetooth-android';


```

## API 列表
- `openBluetoothAdapter`
- `closeBluetoothAdapter`
- `startDevicesDiscovery`
- `stopDevicesDiscovery`
- `listDevices`
- `createConnection`
- `closeConnection`
- `writeBuffer`
- `onDataReceived`
- `onDeviceFound`
- `onConnectionLost`

### openBluetoothAdapter

使用`async`, `await`
```javascript
	try {
		const res = await openBluetoothAdapter()
		console.log(res)
	} catch (e) {
		console.error(e)
	}
```
使用`promise`
```javascript
openBluetoothAdapter().then(result => {
	console.log(res)
}).catch(e => {
	console.error(e)
})
```

### closeBluetoothAdapter

### startDevicesDiscovery
### stopDevicesDiscovery
### listDevices
### createConnection
### closeConnection
### writeBuffer
### onDataReceived
### onDeviceFound
### onConnectionLost