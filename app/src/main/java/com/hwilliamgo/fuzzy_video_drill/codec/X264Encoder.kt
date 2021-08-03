package com.hwilliamgo.fuzzy_video_drill.codec

/**
 * date: 2021/8/1
 * author: HWilliamgo
 * description:
 */
class X264Encoder : IEncoder {
    private var width = 0
    private var height = 0
    private var onEncodeDataCallback: IEncoder.OnIEncoderEncodeDataCallback? = null

    override fun init(
        width: Int,
        height: Int,
        onEncodeDataCallback: IEncoder.OnIEncoderEncodeDataCallback
    ) {
        this.width = width
        this.height = height
        this.onEncodeDataCallback = onEncodeDataCallback
    }

    override fun enableOutputRawEncodeStream(enable: Boolean) {

    }

    override fun enableOutputHexStreamData(enable: Boolean) {

    }

    override fun start() {

    }

    override fun encodeFrame(frameData: ByteArray) {

    }

    override fun stop() {

    }

    override fun destroy() {

    }
}