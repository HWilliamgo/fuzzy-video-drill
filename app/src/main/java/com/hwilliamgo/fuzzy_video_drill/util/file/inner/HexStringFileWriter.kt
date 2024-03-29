package com.hwilliamgo.fuzzy_video_drill.util.file.inner

import java.io.IOException
import java.nio.ByteBuffer

/**
 * date: 3/28/21
 * author: HWilliamgo
 * description: 16进制字符串文件写入器
 */
class HexStringFileWriter(outputFileName: String) : BaseFileWriter(outputFileName) {
    private val hexCode = "0123456789ABCDEF".toCharArray()

    override fun writeData2File(data: ByteArray) {
        val str = printHexBinary(data)
        if (!fosThread.isShutdown) {
            fosThread.submit {
                try {
                    fos?.channel?.write(ByteBuffer.wrap(str.toByteArray()))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun printHexBinary(data: ByteArray): String {
        val r = StringBuilder(data.size * 2)
        for (b in data) {
            r.append(hexCode[b.toInt() shr 4 and 0xF])
            r.append(hexCode[b.toInt() and 0xF])
        }
        r.append("\n")
        return r.toString()
    }

}