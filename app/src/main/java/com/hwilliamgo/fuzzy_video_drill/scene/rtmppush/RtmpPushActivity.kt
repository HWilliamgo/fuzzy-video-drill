package com.hwilliamgo.fuzzy_video_drill.scene.rtmppush

import android.Manifest
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.hwilliamgo.fuzzy_video_drill.R
import com.hwilliamgo.fuzzy_video_drill.camera.CameraFactory
import com.hwilliamgo.fuzzy_video_drill.camera.CameraImplType
import com.hwilliamgo.fuzzy_video_drill.camera.ICamera
import com.hwilliamgo.fuzzy_video_drill.util.file.FileWriterFactory
import com.hwilliamgo.fuzzy_video_drill.util.file.FileWriterType
import com.hwilliamgo.fuzzy_video_drill.util.file.IFileWriter
import com.hwilliamgo.livertmp.jni.X264Jni
import com.william.fastpermisssion.FastPermission
import com.william.fastpermisssion.OnPermissionCallback
import java.util.*

class RtmpPushActivity : AppCompatActivity() {
    private val rtmpPushSurfaceView: SurfaceView by lazy { findViewById(R.id.rtmp_push_surface_view) }
    private var camera: ICamera? = null
    private var fileWriter: IFileWriter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rtmp_push)
        fileWriter = FileWriterFactory.newFileWriter(FileWriterType.HEX_STRING_WRITER, "x264_encod_output.txt")
        X264Jni.init()
        X264Jni.setOnX264JniEncodeListener { data ->
            LogUtils.d(data.size)
            fileWriter?.writeData2File(data)
        }
        camera = CameraFactory.createCamera(CameraImplType.CAMERA_1)

        rtmpPushSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                requestPermission {
                    camera?.init(holder) { w, h ->
                        X264Jni.setVideoCodecInfo(w, h, 30, 720 * 1024)
                    }
                    camera?.setPreviewCallback { yuvData ->
                        X264Jni.encode(yuvData)
                    }
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

    override fun onDestroy() {
        super.onDestroy()
        camera?.destroy()
        X264Jni.destroy();
        fileWriter?.destroy()
    }

    private fun requestPermission(cb: () -> Unit) {
        FastPermission.getInstance().start(this, arrayListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), object : OnPermissionCallback {
            override fun onAllGranted() {
                cb()
            }

            override fun onGranted(grantedPermissions: ArrayList<String>?) {
            }

            override fun onDenied(deniedPermissions: ArrayList<String>?) {
            }

            override fun onDeniedForever(deniedForeverP: ArrayList<String>?) {
            }
        })
    }


}