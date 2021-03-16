package com.hwilliamgo.fuzzy_video_drill.camera

import android.hardware.Camera
import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import com.blankj.utilcode.util.LogUtils
import java.io.IOException

/**
 * date: 2021/3/16
 * author: HWilliamgo
 * description:
 */
class SimpleCamera : ICamera {
    /**** Camera ****/
    private var camera: Camera? = null
    private var cameraWidth = 0
    private var cameraHeight = 0

    /**** thread ****/
    private var cameraHandlerThread: HandlerThread? = null
    private var cameraHandler: Handler? = null

    // camera preview buffer
    private var buffer: ByteArray? = null

    /**** surface ****/
    private var surfaceHolder: SurfaceHolder? = null

    /**** 监听器 ****/
    private var onPreviewCallback: ICamera.PreviewCallback? = null
    private var onCameraSizeReadyCallback: ICamera.OnCameraSizeReadyCallback? = null


    override fun init(
        surfaceHolder: SurfaceHolder,
        onCameraSizeReadyCallback: ICamera.OnCameraSizeReadyCallback
    ) {
        this.surfaceHolder = surfaceHolder
        this.onCameraSizeReadyCallback = onCameraSizeReadyCallback

        cameraHandlerThread = object : HandlerThread("cameraHandlerThread") {
            override fun onLooperPrepared() {
                cameraHandler = Handler()
                cameraHandler?.post {
                    startPreviewCamera()
                }
            }
        }
        cameraHandlerThread?.start()
    }

    override fun setPreviewCallback(callback: ICamera.PreviewCallback) {
        this.onPreviewCallback = callback
    }

    override fun destroy() {
        camera?.release()
        cameraHandler?.removeCallbacksAndMessages(null)
        cameraHandlerThread?.quit()
    }

    private fun startPreviewCamera() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        camera?.let {
            val parameters = it.parameters
            cameraWidth = parameters.previewSize.width
            cameraHeight = parameters.previewSize.height
            onCameraSizeReadyCallback?.onCameraSizeReady(cameraWidth, cameraHeight)
            try {
                it.setPreviewDisplay(surfaceHolder)
                it.setDisplayOrientation(90)
                buffer = ByteArray(cameraWidth * cameraHeight * 3 / 2)
                LogUtils.d("buffer hash=${buffer.hashCode()}")
                it.addCallbackBuffer(buffer)
                it.setPreviewCallbackWithBuffer { data, camera ->

                    onPreviewCallback?.onPreviewFrame(data)

                    camera.addCallbackBuffer(data)
                }
                it.startPreview()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}