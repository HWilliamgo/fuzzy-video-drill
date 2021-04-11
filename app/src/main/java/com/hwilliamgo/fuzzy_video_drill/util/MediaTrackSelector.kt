package com.hwilliamgo.fuzzy_video_drill.util

import android.media.MediaExtractor
import android.media.MediaFormat

/**
 * date: 2021/4/11
 * author: HWilliamgo
 * description:
 */
object MediaTrackSelector {

    /**
     * 通过[mediaExtractor]读取音视频资源的所有的track，选择出对应的音频/视频track，并返回track索引
     */
    fun selectTrack(mediaExtractor: MediaExtractor, isAudio: Boolean): Int {
        val numTracks: Int = mediaExtractor.trackCount
        for (i in 0 until numTracks) {
            val format: MediaFormat = mediaExtractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime.startsWith(if (isAudio) "audio/" else "video/")) {
                return i
            }
        }
        return -1
    }
}