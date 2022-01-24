package com.hwilliamgo.fuzzy_video_drill.scene.rtmppush

import android.Manifest
import android.os.Bundle
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
import com.hwilliamgo.fuzzy_video_drill.util.audio.AudioRecorder
import com.hwilliamgo.fuzzy_video_drill.util.file.FileWriterFactory
import com.hwilliamgo.fuzzy_video_drill.util.file.FileWriterType
import com.hwilliamgo.fuzzy_video_drill.util.file.IFileWriter
import com.hwilliamgo.fuzzy_video_drill.util.video.VideoCapture
import com.hwilliamgo.fuzzy_video_drill.util.video.YuvUtils

class RtmpPushActivity : AppCompatActivity() {
    companion object {
        const val BITRATE = 1080 * 1920
        const val FRAME_RATE = 30
    }

    /**** View ****/
    private val rtmpPushPreviewView: PreviewView by lazy { findViewById(R.id.rtmp_push_preview_view) }

    /**** 核心引擎 ****/
    private var camera: ICamera? = null
    private var audioRecorder: AudioRecorder? = null

    /**** 本地文件记录 ****/
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

    /**** 状态变量 ****/
    private var cameraWidth = 0
    private var cameraHeight = 0
    private var isCapture = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtmp_push)
        RtmpPushManager.init()
        requestPermission(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) {
            camera = CameraFactory.createCamera(this, CameraImplType.CAMERA_X)
            rtmpPushPreviewView.addOnAttachStateChangeListener(object :
                View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View?) {
                    initCamera()
                }

                override fun onViewDetachedFromWindow(v: View?) {}
            })

            RtmpPushManager.setAudioEncoderInfo(
                AudioRecorder.SAMPLE_RATGE,
                2
            )// channels这里只能写1或者2，不能用AudioFormat的常量
            audioRecorder = AudioRecorder()
            audioRecorder?.init()
            audioRecorder?.setAudioDataCallback(object : AudioRecorder.AudioCallDataback {
                override fun onAudioData(pcmData: ByteArray) {
                    LogUtils.d("pcmData=$pcmData")
                    // todo 取消注释
                    RtmpPushManager.pushAudio(pcmData)
                }
            })
            audioRecorder?.startRecord()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.destroy()
        audioRecorder?.destroy()
        fileWriter.destroy()
        byteFileWriter.destroy()
        RtmpPushManager.stop()
        RtmpPushManager.release()
    }

    private fun initCamera() {
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
            // YUV旋转
            YuvUtils.rotateYUVClockwise90(frameData, cameraWidth, cameraHeight)
            RtmpPushManager.pushVideo(frameData)
        }
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.rtmp_push_btn_capture -> {
                isCapture = true
            }
        }
    }
}