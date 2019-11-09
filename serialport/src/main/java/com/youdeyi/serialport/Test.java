package com.youdeyi.serialport;

/**
 * @author Rui Chaoqun
 * @date :2019/11/8 14:24
 * description:
 */
public class Test {
    public static void main(String[] args) {
        byte[] bytes = ByteUtil.hexStr2bytes("41444838313556342E302E3031202020");
        String s = new String(bytes);
        System.out.println("args = [" + s + "]");
    }
}
