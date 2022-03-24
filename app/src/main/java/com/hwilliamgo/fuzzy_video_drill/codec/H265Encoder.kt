package com.hwilliamgo.fuzzy_video_drill.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import com.hwilliamgo.fuzzy_video_drill.VideoDrillApp
import com.hwilliamgo.fuzzy_video_drill.util.file.inner.FastFileWriter
import com.hwilliamgo.fuzzy_video_drill.util.file.inner.HexStringFileWriter
import com.hwilliamgo.fuzzy_video_drill.util.file.IFileWriter
import java.nio.ByteBuffer

/**
 * date: 3/22/21
 * author: HWilliamgo
 * description: h265编码器
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

    private var fastFileWriter: IFileWriter? = null
    private var hexStringFileWriter: IFileWriter? = null

    override fun init(
        width: Int,
        height: Int,
        onEncodeDataCallback: IEncoder.OnIEncoderEncodeDataCallback
    ) {
        this.width = width
        this.height = height
        this.onEncodeDataCallback = onEncodeDataCallback
        configureCodec()
    }

    override fun enableOutputRawEncodeStream(enable: Boolean) {
        if (enable) {
            fastFileWriter = FastFileWriter(VideoDrillApp.getInstance(),"encoderh265.h265")
        }
    }

    override fun enableOutputHexStreamData(enable: Boolean) {
        if (enable) {
            hexStringFileWriter = HexStringFileWriter(VideoDrillApp.getInstance(),"encoderh265HexString.txt")
        }
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
                //经过测试，frameData大小为3.1M, remaining 为3.6M，不会超出的inputBuffer的大小，其实超出了也会在put方法报异常：BufferOverflowException
                inputBuffer.put(frameData)
                val pts = computePresentationTime(frameIndex++)
                codec.queueInputBuffer(inputBufferIndex, 0, frameData.size, pts, 0)
            }
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 100000)
            while (outputBufferIndex >= 0) {
                val outputBuffer = codec.getOutputBuffer(outputBufferIndex) ?: continue
                dealFrame(outputBuffer, bufferInfo)
                codec.releaseOutputBuffer(outputBufferIndex, false)
                //用0的话会导致画面很慢，延迟很高；用10000的话，画面延迟低，但是画面感觉掉帧
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 100000)
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
        hexStringFileWriter?.destroy()
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
        //经过测试，bb转成16进制字符串输出后，单个bb里面包含了VPS,SPS,PPS，他们都在一个连续的bb里面，即MediaCodec输出的第一个缓冲区数据中。

        //如果是00 00 00 01，那么是偏移到第4个元素来取出nalu type。
        //如果是00 00 01，   那么是偏移到第3个元素来取出nalu type。
        val offset: Int = if (bb.get(2).toInt() == 0x01) {
            3
        } else {
            4
        }

        val naluType = (bb.get(offset).toInt() and 0x7E) shr 1
        Log.d(TAG, "naluType=$naluType")
        when (naluType) {
            CodecConstant.HEVC_NALU_TYPE_VPS -> {
                vpsSpsPpsBuffer = ByteArray(bufferInfo.size)
                bb.get(vpsSpsPpsBuffer)
                hexStringFileWriter?.writeData2File(vpsSpsPpsBuffer ?: return)
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
                hexStringFileWriter?.writeData2File(newBuf)
            }
            else -> {
                val bytes = ByteArray(bufferInfo.size)
                bb.get(bytes)
                onEncodeDataCallback?.onEncodeData(bytes)
                fastFileWriter?.writeData2File(bytes)
                hexStringFileWriter?.writeData2File(bytes)
            }
        }
    }
}