package com.hwilliamgo.fuzzy_video_drill.camera

import android.content.Context
import android.util.Size
import android.view.SurfaceHolder
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.window.WindowManager
import com.blankj.utilcode.util.LogUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @date: 2021/12/1
 * @author: HWilliamgo
 * @description:
 * androidx里面的CameraX的封装。由于CameraX使用方式上的和原先的Camera1有比较大的不同，因此无法在api上保持兼容性
 * 他需要：
 * 1. PreviewView
 * 2. LifecycleOwner
 */
class CameraX(private val context: Context) : ICamera {
    companion object {
        private val TAG = CameraX::class.java.simpleName
    }

    private var camera: Camera? = null
    private var preview: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private val windowManager: WindowManager by lazy { WindowManager(context) }
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var previewView: PreviewView? = null
    private var rotation: Int = 0
    private val targetSize = Size(1080, 1920)

    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    /**** Listener ****/
    private var previewCallback: ICamera.PreviewCallback? = null

    override fun init(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        onCameraSizeReadyCallback: ICamera.OnCameraSizeReadyCallback
    ) {
        this.previewView = previewView
        rotation = previewView.display.rotation

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Select lensFacing depending on the available cameras
            lensFacing = when {
                hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                else -> throw IllegalStateException("Back and front camera are unavailable")
            }

            // Build and bind the camera use cases
            bindCameraUseCases(lifecycleOwner)
            // 这里的高度和宽度是手机的高度和宽度，但是外部要的是相机的宽度和高度，而Android系统上相机刚好是横过来的，因此直接调换过来。
            onCameraSizeReadyCallback.onCameraSizeReady(targetSize.height, targetSize.width)
        }, ContextCompat.getMainExecutor(context))
    }

    override fun init(
        surfaceHolder: SurfaceHolder,
        onCameraSizeReadyCallback: ICamera.OnCameraSizeReadyCallback
    ) {
    }

    private fun bindCameraUseCases(lifecycleOwner: LifecycleOwner) {
        // Get screen metrics used to setup camera for full screen resolution
        val metrics = windowManager.getCurrentWindowMetrics().bounds
        LogUtils.d(TAG, "Screen metrics: ${metrics.width()} x ${metrics.height()}")

        val screenAspectRatio = AspectRatio.RATIO_16_9

        // CameraProvider
        val cameraProvider = cameraProvider
            ?: throw IllegalStateException("Camera initialization failed.")

        // CameraSelector
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

        // Preview
        preview = Preview.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation
            .setTargetRotation(rotation)
            .build()

        // ImageCapture
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            // We request aspect ratio but no resolution to match preview config, but letting
            // CameraX optimize for whatever specific resolution best fits our use cases
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes is
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
//            .setTargetAspectRatio(screenAspectRatio)
            .setTargetResolution(targetSize)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(cameraExecutor, { imageProxy ->
                    val planes = imageProxy.planes
                    if (planes.size < 3) {
                        return@setAnalyzer
                    }
                    val yBuffer = planes[0].buffer
                    val uBuffer = planes[1].buffer
                    val vBuffer = planes[2].buffer

                    val ySize = yBuffer.remaining()
                    val uSize = uBuffer.remaining()
                    val vSize = vBuffer.remaining()

                    // 注意，CameraX会把他每个Y、U、V数据分别回调出来给我们，由我们自己组装，下面我们组装成NV21

                    val nv21Buffer = ByteArray(ySize * 3 / 2)
                    // 先把Y填充了
                    yBuffer.get(nv21Buffer, 0, ySize)
                    // 把后面剩下1/3的都先填充成v
                    vBuffer.get(nv21Buffer, ySize, vSize)
                    val u = ByteArray(uSize)
                    uBuffer.get(u)
                    var pos = ySize + 1
                    // 将偶数位置的填充成u
                    for (i in 0..uSize) {
                        if (i % 2 == 0) {
                            nv21Buffer[pos] = u[i]
                            pos += 2
                        }
                    }
                    // 此时[nv21Buffer]变量中就是NV21了
                    previewCallback?.onPreviewFrame(nv21Buffer)
                    LogUtils.d("width=${imageProxy.width},height=${imageProxy.height}")

                    imageProxy.close()
                })
            }
        // bindToLifecycle这个函数调用非常关键，不调用没有画面渲染
        camera = cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture,
            imageAnalyzer
        )
        preview?.setSurfaceProvider(previewView?.surfaceProvider)
    }

    override fun setPreviewCallback(callback: ICamera.PreviewCallback) {
        this.previewCallback = callback
    }

    override fun destroy() {

    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }
}