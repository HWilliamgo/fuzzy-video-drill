package com.hwilliamgo.fuzzy_video_drill.camera

import android.content.Context
import android.util.Log
import android.view.SurfaceHolder
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.window.WindowManager
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @date: 2021/12/1
 * @author: HWilliamgo
 * @description:
 */
class CameraX(private val cocentext: Context) : ICamera {
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
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    fun setPrevieView(previewView: PreviewView) {
        this.previewView = previewView
        rotation = previewView.display.rotation
    }

    fun init(lifecycleOwner: LifecycleOwner) {
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
        Log.d(TAG, "Screen metrics: ${metrics.width()} x ${metrics.height()}")

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
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()

        // ImageAnalysis
        imageAnalyzer = ImageAnalysis.Builder()
            // We request aspect ratio but no resolution
            .setTargetAspectRatio(screenAspectRatio)
            // Set initial target rotation, we will have to call this again if rotation changes
            // during the lifecycle of this use case
            .setTargetRotation(rotation)
            .build()
            // The analyzer can then be assigned to the instance
            .also {
                it.setAnalyzer(cameraExecutor, { imageProxy ->
                    // yuv数据从这个变量取出来
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