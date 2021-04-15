package com.hwilliamgo.fuzzy_video_drill.scene.fasttest

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.hwilliamgo.fuzzy_video_drill.R
import com.hwilliamgo.fuzzy_video_drill.util.Stopper
import com.hwilliamgo.fuzzy_video_drill.util.audio.AudioPlayer
import com.hwilliamgo.fuzzy_video_drill.util.audio.WavReader
import java.io.File

class FastTestActivity : AppCompatActivity() {
    private var stopWAVReader: Stopper? = null
    private var audioTrack: AudioTrack? = null
    private val audioPlayer = AudioPlayer()

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fast_test)
        audioPlayer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopWAVReader?.stop()
        audioPlayer.stop()
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="点击事件">
    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_start_extract_wav -> {
                val wavFile = File(Environment.getExternalStorageDirectory(), "混合后的.wav")
                stopWAVReader = WavReader.readWAV(wavFile, { errMsg ->
                    LogUtils.e(errMsg)
                }) { sampleData ->
                    LogUtils.d("sampleData.size=${sampleData.size}")
                    audioPlayer.writeData(sampleData)
                }
            }
        }
    }
    // </editor-fold>
}