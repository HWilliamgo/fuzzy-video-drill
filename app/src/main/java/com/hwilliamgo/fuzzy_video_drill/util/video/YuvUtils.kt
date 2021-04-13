package com.hwilliamgo.fuzzy_video_drill.util.video

/**
 * date: 2021/3/16
 * author: HWilliamgo
 * description: YUV相关工具函数
 */

object YuvUtils {
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

    /**
     * 将输入的NV21格式的YUV数据[nv21]，转换成NV12输出
     */
    fun nv21toNV12(nv21: ByteArray): ByteArray {
        val size = nv21.size
        val nv12 = ByteArray(size)
        val len = size * 2 / 3
        System.arraycopy(nv21, 0, nv12, 0, len)
        var i = len
        while (i < size - 1) {
            nv12[i] = nv21[i + 1]
            nv12[i + 1] = nv21[i]
            i += 2
        }
        return nv12
    }
}