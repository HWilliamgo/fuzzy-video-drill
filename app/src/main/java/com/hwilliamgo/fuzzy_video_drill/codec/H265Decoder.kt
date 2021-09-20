package com.hwilliamgo.fuzzy_video_drill.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.view.Surface
import com.blankj.utilcode.util.LogUtils
import com.hwilliamgo.fuzzy_video_drill.util.file.inner.FastFileWriter
import com.hwilliamgo.fuzzy_video_drill.util.file.IFileWriter

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
    private var fastFileWriter: IFileWriter?=null

    override fun init(width: Int, height: Int) {
        LogUtils.d("init->width=$width, height=$height")
        this.width = width
        this.height = height

        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
        format.apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, width * height)
            setInteger(MediaFormat.KEY_FRAME_RATE, CodecConstant.FRAME_RATE)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, CodecConstant.I_FRAME_INTERVAL) //IDR帧刷新时间
        }
        mediaFormat = format
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/hevc")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        fastFileWriter= FastFileWriter("rawh265data.h265")
    }

    override fun setOutputSurface(surface: Surface) {
        LogUtils.d("H265Decoder.setOutputSurface")
        try {
            mediaCodec?.configure(mediaFormat, surface, null, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun start() {
        LogUtils.d("H265Decoder.start")
        try {
            mediaCodec?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun decodeData(rawData: ByteArray) {
        //经过输出h265数据，使用ffplay查看，是没有问题的，问题应该出在编码器端。
        fastFileWriter?.writeData2File(rawData)
        val codec = mediaCodec ?: return
        val index = codec.dequeueInputBuffer(100000)
        if (index >= 0) {
            val inputBuffer = codec.getInputBuffer(index) ?: return
            inputBuffer.clear()
            inputBuffer.put(rawData, 0, rawData.size)
            codec.queueInputBuffer(index, 0, rawData.size, System.currentTimeMillis(), 0)
        }
        val bufferInfo = MediaCodec.BufferInfo()
        var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 100000)
        while (outputBufferIndex >= 0) {
            codec.releaseOutputBuffer(outputBufferIndex, true)
            outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
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
        fastFileWriter?.destroy()
        try {
            mediaCodec?.stop()
            mediaCodec?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}