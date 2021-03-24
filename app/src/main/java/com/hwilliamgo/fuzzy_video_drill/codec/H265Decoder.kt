package com.hwilliamgo.fuzzy_video_drill.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface

/**
 * date: 3/23/21
 * author: HWilliamgo
 * description:
 */
class H265Decoder : IDecoder {
    private var width: Int = 0
    private var height: Int = 0
    private var mediaCodec: MediaCodec? = null

    private var mediaFormat: MediaFormat? = null

    override fun init(width: Int, height: Int) {
        this.width = width
        this.height = height

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
        format.apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, width * height)
            setInteger(MediaFormat.KEY_FRAME_RATE, 15)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5) //IDR帧刷新时间
        }
        mediaFormat = format
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/hevc")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun setOutputSurface(surface: Surface) {
        mediaCodec?.configure(mediaFormat, surface, null, 0)
    }

    override fun start() {
        try {
            mediaCodec?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun decodeData(rawData: ByteArray) {
        val codec = mediaCodec ?: return
        val index = codec.dequeueInputBuffer(100000)
        if (index >= 0) {
            val inputBuffer = codec.getInputBuffer(index) ?: return
            inputBuffer.clear()
            inputBuffer.put(rawData, 0, rawData.size)
            codec.queueInputBuffer(index, 0, rawData.size, System.currentTimeMillis(), 0)
        }
    }

    override fun stop() {
        try {
            mediaCodec?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun destroy() {
        try {
            mediaCodec?.stop()
            mediaCodec?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}