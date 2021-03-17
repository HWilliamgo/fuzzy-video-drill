package com.hwilliamgo.fuzzy_video_drill.socket

/**
 * date: 2021/3/17
 * author: HWilliamgo
 * description: Socket工厂类
 */
object ISocketFactory {

    /**
     * 创建客户端socket
     * [remoteUrl] 远端url，目前只支持传递ip地址
     * [remotePort]远端端口
     */
    fun createClientSocket(remoteUrl: String, remotePort: Int): ISocket {
        return SocketClient(remoteUrl, remotePort)
    }

    /**
     * 创建服务端socket
     *[localPort]本地端口
     */
    fun createServerSocket(localPort: Int): ISocket {
        return SocketServer(localPort)
    }

}