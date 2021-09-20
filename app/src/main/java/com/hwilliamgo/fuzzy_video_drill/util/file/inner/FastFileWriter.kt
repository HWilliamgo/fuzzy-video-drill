package com.hwilliamgo.fuzzy_video_drill.util.file.inner

import java.io.IOException
import java.nio.ByteBuffer

/**
 * date: 3/27/21
 * author: HWilliamgo
 * description: 快捷文件输出器，写在SD卡根目录下
 */
class FastFileWriter(outputFileName: String) : BaseFileWriter(outputFileName) {

    override fun writeData2File(data: ByteArray) {
        if (!fosThread.isShutdown) {
            fosThread.submit {
                try {
                    fos?.channel?.write(ByteBuffer.wrap(data))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}