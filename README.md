
# react-native-bluetooth-android

ReactNative 蓝牙库，适用于安卓平台

## 快速开始

使用`npm`

`$ npm install react-native-bluetooth-android --save`  

使用 `yarn`  

`$ yarn add react-native-bluetooth-android`

### 自动链接

`$ react-native link react-native-bluetooth-android`

### 手动安装


#### Android

1. 打开 `android/app/src/main/java/[...]/MainApplication.java`
  - 引入 `import com.tyanbiao.bluetooth.RNBluetoothPackage;`
  - 在 `list()` 方法中添加 `packages.add(new RNBluetoothPackage());` 
2.  在`android/settings.gradle`文件中加入:
  	```
	include ':react-native-bluetooth-android'
	project(':react-native-bluetooth-android').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-bluetooth-android/android')
  	```
3. 在`android/app/build.gradle`的 dependencies 中插入:
  	```
	implementation project(':react-native-bluetooth-android')
  	```
## 示例
1. `git clone https://github.com/tyanbiao/react-native-bluetooth-android.git`
2. `cd react-native-bluetooth-android/example`
3. `npm i`
4. `react-native run-android`

## 使用
全部导入
```javascript
import * as RNBluetooth from 'react-native-bluetooth-android'
```
单个导入
```typescript
import { openBluetoothAdapter } from 'react-native-bluetooth-android'
```
### 初始化

`const res = await openBluetoothAdapter()`

### 获取已绑定设备
` const devices = await listDevices() `

返回设备列表，类型： `Array.<Object>`， devices结构：
```javascript
{
	name: string
	address: string
}
```



### 搜索设备
`startDevicesDiscovery() `

### 停止搜索
`stopDevicesDiscovery()`

### 监听发现设备事件
```javascript
onDeviceFound((device) => {
	console.log(device.name)
	console.log(device.address)
})
```

### 创建连接
`const res = createConnection(address)`

### 断开连接
`closeConnection()`

### 监听连接断开事件
```javscript
onConnectionLost(() => {
	console.log('连接已断开')
})
```
### 发送数据
- 发送二进制数据
```javascript
const bufferView = new Uint8Array(10)
writeBuffer(bufferView.buffer)
```
- 发送ascii字符串
```javascript
const buffer = utils.stringToBuffer('hello world')
writeBuffer(buffer)

```
- 发送hex字符串
```javascript
const buffer = utils.hexToBuffer('550d0aeeff')
writeBuffer(buffer)
```

### 监听数据接收事件
```javascript
const handler = (buffer) => {
	const bufferView = new Uint8Array(buffer)
	// do something
}
onDataReceived(handler)
```
接收到的数据类型为ArrayBuffer，可以根据实际需求做处理，`utils`对象内置了ArrayBuffer转换为ascii和hex编码的方法
```javascript
import { utils } from 'react-native-bluetooth-android'

let hexStr = utils.bufferToHex(arrayBuffer)
let asciiStr = utils.bufferToString(arrayBuffer)
```



## API 列表
- [`openBluetoothAdapter`](#open-bluetooth-adapter)
- [`closeBluetoothAdapter`](#close-bluetooth-adapter)
- [`startDevicesDiscovery`](#list-devices)
- [`stopDevicesDiscovery`](#stop-devices-discovery)
- [`listDevices`](#list-devices)
- [`createConnection`](#create-connection)
- [`closeConnection`](#close-connection)
- [`writeBuffer`](#write-buffer)
- [`onDataReceived`](#on-data-received)
- [`onDeviceFound`](#on-device-found)
- [`onConnectionLost`](#on-connection-lost)
- [`utils`](#utils)

### <span id="open-bluetooth-adapter">openBluetoothAdapter</span>

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

### <span id="close-bluetooth-adapter">closeBluetoothAdapter</span>

### <span id="start-devices-discovery">startDevicesDiscovery</span>
### <span id="stop-devices-discovery">stopDevicesDiscovery</span>
### <span id="list-devices">listDevices</span>
### <span id="create-connection">createConnection</span>
### <span id="close-connection">closeConnection</span>
### <span id="write-buffer">writeBuffer</span>
### <span id="on-data-received">onDataReceived</span>
### <span id="on-device-found">onDeviceFound</span>
### <span id="on-connection-lost">onConnectionLost</span>
### <span id="utils">utils</span>

 - `bufferToString`
 - `stringToBuffer`
 - `bufferToHex`
 - `hexToBuffer`
