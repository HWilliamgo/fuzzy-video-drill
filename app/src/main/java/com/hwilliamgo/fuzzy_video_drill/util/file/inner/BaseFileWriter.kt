package com.hwilliamgo.fuzzy_video_drill.util.file.inner

import android.os.Environment
import com.blankj.utilcode.util.LogUtils
import com.hwilliamgo.fuzzy_video_drill.util.file.IFileWriter
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.Executors

/**
 * date: 3/28/21
 * author: HWilliamgo
 * description:包含一个FileOutputStream和一个单线程线程池，用于写入文件
 */
abstract class BaseFileWriter(outputFileName: String) : IFileWriter {
    protected var fos: FileOutputStream? = null
    protected val fosThread = Executors.newSingleThreadExecutor()

    init {
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

    override fun destroy() {
        try {
            fos?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        fos = null
        fosThread.shutdown()
    }
}