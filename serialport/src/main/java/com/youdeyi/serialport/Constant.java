package com.youdeyi.serialport;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 9:29
 * description:
 */
public class Constant {

    //查询身份信息
    public static final String SEARCH_STATE = "01";

    //查询状态 POLL 轮询指令
    public static final String POLL_STSTUS = "03";

    //设置温度
    public static final String SET_TEMPRETOR = "04";

    //启动电机
    public static final String START_MOTOR = "05";

    //ACK 结果确认
    public static final String ACK_CONFIRM = "06";

    //RUN2 启动 多路电机
    public static final String START_MULTIPLE_MOTOR  = "15";

    //灯关控制
    public static final String LIGHT_CONTROLL  = "07";

    //开门电磁铁控制
    public static final String OPEN_DOOR_CONTROLL  = "08";

    //制冷控制
    public static final String  REFRIGERATION_CONTROLL  = "09";


    public static final String FULL_POLL_STSTUS = getCommand("02",POLL_STSTUS);

    public static final String FULL_ACK_CONFIRM = getCommand("02",ACK_CONFIRM);


    public static String getCommand(String address,String command){
        return address+command+CRCUtils.getCRC(address+command);
    }
}
