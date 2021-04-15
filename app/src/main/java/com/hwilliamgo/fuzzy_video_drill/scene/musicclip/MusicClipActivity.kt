package com.hwilliamgo.fuzzy_video_drill.scene.musicclip

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.hwilliamgo.fuzzy_video_drill.R
import com.hwilliamgo.fuzzy_video_drill.util.audio.AudioPlayer
import com.hwilliamgo.fuzzy_video_drill.util.audio.WavReader
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class MusicClipActivity : AppCompatActivity() {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private var isClipFinished = true
    private var isMixFinished = true
    val audioPlayer = AudioPlayer()


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

    override fun onDestroy() {
        audioPlayer.stop()
        super.onDestroy()
    }

    // <editor-fold defaultstate="collapsed" desc="处理点击事件">
    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_start_clip_audio -> {
                if (isClipFinished) {
                    isClipFinished = false
                    lifecycleScope.launch {
                        val outputPath = viewModel.clipAudio()
                        isClipFinished = true
                        playAudio(outputPath)
                    }
                }
            }
            R.id.btn_start_mix_audio -> {
                if (isMixFinished) {
                    isMixFinished = false
                    lifecycleScope.launch {
                        val outputPath=viewModel.mixAudio()
                        isMixFinished = true
                        playAudio(outputPath)
                    }
                }
            }
        }
    }

    // </editor-fold>
    private fun playAudio(path: String) {
        val outputFile = File(path)
        if (outputFile.exists()) {
            audioPlayer.start()
            WavReader.readWAV(outputFile, {
                LogUtils.e(it)
            }, {
                audioPlayer.writeData(it)
            })
        } else {
            LogUtils.e("文件不存在")
        }
    }
}