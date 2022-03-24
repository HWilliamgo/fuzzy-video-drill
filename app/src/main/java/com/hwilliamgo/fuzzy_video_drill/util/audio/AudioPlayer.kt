package com.hwilliamgo.fuzzy_video_drill.util.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack

/**
 * date: 2021/4/15
 * author: HWilliamgo
 * description: AudioTrack音频播放器
 */
class AudioPlayer {
    private var sampleRate = 44100
    private var encoding = AudioFormat.ENCODING_PCM_16BIT
    private var channelCount = 2


    private var audioTrack: AudioTrack? = null

    fun setConfig(sampleRate: Int, encoding: Int, channelCount: Int) {
        this.sampleRate = sampleRate
        this.encoding = encoding
        this.channelCount = channelCount
    }

    fun start() {
        val audioAttr = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .build()
        val audioFormat = AudioFormat.Builder()
            .setChannelMask(if (channelCount == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO)
            .setSampleRate(sampleRate)
            .setEncoding(encoding)
            .build()
        audioTrack = AudioTrack(
            audioAttr,
            audioFormat,
            2048,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        audioTrack?.play()
    }

    fun writeData(data: ByteArray) {
        audioTrack?.write(data, 0, data.size)
    }

    fun stop() {
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}