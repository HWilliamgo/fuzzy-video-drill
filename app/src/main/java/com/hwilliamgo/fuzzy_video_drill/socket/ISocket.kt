package com.hwilliamgo.fuzzy_video_drill.socket

/**
 * date: 2021/3/17
 * author: HWilliamgo
 * description:
 */
interface ISocket {
    fun init(onByteMessageListener: OnByteMessageListener)
    fun start()
    fun sendData(data: ByteArray)
    fun close()
    fun destroy()

    fun interface OnByteMessageListener {
        fun onMessage(bytes: ByteArray)
    }
}