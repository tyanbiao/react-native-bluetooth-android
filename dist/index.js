"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
var __generator = (this && this.__generator) || function (thisArg, body) {
    var _ = { label: 0, sent: function() { if (t[0] & 1) throw t[1]; return t[1]; }, trys: [], ops: [] }, f, y, t, g;
    return g = { next: verb(0), "throw": verb(1), "return": verb(2) }, typeof Symbol === "function" && (g[Symbol.iterator] = function() { return this; }), g;
    function verb(n) { return function (v) { return step([n, v]); }; }
    function step(op) {
        if (f) throw new TypeError("Generator is already executing.");
        while (_) try {
            if (f = 1, y && (t = op[0] & 2 ? y["return"] : op[0] ? y["throw"] || ((t = y["return"]) && t.call(y), 0) : y.next) && !(t = t.call(y, op[1])).done) return t;
            if (y = 0, t) op = [op[0] & 2, t.value];
            switch (op[0]) {
                case 0: case 1: t = op; break;
                case 4: _.label++; return { value: op[1], done: false };
                case 5: _.label++; y = op[1]; op = [0]; continue;
                case 7: op = _.ops.pop(); _.trys.pop(); continue;
                default:
                    if (!(t = _.trys, t = t.length > 0 && t[t.length - 1]) && (op[0] === 6 || op[0] === 2)) { _ = 0; continue; }
                    if (op[0] === 3 && (!t || (op[1] > t[0] && op[1] < t[3]))) { _.label = op[1]; break; }
                    if (op[0] === 6 && _.label < t[1]) { _.label = t[1]; t = op; break; }
                    if (t && _.label < t[2]) { _.label = t[2]; _.ops.push(op); break; }
                    if (t[2]) _.ops.pop();
                    _.trys.pop(); continue;
            }
            op = body.call(thisArg, _);
        } catch (e) { op = [6, e]; y = 0; } finally { f = t = 0; }
        if (op[0] & 5) throw op[1]; return { value: op[0] ? op[1] : void 0, done: true };
    }
};
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
var _this = this;
Object.defineProperty(exports, "__esModule", { value: true });
var base64_js_1 = __importDefault(require("base64-js"));
var react_native_1 = require("react-native");
var RNBluetoothModule = react_native_1.NativeModules.RNBluetoothModule;
var eventEmitter = new react_native_1.NativeEventEmitter(RNBluetoothModule);
exports.openBluetoothAdapter = function () { return __awaiter(_this, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, RNBluetoothModule.openBluetoothAdapter()];
            case 1:
                res = _a.sent();
                return [2 /*return*/, res];
        }
    });
}); };
exports.closeBluetoothAdapter = function () { return __awaiter(_this, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, RNBluetoothModule.openBluetoothAdapter()];
            case 1:
                res = _a.sent();
                return [2 /*return*/, res];
        }
    });
}); };
exports.startDevicesDiscovery = function () { return __awaiter(_this, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, RNBluetoothModule.startDevicesDiscovery()];
            case 1:
                res = _a.sent();
                return [2 /*return*/, res];
        }
    });
}); };
exports.stopDevicesDiscovery = function () { return __awaiter(_this, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, RNBluetoothModule.stopDevicesDiscovery()];
            case 1:
                res = _a.sent();
                return [2 /*return*/, res];
        }
    });
}); };
exports.listDevices = function () { return __awaiter(_this, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, RNBluetoothModule.listBoundDevices()];
            case 1:
                res = _a.sent();
                return [2 /*return*/, res];
        }
    });
}); };
exports.createConnection = function (address) { return __awaiter(_this, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, RNBluetoothModule.createConnection(address)];
            case 1:
                res = _a.sent();
                return [2 /*return*/, res];
        }
    });
}); };
exports.closeConnection = function () { return __awaiter(_this, void 0, void 0, function () {
    var res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0: return [4 /*yield*/, RNBluetoothModule.closeConnection()];
            case 1:
                res = _a.sent();
                return [2 /*return*/, res];
        }
    });
}); };
exports.writeBuffer = function (data) { return __awaiter(_this, void 0, void 0, function () {
    var uint8Arr, b64, res;
    return __generator(this, function (_a) {
        switch (_a.label) {
            case 0:
                uint8Arr = new Uint8Array(data);
                b64 = base64_js_1.default.fromByteArray(uint8Arr);
                return [4 /*yield*/, RNBluetoothModule.writeBuffer(b64)];
            case 1:
                res = _a.sent();
                return [2 /*return*/, res];
        }
    });
}); };
exports.onDataReceived = function (handler) {
    var linstener = function (b64) {
        var data = base64_js_1.default.toByteArray(b64);
        handler(data.buffer);
    };
    eventEmitter.addListener("onDataReceived", linstener);
};
exports.onDeviceFound = function (handler) {
    eventEmitter.addListener("onDeviceFound", handler);
};
exports.onConnectionLost = function (handler) {
    eventEmitter.addListener("onConnectionLost", handler);
};
exports.utils = {
    bufferToString: function (buffer) {
        var arr = new Uint8Array(buffer);
        return String.fromCharCode.apply(null, arr);
    },
    stringToBuffer: function (str) {
        var buf = new ArrayBuffer(str.length);
        var bufView = new Uint8Array(buf);
        for (var i = 0; i < str.length; i++) {
            bufView[i] = str.charCodeAt(i);
        }
        return buf;
    },
    bufferToHex: function (buffer) {
        var bufferView = new Uint8Array(buffer);
        var hexStr = '';
        for (var i = 0; i < bufferView.length; i++) {
            var temp = '00' + bufferView[i].toString(16);
            hexStr += temp.substring(temp.length - 2);
        }
        return hexStr;
    },
    hexToBuffer: function (hex) {
        var arr = [];
        for (var i = 0; i < Math.floor(hex.length / 2); i++) {
            var str = hex[i * 2] + hex[i * 2 + 1];
            arr[i] = parseInt(str, 16);
        }
        if (hex.length % 2 === 1) {
            arr.push(parseInt(hex[hex.length - 1], 16));
        }
        var bufferView = new Uint8Array(arr);
        return bufferView.buffer;
    }
};
