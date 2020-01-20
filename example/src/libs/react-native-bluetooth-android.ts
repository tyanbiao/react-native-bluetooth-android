import Base64 from 'base64-js'
import { NativeModules, NativeEventEmitter } from "react-native"
const { RNBluetoothModule } = NativeModules
const eventEmitter = new NativeEventEmitter(RNBluetoothModule)

export const openBluetoothAdapter = async () => {
    const res: boolean = await RNBluetoothModule.openBluetoothAdapter()
    return res
}

export const closeBluetoothAdapter = async () => {
    const res: boolean = await RNBluetoothModule.openBluetoothAdapter()
    return res
}

export const startDevicesDiscovery = async () => {
    const res: boolean = await RNBluetoothModule.startDevicesDiscovery()
    return res
}

export const stopDevicesDiscovery = async () => {
    const res: boolean = await RNBluetoothModule.stopDevicesDiscovery()
    return res
}

export const listDevices = async () => {
    const res: { name: string, address: string} = await RNBluetoothModule.listBoundDevices()
    return res
}

export const createConnection = async (address: string) => {
    const res: boolean = await RNBluetoothModule.createConnection(address)
    return res
}
export const closeConnection = async () => {
    const res: boolean = await RNBluetoothModule.closeConnection()
    return res
}
export const writeBuffer = async (data: ArrayBuffer) => {
    const uint8Arr = new Uint8Array(data)
    const b64 = Base64.fromByteArray(uint8Arr)
    const res: boolean = await RNBluetoothModule.writeBuffer(b64)
    return res
}

// events
type Handler = (data: ArrayBuffer) => void
export const onDataReceived = (handler: Handler) => {
    let linstener = (b64: string) => {
        const data = Base64.toByteArray(b64)
        handler(data.buffer)
    }
    eventEmitter.addListener("onDataReceived", linstener)
}

type Handler2 = (device: {name: string, address: string}) => void
export const onDeviceFound = (handler: Handler2) => {
    eventEmitter.addListener("onDeviceFound", handler)
}

export const onConnectionLost = (handler: ()=>void): void => {
    eventEmitter.addListener("onConnectionLost", handler)
}

export const utils = {
    bufferToString: (buffer: ArrayBuffer) => {
        return String.fromCharCode.apply(null, new Uint8Array(buffer))
    },
    stringToBuffer: (str: string) => {
        const buf = new ArrayBuffer(str.length)
        const bufView = new Uint8Array(buf)
        for (let i=0; i < str.length; i++) {
            bufView[i] = str.charCodeAt(i)
        }
        return buf
    },
    bufferToHex: (buffer: ArrayBuffer) => {
        const bufferView = new Uint8Array(buffer)
        let hexStr = ''
        for (let i = 0; i < bufferView.length; i++) {
            let temp = '00' + bufferView[i].toString(16)
            hexStr += temp.substring(temp.length - 2)
        }
        return hexStr
    },
    hexToBuffer: (hex: string) => {
        let arr = []
        for (let i = 0; i < Math.floor(hex.length / 2); i++) {
            let str = hex[i*2] + hex[i*2+1]
            arr[i] = parseInt(str, 16)
        }
        if (hex.length%2 === 1) {
            arr.push(parseInt(hex[hex.length-1], 16))
        }
        let bufferView = new Uint8Array(arr)
        return bufferView.buffer
    }
}
