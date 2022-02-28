package com.hwilliamgo.fuzzy_video_drill.util.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.WorkerThread
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils

/**
 * @date: 2022/1/6
 * @author: HWilliamgo
 * @description: 音频录音器，会回调pcm数据出去
 */
class AudioRecorder {
    companion object {
        const val SAMPLE_RATGE = 44100
        const val CHANNELS = AudioFormat.CHANNEL_IN_STEREO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }

    /**** AudioRecord ****/
    private var minBufferSize = 0
    private var audioRecord: AudioRecord? = null
    private var buffer: ByteArray? = null
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null

    @Volatile
    private var isRunning = false

    /**** Listener ****/
    private var audioCallDataback: AudioCallDataback? = null

    fun init(bufferSizeInByte: Int) {
        if (bufferSizeInByte <= 0) {
            LogUtils.e("bufferSizeInByte <= 0 , can't init AudioRecorder")
            return
        }
        minBufferSize = bufferSizeInByte
        LogUtils.d("bufferSizeInByte=$minBufferSize")
        buffer = ByteArray(minBufferSize)

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
                    .setChannelMask(CHANNELS)
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
            while (isRunning) {
                val b = buffer ?: continue
                audioRecord?.read(b, 0, b.size)
                audioCallDataback?.onAudioData(b)
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
         */
        @WorkerThread
        fun onAudioData(pcmData: ByteArray)
    }
}