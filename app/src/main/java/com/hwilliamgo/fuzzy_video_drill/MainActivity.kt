package com.hwilliamgo.fuzzy_video_drill

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.hwilliamgo.fuzzy_video_drill.scene.cameratest.CameraTestActivity
import com.hwilliamgo.fuzzy_video_drill.scene.musicclip.MusicClipActivity
import com.hwilliamgo.fuzzy_video_drill.scene.screenprojection.ScreenProjectionPushActivity
import com.hwilliamgo.fuzzy_video_drill.scene.screenprojection.ScreenProjectionWatchActivity
import com.hwilliamgo.fuzzy_video_drill.scene.videocall.VideoCallActivity
import com.william.fastpermisssion.FastPermission
import com.william.fastpermisssion.OnPermissionCallback
import java.util.*

class MainActivity : AppCompatActivity() {


    // <editor-fold defaultstate="collapsed" desc="变量">
    private val btnToScreenProjectionPush: Button by lazy { findViewById<Button>(R.id.btn_to_screen_projection_push) }
    private val btnToScreenProjectionWatch: Button by lazy { findViewById<Button>(R.id.btn_to_screen_projection_watch) }
    private val etPushServerIpAddress: EditText by lazy { findViewById<EditText>(R.id.et_push_server_ip_address) }
    private val btnJumpToCameraTest: Button by lazy { findViewById<Button>(R.id.btn_jump_to_camera_test) }
    private val btnVideoCallServer: Button by lazy { findViewById<Button>(R.id.btn_video_call_server) }
    private val btnVideoCallClient: Button by lazy { findViewById<Button>(R.id.btn_video_call_client) }
    private val etServerIpAddress: EditText by lazy { findViewById<EditText>(R.id.et_server_ip_address) }
    private val btnJumpToMusicClip: Button by lazy { findViewById<Button>(R.id.btn_jump_to_music_clip) }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Activity生命周期回调">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        initView()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">

    private fun initView() {
        btnToScreenProjectionPush.setOnClickListener {
            startActivity(Intent(this, ScreenProjectionPushActivity::class.java))
        }
        btnToScreenProjectionWatch.setOnClickListener {
            val ipAddr = etPushServerIpAddress.text.toString().trim()
            if (ipAddr.isEmpty()) {
                ToastUtils.showShort("请输入推流端ip地址")
            } else {
                ScreenProjectionWatchActivity.launch(this, ipAddr)
            }
        }

        btnJumpToCameraTest.setOnClickListener {
            startActivity(Intent(this, CameraTestActivity::class.java))
        }

        btnVideoCallServer.setOnClickListener {
            FastPermission.getInstance().start(this, arrayListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), object : OnPermissionCallback {
                override fun onAllGranted() {
                    VideoCallActivity.launch(this@MainActivity, false)
                }

                override fun onGranted(grantedPermissions: ArrayList<String>?) {
                }

                override fun onDenied(deniedPermissions: ArrayList<String>?) {
                }

                override fun onDeniedForever(deniedForeverP: ArrayList<String>?) {
                }
            })
        }
        btnVideoCallClient.setOnClickListener {
            FastPermission.getInstance().start(this, arrayListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), object : OnPermissionCallback {
                override fun onAllGranted() {
                    val ipAddress = etServerIpAddress.text.toString().trim()
                    if (ipAddress.isEmpty()) {
                        ToastUtils.showShort("请输入客户端ip地址")
                    } else {
                        VideoCallActivity.launch(this@MainActivity, true, ipAddress)
                    }
                }

                override fun onGranted(grantedPermissions: ArrayList<String>?) {
                }

                override fun onDenied(deniedPermissions: ArrayList<String>?) {
                }

                override fun onDeniedForever(deniedForeverP: ArrayList<String>?) {
                }
            })
        }
        btnJumpToMusicClip.setOnClickListener {
            startActivity(Intent(this, MusicClipActivity::class.java))
        }
    }
    // </editor-fold>
}