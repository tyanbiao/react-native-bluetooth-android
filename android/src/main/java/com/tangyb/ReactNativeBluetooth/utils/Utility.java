package com.tangyb.ReactNativeBluetooth.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

public class Utility {

    private static ByteBuffer buffer = ByteBuffer.allocate(8);
    static final String TAG = "Utility:tangyb";
    /**
     * 数组转成十六进制字符串
     *
     * @return HexString
     */
    public static String toHexString1(byte[] b) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < b.length; ++i) {
            buffer.append(toHexString1(b[i]));
        }
        return buffer.toString();
    }

    public static String toHexString1(byte b) {
        String s = Integer.toHexString(b & 0xFF);
        if (s.length() == 1) {
            return "0" + s;
        } else {
            return s;
        }
    }

    public static int getHash(byte[] buffer, int size) {
        int hash = 0x00;
        for (int i = 0; i < size-1; i++) {
            hash += buffer[i];
        }
        hash = hash & 0xff;
        hash = hash ^ 0xff;

        return hash;
    }

    //    生成UUID
    public static String UUID() {
        String s = UUID.randomUUID().toString(); // 生成uuid
        // 去掉“-”符号
        return s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24);
    }



    //byte 与 int 的相互转换
    public static byte intToByte(int x) {
        return (byte) x;
    }

    public static int byteToInt(byte b) {
        //Java 总是把 byte 当做有符处理；我们可以通过将其和 0xFF 进行二进制与得到它的无符值
        return b & 0xFF;
    }

    public static int putShort(byte[] bytes, short number, int index) {
        bytes[index + 1] = (byte) (number >> 8);
        bytes[index + 0] = (byte) (number >> 0);
        return index + 2;
    }

    public static int putLong(byte[] bytes, long lNumber, int index) {
        buffer.putLong(0, lNumber);
        byte[] temp = buffer.array();
        System.arraycopy(temp, 0, bytes, index, temp.length);
        return index + temp.length;
    }

    public static int putChar(byte[] bytes, byte c, int index) {
        bytes[index + 0] = c;
        return index + 1;
    }

    //byte 数组与 long 的相互转换
    public static byte[] longToBytes(long x) {
        buffer.putLong(0, x);
        return buffer.array();
    }

    public static long bytesToLong(byte[] bytes) {
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();//need flip
        return buffer.getLong();
    }
}
