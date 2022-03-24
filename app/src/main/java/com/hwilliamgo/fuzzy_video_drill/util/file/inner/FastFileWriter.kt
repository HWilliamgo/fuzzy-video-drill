package com.hwilliamgo.fuzzy_video_drill.util.file.inner

import android.content.Context
import java.io.IOException
import java.nio.ByteBuffer

/**
 * date: 3/27/21
 * author: HWilliamgo
 * description: 快捷文件输出器
 */
class FastFileWriter(context: Context, outputFileName: String) :
    BaseFileWriter(context, outputFileName) {

    override fun writeData2File(data: ByteArray, end: Int) {
        if (!fosThread.isShutdown) {
            fosThread.submit {
                try {
                    fos?.channel?.write(ByteBuffer.wrap(data, 0, end))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}