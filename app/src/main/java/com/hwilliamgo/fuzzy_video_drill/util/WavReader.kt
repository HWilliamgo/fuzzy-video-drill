package com.hwilliamgo.fuzzy_video_drill.util

import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.nio.ByteBuffer
import kotlin.concurrent.thread

/**
 * date: 2021/4/11
 * author: HWilliamgo
 * description: 读取WAV文件
 */
object WavReader {
    private val TAG = WavReader::class.java.simpleName

    fun readWAV(
        wavFile: File,
        onErrorListener: (msg: String) -> Unit,
        onSampleRead: (sampleData: ByteArray) -> Unit
    ): Stopper {
        val stopper = Stopper()

        thread {
            if (!wavFile.isFile || !wavFile.exists()) {
                onErrorListener("inputFile $wavFile not exist")
                return@thread
            }

            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(wavFile.absolutePath)
            val audioIndex = MediaTrackSelector.selectTrack(mediaExtractor, true)
            mediaExtractor.selectTrack(audioIndex)

            val format = mediaExtractor.getTrackFormat(audioIndex)
            val bufferSize: Int = if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            } else {
                //100 KB
                100 * 1000
            }
            val byteBuffer = ByteBuffer.allocate(bufferSize)

            var isEnd = false
            while (!isEnd && !stopper.isStop()) {
                val sampleTime = mediaExtractor.sampleTime
                if (sampleTime >= 0) {
                    Log.v(TAG, "sampleTime=$sampleTime")
                    val sampleSize = mediaExtractor.readSampleData(byteBuffer, 0)
                    val byteArray = ByteArray(sampleSize)
                    byteBuffer.get(byteArray, 0, sampleSize)
                    onSampleRead(byteArray)
                    mediaExtractor.advance()
                } else {
                    isEnd = true
                    Log.d(TAG, "all sample has been read.")
                }
            }
        }
        return stopper
    }
}