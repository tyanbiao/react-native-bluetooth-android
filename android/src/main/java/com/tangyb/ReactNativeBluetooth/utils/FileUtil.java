package com.tangyb.ReactNativeBluetooth.utils;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class FileUtil {

    ReactContext mContext;
    boolean sdCardState;
    String sdCardPath;
    String filePath;
    static final String defaultPath = "/xeno/collector";
//    static final String defaultPath = "/data";

    static final String TAG = "FileUtil:tangyb";
    private String fileName = "/undefined-"+ dateFormat.format(new Date()) +".log";
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);//日期格式;


    // 缓存
    private int MAX_SZIE = 102400;
    private byte[] fileBuffer = new byte[MAX_SZIE];
    int position = 0;


    public FileUtil(ReactApplicationContext context) {
        mContext = context;
        // 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
        sdCardState = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        if (sdCardState) {
            // 获取应用在外部存储中的files路径: /storage/emulated/0/Android/data/packname/files
            filePath = mContext.getExternalFilesDir("").getAbsolutePath();
            Log.i(TAG, "filePath=" + filePath);
            // 获取SD卡根目录路径: /storage/emulated/0/
            sdCardPath = Environment.getExternalStoragePublicDirectory("").getAbsolutePath();
            Log.i(TAG, "sdCardPath =" + sdCardPath);
            // 设置文件保存目录：不存在则创建
//            boolean res = isFolderExists(filePath + defaultPath);
            boolean res = isFolderExists(sdCardPath + defaultPath);

            if (!res) Log.i(TAG, "创建目录失败");
        }

    }

    // 文件夹不存在则创建
    private boolean isFolderExists(String strFolder) {
        File file = new File(strFolder);
        if (!file.exists()) {
            if (file.mkdirs()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public String getFilePath() {
//        return filePath + defaultPath;
        return sdCardPath + defaultPath;
    }

    public void byte2file(byte[] data) {
        String path = getFilePath() + fileName;
        File file = new File(path);
        Log.i(TAG, path);
        try {
            FileOutputStream outputStream  =new FileOutputStream(file, true);
            outputStream.write(data);
            outputStream.close();

            // 解决Android的bug
            MediaScannerConnection.scanFile(this.mContext, new String[] { file.getAbsolutePath() }, null, null);

        } catch (Exception e) {
//            e.printStackTrace();
            Log.e(TAG, e.toString());
            Log.e(TAG, "文件写入失败");
        }
    }

    public void byte2HexFile(byte[] data) {
        byte[] bytes =  Utility.toHexString1(data).getBytes();
        byte2file(bytes);
    }

    public void setFileName(String fileName) {
        this.fileName = "/" + fileName + "-" + dateFormat.format(new Date()) + ".log";
    }

    public String getFileName() {
        return fileName;
    }

    // 保存byte数据为hex字符串
    public void fileBufferAppend(byte para) {
        if (position + 1 < MAX_SZIE) {
            fileBuffer[position] = para;
            position++;
        } else {
            // 满了
            byte [] temp = fileBuffer;
            position = 0;
            fileBuffer = new byte[MAX_SZIE];
            byte2HexFile(temp);
            fileBuffer[position] = para;
            position++;
        }
    }
    public void fileBufferAppend(byte[] bytes) {
        if (position + bytes.length < MAX_SZIE) {
            System.arraycopy(bytes, 0, fileBuffer, position, bytes.length);
            position += bytes.length;
        } else {
            byte[] temp = fileBuffer;
            fileBuffer = new byte[MAX_SZIE];
            position = 0;
            byte2HexFile(temp);
            System.arraycopy(bytes, 0, fileBuffer, position, bytes.length);
            position += bytes.length;
            temp = null;
        }
    }
    public void saveAll() {
        if (position > 0) {
//            byte[] temp = new byte[position];
//            System.arraycopy(fileBuffer, 0, temp, 0, position);
            byte2HexFile(Arrays.copyOf(fileBuffer, position));
            position = 0;
        }
    }
}
