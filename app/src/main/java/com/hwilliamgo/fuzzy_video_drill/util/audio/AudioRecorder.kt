package com.hwilliamgo.fuzzy_video_drill.util.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.WorkerThread
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.hwilliamgo.fuzzy_video_drill.util.file.inner.HexStringFileWriter

/**
 * @date: 2022/1/6
 * @author: HWilliamgo
 * @description: 音频录音器，会回调pcm数据出去
 */
class AudioRecorder {
    companion object {
        private val TAG = AudioRecorder::class.java.simpleName
        const val SAMPLE_RATGE = 44100
        const val CHANNELS_MASK = AudioFormat.CHANNEL_IN_MONO
        const val CHANNELS_COUNT = 1
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    /**** AudioRecord ****/
    // AudioRecord的最小buffer大小
    private var minBufferSize = 0

    // AudioRecord的buffer
    private var audioRecordBuffer: ByteArray? = null

    // 当前类回调出去的buffer长度大小
    private var outputBufferSize = 0

    // 当前类回调出去的buffer
    private var outputBuffer: ByteArray? = null
    private var outputBufferForTest: ByteArray? = null

    private var audioRecord: AudioRecord? = null

    // 工作线程
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    @Volatile
    private var isRunning = false

    /**** Listener ****/
    private var audioCallDataback: AudioCallDataback? = null

    fun init(outputBufferSize: Int) {
        if (outputBufferSize <= 0) {
            LogUtils.e("bufferSizeInByte <= 0 , can't init AudioRecorder")
            return
        }

        minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATGE, CHANNELS_MASK, AUDIO_FORMAT)
        audioRecordBuffer = ByteArray(minBufferSize)

        this.outputBufferSize = outputBufferSize
        outputBuffer = ByteArray(this.outputBufferSize)
        outputBufferForTest = ByteArray(minBufferSize)

        LogUtils.d("outputBufferSize=$outputBufferSize, minBufferSize=$minBufferSize")

        handlerThread = object : HandlerThread(AudioRecorder::class.java.name) {
            override fun onLooperPrepared() {
                LogUtils.d("onLooperPrepared")
                handler = Handler(handlerThread?.looper)
            }
        }
        handlerThread?.start()
    }

    fun startRecord() {
        stopRecord()
        if (!PermissionUtils.isGranted(Manifest.permission.RECORD_AUDIO)) {
            return
        }
        isRunning = true

        audioRecord = AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(
                AudioFormat.Builder()
                    .setChannelMask(CHANNELS_MASK)
                    .setSampleRate(SAMPLE_RATGE)
                    .setEncoding(AUDIO_FORMAT)
                    .build()
            )
            .setBufferSizeInBytes(minBufferSize)
            .build()
        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            LogUtils.e("audioRecord 状态异常")
        } else {
            audioRecord?.startRecording()
        }
        handler?.post {
            var fullCopyCount = 0;
            var leftByteCount = 0;
            if (minBufferSize > outputBufferSize) {
                fullCopyCount = minBufferSize / outputBufferSize
                leftByteCount = minBufferSize % outputBufferSize
            }
            val outputStr = StringBuilder()
            val outputTestStr = StringBuilder()

            Log.d(
                TAG,
                "minBufferSize=$minBufferSize, outputBufferSize=$outputBufferSize, fullCopyCount=$fullCopyCount, " +
                        "leftByteCount=$leftByteCount, audioRecordBuffer=${audioRecordBuffer?.size}"
            )

            while (isRunning) {
                outputStr.clear()
                outputTestStr.clear()

                val b = audioRecordBuffer ?: continue
                val output = outputBuffer ?: continue
                val outputTest = outputBufferForTest ?: continue

                audioRecord?.read(b, 0, b.size)
                Log.d(TAG,"source=\n${HexStringFileWriter.printHexBinary(b)}")

//                if (minBufferSize > outputBufferSize) {

                for (i in 0 until fullCopyCount) {
                    System.arraycopy(b, i * outputBufferSize, output, 0, outputBufferSize)
                    outputStr.append("$i ->\n").append(HexStringFileWriter.printHexBinary(output))
                    Log.d(TAG, "$i -> ${HexStringFileWriter.printHexBinary(output)}")
                    audioCallDataback?.onAudioDara2(
                        output,
                        outputBufferSize,
                        outputBufferSize
                    )
                }
                if (leftByteCount > 0) {
                    System.arraycopy(
                        b,
                        minBufferSize - leftByteCount,
                        output,
                        0,
                        leftByteCount
                    )
                    outputStr.append("$fullCopyCount ->\n")
                        .append(HexStringFileWriter.printHexBinary(output))
                    Log.d(TAG, "$fullCopyCount -> ${HexStringFileWriter.printHexBinary(output)}")
                    audioCallDataback?.onAudioDara2(output, leftByteCount, outputBufferSize)
                }

//                } else {

                System.arraycopy(b, 0, outputTest, 0, minBufferSize)
                audioCallDataback?.onAudioData(outputTest, minBufferSize, outputBufferSize)
                outputTestStr.append("\n").append(HexStringFileWriter.printHexBinary(outputTest))

//                Log.d(TAG,"outputStr=$outputStr")
                Log.d(TAG, " outputTestStr=$outputTestStr")
//                }
            }
        }
    }

    fun stopRecord() {
        isRunning = false
        audioRecord?.apply {
            if (state == AudioRecord.STATE_INITIALIZED) {
                stop()
            }
        }
        audioRecord = null
    }

    fun setAudioDataCallback(audioCallDataback: AudioCallDataback) {
        this.audioCallDataback = audioCallDataback
    }

    fun destroy() {
        audioCallDataback = null
        stopRecord()
    }

    /**
     * 音频数据回调监听器
     */
    interface AudioCallDataback {
        /**
         * 在工作线程中回调音频数据
         * [pcmData] 裸pcm数据，格式参考[AudioRecorder]的伴生对象
         * [end] 最后一位有效数据的下标
         * [length] 数组长度
         */
        @WorkerThread
        fun onAudioData(pcmData: ByteArray, end: Int, length: Int)

        /**
         * 测试用
         */
        fun onAudioDara2(pcmData: ByteArray, end: Int, length: Int);
    }
}