package com.hwilliamgo.fuzzy_video_drill.scene.rtmppush

import android.Manifest
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
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
import com.hwilliamgo.livertmp.jni.X264Jni

class RtmpPushActivity : AppCompatActivity() {
    companion object {
        const val BITRATE = 720 * 1024 * 1024
        const val FRAME_RATE = 30
    }

    private val rtmpPushSurfaceView: SurfaceView by lazy { findViewById(R.id.rtmp_push_surface_view) }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtmp_push)
        RtmpPushManager.init()
//        X264Jni.init()
        camera = CameraFactory.createCamera(CameraImplType.CAMERA_1)

        rtmpPushSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                requestPermission(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) {
                    camera?.init(holder) { w, h ->
                        RtmpPushManager.setVideoEncoderInfo(w, h, FRAME_RATE, BITRATE)
                        RtmpPushManager.start(BuildConfig.RTMP_PUSH_URL)
//                        X264Jni.setVideoCodecInfo(w, h, FRAME_RATE, BITRATE)
                    }
                    camera?.setPreviewCallback { yuvData ->
                        RtmpPushManager.pushVideo(yuvData)
//                        X264Jni.encode(yuvData)
                    }
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
                LogUtils.d("surfaceChanged $holder, format=$format , width=$width, height=$height")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                LogUtils.d("surfaceDestroyed $holder")
            }
        })

        X264Jni.setOnX264JniEncodeListener {
            fileWriter.writeData2File(it)
            byteFileWriter.writeData2File(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        camera?.destroy()
        fileWriter.destroy()
        byteFileWriter.destroy()
//        X264Jni.destroy()
        RtmpPushManager.stop()
        RtmpPushManager.release()
    }
}