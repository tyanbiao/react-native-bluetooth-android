package com.tangyb.ReactNativeBluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tangyb.ReactNativeBluetooth.utils.FileUtil;
import com.tangyb.ReactNativeBluetooth.utils.Utility;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BluetoothService {
    private static final String TAG = "tangyb:BluetoothService";
    private Handler handler; // handler that gets info from Bluetooth service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter bluetoothAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private Thread parserThread = new Thread(new BufferPaserRunble());
    private StateEnum state;
    private int taskMode = 0;
    private boolean taskState = false;

    // 自定义
    FileUtil fileUtil;
    // 定时任务线程池
    ScheduledExecutorService timerService = Executors.newScheduledThreadPool(4);
    //
    private BufferQueue bufferQueue = new BufferQueue();

    public static enum StateEnum {
        NONE, CONNECTING, CONNECTED
    }

    public BluetoothService(Handler handler) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        state = StateEnum.NONE;
        parserThread.start();
        this.handler = handler;
        timerService.scheduleWithFixedDelay(new CommandParseTimerTask(), 100, 50, TimeUnit.MILLISECONDS);
        timerService.scheduleWithFixedDelay(new DataParseTimerTask(), 100, 100, TimeUnit.MILLISECONDS);
    }

    private synchronized void setState(StateEnum state) {
        this.state = state;
    }
    public synchronized StateEnum getState() {
        return state;
    }

    public synchronized void connect(BluetoothDevice device, boolean secure) {
        if (state == StateEnum.CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(StateEnum.CONNECTING);
    }
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(StateEnum.NONE);
    }

    public boolean write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (state != StateEnum.CONNECTED) {
                Log.e(TAG, "蓝牙未连接");
                return false;
            }
            r = mConnectedThread;
        }
        return r.write(out);
    }

    public void setTaskState(boolean state) {
        if (state == taskState) return; // 状态未改变
        taskState = state;
        if (taskState == false) {
            fileUtil.saveAll();
        }
    }
    public void setTaskMode(int mode) {
        taskMode = mode;
    }

    private void connectionFailed() {
        setState(StateEnum.NONE);
        Log.e(TAG, "service 连接失败");
        Message msg = handler.obtainMessage(Constants.MESSAGE_CONNECT_FAIL);
        handler.sendMessage(msg);
    }

    private void connectionLost() {
        setState(StateEnum.NONE);

        Message msg = handler.obtainMessage(Constants.MESSAGE_CONNECT_LOST);
        handler.sendMessage(msg);
    }

    // 自定义
    public void setFileUtil(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    public synchronized void manageConnectedSocket(BluetoothSocket socket, BluetoothDevice device, final String socketType) {
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        Log.i(TAG, "连接到:" + device.getAddress());
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(StateEnum.CONNECTED);
        Message msg = handler.obtainMessage(Constants.MESSAGE_CONNECT_OK);
        handler.sendMessage(msg);
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String socketType;

        public ConnectThread(BluetoothDevice device, Boolean secure) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            socketType = secure ? "Secure" : "Insecure";

            try {
                tmp = secure ? device.createRfcommSocketToServiceRecord(MY_UUID) : device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run()  {
            // 取消扫描
            bluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                // Some 4.1 devices have problems, try an alternative way to connect
                // See https://github.com/don/BluetoothSerial/issues/89
                try {
                    Log.i(TAG,"Trying fallback...");
                    mmSocket = (BluetoothSocket) mmDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class}).invoke(mmDevice,1);
                    mmSocket.connect();
                    Log.i(TAG,"Connected");
                } catch (NoSuchMethodException | IOException | IllegalAccessException | InvocationTargetException e) {
                    try {
                        mmSocket.close();
                    } catch (IOException closeException) {
                        Log.e(TAG, "Could not close the client socket", closeException);
                        connectionFailed();
                        return;
                    }
                }
            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            manageConnectedSocket(mmSocket, mmDevice, socketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean running;
        byte[] buffer = new byte[1024];
        int size;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            running = true;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "进入读数据线程");
            while (running) {
                try {
                    readInputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Input stream was disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        private void readInputStream() throws IOException{
            int count = mmInStream.available();
            if (count <= 0) {
                return ;
            }
            size = mmInStream.read(buffer, 0, Math.min(count, 1024));
            byte[] rawdata = Arrays.copyOf(buffer, size);

            // 保存数据到文件
            if (taskState) {
                fileUtil.fileBufferAppend(rawdata);
            }

            // 往数据队列添加数据，通知解析线程
            bufferQueue.addTask(rawdata);
        }

        public boolean write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
//                Log.i(TAG, Utility.toHexString1(bytes));
                bufferQueue.setCanParseCommand(true);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
                return false;
            }
        }

        public void cancel() {
            running = false;
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class DataParseTimerTask implements Runnable {
        @Override
        public void run() {
            // 步态采集模式且任务处于开始状态
//            if (taskMode == 0 && taskState) {
                bufferQueue.setCanParseData(true); // 只要连接上蓝牙任何状态都要显示数据
//            }
        }
    }

    private class CommandParseTimerTask implements Runnable {
        private int count = 0;
        private final int MAX_TIMEOUT = 6; // 50ms执行一次， 超时时间为300ms
        @Override
        public void run() {
            if (bufferQueue.getCanParseCommand()) {
                count++;
                if (count >= MAX_TIMEOUT) {
                    bufferQueue.setCanParseCommand(false);
                    count = 0;
                    return;
                }
            } else {
                count = 0;
            }
        }
    }

    private class BufferPaserRunble implements Runnable {
        private byte[] buffer;
        private final byte HEAD = 0x65;
        private final byte CLASS = 0x04;

        private final byte DATA_HEAD = 0x55;
        private final byte DATA_LENGTH = 0x4e;

        private byte[] dataBuffer = new byte[128];
        private byte[] commandBuffer = new byte[128];
        private int position = 0;
        private int commandPosition = 0;
        private int commandHeadIndex = -1;
        private CommandHandler commandHandler = new CommandHandler();

        @Override
        public void run () {
            Log.i(TAG, "进入解析线程");
            while(true) {
                try {
                    buffer = bufferQueue.getTask();
                    if (bufferQueue.getCanParseCommand()) {
//                        parseCommand();
                        commandHandler.append(buffer);
                    }
                    if (bufferQueue.getCanParseData()) {
                        parseData();
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        public void parseCommand() {
            if (commandPosition + buffer.length >= 128) {
                commandPosition = 0;
            }
            System.arraycopy(buffer, 0, commandBuffer, 0, buffer.length);
            commandPosition += buffer.length;

            int index = commandHeadIndex >= 0 ? commandHeadIndex : 0;
            while (index < commandPosition) {
                if (index == commandPosition - 1) return;
                if (commandBuffer[index] == HEAD) {
                    if (index + 3 < commandPosition && commandBuffer[index + 1] == CLASS) {
                        byte LENGTH = commandBuffer[index+3];
                        if (LENGTH == 1 || LENGTH == 16) {
                            if (index+3+LENGTH+1 < commandPosition) {
                                int size = LENGTH + 5;
                                byte[] command = new byte[size];
                                System.arraycopy(commandBuffer, index, command, 0, size);
                                if ((command[command.length - 1] & 0xFF) == Utility.getHash(command, size)) {
                                    bufferQueue.setCanParseCommand(false);
                                    handler.obtainMessage(Constants.MESSAGE_READ_COMMAND, command).sendToTarget(); // 发送到UI
                                    commandHeadIndex = -1;
                                    return;
                                } else {
                                    Log.e(TAG, "hash error");
                                    Log.i(TAG, Utility.toHexString1(command));
                                    Log.e(TAG, "hash = " + Utility.toHexString1((byte) Utility.getHash(command, size)));
                                }
                            } else {
                                commandHeadIndex = index;
                                return;
                            }
                        } else {
                            commandHeadIndex = index;
                            return;
                        }
                    }
                }
                index++;
            }
            commandPosition = 0;
        }


        public class CommandHandler {
            public int MAX_SIZE = 128;
            public byte[] mBuffer = new byte[MAX_SIZE];
            public int bufferPosition = 0;


            public void append(byte[] arr) {
                if (bufferPosition > 0) {
                    // 合并数组
                    byte[] newBuffer = new byte[arr.length + bufferPosition];
                    System.arraycopy(mBuffer, 0, newBuffer, 0, bufferPosition);
                    System.arraycopy(arr, 0, newBuffer, bufferPosition, arr.length);

                    // 清空
                    bufferPosition = 0;

                    // 调用解析
                    parse(newBuffer);
                } else {
                    parse(arr);
                }
            }

            public void parse(byte[] dataBuffer) {
                int index = 0;
                while (index < dataBuffer.length) {
                    if (dataBuffer[index] != HEAD) {
                        index ++;
                        continue;
                    }

                    if (index + 4 >= dataBuffer.length) {
                        System.arraycopy(dataBuffer, index, mBuffer, 0, dataBuffer.length - index);
                        bufferPosition = dataBuffer.length - index;
                        return;
                    }

                    byte mCLASS = dataBuffer[index + 1];
                    byte mCMD = dataBuffer[index + 2];
                    byte Len = dataBuffer[index + 3];

                    if (mCLASS != CLASS || (Len != 1 && Len != 16)) {
                        index++;
                        continue;
                    }

                    if (index + 4 + Len >= dataBuffer.length) {
                        System.arraycopy(dataBuffer, index, mBuffer, 0, dataBuffer.length - index);
                        bufferPosition = dataBuffer.length - index;
                        return;
                    }

                    int size = Len + 5;
                    byte[] command = new byte[size];
                    System.arraycopy(dataBuffer, index, command, 0, size);
                    if ((command[command.length - 1] & 0xFF) == Utility.getHash(command, size)) {
                        bufferQueue.setCanParseCommand(false);
                        handler.obtainMessage(Constants.MESSAGE_READ_COMMAND, command).sendToTarget(); // 发送到UI
                        return;
                    } else {
                        Log.e(TAG, "hash error");
                        Log.i(TAG, Utility.toHexString1(command));
                        Log.e(TAG, "hash = " + Utility.toHexString1((byte) Utility.getHash(command, size)));
                        index++;
                    }
                }
            }
        }

        public void parseData() {
            // 有上一帧的缓存
            if (position > 0) {
                if (buffer.length >= DATA_LENGTH + 2 - position) {
                    System.arraycopy(buffer, 0, dataBuffer, position, DATA_LENGTH + 2 - position);
                    position = 0;
                    if (dataBuffer[DATA_LENGTH+1] == 0x0a && dataBuffer[DATA_LENGTH] == 0x0d) {
                        byte[] data = new byte[DATA_LENGTH + 2];
                        System.arraycopy(dataBuffer, 0, data, 0, DATA_LENGTH + 2);
                        handler.obtainMessage(Constants.MESSAGE_READ_DATA, data).sendToTarget(); // 发送到UI
                        bufferQueue.setCanParseData(false);
                        return;
                    }
                } else {
                    position = 0;
                }

            }


            int index = 0;
            while( index < this.buffer.length) {
                if (buffer[index] == DATA_HEAD) {
                    if (index+1 < buffer.length && buffer[index + 1] == DATA_LENGTH) {
                        if (index+1+DATA_LENGTH < buffer.length) {
                            byte[] data = new byte[DATA_LENGTH + 2];
                            System.arraycopy(buffer, index, data, 0, DATA_LENGTH + 2);
                            if (data[DATA_LENGTH+1] == 0x0a && data[DATA_LENGTH] == 0x0d) {
                                handler.obtainMessage(Constants.MESSAGE_READ_DATA, data).sendToTarget(); // 发送到UI
                                bufferQueue.setCanParseData(false);
                                return;
                            }
                        } else {
                            System.arraycopy(buffer, index, dataBuffer, 0, buffer.length - index);
                            position = buffer.length - index;
                            return;
                        }
                    }
                }
                index++;
            }
        }

        public void clearBuffer() {
            commandPosition = 0;
        }
    }

}