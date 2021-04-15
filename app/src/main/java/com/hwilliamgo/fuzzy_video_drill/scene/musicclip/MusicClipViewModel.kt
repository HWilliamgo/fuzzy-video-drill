package com.hwilliamgo.fuzzy_video_drill.scene.musicclip

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import com.hwilliamgo.fuzzy_video_drill.util.audio.AudioClipper
import com.william.fastpermisssion.FastPermission
import com.william.fastpermisssion.OnPermissionCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * date: 2021/4/13
 * author: HWilliamgo
 * description:
 */
class MusicClipViewModel : ViewModel() {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private val musicA = "Afterglow - Taylor Swift.mp3"
    private val musicB = "music.mp3"
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="请求权限">
    suspend fun requestPermission(activity: Activity): Boolean {
        return suspendCoroutine<Boolean> {
            FastPermission.getInstance().start(activity, arrayListOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), object : OnPermissionCallback {
                override fun onAllGranted() {
                    it.resume(true)
                }

                override fun onGranted(grantedPermissions: ArrayList<String>?) {
                }

                override fun onDenied(deniedPermissions: ArrayList<String>?) {
                    it.resume(false)
                }

                override fun onDeniedForever(deniedForeverP: ArrayList<String>?) {
                    it.resume(false)
                }
            })
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="复制asset">
    suspend fun copyAssets(context: Context) = withContext(Dispatchers.IO) {
        val assets = context.assets.list("")
        assets?.forEach { assetName ->
            if (assetName.endsWith(".mp3")) {
                copyAssets(
                    context,
                    assetName,
                    getPathOfMusic(assetName)
                )
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="剪裁音频">
    suspend fun clipAudio(): String = withContext(Dispatchers.IO) {
        var outputPath = ""
        try {
            outputPath = getPathOfMusic("outputClipWav")
            AudioClipper.clip(
                getPathOfMusic(musicA), outputPath,
                10 * 1000 * 1000,
                15 * 1000 * 1000
            )
            return@withContext outputPath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext outputPath
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="混合音频">
    suspend fun mixAudio() = withContext(Dispatchers.IO) {
        var outputPath = ""
        try {
            outputPath = getPathOfMusic("mixAudioWAV.wav")
            AudioClipper.mixAudioTrack(
                getPathOfMusic(musicA),
                getPathOfMusic(musicB),
                outputPath,
                60 * 1000 * 1000,
                70 * 1000 * 1000,
                50,
                50
            )
            return@withContext outputPath
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext outputPath
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="内部工具方法">
    private fun getPathOfMusic(fileName: String): String {
        return File(Environment.getExternalStorageDirectory(), fileName).absolutePath
    }

    private fun copyAssets(context: Context, assetsName: String, path: String) {
        val assetFileDescriptor = context.assets.openFd(assetsName)
        val from = FileInputStream(assetFileDescriptor.fileDescriptor).channel
        val to = FileOutputStream(path).channel
        from.transferTo(assetFileDescriptor.startOffset, assetFileDescriptor.length, to)
    }
    // </editor-fold>
}