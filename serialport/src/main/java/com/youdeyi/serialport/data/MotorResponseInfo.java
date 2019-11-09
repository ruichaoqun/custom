package com.youdeyi.serialport.data;

import com.youdeyi.serialport.ErrorCode;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 17:04
 * description:电机运行应答指令
 */
public class MotorResponseInfo extends ICommandParse<String> {

    public MotorResponseInfo(CallBack<String> callBack) {
        super(callBack);
    }

    @Override
    public void parseCommand(String hexStr) {
        int state = Integer.parseInt(hexStr.substring(4,6),16);
        switch (state){
            case 0:
                mCallBack.onCommandRecieve("命令已执行");
                break;
            case 1:
                mCallBack.onError(ErrorCode.ERROR_INVALID_INDEX);
                break;
            case 2:
                mCallBack.onError(ErrorCode.ERROR_ORTHER_RUNNING);
                break;
            case 3:
                mCallBack.onError(ErrorCode.ERROR_OTHER_RESULT_NOT_TAKE_OFF);
                break;
                default:
                    mCallBack.onError(ErrorCode.ERROR_UNKNOWN);
        }
    }
}
