package com.hwilliamgo.fuzzy_video_drill.scene.cameratest

import android.Manifest
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.Bundle
import android.os.Environment
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.hwilliamgo.fuzzy_video_drill.R
import com.hwilliamgo.fuzzy_video_drill.camera.CameraFactory
import com.hwilliamgo.fuzzy_video_drill.camera.CameraImplType
import com.hwilliamgo.fuzzy_video_drill.camera.ICamera
import com.hwilliamgo.fuzzy_video_drill.util.YuvUtils
import com.william.fastpermisssion.FastPermission
import com.william.fastpermisssion.OnPermissionCallback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors

class CameraTestActivity : AppCompatActivity() {

    // <editor-fold defaultstate="collapsed" desc="变量">
    /**** view ****/
    private lateinit var cameraTestSurfaceView: SurfaceView
    private lateinit var cameraTestBtnCapture: Button

    /**** camera ****/
    private var camera: ICamera? = null
    private var cameraWidth = 0
    private var cameraHeight = 0

    @Volatile
    private var isCapture: Boolean = false
    private var captureIndex = 0

    /**** thread ****/
    private val executor = Executors.newSingleThreadExecutor()

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Activity生命周期回调">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_test)

        findView()
        initView()

        requestPermission {
            startPreviewCamera()
        }
    }

    override fun onDestroy() {
        camera?.destroy()
        executor.shutdown()
        super.onDestroy()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - View">
    private fun findView() {
        cameraTestSurfaceView = findViewById(R.id.camera_test_surface_view)
        cameraTestBtnCapture = findViewById(R.id.camera_test_btn_capture)
    }

    private fun initView() {
        cameraTestBtnCapture.setOnClickListener {
            captureOnce()
        }

        cameraTestSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                LogUtils.d("CameraTestActivity.surfaceCreated")
            }

            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
                LogUtils.d("CameraTestActivity.surfaceChanged->width=$width, height=$height")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                LogUtils.d("CameraTestActivity.surfaceDestroyed")
            }
        })
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化 - 相机">
    private fun startPreviewCamera() {
        camera = CameraFactory.createCamera(CameraImplType.CAMERA_1)
        camera?.init(cameraTestSurfaceView.holder) { width, height ->
            cameraWidth = width
            cameraHeight = height
        }
        camera?.setPreviewCallback { buffer ->
            if (isCapture) {
                val copyBuffer = ByteArray(buffer.size)
                buffer.copyInto(copyBuffer)
                isCapture = false
                executor.submit {
                    YuvUtils.rotateYUVClockwise90(copyBuffer, cameraWidth, cameraHeight)
                    capture(copyBuffer)
                }
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="拍照并保存到本地">
    private fun captureOnce() {
        isCapture = true
    }

    private fun capture(data: ByteArray) {
        val fileName = "IMG_${captureIndex++}.jpg"
        val sdRoot = Environment.getExternalStorageDirectory()
        val pictureFile = File(sdRoot, fileName)

        if (pictureFile.exists()) {
            pictureFile.delete()
        }
        val createRet = pictureFile.createNewFile()
        if (createRet) {
            try {
                val fos = FileOutputStream(pictureFile)
                //已经对YUV数据做了旋转，因此YUV宽高要和摄像头宽高互换
                val imageWidth = cameraHeight
                val imageHeight = cameraWidth
                val yuvImage = YuvImage(data, ImageFormat.NV21, imageWidth, imageHeight, null)
                yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, fos)
                LogUtils.d("compressToJpeg, width=$imageWidth, height=$imageHeight")
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } else {
            ToastUtils.showShort("create file failed")
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="请求权限">
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
    // </editor-fold>
}