package com.hwilliamgo.fuzzy_video_drill.videowidget

/**
 * date: 3/24/21
 * author: HWilliamgo
 * description:远端流视图
 */
interface IRemoteStreamSurfaceView {
    /**
     * 输入未解码的数据
     */
    fun feedEncodedData(encodedData: ByteArray)

    /**
     * 销毁
     */
    fun destroy()
}