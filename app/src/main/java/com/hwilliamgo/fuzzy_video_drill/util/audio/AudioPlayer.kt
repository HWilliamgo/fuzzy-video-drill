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
    private var audioTrack: AudioTrack? = null

    fun start() {
        val audioAttr = AudioAttributes.Builder()
            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
            .build()
        val audioFormat = AudioFormat.Builder()
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .setSampleRate(44100)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
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