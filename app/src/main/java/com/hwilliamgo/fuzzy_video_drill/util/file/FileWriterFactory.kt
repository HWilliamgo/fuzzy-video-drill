package com.hwilliamgo.fuzzy_video_drill.util.file

import com.hwilliamgo.fuzzy_video_drill.util.file.inner.FastFileWriter
import com.hwilliamgo.fuzzy_video_drill.util.file.inner.HexStringFileWriter

/**
 * @date: 2021/9/19
 * @author: HWilliamgo
 * @description: 文件写入器工厂类
 */
object FileWriterFactory {
    fun newFileWriter(type: FileWriterType, outputFileName: String): IFileWriter {
        return when (type) {
            FileWriterType.FAST_WRITER -> FastFileWriter(outputFileName)
            FileWriterType.HEX_STRING_WRITER -> HexStringFileWriter(outputFileName)
        }
    }
}