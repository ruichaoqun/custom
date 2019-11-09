package com.youdeyi.serialport;

import android.text.TextUtils;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 14:24
 * description:
 */
public class Test {

    public static void main(String[] args) {
        verifyCommand("0005007290");
    }

    public static void verify1() {
        byte[] bytes = ByteUtil.hexStr2bytes("41444838313556342E302E3031202020");
        String s = new String(bytes);
        System.out.println("args = [" + s + "]");
    }

    public static void verifyCommand(String hexStr) {
        if (hexStr.length() < 8) {
            return;
        }
        String command = hexStr.substring(0, hexStr.length() - 4);
        String crc = hexStr.substring(hexStr.length() - 4);
        String s = CRCUtils.getCRC(command);
    }
}
