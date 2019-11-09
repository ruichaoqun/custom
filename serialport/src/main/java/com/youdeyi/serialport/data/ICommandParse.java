package com.youdeyi.serialport.data;

import android.text.TextUtils;

import com.youdeyi.serialport.CRCUtils;
import com.youdeyi.serialport.ErrorCode;
import com.youdeyi.serialport.RealSerialPort;

/**
 * @author Rui Chaoqun
 * @date :2019/10/23 10:28
 * description:返回指令解析基类
 */
public abstract class ICommandParse<T> {
    protected CallBack<T> mCallBack;
    protected RealSerialPort mRealSerialPort;
    private String preCommand = "";

    public ICommandParse(CallBack callBack) {
        this.mCallBack = callBack;
    }

    public void setRealSerialPort(RealSerialPort realSerialPort) {
        this.mRealSerialPort = realSerialPort;
    }

    public void verifyCommand(String hexStr){
        preCommand = preCommand + hexStr;
        if(preCommand.length() < 8){
            return;
        }
        String command = preCommand.substring(0,preCommand.length()-2);
        String crc = preCommand.substring(preCommand.length()-2,preCommand.length());
        //验证成功
        if(TextUtils.equals(CRCUtils.getCRC(command),crc)){
            parseCommand(preCommand);
            preCommand = "";
        }
    }

    /**
     *
     * @param hexStr 16进制字符串
     */
    public abstract void parseCommand(String hexStr);

    public void clear(){
        this.mCallBack = null;
    }

    public CallBack getCallBack() {
        return this.mCallBack;
    }

    public interface CallBack<T>{
        void onCommandRecieve(T result);
        void onError(ErrorCode code);
    }
}
