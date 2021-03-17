package com.hwilliamgo.fuzzy_video_drill.socket

/**
 * date: 2021/3/17
 * author: HWilliamgo
 * description: socket抽象
 */
interface ISocket {
    /**
     * 初始化
     * [onByteMessageListener]是收到远端发来的数据的监听器
     */
    fun init(onByteMessageListener: OnByteMessageListener)

    /**
     * 开始连接
     */
    fun start()

    /**
     * 发送数据
     */
    fun sendData(data: ByteArray)

    /**
     * 关闭连接
     */
    fun close()

    /**
     * 销毁
     */
    fun destroy()

    /**
     *字节消息监听器
     */
    fun interface OnByteMessageListener {
        fun onMessage(bytes: ByteArray)
    }
}