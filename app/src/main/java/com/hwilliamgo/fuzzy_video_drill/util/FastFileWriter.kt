package com.hwilliamgo.fuzzy_video_drill.util

import android.os.Environment
import com.blankj.utilcode.util.LogUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors

/**
 * date: 3/27/21
 * author: HWilliamgo
 * description: 快捷文件输出器，写在SD卡根目录下
 */
class FastFileWriter(outputFileName: String) {

    private var fos: FileOutputStream? = null
    private val fosThread = Executors.newSingleThreadExecutor()

    init {
        //create h265 file writer
        val sdRoot = Environment.getExternalStorageDirectory()
        val pictureFile = File(sdRoot, outputFileName)

        if (pictureFile.exists()) {
            pictureFile.delete()
        }
        val createRet = pictureFile.createNewFile()
        if (createRet) {
            try {
                fos = FileOutputStream(pictureFile)
                LogUtils.d("create fos")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun writeData2File(data: ByteArray) {
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

    fun destroy() {
        try {
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        fos = null
        fosThread.shutdown()
    }
}