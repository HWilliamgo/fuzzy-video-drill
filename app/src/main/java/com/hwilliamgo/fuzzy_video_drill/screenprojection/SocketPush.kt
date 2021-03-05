package com.hwilliamgo.fuzzy_video_drill.screenprojection

import com.blankj.utilcode.util.LogUtils
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

/**
 * date: 2021/3/4
 * author: HWilliamgo
 * description:
 */
class SocketPush(private val port: Int, private val codecLiveH265: CodecLiveH265) {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private var webSocket: WebSocket? = null
    private val webSocketServer = object : WebSocketServer(InetSocketAddress(port)) {
        override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
            LogUtils.d("SocketPush.onOpen")
            webSocket = conn
        }

        override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
            LogUtils.d("SocketPush.onClose")
        }

        override fun onMessage(conn: WebSocket?, message: String?) {
            LogUtils.d("SocketPush.onMessage")
        }

        override fun onError(conn: WebSocket?, ex: Exception?) {
            LogUtils.d("SocketPush.onError")
        }

        override fun onStart() {
            LogUtils.d("SocketPush.onStart")
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    fun start() {
        webSocketServer.start()
        codecLiveH265.startLive()
    }

    fun close() {
        webSocket?.close()
        try {
            webSocketServer.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendData(data: ByteArray) {
        webSocket?.apply {
            if (isOpen) {
                send(data)
            }
        }
    }
    // </editor-fold>
}