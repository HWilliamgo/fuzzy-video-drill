package com.hwilliamgo.fuzzy_video_drill.socket

/**
 * date: 2021/3/17
 * author: HWilliamgo
 * description:
 */
object SocketFactory {

    fun createClientSocket(remoteUrl: String, remotePort: Int): ISocket {
        return SocketWatch(remoteUrl, remotePort)
    }

    fun createServerSocket(localPort: Int): ISocket {
        return SocketPush(localPort)
    }

}