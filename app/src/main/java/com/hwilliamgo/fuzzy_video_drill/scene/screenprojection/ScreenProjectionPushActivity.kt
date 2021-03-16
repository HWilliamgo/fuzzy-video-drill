package com.hwilliamgo.fuzzy_video_drill.scene.screenprojection

import android.Manifest
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hwilliamgo.fuzzy_video_drill.R
import com.william.fastpermisssion.FastPermission
import com.william.fastpermisssion.OnPermissionCallback
import java.util.*

class ScreenProjectionPushActivity : AppCompatActivity() {

    // <editor-fold defaultstate="collapsed" desc="静态变量">
    companion object {
        const val CAPTURE_REQUEST_CODE = 1
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="实例变量">
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var socketPush: SocketPush? = null
    private var codecLiveH265: CodecLiveH265? = null
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Activity 回调">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_screen_projection_push)

        mediaProjectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()

        requestPermission {
            startActivityForResult(captureIntent, CAPTURE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_REQUEST_CODE) {
                onAllowCaptureScreen(resultCode, data!!)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        codecLiveH265?.stopLive()
        socketPush?.close()
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="允许投屏回调">
    private fun onAllowCaptureScreen(resultCode: Int, data: Intent) {
        val mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
        socketPush = SocketPush(SERVER_PORT)
        codecLiveH265 = CodecLiveH265(socketPush!!, mediaProjection)

        socketPush?.start()
        codecLiveH265?.startLive()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="请求权限">
    private fun requestPermission(cb: () -> Unit) {
        FastPermission.getInstance().start(this, arrayListOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
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