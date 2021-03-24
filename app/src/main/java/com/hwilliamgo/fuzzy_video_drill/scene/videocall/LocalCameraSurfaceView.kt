package com.hwilliamgo.fuzzy_video_drill.scene.videocall

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.hwilliamgo.fuzzy_video_drill.camera.CameraFactory
import com.hwilliamgo.fuzzy_video_drill.camera.CameraImplType
import com.hwilliamgo.fuzzy_video_drill.camera.ICamera
import com.hwilliamgo.fuzzy_video_drill.codec.H265Encoder
import com.hwilliamgo.fuzzy_video_drill.codec.IEncoder
import com.hwilliamgo.fuzzy_video_drill.util.YuvUtils
import com.hwilliamgo.fuzzy_video_drill.videowidget.ILocalCameraSurfaceView
import java.util.concurrent.Executors

/**
 * date: 2021/3/18
 * author: HWilliamgo
 * description: 本地相机SurfaceView
 */
class LocalCameraSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), ILocalCameraSurfaceView {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private var camera: ICamera? = null
    private var cameraPreviewThreadPool = Executors.newSingleThreadExecutor()
    private var encoderThreadPoolExecutor = Executors.newSingleThreadExecutor()
    private var cameraWidth = 0
    private var cameraHeight = 0
    private var encoder: IEncoder? = null
    private var onCameraFrameEncodedCallback: ILocalCameraSurfaceView.OnCameraFrameEncodedCallback? =
        null
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    init {
        initView()
        initCamera()
        initEncoder()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    override fun setCameraFrameEncodedCallback(onCameraFrameEncodedCallback: ILocalCameraSurfaceView.OnCameraFrameEncodedCallback) {
        this.onCameraFrameEncodedCallback = onCameraFrameEncodedCallback
    }

    override fun destroy() {
        cameraPreviewThreadPool.shutdown()
        encoderThreadPoolExecutor.shutdown()
        encoder?.stop()
        encoder?.destroy()
        encoder = null
        camera?.destroy()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private fun initView() {
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(h: SurfaceHolder?) {
                camera?.init(holder) { width, height ->
                    cameraWidth = width
                    cameraHeight = height
                }
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

    // <editor-fold defaultstate="collapsed" desc="初始化相机">
    private fun initCamera() {
        camera = CameraFactory.createCamera(CameraImplType.CAMERA_1)
        camera?.setPreviewCallback { data ->
            val buffer = ByteArray(data.size)
            data.copyInto(buffer)
            cameraPreviewThreadPool.submit {
                YuvUtils.rotateYUVClockwise90(buffer, cameraWidth, cameraHeight)
                val nv12 = YuvUtils.nv21toNV12(buffer)
                encoderThreadPoolExecutor.submit {
                    encoder?.encodeFrame(nv12)
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化编码器">
    private fun initEncoder() {
        encoder = H265Encoder()
        encoder?.init(width, height) { encodedFrame ->
            onCameraFrameEncodedCallback?.onCameraPreviewFrameEncoded(encodedFrame)
        }
        encoder?.start()
    }
    // </editor-fold>

    // </editor-fold>
}