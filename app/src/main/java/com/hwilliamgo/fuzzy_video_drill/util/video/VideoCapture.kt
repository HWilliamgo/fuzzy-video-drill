package com.hwilliamgo.fuzzy_video_drill.util.video

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.MediaScannerConnection
import android.os.Environment
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @date: 2021/12/12
 * @author: HWilliamgo
 * @description: 视频捕获工具类
 */
object VideoCapture {

    private val savedFileRootDir =
        Utils.getApp().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

    /**
     *捕获竖屏NV21的YUV数据并保存成图片，内部实现会自动对yuv做旋转操作
     * [data] 竖屏NV21的YUV数据
     * [cameraWidth] 相机宽度
     * [cameraHeight] 相机高度
     * [filePath] 文件路径
     */
    fun capturePortraitNV21(
        context: Context,
        data: ByteArray,
        cameraWidth: Int,
        cameraHeight: Int,
        filePath: File? = null
    ) {
        val pictureFile =
            if (filePath == null) {
                val fileName =
                    "IMG_VideoCapture.capturePortraitNV21_${System.currentTimeMillis()}.jpg"
                File(savedFileRootDir, fileName)
            } else {
                filePath
            }

        if (pictureFile.exists()) {
            pictureFile.delete()
        }
        val createRet = pictureFile.createNewFile()

        if (!createRet) {
            LogUtils.e("create file failed")
            return
        }

        // YUV旋转
        YuvUtils.rotateYUVClockwise90(data, cameraWidth, cameraHeight)

        try {
            val fos = FileOutputStream(pictureFile)
            //已经对YUV数据做了旋转，因此YUV宽高要和摄像头宽高互换
            val imageWidth = cameraHeight
            val imageHeight = cameraWidth
            val yuvImage = YuvImage(data, ImageFormat.NV21, imageWidth, imageHeight, null)
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, fos)
            LogUtils.d("compressToJpeg, width=$imageWidth, height=$imageHeight")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        MediaScannerConnection.scanFile(
            context, arrayOf(pictureFile.absolutePath), null
        ) { path, uri ->
            LogUtils.d("VideoCapture.capturePortraitNV21->MediaScannerConnection.scanFile complete path=${path}, uri=${uri}")
        }
    }
}