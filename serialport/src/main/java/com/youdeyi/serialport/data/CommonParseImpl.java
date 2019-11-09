package com.youdeyi.serialport.data;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 18:58
 * description:通用的应答指令解析，返回16进制字符串
 */
public class CommonParseImpl extends ICommandParse<String> {
    public CommonParseImpl(CallBack<String> callBack) {
        super(callBack);
    }

    @Override
    public void parseCommand(String hexStr) {
        mCallBack.onCommandRecieve(hexStr);
    }
}
