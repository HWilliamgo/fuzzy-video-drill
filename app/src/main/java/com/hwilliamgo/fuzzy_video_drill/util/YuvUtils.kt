package com.hwilliamgo.fuzzy_video_drill.util

/**
 * date: 2021/3/16
 * author: HWilliamgo
 * description: YUV相关工具函数
 */

/**
 * YUV顺时针旋转90度
 */
fun rotateYUVClockwise90(data: ByteArray, cameraWidth: Int, cameraHeight: Int) {
    if (cameraHeight * cameraWidth <= 0) {
        return
    }

    val source = ByteArray(data.size)
    data.copyInto(source)
    val dst = data

    val oldW = cameraWidth
    val oldH = cameraHeight

    //first rotate Y , then rotate U and V

    //Y分量的长度
    val lengthY = oldW * oldH
    //UV分量的高度
    val heightUV = oldH / 2

    var copyIndex = 0

    //旋转Y
    for (j in 0 until oldW) {
        //j 是第几列
        for (i in oldH - 1 downTo 0) {
            //i 是第几行
            dst[copyIndex++] = source[i * oldW + j]
        }
    }

    //旋转U和V
    for (j in 0 until oldW step 2) {
        for (i in heightUV - 1 downTo 0) {
            dst[copyIndex++] = source[lengthY + i * oldW + j]
            dst[copyIndex++] = source[lengthY + i * oldW + j + 1]
        }
    }
}