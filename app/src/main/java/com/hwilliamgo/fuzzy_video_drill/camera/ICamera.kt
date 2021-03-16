package com.hwilliamgo.fuzzy_video_drill.camera

import android.view.SurfaceHolder

/**
 * date: 2021/3/16
 * author: HWilliamgo
 * description:
 */
interface ICamera {

    fun init(surfaceHolder: SurfaceHolder, onCameraSizeReadyCallback: OnCameraSizeReadyCallback)

    fun setPreviewCallback(callback: PreviewCallback)

    fun destroy()

    fun interface PreviewCallback {
        /**
         *[frameData]可能指向同一块缓冲区，外部收到回调时，应该自行对该缓冲区进行拷贝再使用
         */
        fun onPreviewFrame(frameData: ByteArray)
    }

    fun interface OnCameraSizeReadyCallback {
        fun onCameraSizeReady(width: Int, height: Int)
    }
}