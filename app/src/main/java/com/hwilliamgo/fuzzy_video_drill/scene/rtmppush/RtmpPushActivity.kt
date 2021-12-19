package com.hwilliamgo.fuzzy_video_drill.scene.rtmppush

import android.Manifest
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import com.blankj.utilcode.util.LogUtils
import com.hwilliamgo.fuzzy_video_drill.BuildConfig
import com.hwilliamgo.fuzzy_video_drill.R
import com.hwilliamgo.fuzzy_video_drill.camera.CameraFactory
import com.hwilliamgo.fuzzy_video_drill.camera.CameraImplType
import com.hwilliamgo.fuzzy_video_drill.camera.ICamera
import com.hwilliamgo.fuzzy_video_drill.ext.requestPermission
import com.hwilliamgo.fuzzy_video_drill.util.file.FileWriterFactory
import com.hwilliamgo.fuzzy_video_drill.util.file.FileWriterType
import com.hwilliamgo.fuzzy_video_drill.util.file.IFileWriter
import com.hwilliamgo.fuzzy_video_drill.util.video.VideoCapture
import com.hwilliamgo.fuzzy_video_drill.util.video.YuvUtils

class RtmpPushActivity : AppCompatActivity() {
    companion object {
        const val BITRATE = 720 * 1024 * 1024
        const val FRAME_RATE = 30
    }

    private val rtmpPushPreviewView: PreviewView by lazy { findViewById(R.id.rtmp_push_preview_view) }
    private var camera: ICamera? = null
    private val fileWriter: IFileWriter by lazy {
        FileWriterFactory.newFileWriter(
            FileWriterType.HEX_STRING_WRITER, "x264_encod_output.txt"
        )
    }
    private val byteFileWriter: IFileWriter by lazy {
        FileWriterFactory.newFileWriter(
            FileWriterType.FAST_WRITER, "byteData_x264_encode_output.h264"
        )
    }
    private var cameraWidth = 0
    private var cameraHeight = 0
    private var isCapture = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtmp_push)
        RtmpPushManager.init()
        requestPermission(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {
            camera = CameraFactory.createCamera(this, CameraImplType.CAMERA_X)
            rtmpPushPreviewView.addOnAttachStateChangeListener(object :
                View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View?) {
                    camera?.init(rtmpPushPreviewView, this@RtmpPushActivity) { width, height ->
                        this@RtmpPushActivity.cameraWidth = width
                        this@RtmpPushActivity.cameraHeight = height
                        RtmpPushManager.setVideoEncoderInfo(
                            cameraHeight,
                            cameraWidth,
                            FRAME_RATE,
                            BITRATE
                        )
                        RtmpPushManager.start(BuildConfig.RTMP_PUSH_URL)
                    }
                    camera?.setPreviewCallback { frameData ->
                        if (isCapture) {
                            isCapture = false
                            VideoCapture.capturePortraitNV21(
                                this@RtmpPushActivity,
                                frameData,
                                cameraWidth,
                                cameraHeight
                            )
                        }
                        LogUtils.d("当前线程是main线程吗？=${Thread.currentThread() == Looper.getMainLooper().thread}")
                        // YUV旋转
                        YuvUtils.rotateYUVClockwise90(frameData, cameraWidth, cameraHeight)
                        RtmpPushManager.pushVideo(frameData)
                    }
                }

                override fun onViewDetachedFromWindow(v: View?) {}
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.destroy()
        fileWriter.destroy()
        byteFileWriter.destroy()
        RtmpPushManager.stop()
        RtmpPushManager.release()
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.rtmp_push_btn_capture -> {
                isCapture = true
            }
        }
    }
}