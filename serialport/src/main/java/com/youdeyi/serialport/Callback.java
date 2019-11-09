package com.youdeyi.serialport;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 10:32
 * description:
 */
public interface Callback {
    void onSucccess(Object result);

    void onError(int code,String errMsg);
}
