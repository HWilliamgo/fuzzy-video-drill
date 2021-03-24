package com.hwilliamgo.fuzzy_video_drill.codec

/**
 * date: 3/22/21
 * author: HWilliamgo
 * description:
 */
interface IEncoder {

    fun init(width: Int, height: Int, onEncodeDataCallback: OnIEncoderEncodeDataCallback)

    fun start()

    fun encodeFrame(frameData: ByteArray)

    fun stop()

    fun destroy()

    fun interface OnIEncoderEncodeDataCallback {
        fun onEncodeData(encodedData: ByteArray)
    }
}