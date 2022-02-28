package com.hwilliamgo.fuzzy_video_drill.scene.rtmppush

import com.hwilliamgo.livertmp.jni.RTMPX264Jni

/**
 * date: 2021/8/1
 * author: HWilliamgo
 * description: RTMP推流管理器
 */
object RtmpPushManager {
    fun init() {
        RTMPX264Jni.native_init()
    }

    fun setVideoEncoderInfo(width: Int, height: Int, fps: Int, bitrate: Int) {
        RTMPX264Jni.native_setVideoEncoderInfo(width, height, fps, bitrate)
    }

    fun setAudioEncoderInfo(sampleRate: Int, channels: Int): Int {
        return RTMPX264Jni.native_setAudioEncoderInfo(sampleRate, channels)
    }

    fun start(url: String) {
        RTMPX264Jni.native_start(url)
    }

    fun pushVideo(yuvData: ByteArray) {
        RTMPX264Jni.native_pushVideo(yuvData)
    }

    fun pushAudio(pcmData: ByteArray) {
        RTMPX264Jni.natvie_pushAudio(pcmData)
    }

    fun stop() {
        RTMPX264Jni.native_stop()
    }

    fun release() {
        RTMPX264Jni.native_release()
    }
}