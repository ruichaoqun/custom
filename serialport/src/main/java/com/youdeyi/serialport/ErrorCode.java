package com.youdeyi.serialport;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 10:20
 * description:
 */
public enum  ErrorCode {
    //
    ERROR_BOARD_OFFLINE(200001,"板卡不在线"),
    ERROR_INVALID_INDEX(200002,"无效的电机索引"),
    ERROR_ORTHER_RUNNING(200003,"另一个电机正在运行"),
    ERROR_OTHER_RESULT_NOT_TAKE_OFF(200004,"另一台电机的运转结果还未取走"),
    ERROR_UNKNOWN(-1,"未知错误"),
    ERROR_OVER_CURRENT(200005,"过流"),
    ERROR_OFFLINE(200006,"断线"),
    ERROR_OVERTIME(200007,"超时"),
    ;


    private String msg;
    private int errCode;

    ErrorCode(int errCode,String msg) {
        this.msg = msg;
        this.errCode = errCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }
}
