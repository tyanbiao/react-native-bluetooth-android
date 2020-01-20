import React from 'react'
import { View, Text, StyleSheet, TouchableOpacity }  from 'react-native'
import * as Bluetooth from './libs/react-native-bluetooth-android'
import Base64 from 'base64-js'

interface Props {
    [name: string]: any
}
interface State {
    [name: string]: any
}
class Component extends React.Component<Props, State> {
    constructor(props: Props) {
        super(props)
        this.state = {

        }
    }
    async componentDidMount() {
        let res = await Bluetooth.openBluetoothAdapter()
        console.log(res)
        let devices = await Bluetooth.listDevices()
        console.log(devices)
        Bluetooth.onDeviceFound(this.onDeviceFound)
        Bluetooth.onDataReceived(this.dataHandler)
        console.log('c2Zkc2ZkZ2ZkaGdm'.length)

    }
    onDeviceFound = (device: any) => {
        console.log(device)
    }
    connect = async () => {
        const res = await Bluetooth.createConnection('A4:C3:F0:BE:9B:EF')
        console.log(res)
    }
    dataHandler = (data: ArrayBuffer) => {
        const arr = new Uint8Array(data)
        console.log(arr)
    }
    write = () => {
        let data = "Hello world"
        let bufView = new Uint8Array([0x00, 0x01, 0x02, 0x03])

        Bluetooth.writeBuffer(bufView.buffer)
    }
    render() {
        return (
            <View style={styles.container}>
                <TouchableOpacity style={styles.button} onPress={Bluetooth.startDevicesDiscovery}>
                    <Text>开始扫描</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.button} onPress={Bluetooth.stopDevicesDiscovery}>
                    <Text>停止扫描</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.button} onPress={this.connect}>
                    <Text>连接</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.button} onPress={this.write}>
                    <Text>发送</Text>
                </TouchableOpacity>
            </View>
        )
    }
}

export default Component

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#ccc',
        alignItems: 'center',
        paddingTop: 50
    },
    button: {
        maxWidth: 100,
        padding: 10,
        backgroundColor: 'green',
        marginBottom: 10,
        marginTop: 10
    }
})