package com.hwilliamgo.fuzzy_video_drill.scene.videocall

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.hwilliamgo.fuzzy_video_drill.codec.H265Decoder
import com.hwilliamgo.fuzzy_video_drill.codec.IDecoder
import com.hwilliamgo.fuzzy_video_drill.videowidget.IRemoteStreamSurfaceView

/**
 * date: 3/23/21
 * author: HWilliamgo
 * description: 远端流视图
 */
class RemoteStreamSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), IRemoteStreamSurfaceView {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private var decoder: IDecoder? = null

    //宽和高理论上应该通过其他方式获得
    private val remoteVideoWidth = 1920
    private var remoteVideoHeight = 1080
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    init {
        initView()
        initDecoder()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    override fun feedEncodedData(encodedData: ByteArray) {
        decoder?.decodeData(encodedData)
    }

    override fun destroy() {
        decoder?.stop()
        decoder?.destroy()
        decoder = null
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private fun initView() {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                decoder?.setOutputSurface(holder?.surface ?: return)
                decoder?.start()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {

            }
        })
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化解码器">
    private fun initDecoder() {
        decoder = H265Decoder()
        decoder?.init(remoteVideoWidth, remoteVideoHeight)
    }
    // </editor-fold>

}