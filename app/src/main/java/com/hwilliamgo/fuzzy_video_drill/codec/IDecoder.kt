package com.hwilliamgo.fuzzy_video_drill.codec

import android.view.Surface

/**
 * date: 3/23/21
 * author: HWilliamgo
 * description: 解码器
 */
interface IDecoder {
    fun init(width: Int, height: Int)

    fun setOutputSurface(surface: Surface)

    fun start()

    fun decodeData(rawData: ByteArray)

    fun stop()

    fun destroy()
}