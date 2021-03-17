package com.hwilliamgo.fuzzy_video_drill.camera

import android.view.SurfaceHolder

/**
 * date: 2021/3/16
 * author: HWilliamgo
 * description: 相机抽象
 */
interface ICamera {

    /**
     * 初始化
     * [surfaceHolder] 预览SurfaceHolder
     * [onCameraSizeReadyCallback]相机尺寸初始化回调
     */
    fun init(surfaceHolder: SurfaceHolder, onCameraSizeReadyCallback: OnCameraSizeReadyCallback)

    /**
     * 设置相机预览数据回调
     */
    fun setPreviewCallback(callback: PreviewCallback)

    /**
     * 销毁、释放资源
     */
    fun destroy()

    /**
     * 相机预览回调
     */
    fun interface PreviewCallback {
        /**
         *[frameData]可能指向同一块缓冲区，外部收到回调时，应该自行对该缓冲区进行拷贝再使用
         */
        fun onPreviewFrame(frameData: ByteArray)
    }

    /**
     * 相机尺寸回调
     */
    fun interface OnCameraSizeReadyCallback {
        fun onCameraSizeReady(width: Int, height: Int)
    }
}