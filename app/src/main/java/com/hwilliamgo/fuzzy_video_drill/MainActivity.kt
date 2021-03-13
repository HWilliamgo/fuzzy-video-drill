package com.hwilliamgo.fuzzy_video_drill

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ToastUtils
import com.hwilliamgo.fuzzy_video_drill.cameratest.CameraTestActivity
import com.hwilliamgo.fuzzy_video_drill.screenprojection.ScreenProjectionPushActivity
import com.hwilliamgo.fuzzy_video_drill.screenprojection.ScreenProjectionWatchActivity

class MainActivity : AppCompatActivity() {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private lateinit var btnToScreenProjectionPush: Button
    private lateinit var btnToScreenProjectionWatch: Button
    private lateinit var etPushServerIpAddress: EditText
    private lateinit var btnJumpToCameraTest: Button


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
    }
    // </editor-fold>
}