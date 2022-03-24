package com.hwilliamgo.fuzzy_video_drill.util.file.inner

import android.content.Context
import android.os.Environment
import com.blankj.utilcode.util.LogUtils
import com.hwilliamgo.fuzzy_video_drill.util.file.FileWriterConfig
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
abstract class BaseFileWriter(context: Context, outputFileName: String) : IFileWriter {
    protected var fos: FileOutputStream? = null
    protected val fosThread = Executors.newSingleThreadExecutor()
    private val outputFile: File

    init {
        val parentDir = if (FileWriterConfig.STORE_IN_SD_CARD_ROOT) {
            Environment.getExternalStorageDirectory()
        } else {
            context.externalCacheDir
        }
        val pictureFile = File(parentDir, outputFileName)

        if (pictureFile.exists()) {
            pictureFile.delete()
        }
        outputFile = pictureFile
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

    override fun getOutputFile(): File {
        return outputFile
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