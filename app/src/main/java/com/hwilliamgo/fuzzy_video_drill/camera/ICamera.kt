package com.hwilliamgo.fuzzy_video_drill.camera

import android.view.SurfaceHolder
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner

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
     * CameraX 专用初始化
     * 当使用了CameraX的时候，请使用这个初始化函数
     * [previewView] 渲染器
     * [lifecycleOwner] 生命周期宿主
     * [onCameraSizeReadyCallback] 相机尺寸回调
     */
    fun init(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onCameraSizeReadyCallback: ICamera.OnCameraSizeReadyCallback
    ) {

    }

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
        /**
         * 相机尺寸回调，Android的相机是横过来的，所以宽会比高要长。
         * [width]
         * [height]
         */
        fun onCameraSizeReady(width: Int, height: Int)
    }
}