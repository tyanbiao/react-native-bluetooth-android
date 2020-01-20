import React from 'react'
import { View, Text, StyleSheet } from 'react-native'

const Component = (props: any) => {
    return (
        <View style={styles.container}>
            <Text>Hello world</Text>
        </View>
    )
}
const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#3467ef'
    }
})
export default Component