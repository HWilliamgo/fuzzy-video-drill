package com.hwilliamgo.fuzzy_video_drill.scene.videocall

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Environment
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.widget.Button
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.hwilliamgo.fuzzy_video_drill.camera.CameraFactory
import com.hwilliamgo.fuzzy_video_drill.camera.CameraImplType
import com.hwilliamgo.fuzzy_video_drill.camera.ICamera
import com.hwilliamgo.fuzzy_video_drill.codec.H265Encoder
import com.hwilliamgo.fuzzy_video_drill.codec.IEncoder
import com.hwilliamgo.fuzzy_video_drill.util.YuvUtils
import com.hwilliamgo.fuzzy_video_drill.videowidget.ILocalCameraSurfaceView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
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
    private var isCapture = false
    private var captureIndex = 0
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="构造器">
    init {
        initView()
        initCamera()
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
                    initEncoder()
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
        ActivityUtils.getTopActivity()
            ?.findViewById<ViewGroup>(android.R.id.content)
            ?.addView(Button(context).apply {
                setOnClickListener {
                    isCapture = true
                }
            },ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化相机">
    private fun initCamera() {
        camera = CameraFactory.createCamera(CameraImplType.CAMERA_1)
        camera?.setPreviewCallback { data ->
            val buffer = ByteArray(data.size)
            data.copyInto(buffer)
//            cameraPreviewThreadPool.submit {

//                encoderThreadPoolExecutor.submit {
            YuvUtils.rotateYUVClockwise90(data, cameraWidth, cameraHeight)
            val nv12 = YuvUtils.nv21toNV12(data)
            encoder?.encodeFrame(nv12)
            if (isCapture) {
                isCapture = false
                saveYuv(nv12)
            }
//                }
//            }
        }
    }

    //通过输出的yuv  420P 的数据到本地，用ffplay查看是正常的。说明问题不在相机数据捕获，在编码器
    private fun saveYuv(data: ByteArray) {
        val fileName = "IMG_${captureIndex++}.yuv"
        val sdRoot = Environment.getExternalStorageDirectory()
        val pictureFile = File(sdRoot, fileName)

        if (pictureFile.exists()) {
            pictureFile.delete()
        }
        val createRet = pictureFile.createNewFile()
        if (createRet) {
            try {
                val fos = FileOutputStream(pictureFile)
                fos.channel.write(ByteBuffer.wrap(data))
                LogUtils.d("saveYuv done")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            ToastUtils.showShort("create file failed")
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化编码器">
    private fun initEncoder() {
        encoder = H265Encoder()
        encoder?.init(cameraHeight, cameraWidth) { encodedFrame ->
            onCameraFrameEncodedCallback?.onCameraPreviewFrameEncoded(encodedFrame)
        }
        encoder?.start()
    }
    // </editor-fold>

    // </editor-fold>
}