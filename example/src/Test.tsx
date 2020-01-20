import React from 'react'
import { View, Text, StyleSheet, TouchableOpacity }  from 'react-native'
import * as Bluetooth from './libs/react-native-bluetooth-android'

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
    }
    onDeviceFound = (device: any) => {
        console.log(device)
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
                <TouchableOpacity style={styles.button}>
                    <Text>连接</Text>
                </TouchableOpacity>
                <TouchableOpacity style={styles.button}>
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