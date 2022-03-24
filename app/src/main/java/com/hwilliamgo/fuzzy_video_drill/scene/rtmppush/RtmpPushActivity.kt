package com.hwilliamgo.fuzzy_video_drill.scene.rtmppush

import android.Manifest
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.hwilliamgo.fuzzy_video_drill.BuildConfig
import com.hwilliamgo.fuzzy_video_drill.R
import com.hwilliamgo.fuzzy_video_drill.camera.CameraFactory
import com.hwilliamgo.fuzzy_video_drill.camera.CameraImplType
import com.hwilliamgo.fuzzy_video_drill.camera.ICamera
import com.hwilliamgo.fuzzy_video_drill.ext.requestPermission
import com.hwilliamgo.fuzzy_video_drill.scene.fasttest.FastTestActivity
import com.hwilliamgo.fuzzy_video_drill.util.audio.AudioRecorder
import com.hwilliamgo.fuzzy_video_drill.util.audio.PcmToWavUtil
import com.hwilliamgo.fuzzy_video_drill.util.file.FileWriterFactory
import com.hwilliamgo.fuzzy_video_drill.util.file.FileWriterType
import com.hwilliamgo.fuzzy_video_drill.util.file.IFileWriter
import com.hwilliamgo.fuzzy_video_drill.util.video.VideoCapture
import com.hwilliamgo.fuzzy_video_drill.util.video.YuvUtils
import kotlinx.android.synthetic.main.activity_screen_projection_watch.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class RtmpPushActivity : AppCompatActivity() {
    companion object {
        const val BITRATE = 1080 * 1920
        const val FRAME_RATE = 15
    }

    /**** View ****/
//    private val rtmpPushPreviewView: PreviewView by lazy { findViewById(R.id.rtmp_push_preview_view) }
    private val rtmpPushSurfaceView: SurfaceView by lazy { findViewById(R.id.rtmp_push_surface_view) }

    /**** 核心引擎 ****/
    private var camera: ICamera? = null
    private var audioRecorder: AudioRecorder? = null

    /**** 本地文件记录 ****/
    private val fileWriter: IFileWriter by lazy {
        FileWriterFactory.newFileWriter(
            FileWriterType.HEX_STRING_WRITER, "x264_encod_output.txt"
        )
    }
    private val audioPcmFileWriter: IFileWriter by lazy {
        FileWriterFactory.newFileWriter(
            FileWriterType.FAST_WRITER, "rtmp_push_audio_pcm.pcm"
        )
    }
    private val audioPcmFileWriter2: IFileWriter by lazy {
        FileWriterFactory.newFileWriter(
            FileWriterType.FAST_WRITER, "rtmp_push_audio_pcm2.pcm"
        )
    }
    private var pcmToWavUtil: PcmToWavUtil? = null

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
            camera = CameraFactory.createCamera(this, CameraImplType.CAMERA_1)
            rtmpPushSurfaceView.holder.addCallback(object : SurfaceHolder.Callback2 {
                override fun surfaceCreated(holder: SurfaceHolder?) {
                    initCamera()
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

                override fun surfaceRedrawNeeded(holder: SurfaceHolder?) {
                }
            })
//            rtmpPushPreviewView.addOnAttachStateChangeListener(object :
//                View.OnAttachStateChangeListener {
//                override fun onViewAttachedToWindow(v: View?) {
//                    initCamera()
//                }
//
//                override fun onViewDetachedFromWindow(v: View?) {}
//            })

            pcmToWavUtil = PcmToWavUtil(
                AudioRecorder.SAMPLE_RATGE,
                AudioRecorder.CHANNELS_MASK,
                AudioRecorder.CHANNELS_COUNT,
                AudioRecorder.AUDIO_FORMAT
            )

            val inputBufferSize = RtmpPushManager.setAudioEncoderInfo(
                AudioRecorder.SAMPLE_RATGE,
                AudioRecorder.CHANNELS_COUNT
            )// channels这里只能写1或者2，不能用AudioFormat的常量
            audioRecorder = AudioRecorder()
            audioRecorder?.init(inputBufferSize)
            audioRecorder?.setAudioDataCallback(object : AudioRecorder.AudioCallDataback {
                override fun onAudioData(pcmData: ByteArray, end: Int, length: Int) {
//                    RtmpPushManager.pushAudio(pcmData,end,length)
                    audioPcmFileWriter.writeData2File(pcmData, end)
                }

                override fun onAudioDara2(pcmData: ByteArray, end: Int, length: Int) {
                    audioPcmFileWriter2.writeData2File(pcmData, end)
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
        audioPcmFileWriter.destroy()
        audioPcmFileWriter2.destroy()
        RtmpPushManager.stop()
        RtmpPushManager.release()
    }

    private fun initCamera() {
//        camera?.init(rtmpPushPreviewView, this@RtmpPushActivity) { width, height ->
//            this@RtmpPushActivity.cameraWidth = width
//            this@RtmpPushActivity.cameraHeight = height
//            RtmpPushManager.setVideoEncoderInfo(
//                cameraHeight,
//                cameraWidth,
//                FRAME_RATE,
//                BITRATE
//            )
//            RtmpPushManager.start(BuildConfig.RTMP_PUSH_URL)
//        }
        camera?.init(rtmpPushSurfaceView.holder) { width, height ->
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
            R.id.rtmp_push_btn_pcm_to_wav -> {
                lifecycleScope.launch {
                    val outputFile1 = convertPcm2WavAndJumpToPlay(
                        audioPcmFileWriter2.getOutputFile().absolutePath,
                        "rtmp_push_wav2.wav"
                    )
                    val outputFile2 = convertPcm2WavAndJumpToPlay(
                        audioPcmFileWriter.getOutputFile().absolutePath,
                        "rtmp_push_wav.wav"
                    )
                    finish()
                    FastTestActivity.launch(this@RtmpPushActivity, outputFile1, outputFile2)
                }
            }
        }
    }

    private suspend fun convertPcm2WavAndJumpToPlay(
        pcmFilePath: String,
        outputWavFileName: String
    ): String {
        val outputFile = File(externalCacheDir, outputWavFileName)
        withContext(Dispatchers.IO) {
            if (outputFile.exists()) {
                outputFile.delete()
            }
            outputFile.createNewFile()
            pcmToWavUtil?.pcmToWav(
                pcmFilePath,
                outputFile.absolutePath
            )
        }
        return outputFile.absolutePath
    }
}