package com.youdeyi.serialport.data;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 18:58
 * description:
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
