package com.hwilliamgo.fuzzy_video_drill.util.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Environment
import android.util.Log
import com.blankj.utilcode.util.Utils
import com.hwilliamgo.fuzzy_video_drill.util.MediaTrackSelector
import com.hwilliamgo.fuzzy_video_drill.util.file.HexStringFileWriter
import com.hwilliamgo.fuzzy_video_drill.util.file.IFileWriter
import java.io.File
import java.io.FileInputStream
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
    private const val CODEC_TIMEOUT_SMALL = 1000L;
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
        val audioFormat = decodeToPcm(
            inputPath, outputTmpPcm.absolutePath, startTime, endTime
        ) { sampleData ->
            fileWriter.writeData2File(sampleData)
        } ?: return

        val sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val channelConfig = getChannelConfigFromCount(channelCount)
        val pcmToWavUtil =
            PcmToWavUtil(
                sampleRate,
                channelConfig,
                channelCount,
                AudioFormat.ENCODING_PCM_16BIT
            )
        val outputWavFile = File(Environment.getExternalStorageDirectory(), "$outputFileName.wav")
        pcmToWavUtil.pcmToWav(outputTmpPcm.absolutePath, outputWavFile.absolutePath)
        fileWriter.destroy()
        Log.d(
            TAG,
            "successfully clip audio, input :$inputPath, output:${outputWavFile.absolutePath}"
        )
    }

    /**
     * 混音
     *  [inputPathA]输入路径A
     *  [inputPathB]输入路劲B
     *  [outputPath]输出路径
     *  [startTimeUs]开始时间
     *  [endTimeUs]结束时间
     *  [inputAVolume]输入A的音量
     *  [inputBVolume]输入B的音量
     */
    fun mixAudioTrack(
        inputPathA: String,
        inputPathB: String,
        outputPath: String,
        startTimeUs: Long,
        endTimeUs: Long,
        inputAVolume: Int,
        inputBVolume: Int,
    ) {
        val inputFileA = File(inputPathA)
        val outputPcmA =
            File(
                inputFileA.parent,
                inputFileA.name.substringBefore(".").plus("PCM.pcm")
            )
        val inputFileB = File(inputPathB)
        val outputPcmB = File(
            inputFileB.parent,
            inputFileB.name.substringBefore(".").plus("PCM.pcm")
        )

        val formatA =
            decodeToPcm(inputPathA, outputPcmA.absolutePath, startTimeUs, endTimeUs) ?: return
        val sampleRateA = formatA.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCountA = formatA.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val channelConfigA = getChannelConfigFromCount(channelCountA)
        PcmToWavUtil(
            sampleRateA, channelConfigA, channelCountA, AudioFormat.ENCODING_PCM_16BIT
        ).pcmToWav(
            outputPcmA.absolutePath,
            File(
                outputPcmA.parent,
                outputPcmA.name.substringBefore(".").plus("WAV.wav")
            ).absolutePath
        )

        val formatB =
            decodeToPcm(inputPathB, outputPcmB.absolutePath, startTimeUs, endTimeUs) ?: return
        val sampleRateB = formatB.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCountB = formatB.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val channelConfigB = getChannelConfigFromCount(channelCountB)

        PcmToWavUtil(
            sampleRateB, channelConfigB, channelCountB, AudioFormat.ENCODING_PCM_16BIT
        ).pcmToWav(
            outputPcmB.absolutePath,
            File(
                outputPcmB.parent,
                outputPcmB.name.substringBefore(".").plus("WAV.wav")
            ).absolutePath
        )

        val outputFile = File(outputPath)
        val tmpOutputPCMFile =
            File(outputFile.parent, outputFile.name.substringBefore(".").plus("PCM.pcm"))
        mixPcm(
            outputPcmA.absolutePath,
            outputPcmB.absolutePath,
            tmpOutputPCMFile.absolutePath,
            inputAVolume,
            inputBVolume
        )

        PcmToWavUtil(
            sampleRateB, channelConfigB, channelCountB, AudioFormat.ENCODING_PCM_16BIT
        ).pcmToWav(tmpOutputPCMFile.absolutePath, outputPath)
        Log.d(TAG, "mixAudioTrack finished,output file is $outputPath")
    }

    /**
     * 混音
     *  [inputPathA]输入路径A
     *  [inputPathB]输入路劲B
     *  [outputPath]输出路径
     *  [inputAVolume]输入A的音量
     *  [inputBVolume]输入B的音量
     */
    private fun mixPcm(
        inputPathA: String,
        inputPathB: String,
        outputPath: String,
        inputAVolume: Int,
        inputBVolume: Int,
    ) {

        val makeVolumeAsPercent = fun(volume: Int): Float {
            return volume / 100f * 1
        }
        val volumeA = makeVolumeAsPercent(inputAVolume)
        val volumeB = makeVolumeAsPercent(inputBVolume)

        val bufferSize = 2048
        val inputBufferA = ByteArray(bufferSize)
        val inputBufferB = ByteArray(bufferSize)
        val outputBuffer = ByteArray(bufferSize)

        val isA = FileInputStream(inputPathA)
        val isB = FileInputStream(inputPathB)
        val fos = FileOutputStream(outputPath)

        var tmpVolumeA: Int
        var tmpVolumeB: Int
        var tmpVolumeOutput: Short

        var isAEnd = false
        var isBEnd = false
        while (!isAEnd || !isBEnd) {
            if (!isAEnd) {
                isAEnd = isA.read(inputBufferA) == -1
//                inputBufferA.copyInto(outputBuffer)
            }
            if (!isBEnd) {
                isBEnd = isB.read(inputBufferB) == -1
                for (i in inputBufferB.indices step 2) {
                    tmpVolumeA =
                        (inputBufferA[i].toInt() and 0xff) or ((inputBufferA[i + 1].toInt() and 0xff) shl 8)
//                    tmpVolumeA = valueWithinShort(tmpVolumeA).toInt()
                    tmpVolumeB =
                        (inputBufferB[i].toInt() and 0xff) or ((inputBufferB[i + 1].toInt() and 0xff) shl 8)
//                    tmpVolumeB = valueWithinShort(tmpVolumeB).toInt()
                    tmpVolumeOutput = (tmpVolumeA * volumeA + tmpVolumeB * volumeB).toInt().toShort()
                    if (tmpVolumeOutput > Short.MAX_VALUE) {
                        tmpVolumeOutput = Short.MAX_VALUE
                    } else if (tmpVolumeOutput < Short.MIN_VALUE) {
                        tmpVolumeOutput = Short.MIN_VALUE
                    }

                    outputBuffer[i] = (tmpVolumeOutput.toInt() and 0xFF).toByte()
                    outputBuffer[i + 1] = (tmpVolumeOutput.toInt() ushr 8 and 0xFF).toByte()
                }
                fos.write(outputBuffer)
            }
        }
        isA.close()
        isB.close()
        fos.close()
    }

    /**
     * 将音频文件解码成pcm文件
     * 1. init io reader and writer. reader: MediaExtractor; writer: OutputStream
     * 2. get audio format
     * 3. init extractor buffer
     * 4. init and start decoder
     * 5. loop, to read encoded audio to send to codec, and write output pcm data from codec to file.
     * 6. release resource
     *
     * @return MediaFormat for client to wrap pcm to wav
     */
    @SuppressLint("WrongConstant")
    private fun decodeToPcm(
        inputPath: String,
        outputPcmPath: String,
        startTimeUs: Long,
        endTimeUs: Long,
        deleteOld: Boolean = false,
        onReadInputSampleCallback: ((sampleData: ByteArray) -> Unit)? = null
    ): MediaFormat? {
        if (endTimeUs < startTimeUs) {
            Log.e(TAG, "endTimeUs < startTimeUs")
            return null
        }
        //1. init io reader and writer. reader: MediaExtractor; writer: OutputStream
        val audioTrackIndex: Int
        val mediaExtractor = MediaExtractor().apply {
            setDataSource(inputPath)
            audioTrackIndex = MediaTrackSelector.selectTrack(this, true)
            selectTrack(audioTrackIndex)
            seekTo(startTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        }

        val outputTmpPcm = File(outputPcmPath)


        //2. get audio format
        val audioFormat = mediaExtractor.getTrackFormat(audioTrackIndex)

        if (!deleteOld && outputTmpPcm.exists()) {
            return audioFormat
        }
        val writeChannel = FileOutputStream(outputTmpPcm).channel

        //3. init extractor buffer
        val maxBufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
            audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        } else {
            100 * 1000
        }
        Log.d(TAG, "media extractor buffer size=$maxBufferSize")
        val buffer = ByteBuffer.allocateDirect(maxBufferSize)

        //4. init and start decoder
        val mediaCodec = MediaCodec.createDecoderByType(audioFormat.getString(MediaFormat.KEY_MIME))
        mediaCodec.configure(audioFormat, null, null, 0)
        mediaCodec.start()

        //5. loop, to read encoded audio to send to codec, and write output pcm data from codec to file.
        val bufferInfo = MediaCodec.BufferInfo()
        var outputBufferIndex = -1
        while (true) {
            val inputBufferIndex = mediaCodec.dequeueInputBuffer(CODEC_TIMEOUT_SMALL)
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
                    // skip the sample before startTimeUs
                    in 0 until startTimeUs -> {
                        mediaExtractor.advance()
                        continue
                    }
                    // sampleTime between the range
                    in startTimeUs..endTimeUs -> {
                        bufferInfo.size = mediaExtractor.readSampleData(buffer, 0)
                        bufferInfo.presentationTimeUs = sampleTime
                        bufferInfo.flags = mediaExtractor.sampleFlags

                        val content = ByteArray(buffer.remaining())
                        buffer.get(content)
                        onReadInputSampleCallback?.invoke(content)
                        val inputBuffer = mediaCodec.getInputBuffer(inputBufferIndex) ?: continue
                        inputBuffer.put(content)
                        mediaCodec.queueInputBuffer(
                            inputBufferIndex,
                            0,
                            bufferInfo.size,
                            bufferInfo.presentationTimeUs,
                            bufferInfo.flags
                        )
                        mediaExtractor.advance()
                    }
                    // skip the sample over endTimeUs
                    else -> {
                        //已经超过了给定的范围，不再读取数据
                        break
                    }
                }
            }

            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, CODEC_TIMEOUT_SMALL)
            while (outputBufferIndex >= 0) {
                val outputBuffer = mediaCodec.getOutputBuffer(outputBufferIndex)
                writeChannel.write(outputBuffer)
                mediaCodec.releaseOutputBuffer(outputBufferIndex, false)
                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, CODEC_TIMEOUT_SMALL)
            }
        }

        //6. release resource
        mediaCodec.stop()
        mediaCodec.release()
        writeChannel.close()
        mediaExtractor.release()
        return audioFormat
    }
    // </editor-fold>

    private fun getChannelConfigFromCount(channelCount: Int): Int {
        return if (channelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
    }

    private fun valueWithinShort(value: Int): Short {
        return when {
            value > Short.MAX_VALUE -> {
                Short.MAX_VALUE
            }
            value < Short.MIN_VALUE -> {
                Short.MIN_VALUE
            }
            else -> {
                value.toShort()
            }
        }
    }
}