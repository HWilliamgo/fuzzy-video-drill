package com.hwilliamgo.fuzzy_video_drill.util

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Environment
import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.hwilliamgo.fuzzy_video_drill.util.file.HexStringFileWriter
import com.hwilliamgo.fuzzy_video_drill.util.file.IFileWriter
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

/**
 * date: 3/31/21
 * author: HWilliamgo
 * description: 音频剪裁器
 */
object AudioClipper {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private val TAG = AudioClipper::class.java.simpleName
    private const val CODEC_TIMEOUT = 100000L
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    /**
     * 剪裁音频
     */
    @SuppressLint("WrongConstant")
    fun clip(inputPath: String, outputFileName: String, startTime: Long, endTime: Long) {
        if (endTime < startTime) {
            return
        }
        val fileWriter: IFileWriter = HexStringFileWriter("audio-clip.txt")

        val outputTmpPcm = File(Utils.getApp().cacheDir, "outputTmpPcm.pcm")
        val writeChannel = FileOutputStream(outputTmpPcm).channel

        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(inputPath)
        //选择音轨
        val audioTrack = selectAudioTrack(mediaExtractor)
        mediaExtractor.selectTrack(audioTrack)
        //seek 到开始时间
        mediaExtractor.seekTo(startTime, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

        val audioFormat = mediaExtractor.getTrackFormat(audioTrack)

        //获取buffer size，或者使用默认值
        val maxBufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        } else {
            100 * 1000
        }

        val buffer = ByteBuffer.allocateDirect(maxBufferSize)

        val mediaCodec = MediaCodec.createDecoderByType(audioFormat.getString(MediaFormat.KEY_MIME))
        mediaCodec.configure(audioFormat, null, null, 0)
        mediaCodec.start()
        val info = MediaCodec.BufferInfo()
        var outputBufferIndex = -1
        while (true) {
            val inputBufferIndex = mediaCodec.dequeueInputBuffer(CODEC_TIMEOUT)
            if (inputBufferIndex >= 0) {
                //todo log mediaExtractor.sampleTime
                val sampleTime = mediaExtractor.sampleTime
                Log.d(TAG, "sampleTime=$sampleTime")
                when (sampleTime) {
                    // no more samples are available.
                    -1L -> {
                        Log.d(TAG, "no more samples are available")
                        break
                    }
                    // skip the sample not between ths time section
                    in 0 until startTime -> {
                        mediaExtractor.advance()
                        continue
                    }
                    in startTime..endTime -> {
                        info.size = mediaExtractor.readSampleData(buffer, 0)
                        info.presentationTimeUs = sampleTime
                        info.flags = mediaExtractor.sampleFlags

                        val content = ByteArray(buffer.remaining())
                        buffer.get(content)
                        //todo 输出到文件
                        fileWriter.writeData2File(content)
                        val inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex) ?: continue
                        inputBuffer.put(content)
                        mediaCodec.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            info.size,
                            info.presentationTimeUs,
                            info.flags
                        )
                        mediaExtractor.advance()
                    }
                    else -> {
                        //已经超过了给定的范围，不再读取数据
                        break
                    }
                }
            }
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, CODEC_TIMEOUT)
            while (outputBufferIndex >= 0) {
                val outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
                writeChannel.write(outputBuffer)
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(info, CODEC_TIMEOUT)
            }
        }

        writeChannel.close()
        mediaExtractor.release()
        mediaCodec.stop()
        mediaCodec.release()

        // TODO: 3/31/21 convert output pcm file to wav file to play easily
        val sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val channelConfig =
            if (channelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
        val pcmToWavUtil =
            PcmToWavUtil(sampleRate, channelConfig, channelCount, AudioFormat.ENCODING_PCM_16BIT)
        val outputWavFile = File(Environment.getExternalStorageDirectory(), "$outputFileName.wav")
        pcmToWavUtil.pcmToWav(outputTmpPcm.absolutePath, outputWavFile.absolutePath)
        fileWriter.destroy()
        Log.d(
            TAG,
            "successfully clip audio, input :$inputPath, output:${outputWavFile.absolutePath}"
        )
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="选择轨道">
    private fun selectAudioTrack(mediaExtractor: MediaExtractor): Int {
        val numTracks: Int = mediaExtractor.trackCount
        for (i in 0 until numTracks) {
            val format: MediaFormat = mediaExtractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime.startsWith("audio/")) {
                return i
            }
        }
        return -1
    }
    // </editor-fold>
}