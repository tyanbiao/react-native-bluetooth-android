package com.tyanbiao;



import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

class BluetoothService {
    // Debugging
    private static final boolean D = true;
    private final String TAG = RNBluetoothModule.TAG;
    // UUIDs
    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
//    private RCTBluetoothSerialModule mModule;
    private String mState;

    private Handler handler;

    // Constants that indicate the current connection state
    private static final String STATE_NONE = "none";       // we're doing nothing
    private static final String STATE_CONNECTING = "connecting"; // now initiating an outgoing connection
    private static final String STATE_CONNECTED = "connected";  // now connected to a remote device


    BluetoothService(Handler mHandler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        handler = mHandler;
    }

    synchronized void connect(BluetoothDevice device) {
        Logger.d(TAG, "connect to: " + device);

        if (mState.equals(STATE_CONNECTING)) {
            cancelConnectThread(); // Cancel any thread attempting to make a connection
        }

        cancelConnectedThread(); // Cancel any thread currently running a connection

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Check whether service is connected to device
     * @return Is connected to device
     */
    boolean isConnected () {
        return getState().equals(STATE_CONNECTED);
    }

    void write(byte[] out) {
        Logger.d(TAG, "Write in service, state is " + STATE_CONNECTED);
        ConnectedThread r; // Create temporary object

        synchronized (this) {
            if (!isConnected()) return;
            r = mConnectedThread;
        }
        r.write(out);
    }

    synchronized void stop() {
        Logger.d(TAG, "stop");

        cancelConnectThread();
        cancelConnectedThread();

        setState(STATE_NONE);
    }


    private synchronized String getState() {
        return mState;
    }

    private synchronized void setState(String state) {
        Logger.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    private synchronized void connectionSuccess(BluetoothSocket socket, BluetoothDevice device) {
        Logger.d(TAG, "connected");

        cancelConnectThread(); // Cancel any thread attempting to make a connection
        cancelConnectedThread(); // Cancel any thread currently running a connection

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
        Message msg = handler.obtainMessage(Constants.MESSAGE_CONNECT_OK);
        handler.sendMessage(msg);
    }


    private void connectionFailed() {
        Message msg = handler.obtainMessage(Constants.MESSAGE_CONNECT_FAIL);
        handler.sendMessage(msg);
        BluetoothService.this.stop(); // Start the service over to restart listening mode
    }

    private void connectionLost() {
        Message msg = handler.obtainMessage(Constants.MESSAGE_CONNECTION_LOST);
        handler.sendMessage(msg);
    }

    private void cancelConnectThread () {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    /**
     * Cancel connected thread
     */
    private void cancelConnectedThread () {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }

    private void onError(Exception e) {
        Message msg = handler.obtainMessage(Constants.MESSAGE_ERROR, e);
        handler.sendMessage(msg);
    }
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID_SPP);
            } catch (Exception e) {
//                mModule.onError(e);
                Logger.e(TAG, "Socket create() failed" + e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Logger.d(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                mmSocket.connect();
            } catch (Exception e) {
                onError(e);

                // Some 4.1 devices have problems, try an alternative way to connect
                // See https://github.com/don/RCTBluetoothSerialModule/issues/89
                try {
                    Logger.i(TAG,"Trying fallback...");
                    mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    mmSocket.connect();
                    Logger.i(TAG,"Connected");
                } catch (Exception e2) {
                    Logger.e(TAG, "Couldn't establish a Bluetooth connection.");
                    onError(e2);
                    try {
                        mmSocket.close();
                    } catch (Exception e3) {
                        Logger.e(TAG, "unable to close() socket during connection failure");
                        onError(e3);
                    }
                    connectionFailed();
                    return;
                }
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }

            connectionSuccess(mmSocket, mmDevice);  // Start the connected thread

        }

        void cancel() {
            try {
                mmSocket.close();
            } catch (Exception e) {
                Logger.e(TAG, "close() of connect socket failed");
                onError(e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            Logger.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (Exception e) {
                Logger.e(TAG, "temp sockets not created" + e.toString());
                onError(e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Logger.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    bytes = mmInStream.read(buffer); // Read from the InputStream
                    byte[] data = Arrays.copyOf(buffer, bytes);
                    handler.obtainMessage(Constants.MESSAGE_DATA_RECEIVED, data).sendToTarget(); // 发送到UI
                } catch (Exception e) {
                    onError(e);
                    connectionLost();
                    BluetoothService.this.stop(); // Start the service over to restart listening mode
                    break;
                }
            }
        }

        void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (Exception e) {
                Message msg = handler.obtainMessage(Constants.MESSAGE_ERROR);
                handler.sendMessage(msg);
            }
        }

        void cancel() {
            try {
                mmSocket.close();
            } catch (Exception e) {
                Logger.e(TAG, "close() of connect socket failed" + e.toString());
            }
        }
    }
}