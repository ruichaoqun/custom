package com.youdeyi.serialport.data;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 15:13
 * description:
 */
public class CheckStatusInfo {
    //控制板状态 0=空闲 1=出货中 2=出货结束（注意仅表示结束， 不代表成功或者失败）。
    // 状态为 2（出货结束）时， 需要发送 ACK 确认帧，控制板收到确认帧后，状 态会变为 0（空闲），否则不能接收 RUN 启动电机 指令。
    private int state;

    //电机索引
    private int motorIndex;

    //电机操作结果  0= 无故障 1=过流 2=断线 3=超时
    private int result;

    //是否掉落货物成功 Bit2  : 0，掉货成功；1，掉货失败
    private int isDropGoodSuccess;

    //Bit3   0光 眼检测到货品掉落，1没有检测到。
    private int photeEyeState;

    //Bit4-bit5: 0= 货已被取走， 1=掉完货后，还在送货中。2=货未取走。3=送 货装置故障。
    private int goodState;

    //最大电流 单位：mA； 高字节在前、低字节在后
    private int maxElectricity;

    //平均电流 单位：mA； 高字节在前、低字节在后
    private int averageElectricity;

    //运行时间 单位：0.1 秒 数值：0-255
    private int time;

    //实时温度  数值：0---127; 单位：℃
    private int currentTemp;

    //实时湿度  数值：0---100; 单位：%
    private int currentHumidity;


    public static CheckStatusInfo parseCheckStatusInfo(String hexStr){
        CheckStatusInfo info = new CheckStatusInfo();

        info.state = Integer.parseInt(hexStr.substring(4,6),16);
        info.motorIndex = Integer.parseInt(hexStr.substring(6,8),16);
        int result = Integer.parseInt(hexStr.substring(8,10),16);
        info.result = result%4;
        info.isDropGoodSuccess = result/4%2;
        info.photeEyeState = result/8%2;
        info.goodState = result/16%4;
        info.maxElectricity =  Integer.parseInt(hexStr.substring(10,14),16);
        info.averageElectricity =  Integer.parseInt(hexStr.substring(14,18),16);
        info.time = Integer.parseInt(hexStr.substring(18,20),16);
        info.currentTemp = Integer.parseInt(hexStr.substring(20,22),16);
        info.currentHumidity = Integer.parseInt(hexStr.substring(22,24),16);

        return info;
    }



    public int getState() {
        return state;
    }

    public int getMotorIndex() {
        return motorIndex;
    }

    public int getResult() {
        return result;
    }

    public int getIsDropGoodSuccess() {
        return isDropGoodSuccess;
    }

    public int getPhoteEyeState() {
        return photeEyeState;
    }

    public int getGoodState() {
        return goodState;
    }

    public int getMaxElectricity() {
        return maxElectricity;
    }

    public int getAverageElectricity() {
        return averageElectricity;
    }

    public int getTime() {
        return time;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getCurrentHumidity() {
        return currentHumidity;
    }
}
