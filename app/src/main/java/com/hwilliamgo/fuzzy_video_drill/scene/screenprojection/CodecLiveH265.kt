package com.hwilliamgo.fuzzy_video_drill.scene.screenprojection

import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import com.hwilliamgo.fuzzy_video_drill.socket.ISocket
import java.nio.ByteBuffer
import kotlin.concurrent.thread

/**
 * date: 2021/3/4
 * author: HWilliamgo
 * description:
 */
class CodecLiveH265(
    private val socketPush: ISocket,
    private val mediaProjection: MediaProjection
) {
    companion object {
        const val NAL_I = 19
        const val NAL_VPS = 32
    }

    private var mediaCodec: MediaCodec? = null
    private var virtualDisplay: VirtualDisplay? = null

    private val width = 720
    private val height = 1280

    private var vps_sps_pps_buffer: ByteArray? = null

    @Volatile
    private var isRunning = false

    fun startLive() {
        //create format
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, width, height)
        format.apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            setInteger(MediaFormat.KEY_BIT_RATE, width * height)
            setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        //create codec
        mediaCodec = MediaCodec.createEncoderByType("video/hevc")
        mediaCodec?.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)

        //get input surface
        val surface = mediaCodec?.createInputSurface()

        //create screen projection display, and set input surface of codec to it.
        virtualDisplay = mediaProjection.createVirtualDisplay(
            "-display",
            width,
            height,
            1,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            surface,
            null,
            null
        )

        isRunning = true
        thread {
            val codec = mediaCodec ?: return@thread
            codec.start()
            val bufferInfo = MediaCodec.BufferInfo()
            while (isRunning) {
                try {
                    val outputBufferId = codec.dequeueOutputBuffer(bufferInfo, 10000)
                    if (outputBufferId >= 0) {
                        val byteBuffer = codec.getOutputBuffer(outputBufferId)!!
                        dealFrame(byteBuffer, bufferInfo)
                        codec.releaseOutputBuffer(outputBufferId, false);
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    break
                }

            }
        }
    }

    fun stopLive() {
        isRunning = false
    }

    private fun dealFrame(bb: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        //如果是00 00 00 01，那么是偏移到第4个元素来取出nalu type。
        //如果是00 00 01，   那么是偏移到第3个元素来取出nalu type。
        val offset: Int = if (bb.get(2).toInt() == 0x01) {
            3
        } else {
            4
        }

        val type = (bb.get(offset).toInt() and 0x7E) ushr 1
        when (type) {
            NAL_VPS -> {
                vps_sps_pps_buffer = ByteArray(bufferInfo.size)
                bb.get(vps_sps_pps_buffer)
            }
            NAL_I -> {
                val bytes = ByteArray(bufferInfo.size)
                bb.get(bytes)
                val vpsSpsPpsBufferTmp = vps_sps_pps_buffer!!
                val newBuf: ByteArray
                newBuf = ByteArray(vpsSpsPpsBufferTmp.size + bytes.size)
                System.arraycopy(vpsSpsPpsBufferTmp, 0, newBuf, 0, vpsSpsPpsBufferTmp.size)
                System.arraycopy(bytes, 0, newBuf, vpsSpsPpsBufferTmp.size, bytes.size)
                socketPush.sendData(newBuf)
            }
            else -> {
                val bytes = ByteArray(bufferInfo.size)
                bb.get(bytes)
                socketPush.sendData(bytes)
            }
        }
    }
}