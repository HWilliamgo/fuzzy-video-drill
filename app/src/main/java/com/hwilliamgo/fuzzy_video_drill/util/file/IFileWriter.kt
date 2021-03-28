package com.hwilliamgo.fuzzy_video_drill.util.file

/**
 * date: 3/28/21
 * author: HWilliamgo
 * description: 文件写入器
 */
interface IFileWriter {

    /**
     * 写入数据到文件
     */
    fun writeData2File(data: ByteArray)

    /**
     * 销毁
     */
    fun destroy()

}