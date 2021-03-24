package com.hwilliamgo.fuzzy_video_drill.scene.videocall

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.hwilliamgo.fuzzy_video_drill.R
import com.hwilliamgo.fuzzy_video_drill.scene.screenprojection.VIDEO_CALL_SERVER_PORT
import com.hwilliamgo.fuzzy_video_drill.socket.ISocket
import com.hwilliamgo.fuzzy_video_drill.socket.ISocketFactory
import com.hwilliamgo.fuzzy_video_drill.videowidget.ILocalCameraSurfaceView
import com.hwilliamgo.fuzzy_video_drill.videowidget.IRemoteStreamSurfaceView

class VideoCallActivity : AppCompatActivity() {

    // <editor-fold defaultstate="collapsed" desc="伴生对象">
    companion object {
        private const val EXTRA_IS_CLIENT = "extra_is_client"
        private const val EXTRA_REMOTE_IP = "extra_remote_ip"
        fun launch(activity: Activity, isClient: Boolean, remoteIp: String = "") {
            activity.startActivity(
                Intent(activity, VideoCallActivity::class.java).apply {
                    putExtra(EXTRA_IS_CLIENT, isClient)
                    putExtra(EXTRA_REMOTE_IP, remoteIp)
                }
            )
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="变量">
    /**** View ****/
    private lateinit var localCameraSurfaceView: ILocalCameraSurfaceView
    private lateinit var remoteSurfaceView: IRemoteStreamSurfaceView
    private lateinit var btnStartConnect: Button

    /**** socket ****/
    private var socket: ISocket? = null

    /**** param ****/
    //empty if is server
    private var remoteIp = ""
    private var isClient: Boolean = false
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="生命周期回调">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        initParam()
        initSocket()
        findView()
        initView()
    }

    override fun onDestroy() {
        socket?.close()
        socket?.destroy()
        socket = null
        localCameraSurfaceView.destroy()
        remoteSurfaceView.destroy()
        super.onDestroy()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化">
    private fun initParam() {
        isClient = intent.getBooleanExtra(EXTRA_IS_CLIENT, false)
        remoteIp = intent.getStringExtra(EXTRA_REMOTE_IP)
    }

    private fun findView() {
        localCameraSurfaceView = findViewById(R.id.local_camera_surface_view)
        btnStartConnect = findViewById(R.id.btn_start_connect)
        remoteSurfaceView = findViewById(R.id.remote_surface_view)
    }

    private fun initView() {
        localCameraSurfaceView.setCameraFrameEncodedCallback { encodedData ->
            socket?.sendData(encodedData)
        }

        var isConnectPress = false
        btnStartConnect.setOnClickListener {
            isConnectPress = !isConnectPress
            if (isConnectPress) {
                socket?.start()
                btnStartConnect.text = "挂断"
            } else {
                socket?.close()
                btnStartConnect.text = "开始连接"
            }
        }
    }

    private fun initSocket() {
        socket = if (isClient) {
            ISocketFactory.createClientSocket(remoteIp, VIDEO_CALL_SERVER_PORT)
        } else {
            ISocketFactory.createServerSocket(VIDEO_CALL_SERVER_PORT)
        }
        socket?.init { remoteData ->
            remoteSurfaceView.feedEncodedData(remoteData)
        }
    }
    // </editor-fold>

}