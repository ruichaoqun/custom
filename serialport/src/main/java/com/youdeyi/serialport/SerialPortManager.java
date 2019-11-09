package com.youdeyi.serialport;

import android.util.Log;

import com.youdeyi.serialport.data.CheckStatusCommandParseImpl;
import com.youdeyi.serialport.data.CommonParseImpl;
import com.youdeyi.serialport.data.ICommandParse;
import com.youdeyi.serialport.data.LightControlType;
import com.youdeyi.serialport.data.MotorRequestInfo;
import com.youdeyi.serialport.data.MotorResponseInfo;
import com.youdeyi.serialport.data.RefrigerationControlType;
import com.youdeyi.serialport.data.SearchInfoParseImpl;
import com.youdeyi.serialport.data.TempretureType;

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

    //串口集合
    private Map<String, RealSerialPort> serialPortMap;


    private SerialPortManager() {
        serialPortMap = new HashMap<>();
        mRequestInfos = new ArrayList<>();
        mLoggerProvider = new LoggerProvider() {
            @Override
            public void w(String tag, String msg) {
                Log.w(tag,msg);
            }

            @Override
            public void e(String tag, String msg) {
                Log.w(tag,msg);
            }

            @Override
            public void d(String tag, String msg) {
                Log.w(tag,msg);
            }
        };
    }

    //取货指令集合
    private List<MotorRequestInfo> mRequestInfos;
    private int requestCount = 0;
    //外部获取详细日志的接口
    private LoggerProvider mLoggerProvider;

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

    /**
     * 获取指定的串口，如果不存在则新建，如果已存在则重新设置回调方法
     * @param path 串口id
     * @param baudrateString 串口波特率
     * @param iCommandParse 回调接口
     * @return 串口
     */
    private RealSerialPort getSerialPort(String path, String baudrateString, ICommandParse iCommandParse) {
        if (serialPortMap.containsKey(path)) {
            RealSerialPort s = serialPortMap.get(path);
            s.setICommandParse(iCommandParse);
            return serialPortMap.get(path);
        }
        RealSerialPort serialPort = new RealSerialPort(path, baudrateString, iCommandParse);
        serialPortMap.put(path, serialPort);
        return serialPort;
    }

    /**
     * 关闭所有串口
     */
    void close() {
        for (RealSerialPort port : serialPortMap.values()) {
            port.close();
        }
        serialPortMap.clear();
    }

    /**
     * 指定关闭串口
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

    //根据CRC校验工具获取校验码再与指令拼接成完整指令
    private String getCommand(String str) {
        return str + CRCUtils.getCRC(str);
    }

    /**
     * 发送指令 TODO 暂时默认全部串口都相同，后面要确认各指令对应的串口地址
     * @param command 指令
     * @param iCommandParse 回调
     */
    private void sendCommand(String command, ICommandParse iCommandParse) {
        getSerialPort(Device.getSearchInfoDevice().getPort(), Device.getSearchInfoDevice().getDaud(), iCommandParse).sendCommand(command);
    }


    /**
     * 开启轮询指令，该指令轮询查询出货指令的执行情况
     * 当确认出货已经成功且检测到货物掉落后将执行ACK指令来清空数据
     * ACK指令执行成功后才可以执行下一个出货指令
     *
     * @param callback 回调
     */
    private void checkStatus(final Callback callback) {
        sendCommand(Constant.FULL_POLL_STSTUS, new CheckStatusCommandParseImpl(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String object) {
                //轮询查询到出货结束，此时发送ack确认指令
                sendAckCommand(callback);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                mLoggerProvider.e(TAG,"轮询指令执行错误："+errorCode.toString());
                reset();
                callback.onError(errorCode.getErrCode(), errorCode.getMsg());
            }
        }));
    }

    /**
     * 发送ack确认指令
     *
     * @param callback 回调
     */
    private void sendAckCommand(final Callback callback) {
        sendCommand(Constant.FULL_ACK_CONFIRM, new CommonParseImpl(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String result) {
                mLoggerProvider.w(TAG,"第"+requestCount+"个药品出货成功");
                //轮询指令确认成功
                if (mRequestInfos.size() > 0) {
                    //移除队列中第一条指令
                    mRequestInfos.remove(0);
                    requestCount--;
                    //继续执行剩余的指令
                    mLoggerProvider.w(TAG,"开始执行取"+requestCount+"个药品");
                    startMotor(callback);
                }
                callback.onSucccess("出货成功");
            }

            @Override
            public void onError(ErrorCode code) {
                mLoggerProvider.e(TAG,"ACK确认指令未知错误");
                reset();
                callback.onError(code.getErrCode(), code.getMsg());
            }
        }));
    }

    /**
     * 启动单个电机
     */
    private void startMotor(final Callback callback) {
        if (mRequestInfos.size() == 0) {
            mLoggerProvider.e(TAG, "全部出货完毕");
            callback.onSucccess("全部出货完毕");
            return;
        }
        String command = getCommand("01" + Constant.START_MULTIPLE_MOTOR + ByteUtil.decimal2fitHex(mRequestInfos.get(0).getId(), 2) + ByteUtil.decimal2fitHex(mRequestInfos.get(0).getCount(), 2));
        sendCommand(command, new MotorResponseInfo(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String result) {

            }

            @Override
            public void onError(ErrorCode code) {
                mLoggerProvider.e(TAG,"启动电机指令错误："+code.toString());
                reset();
                callback.onError(code.getErrCode(), code.getMsg());
            }
        }));
        checkStatus(callback);
    }

    private void reset() {
        mRequestInfos.clear();
        requestCount = 0;
    }


    //********************************暴露给外部的API****************************//
    /**
     * 连接所有串口
     * 该方法在application中调用进行初始化
     */
    public void openAllSerial(){

    }

    public void setLoggerProvider(LoggerProvider loggerProvider) {
        mLoggerProvider = loggerProvider;
    }

    LoggerProvider getLoggerProvider() {
        return mLoggerProvider;
    }

    /**
     * 检查主板状态
     * @param callback 回调
     */
    public void checkConnect(final Callback callback) {
        String command = getCommand("01" + Constant.SEARCH_STATE);
        sendCommand(command, new SearchInfoParseImpl(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String object) {
                mLoggerProvider.w(TAG,"主板状态检查完毕    "+object);
                callback.onSucccess(object);
            }

            @Override
            public void onError(ErrorCode errorCode) {
                mLoggerProvider.e(TAG,"主板状态错误："+errorCode.toString());
                reset();
                callback.onError(errorCode.getErrCode(), errorCode.getMsg());
            }
        }));
    }

    /**
     * 启动电机，不论单个、多个还是单路、多路都是走这个方法去启动电机
     * @param list 电机列表
     * @param callback 出货回调
     */
    public void startMultiMotor(List<MotorRequestInfo> list, final Callback callback) {
        mRequestInfos.addAll(list);
        requestCount = mRequestInfos.size();
        startMotor(callback);
    }

    /**
     * 温度控制指令
     */
    public void tempControl(@TempretureType String type, int tempreture, final Callback callback){
        String command = getCommand("02"+Constant.SET_TEMPRETOR+type+ByteUtil.decimal2fitHex(tempreture,4));
        sendCommand(command,new CommonParseImpl(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String result) {
                //温度设置成功
                mLoggerProvider.w(TAG,"温度设置成功    "+result);
                callback.onSucccess(result);
            }

            @Override
            public void onError(ErrorCode code) {
            }
        }));
    }

    /**
     * 制冷控制
     * @param type  开或关
     * @param callback 回调
     */
    public void refrigerationControl(@RefrigerationControlType String type,final Callback callback){
        String command = getCommand("01"+Constant.REFRIGERATION_CONTROLL+type);
        sendCommand(command,new CommonParseImpl(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String result) {
                //温度设置成功
                mLoggerProvider.w(TAG,"制冷控制成功    "+result);
                callback.onSucccess(result);
            }

            @Override
            public void onError(ErrorCode code) {

            }
        }));
    }


    /**
     * 灯关控制
     * @param type  开或关
     * @param callback 回调
     */
    public void lightControl(@LightControlType String type, final Callback callback){
        String command = getCommand("01"+Constant.LIGHT_CONTROLL+type);
        sendCommand(command,new CommonParseImpl(new ICommandParse.CallBack<String>() {
            @Override
            public void onCommandRecieve(String result) {
                //温度设置成功
                mLoggerProvider.w(TAG,"灯关控制成功    "+result);
                callback.onSucccess(result);
            }

            @Override
            public void onError(ErrorCode code) {

            }
        }));
    }

    /**
     * 日志打印接口
     */
    public interface LoggerProvider{
        void w(String tag,String msg);
        void e(String tag,String msg);
        void d(String tag,String msg);
    }
}
