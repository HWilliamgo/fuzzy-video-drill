package com.hwilliamgo.fuzzy_video_drill

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.hwilliamgo.fuzzy_video_drill.screenprojection.ScreenProjectionPushActivity
import com.hwilliamgo.fuzzy_video_drill.screenprojection.ScreenProjectionWatchActivity

class MainActivity : AppCompatActivity() {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private lateinit var btnToScreenProjectionPush: Button
    private lateinit var btnToScreenProjectionWatch: Button
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
    }

    private fun initView() {
        btnToScreenProjectionPush.setOnClickListener {
            startActivity(Intent(this, ScreenProjectionPushActivity::class.java))
        }
        btnToScreenProjectionWatch.setOnClickListener {
            startActivity(Intent(this, ScreenProjectionWatchActivity::class.java))
        }
    }
    // </editor-fold>
}