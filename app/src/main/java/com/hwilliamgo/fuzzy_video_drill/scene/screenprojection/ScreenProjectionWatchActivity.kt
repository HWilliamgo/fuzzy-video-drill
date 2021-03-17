package com.hwilliamgo.fuzzy_video_drill.scene.screenprojection

import android.app.Activity
import android.content.Intent
import android.media.MediaCodec
import android.media.MediaFormat
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.hwilliamgo.fuzzy_video_drill.R
import com.hwilliamgo.fuzzy_video_drill.socket.ISocket
import com.hwilliamgo.fuzzy_video_drill.socket.SocketFactory
import java.io.IOException

class ScreenProjectionWatchActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_IP_ADDRESS = "ip_address"

        fun launch(activity: Activity, ip: String) {
            activity.startActivity(
                Intent(activity, ScreenProjectionWatchActivity::class.java).apply {
                    putExtra(EXTRA_IP_ADDRESS, ip)
                }
            )
        }
    }

    // <editor-fold defaultstate="collapsed" desc="变量">
    private lateinit var surfaceView: SurfaceView
    private var surface: Surface? = null

    private var socketWatch: ISocket? = null
    private var mediaCodec: MediaCodec? = null

    private var ipAddress: String = ""
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Activity回调">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_projection_watch)

        //init intent data
        ipAddress = intent.getStringExtra(EXTRA_IP_ADDRESS)

        //init view
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()

        mediaCodec?.stop()
        mediaCodec?.release()
        mediaCodec = null

        socketWatch?.close()
    }
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private fun initView() {
        surfaceView = findViewById(R.id.surface_view)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                surface = holder?.surface
                surface?.let {
                    initSocket()
                    initDecoder(it)
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
// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化socket">
    private fun initSocket() {
        socketWatch = SocketFactory.createClientSocket(ipAddress, SERVER_PORT)
        socketWatch?.init { data ->
            val codec = mediaCodec ?: return@init
            val index = codec.dequeueInputBuffer(10000)
            if (index >= 0) {
                val inputBuffer = codec.getInputBuffer(index)
                inputBuffer?.clear()
                inputBuffer?.put(data, 0, data.size)
                codec.queueInputBuffer(index, 0, data.size, System.currentTimeMillis(), 0)
            }
            val bufferInfo = MediaCodec.BufferInfo()
            var outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000)
            while (outputBufferIndex > 0) {
                codec.releaseOutputBuffer(outputBufferIndex, true)
                outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 0)
            }
        }
        socketWatch?.start()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化解码器">
    private fun initDecoder(surface: Surface) {
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_HEVC)
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_HEVC, 720, 1280)
            format.setInteger(MediaFormat.KEY_BIT_RATE, 720 * 1280)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            mediaCodec?.configure(
                format,
                surface,
                null, 0
            )
            mediaCodec?.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    // </editor-fold>
}