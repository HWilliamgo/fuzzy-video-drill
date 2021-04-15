package com.hwilliamgo.fuzzy_video_drill.util;

/**
 * date: 2021/4/15
 * author: HWilliamgo
 * description:
 */
public class BitOperation {
    public static short a(byte a,byte b) {
        return (short) ((a & 0xff) | (b & 0xff) << 8);
    }
}
