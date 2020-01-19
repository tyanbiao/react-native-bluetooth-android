
# react-native-react-native-bluetooth

## Getting started

`$ npm install react-native-react-native-bluetooth --save`

### Mostly automatic installation

`$ react-native link react-native-react-native-bluetooth`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNReactNativeBluetoothPackage;` to the imports at the top of the file
  - Add `new RNReactNativeBluetoothPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-react-native-bluetooth'
  	project(':react-native-react-native-bluetooth').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-react-native-bluetooth/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-react-native-bluetooth')
  	```


## Usage
```javascript
import RNReactNativeBluetooth from 'react-native-react-native-bluetooth';

// TODO: What to do with the module?
RNReactNativeBluetooth;
```
  