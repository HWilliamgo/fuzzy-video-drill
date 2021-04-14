package com.hwilliamgo.fuzzy_video_drill.scene.musicclip

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.hwilliamgo.fuzzy_video_drill.R
import kotlinx.coroutines.launch
import java.util.*

class MusicClipActivity : AppCompatActivity() {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private var isClipFinished = true
    private var isMixFinished = true

    private val viewModel by lazy {
        ViewModelProvider(this).get(MusicClipViewModel::class.java)
    }
    // </editor-fold>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_clicp)

        lifecycleScope.launch {
            viewModel.requestPermission(this@MusicClipActivity)
            viewModel.copyAssets(this@MusicClipActivity)
        }
    }

    // <editor-fold defaultstate="collapsed" desc="处理点击事件">
    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_start_clip_audio -> {
                if (isClipFinished) {
                    isClipFinished = false
                    lifecycleScope.launch {
                        viewModel.clipAudio()
                        isClipFinished = true
                    }
                }
            }
            R.id.btn_start_mix_audio -> {
                if (isMixFinished) {
                    isMixFinished = false
                    lifecycleScope.launch {
                        viewModel.mixAudio()
                        isMixFinished = true
                    }

                }
            }
        }
    }
    // </editor-fold>
}