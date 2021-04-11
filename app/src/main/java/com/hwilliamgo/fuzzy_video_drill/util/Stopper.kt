package com.hwilliamgo.fuzzy_video_drill.util

/**
 * date: 2021/4/11
 * author: HWilliamgo
 * description:
 */
class Stopper {
    private var isStop: Boolean = false

    @Synchronized
    fun isStop(): Boolean {
        return isStop
    }

    @Synchronized
    fun stop() {
        isStop = true
    }
}