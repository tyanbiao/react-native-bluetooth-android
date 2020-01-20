package com.tyanbiao.bluetooth;



import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

class BluetoothService {
    private final String TAG = RNBluetoothModule.TAG;
    private static final UUID UUID_SPP = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int state;

    private Handler handler;

    private static final int STATE_NONE = 0;       // we're doing nothing
    private static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    private static final int STATE_CONNECTED = 2;  // now connected to a remote device


    BluetoothService(Handler mHandler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
        handler = mHandler;
    }

    synchronized void connect(BluetoothDevice device) {
        if (state == STATE_CONNECTING) {
            cancelConnectThread(); // Cancel any thread attempting to make a connection
        }

        cancelConnectedThread(); // Cancel any thread currently running a connection

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    boolean isConnected () {
        return state == STATE_CONNECTED;
    }

    boolean write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (!isConnected()) return false;
            r = mConnectedThread;
        }
        return r.write(out);
    }

    synchronized void stop() {
        cancelConnectThread();
        cancelConnectedThread();
        setState(STATE_NONE);
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mAdapter;
    }

    private synchronized int getState() {
        return state;
    }

    private synchronized void setState(int state) {
        this.state = state;
    }

    private synchronized void connectionSuccess(BluetoothSocket socket, BluetoothDevice device) {
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

            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID_SPP);
            } catch (Exception e) {
               onError(e);
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
                    onError(e2);
                    try {
                        mmSocket.close();
                    } catch (Exception e3) {
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
                onError(e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean running = false;

        ConnectedThread(BluetoothSocket socket) {
            Logger.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                running = true;
            } catch (Exception e) {
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
            while (running) {
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

        boolean write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                return true;
            } catch (Exception e) {
                Message msg = handler.obtainMessage(Constants.MESSAGE_ERROR);
                handler.sendMessage(msg);
                return false;
            }
        }

        void cancel() {
            running = false;
            try {
                mmSocket.close();
            } catch (Exception e) {
                onError(e);
            }
        }
    }
}