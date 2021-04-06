package com.hwilliamgo.fuzzy_video_drill.scene.musicclip

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.hwilliamgo.fuzzy_video_drill.R
import com.hwilliamgo.fuzzy_video_drill.util.AudioClipper
import com.william.fastpermisssion.FastPermission
import com.william.fastpermisssion.OnPermissionCallback
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.thread

class MusicClipActivity : AppCompatActivity() {

    // <editor-fold defaultstate="collapsed" desc="变量">
    private val assetsName="Afterglow - Taylor Swift.mp3"

    private val btnStartClipAudio: Button by lazy { findViewById<Button>(R.id.btn_start_clip_audio) }
    private val musicSourcePath =
        File(Environment.getExternalStorageDirectory(), assetsName).absolutePath

    @Volatile
    private var isClipFinished = true
    // </editor-fold>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_clicp)

        initView()
        requestPermission {
            copyAssets(assetsName, musicSourcePath)
        }
    }

    // <editor-fold defaultstate="collapsed" desc="初始化View">
    private fun initView() {
        btnStartClipAudio.setOnClickListener {
            if (isClipFinished) {
                isClipFinished=false
                thread {
                    try {
                        AudioClipper.clip(
                            musicSourcePath, "outputClipWav",
                            10 * 1000 * 1000,
                            15 * 1000 * 1000
                        )
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                    isClipFinished = true
                }
            }
        }
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

    private fun copyAssets(assetsName: String, path: String) {
        val assetFileDescriptor = assets.openFd(assetsName)
        val from = FileInputStream(assetFileDescriptor.fileDescriptor).channel
        val to = FileOutputStream(path).channel
        from.transferTo(assetFileDescriptor.startOffset, assetFileDescriptor.length, to)
    }
}