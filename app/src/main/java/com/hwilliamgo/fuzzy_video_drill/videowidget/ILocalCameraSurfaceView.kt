package com.hwilliamgo.fuzzy_video_drill.videowidget

/**
 * date: 3/24/21
 * author: HWilliamgo
 * description:本地相机SurfaceView
 */
interface ILocalCameraSurfaceView {
    /**
     * 设置相机数据回调
     */
    fun setCameraFrameEncodedCallback(onCameraFrameEncodedCallback: OnCameraFrameEncodedCallback)

    /**
     * 销毁
     */
    fun destroy()

    fun interface OnCameraFrameEncodedCallback {
        /**
         * 相机数据回调，[byteArray]已编码
         */
        fun onCameraPreviewFrameEncoded(byteArray: ByteArray)
    }

}