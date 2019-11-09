package com.youdeyi.serialport.data;

import com.youdeyi.serialport.Constant;
import com.youdeyi.serialport.ErrorCode;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 14:38
 * description:轮询指令应答指令处理类
 */
public class CheckStatusCommandParseImpl extends ICommandParse<String>{


    public CheckStatusCommandParseImpl(CallBack callBack) {
        super(callBack);
    }

    @Override
    public void parseCommand(String hexStr) {
        CheckStatusInfo info = CheckStatusInfo.parseCheckStatusInfo(hexStr);
        switch (info.getResult()){
            case 0:
                break;
            case 1:
                mCallBack.onError(ErrorCode.ERROR_OVER_CURRENT);
                return;
            case 2:
                mCallBack.onError(ErrorCode.ERROR_OFFLINE);
                return;
            case 3:
                mCallBack.onError(ErrorCode.ERROR_OVERTIME);
                return;
                default:
        }

        switch (info.getState()){
            case 0:
                break;
            case 1:
                //出货中，继续轮询发送指令
                mRealSerialPort.sendCommand(Constant.FULL_POLL_STSTUS);
                break;
            case 2:
                //出货成功，光眼检测到物品掉落
                if(info.getPhoteEyeState() == 0){
                    mCallBack.onCommandRecieve("");
                }
                break;
                default:
        }
    }
}
