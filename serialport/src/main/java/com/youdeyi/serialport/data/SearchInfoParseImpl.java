package com.youdeyi.serialport.data;

import com.youdeyi.serialport.ByteUtil;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 11:55
 * description:
 */
public class SearchInfoParseImpl extends ICommandParse<String> {
    public SearchInfoParseImpl(CallBack callBack) {
        super(callBack);
    }

    @Override
    public void parseCommand(String hexStr) {
        byte[] bytes = ByteUtil.hexStr2bytes(hexStr);
        mCallBack.onCommandRecieve(new String(bytes));
    }
}
