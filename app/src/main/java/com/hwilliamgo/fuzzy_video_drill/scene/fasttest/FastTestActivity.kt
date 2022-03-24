package com.hwilliamgo.fuzzy_video_drill.scene.fasttest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.hwilliamgo.fuzzy_video_drill.R
import com.hwilliamgo.fuzzy_video_drill.util.Stopper
import com.hwilliamgo.fuzzy_video_drill.util.audio.AudioPlayer
import com.hwilliamgo.fuzzy_video_drill.util.audio.AudioRecorder
import com.hwilliamgo.fuzzy_video_drill.util.audio.WavReader
import java.io.File

class FastTestActivity : AppCompatActivity() {
    companion object {
        private const val EXTRA_WAV_FILE_PATH = "extra_wav_file_path"
        private const val EXTRA_WAV_FILE_PATH2 = "extra_wav_file_path2"

        fun launch(context: Context, wavFilePath: String, wavFilePath2: String) {
            context.startActivity(Intent(context, FastTestActivity::class.java).apply {
                putExtra(EXTRA_WAV_FILE_PATH, wavFilePath)
                putExtra(EXTRA_WAV_FILE_PATH2, wavFilePath2)
            })
        }
    }

    private var stopWAVReader: Stopper? = null
    private val audioPlayer = AudioPlayer()

    private var wavFilePath = ""
    private var wavFilePath2 = ""

    // <editor-fold defaultstate="collapsed" desc="生命周期">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fast_test)
        wavFilePath = intent.getStringExtra(EXTRA_WAV_FILE_PATH)
        wavFilePath2 = intent.getStringExtra(EXTRA_WAV_FILE_PATH2)
        audioPlayer.setConfig(
            AudioRecorder.SAMPLE_RATGE,
            AudioRecorder.AUDIO_FORMAT,
            AudioRecorder.CHANNELS_COUNT
        )
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
                playWAV(wavFilePath)
            }
            R.id.btn_start_extract_wav2 -> {
                playWAV(wavFilePath2)
            }
        }
    }
    // </editor-fold>

    private fun playWAV(wavPath: String) {
        val wavFile = File(wavPath)
        stopWAVReader = WavReader.readWAV(wavFile, { errMsg ->
            LogUtils.e(errMsg)
        }) { sampleData ->
            LogUtils.d("sampleData.size=${sampleData.size}")
            audioPlayer.writeData(sampleData)
        }
    }
}