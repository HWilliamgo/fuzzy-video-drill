package com.hwilliamgo.fuzzy_video_drill

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.hwilliamgo.fuzzy_video_drill.scene.cameratest.CameraTestActivity
import com.hwilliamgo.fuzzy_video_drill.scene.screenprojection.ScreenProjectionPushActivity
import com.hwilliamgo.fuzzy_video_drill.scene.screenprojection.ScreenProjectionWatchActivity
import com.hwilliamgo.fuzzy_video_drill.scene.videocall.VideoCallActivity
import com.william.fastpermisssion.FastPermission
import com.william.fastpermisssion.OnPermissionCallback
import java.util.*

class MainActivity : AppCompatActivity() {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private lateinit var btnToScreenProjectionPush: Button
    private lateinit var btnToScreenProjectionWatch: Button
    private lateinit var etPushServerIpAddress: EditText
    private lateinit var btnJumpToCameraTest: Button
    private lateinit var btnVideoCallServer: Button
    private lateinit var btnVideoCallClient: Button
    private lateinit var etServerIpAddress: EditText


    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Activity生命周期回调">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findView()
        initView()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private fun findView() {
        btnToScreenProjectionPush = findViewById(R.id.btn_to_screen_projection_push)
        btnToScreenProjectionWatch = findViewById(R.id.btn_to_screen_projection_watch)
        etPushServerIpAddress = findViewById(R.id.et_push_server_ip_address)
        btnJumpToCameraTest = findViewById(R.id.btn_jump_to_camera_test)
        btnVideoCallServer = findViewById<Button>(R.id.btn_video_call_server)
        btnVideoCallClient = findViewById<Button>(R.id.btn_video_call_client)
        etServerIpAddress = findViewById<EditText>(R.id.et_server_ip_address)
    }

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
    }
    // </editor-fold>
}