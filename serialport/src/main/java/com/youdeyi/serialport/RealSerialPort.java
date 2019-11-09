package com.youdeyi.serialport;

import android.serialport.SerialPort;
import android.util.Log;


import com.youdeyi.serialport.data.ICommandParse;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Rui Chaoqun
 * @description: 串口操作类
 * @date :2019/5/27 9:29
 */
public class RealSerialPort{
    private static final String TAG = "RealSerialPort";
    private String devicePath;
    private String baudrateString;
    //串口设备
    private SerialPort mSerialPort;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;
    private byte[] received = new byte[1024];
    private Disposable disposable;
    private ICommandParse mICommandParse;

    public RealSerialPort(String devicePath, String baudrateString,ICommandParse iCommandParse) {
        this.devicePath = devicePath;
        this.baudrateString = baudrateString;
        this.mICommandParse = iCommandParse;
        this.mICommandParse.setRealSerialPort(this);
        open();
    }

    /**
     * 开启串口
     */
    public SerialPort open() {
        if (mSerialPort == null) {
            try {
                File device = new File(devicePath);
                int baurate = Integer.parseInt(baudrateString);
                mSerialPort = new SerialPort(device, baurate, 0);
                inputStream = new BufferedInputStream(mSerialPort.getInputStream());
                outputStream = new BufferedOutputStream(mSerialPort.getOutputStream());
                startReceiveCommand();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, devicePath+"串口打开失败" + e.getMessage());
                mICommandParse.getCallBack().onError(ErrorCode.ERROR_SERIALPORT_OPEN);
                SerialPortManager.getInstance().close();
            }
        }
        return mSerialPort;
    }


    /**
     * 发送指令
     *
     * @param hexStr 指令
     */
    public void sendCommand(final String hexStr) {
        if (mSerialPort == null) {
//            LogUtil.e(TAG, "打开串口失败");
            mICommandParse.getCallBack().onError(ErrorCode.ERROR_SERIALPORT_OPEN);
            SerialPortManager.getInstance().close();
            return;
        }
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                byte[] bytes = ByteUtil.hexStr2bytes(hexStr);
                outputStream.write(bytes);
                outputStream.flush();
                emitter.onNext(true);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    /**
     * 开启定时器定时读取返回数据
     */
    private void startReceiveCommand() {
        disposable = Observable.interval(30, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        int available = inputStream.available();
                        if (available > 0) {
                            int size = inputStream.read(received);
                            if (size > 0) {
                                String hexStr = ByteUtil.bytes2HexStr(received, 0, size);
                                mICommandParse.parseCommand(hexStr);
//                                mHandler.sendMessage(mHandler.obtainMessage(1,hexStr));
//                                onDataReceive(hexStr);
                            }
                        }
                    }
                });
    }

    public void setICommandParse(ICommandParse ICommandParse) {
        this.mICommandParse = ICommandParse;
        this.mICommandParse.setRealSerialPort(this);
    }

    /**
     * 关闭串口
     */
    public synchronized void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mSerialPort != null) {
            mSerialPort.close();
        }

        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        mICommandParse.clear();
        inputStream = null;
        outputStream = null;
        mSerialPort = null;
    }
}
