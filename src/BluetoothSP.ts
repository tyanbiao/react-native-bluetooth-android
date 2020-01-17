import { NativeModules, NativeEventEmitter } from "react-native"
const { BluetoothModule } = NativeModules;
const eventEmitter = new NativeEventEmitter(BluetoothModule);
import Base64 from '../utils/Base64';
import { number2Hex } from '../utils/util';

export const test = async () => {
    const test: string = await BluetoothModule.test();
    console.log(test);
}

export const init = async () => {
    const res: boolean = await BluetoothModule.init();
    console.log(res);
}

export const getBoundDevices = async () => {
    const res = await BluetoothModule.getBoundDevices();
    return res;
}

export const startDiscovery = async() => {
    BluetoothModule.startDiscovery();
}

export const stopDiscovery = async() => {
    BluetoothModule.stopDiscovery();
}

export const onDeviceFound = (linstener: any): void => {
    eventEmitter.addListener('onDeviceFound', linstener)
}

export const onDiscoveryFinshed = (linstener: any): void => {
    eventEmitter.addListener("onDiscoveryFinshed", linstener)
}

export const onConnectionLost = (linstener: any): void => {
    eventEmitter.addListener("onConnectionLost", linstener)
}

export const connect = async (address: string) => {
    try {
        const res = await BluetoothModule.connect(address);
        console.log("连接成功");
        return res;
    } catch (e) {
        console.error("连接失败")
        console.error(e);
        return false;
    }
}

export const disconnect = async () => {
    BluetoothModule.disconnect()
}

export const write = async (buffer: number[]) => {
    // console.log(Arr.map(value => number2Hex(value)).join(" "));

    let res= false;
    let uintArr = new Uint8Array(buffer);
    let Arr = Array.from(uintArr);
    try {
        const b64encoded = Base64.btoa(String.fromCharCode.apply(null, buffer));
        res = await BluetoothModule.write(b64encoded);
    } catch (e) {
        console.error(e);
    }
    return res;
}

export const setTaskMode = (mode: 0 | 1) => {
    BluetoothModule.setTaskMode(mode);
}

export const startTask = () => {
    BluetoothModule.setTaskState(true);
}
export const stopTask = () => {
    BluetoothModule.setTaskState(false);
}
export const setFileName = (filename: string) => {
    BluetoothModule.setFileName(filename);
}

export const onDataReceived = (linstener: any): void => {
    eventEmitter.addListener("onDataReceived", linstener)
}

export const onCommandReceived = (linstener: any) => {
    eventEmitter.addListener("onCommandReceived", linstener)
}

export const onDisconnected = (linstener: any) => {
    eventEmitter.addListener("onDisconnected", linstener)
}
