package com.hwilliamgo.fuzzy_video_drill.cameratest

import android.Manifest
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.hardware.Camera
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.hwilliamgo.fuzzy_video_drill.R
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
    private var camera: Camera? = null
    private var cameraWidth = 0
    private var cameraHeight = 0

    // camera preview buffer
    private var buffer: ByteArray? = null

    @Volatile
    private var isCapture: Boolean = false
    private var captureIndex = 0

    /**** thread ****/
    private var cameraHandlerThread: HandlerThread? = null
    private var cameraHandler: Handler? = null
    private val executor = Executors.newSingleThreadExecutor()

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Activity生命周期回调">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_test)

        findView()
        initView()

        requestPermission {
            initCameraThread {
                cameraHandler?.post {
                    startPreviewCamera()
                }
            }
        }
    }

    override fun onDestroy() {
        camera?.release()
        cameraHandler?.removeCallbacksAndMessages(null)
        cameraHandlerThread?.quit()
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
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        camera?.let {
            val parameters = it.parameters
            cameraWidth = parameters.previewSize.width
            cameraHeight = parameters.previewSize.height
            try {
                it.setPreviewDisplay(cameraTestSurfaceView.holder)
                it.setDisplayOrientation(90)
                buffer = ByteArray(cameraWidth * cameraHeight * 3 / 2)
                LogUtils.d("buffer hash=${buffer.hashCode()}")
                it.addCallbackBuffer(buffer)
                it.setPreviewCallbackWithBuffer { data, camera ->
                    if (isCapture) {
                        val copyBuffer = ByteArray(buffer!!.size)
                        buffer!!.copyInto(copyBuffer)
                        LogUtils.d("setPreviewCallbackWithBuffer hash=${data.hashCode()}")
                        isCapture = false
                        executor.submit {
                            rotateYUV(copyBuffer)
                            capture(copyBuffer)
                        }
                    }
                    camera.addCallbackBuffer(data)
                }
//                it.setPreviewCallback { data, camera ->
//
//                    if (isCapture) {
//                        LogUtils.d("setPreviewCallback hash=${data.hashCode()}")
//                        isCapture = false
//                        capture(data)
//                    }
//                }
                it.startPreview()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化工作线程">
    private fun initCameraThread(cb: () -> Unit) {
        cameraHandlerThread = object : HandlerThread("cameraHandlerThread") {
            override fun onLooperPrepared() {
                cameraHandler = Handler()
                runOnUiThread {
                    cb()
                }
            }
        }
        cameraHandlerThread?.start()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="旋转yuv数据">
    private fun rotateYUV(data: ByteArray) {
        if (cameraHeight * cameraWidth <= 0) {
            return
        }

        val source = ByteArray(data.size)
        data.copyInto(source)
        val dst = data

        val oldW = cameraWidth
        val oldH = cameraHeight

        //first rotate Y , then rotate U and V

        //Y分量的长度
        val lengthY = oldW * oldH
        //UV分量的高度
        val heightUV = oldH / 2

        var copyIndex = 0

        //旋转Y
        for (j in 0 until oldW) {
            //j 是第几列
            for (i in oldH - 1 downTo 0) {
                //i 是第几行
                dst[copyIndex++] = source[i * oldW + j]
            }
        }

        //旋转U和V
        for (j in 0 until oldW step 2) {
            for (i in heightUV - 1 downTo 0) {
                dst[copyIndex++] = source[lengthY + i * oldW + j]
                dst[copyIndex++] = source[lengthY + i * oldW + j + 1]
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