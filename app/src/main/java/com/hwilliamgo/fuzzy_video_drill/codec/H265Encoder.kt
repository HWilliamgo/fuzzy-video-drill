package com.hwilliamgo.fuzzy_video_drill.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.hwilliamgo.fuzzy_video_drill.util.FastFileWriter
import java.nio.ByteBuffer

/**
 * date: 3/22/21
 * author: HWilliamgo
 * description:
 */
class H265Encoder : IEncoder {
    companion object {
        val TAG = H265Encoder::class.java.simpleName
    }

    /**** 元数据 ****/
    private var width = 0
    private var height = 0
    private var frameIndex = 0L

    /**** listener ****/
    private var onEncodeDataCallback: IEncoder.OnIEncoderEncodeDataCallback? = null

    /**** codec ****/
    private var mediaCodec: MediaCodec? = null

    /**** buffer ****/
    private var vpsSpsPpsBuffer: ByteArray? = null

    private var fastFileWriter: FastFileWriter? = null

    override fun init(
        width: Int,
        height: Int,
        onEncodeDataCallback: IEncoder.OnIEncoderEncodeDataCallback
    ) {
        this.width = width
        this.height = height
        this.onEncodeDataCallback = onEncodeDataCallback
        configureCodec()
        fastFileWriter = FastFileWriter("encoderh265.h265")
    }

    override fun start() {
        frameIndex = 0
        try {
            mediaCodec?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun encodeFrame(frameData: ByteArray) {
        try {
            val codec = mediaCodec ?: return
            val inputBufferIndex = codec.dequeueInputBuffer(100000)
            if (inputBufferIndex >= 0) {
                val inputBuffer = codec.getInputBuffer(inputBufferIndex) ?: return
                inputBuffer.clear()
                if (inputBufferIndex == 0) {
                    val remaining = inputBuffer.remaining()
                    val capacity = inputBuffer.capacity()
                    LogUtils.d("remaining=$remaining, capacity=$capacity, frameData.size=${frameData.size}")
                }
                inputBuffer.put(frameData)
                val pts = computePresentationTime(frameIndex++)
                codec.queueInputBuffer(inputBufferIndex, 0, inputBuffer.remaining(), pts, 0)
            }
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 100000)
            while (outputBufferIndex >= 0) {
                val outputBuffer = codec.getOutputBuffer(outputBufferIndex) ?: continue
                dealFrame(outputBuffer, bufferInfo)
                codec.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
            mediaCodec?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaCodec = null
    }

    private fun configureCodec() {
        try {
            val format =
                MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
            format.apply {
                setInteger(
                    MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
                )
                setInteger(MediaFormat.KEY_BIT_RATE, width * height)
                setInteger(MediaFormat.KEY_FRAME_RATE, CodecConstant.FRAME_RATE)
                setInteger(
                    MediaFormat.KEY_I_FRAME_INTERVAL,
                    CodecConstant.I_FRAME_INTERVAL
                ) //IDR帧刷新时间
            }
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC);
            mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun computePresentationTime(frameIndex: Long): Long {
        return 132 + frameIndex * 1000000 / 15
    }

    private fun dealFrame(bb: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {

        //如果是00 00 00 01，那么是偏移到第4个元素来取出nalu type。
        //如果是00 00 01，   那么是偏移到第3个元素来取出nalu type。
        val offset: Int = if (bb.get(2).toInt() == 0x01) {
            3
        } else {
            4
        }

        val naluType = (bb.get(offset).toInt() and 0x7E) ushr 1
        Log.d(TAG, "naluType=$naluType")
        when (naluType) {
            CodecConstant.HEVC_NALU_TYPE_VPS -> {
                vpsSpsPpsBuffer = ByteArray(bufferInfo.size)
                bb.get(vpsSpsPpsBuffer)
            }
            CodecConstant.HEVC_NALU_TYPE_I -> {
                val bytes = ByteArray(bufferInfo.size)
                bb.get(bytes)
                val vpsSpsPpsBufferTmp = vpsSpsPpsBuffer!!
                val newBuf = ByteArray(vpsSpsPpsBufferTmp.size + bytes.size)
                System.arraycopy(vpsSpsPpsBufferTmp, 0, newBuf, 0, vpsSpsPpsBufferTmp.size)
                System.arraycopy(bytes, 0, newBuf, vpsSpsPpsBufferTmp.size, bytes.size)
                onEncodeDataCallback?.onEncodeData(newBuf)
                fastFileWriter?.writeData2File(newBuf)
            }
            else -> {
                val bytes = ByteArray(bufferInfo.size)
                bb.get(bytes)
                onEncodeDataCallback?.onEncodeData(bytes)
                fastFileWriter?.writeData2File(bytes)
            }
        }
    }
}