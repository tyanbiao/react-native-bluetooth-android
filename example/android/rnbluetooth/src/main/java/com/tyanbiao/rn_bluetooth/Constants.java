package com.tyanbiao.rn_bluetooth;


public class Constants {
    public static final int  MESSAGE_CONNECT_OK = 0;
    public static final int MESSAGE_CONNECT_FAIL = 1;
    public static final int MESSAGE_CONNECTION_LOST = 2;
    public static final int MESSAGE_READ = 3;
    public static final int MESSAGE_WRITE_OK = 4;
    public static final int MESSAGE_WRITE_FAIL = 5;
    public static final int MESSAGE_DATA_RECEIVED = 6;
    public static final int MESSAGE_ERROR = 7;

    public static final String onDataReceived = "onDataReceived";
    public static final String onDeviceFound = "onDeviceFound";
    public static final String onConnectionLoast = "onConnectionLoast";
}
