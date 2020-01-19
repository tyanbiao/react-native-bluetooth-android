export declare const openBluetoothAdapter: () => Promise<boolean>;
export declare const closeBluetoothAdapter: () => Promise<boolean>;
export declare const startDevicesDiscovery: () => Promise<boolean>;
export declare const stopDevicesDiscovery: () => Promise<boolean>;
export declare const listBoundDdevices: () => Promise<{
    name: string;
    address: string;
}>;
export declare const createConnection: (address: string) => Promise<boolean>;
export declare const closeConnection: () => Promise<boolean>;
export declare const writeBuffer: (data: ArrayBuffer) => Promise<boolean>;
declare type Handler = (data: ArrayBuffer) => void;
export declare const onDataReceived: (handler: Handler) => void;
declare type Handler2 = (device: {
    name: string;
    address: string;
}) => void;
export declare const onDeviceFound: (handler: Handler2) => void;
export declare const onConnectionLost: (handler: () => void) => void;
export {};
