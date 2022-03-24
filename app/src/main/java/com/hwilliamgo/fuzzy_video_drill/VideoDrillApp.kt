package com.hwilliamgo.fuzzy_video_drill

import android.app.Application
import com.hwilliamgo.fuzzy_video_drill.util.file.FileWriterConfig
import kotlin.properties.Delegates

/**
 * date: 2021/3/4
 * author: HWilliamgo
 * description: App类
 */
class VideoDrillApp : Application() {
    companion object {
        private var INSTANCE: VideoDrillApp by Delegates.notNull()
        fun getInstance() = INSTANCE
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        FileWriterConfig.STORE_IN_SD_CARD_ROOT = false
    }

}