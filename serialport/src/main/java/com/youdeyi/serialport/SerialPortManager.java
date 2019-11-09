package com.youdeyi.serialport;

import android.util.Log;

import com.youdeyi.serialport.data.CheckStatusCommandParseImpl;
import com.youdeyi.serialport.data.CommonParseImpl;
import com.youdeyi.serialport.data.ICommandParse;
import com.youdeyi.serialport.data.MotorRequestInfo;
import com.youdeyi.serialport.data.MotorResponseInfo;
import com.youdeyi.serialport.data.SearchInfoParseImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Administrator on 2017/3/28 0028.
 */
public class SerialPortManager {
    private static final String TAG = SerialPortManager.class.getSimpleName();

    private volatile static SerialPortManager instance;

    private Map<String, RealSerialPort> serialPortMap = new HashMap<>();

    private SerialPortManager() {
        mRequestInfos = new ArrayList<>();
    }

    private List<MotorRequestInfo> mRequestInfos;

    public static SerialPortManager getInstance() {
        if (instance == null) {
            synchronized (SerialPortManager.class) {
                if (instance == null) {
                    instance = new SerialPortManager();
                }
            }
        }
        return instance;
    }

    public void init() {

    }

    private RealSerialPort getSerialPort(String path, String baudrateString, ICommandParse iCommandParse) {
        if (serialPortMap.containsKey(path)) {
            return serialPortMap.get(path);
        }
        RealSerialPort serialPort = new RealSerialPort(path, baudrateString, iCommandParse);
        serialPortMap.put(path, serialPort);
        return serialPort;
    }

    public void close() {
        for (RealSerialPort port : serialPortMap.values()) {
            port.close();
        }
        serialPortMap.clear();
    }

    /**
     * 指定断开串口连接
     *
     * @param path 串口地址
     */
    public void close(String path) {
        for (String key : serialPortMap.keySet()) {
            if (path == key) {
                serialPortMap.get(key).close();
                serialPortMap.remove(key);
                return;
            }
        }
    }

    private String getCommand(String str) {
        return str + CRCUtils.getCRC(str);
    }

    private void sendCommand(String command, ICommandParse iCommandParse) {
        getSerialPort(Device.getSearchInfoDevice().getPort(), Device.getSearchInfoDevice().getDaud(), iCommandParse).sendCommand(command);
    }


    /**
     * 开启轮询
     *
     * @param callback
     */
    public void checkStatus(final Callback callback) {
        sendCommand(Constant.FULL_POLL_STSTUS, new CheckStatusCommandParseImpl(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String object) {
                //轮询查询到出货结束，此时发送ack确认指令
                sendAckCommand(callback);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                callback.onError(errorCode.getErrCode(), errorCode.getMsg());
            }
        }));
    }

    /**
     * 发送ack确认指令
     *
     * @param callback
     */
    private void sendAckCommand(final Callback callback) {
        sendCommand(Constant.FULL_ACK_CONFIRM, new CommonParseImpl(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String result) {
                //轮询指令确认成功
                if (mRequestInfos.size() > 0) {
                    //移除队列中第一条指令
                    mRequestInfos.remove(0);
                    //继续执行剩余的指令
                    startMotor(callback);
                }
                callback.onSucccess("出货成功");
            }

            @Override
            public void onError(ErrorCode code) {
                callback.onError(code.getErrCode(), code.getMsg());
            }
        }));
    }


    //********************************暴露给外部的API****************************//
    public void checkConnect(final Callback callback) {
        String command = getCommand("01" + Constant.SEARCH_STATE);
        sendCommand(command, new SearchInfoParseImpl(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String object) {
                callback.onSucccess(object);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                callback.onError(errorCode.getErrCode(), errorCode.getMsg());
            }
        }));
    }

    public void startMultiMotor(List<MotorRequestInfo> list, final Callback callback) {
        mRequestInfos.addAll(list);
        startMotor(callback);
    }


    /**
     * 启动单个电机
     */
    public void startMotor(final Callback callback) {
        if (mRequestInfos.size() == 0) {
            Log.e(TAG, "全部出货完毕");
            return;
        }
        String command = getCommand("01" + Constant.START_MULTIPLE_MOTOR + ByteUtil.decimal2fitHex(mRequestInfos.get(0).getId(), 2) + ByteUtil.decimal2fitHex(mRequestInfos.get(0).getCount(), 2));
        sendCommand(command, new MotorResponseInfo(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String result) {

            }

            @Override
            public void onError(ErrorCode code) {
                callback.onError(code.getErrCode(), code.getMsg());
            }
        }));
        checkStatus(callback);
    }
}
