export default {
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