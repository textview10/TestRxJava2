package com.zonkey.mobileteach_lib.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xu.wang
 * Date on 2016/11/11 15:36
 */
public class ByteUtil {
    /**
     * 将int转为长度为4的byte数组
     *
     * @param length
     * @return
     */
    public static byte[] int2Bytes(int length) {
        byte[] result = new byte[4];
        result[0] = (byte) length;
        result[1] = (byte) (length >> 8);
        result[2] = (byte) (length >> 16);
        result[3] = (byte) (length >> 24);
        return result;
    }

    /**
     * 前置命令符
     *
     * @param command
     * @return
     */
    public static byte[] commandToStructFormatBytes(byte command) {
        byte[] result = new byte[4];
        result[0] = command;
        result[1] = 0x00;
        result[2] = 0x00;
        result[3] = 0x00;
        return result;
    }

    //转成2个字节
    public static byte[] short2Bytes(short size) {
        byte[] result = new byte[2];
        result[0] = (byte) size;
        result[1] = (byte) (size >> 8);
        return result;
    }

    public static byte[] long2Bytes(long size) {
        byte[] result = new byte[8];
        result[0] = (byte) size;
        result[1] = (byte) (size >> 8);
        result[2] = (byte) (size >> 16);
        result[3] = (byte) (size >> 24);
        result[4] = (byte) (size >> 32);
        result[5] = (byte) (size >> 40);
        result[6] = (byte) (size >> 48);
        result[7] = (byte) (size >> 56);
        return result;
    }

    //将输入流转成byte数组
    public static byte[] is2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buff = new byte[3 * 1024];
        int len = 0;
        while ((len = inStream.read(buff, 0, 3 * 1024)) > 0) {
            baos.write(buff, 0, len);
        }
        byte[] temp = baos.toByteArray();
        baos.close();
        return temp;
    }

//    /**
//     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
//     * @param src byte数组
//     * @return int数值
//     */
//    public static int bytesToInt(byte[] src) {
//        int value;
//        value = (int) ((src[0] & 0xFF)
//                | ((src[1] & 0xFF) << 8)
//                | ((src[2] & 0xFF) << 16)
//                | ((src[3] & 0xFF) << 24));
//        return value;
//    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序，和和intToBytes（）配套使用
     *
     * @param src byte数组
     * @return int数值
     */
    public static int bytesToInt(byte[] src) {
        int value;
        value = (int) ((src[0] & 0xFF)
                | ((src[1] & 0xFF) << 8)
                | ((src[2] & 0xFF) << 16)
                | ((src[3] & 0xFF) << 24));
        return value;
    }


    public static short bytesToShort(byte[] src) {
        short value;
        value = (short) ((src[0] & 0xFF)
                | ((src[1] & 0xFF) << 8));
        return value;
    }

    /**
     * 获得校验码
     *
     * @param bytes 根据通讯协议v2的前12个字节
     * @return
     */
    public static byte getCheckCode(byte[] bytes) {
        byte b = 0x00;
        for (int i = 0; i < bytes.length; i++) {
            b ^= bytes[i];
        }
        return b;
    }
}
