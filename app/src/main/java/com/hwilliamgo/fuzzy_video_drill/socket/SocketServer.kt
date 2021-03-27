package com.hwilliamgo.fuzzy_video_drill.socket

import com.blankj.utilcode.util.LogUtils
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

/**
 * date: 2021/3/4
 * author: HWilliamgo
 * description:socket服务端
 */
class SocketServer(private val port: Int) : ISocket {
    // <editor-fold defaultstate="collapsed" desc="变量">
    private var webSocket: WebSocket? = null
    private var onByteMessageListener: ISocket.OnByteMessageListener? = null
    private var webSocketServer: WebSocketServer? = null

    override fun init(onByteMessageListener: ISocket.OnByteMessageListener) {
        this.onByteMessageListener = onByteMessageListener
        webSocketServer = object : WebSocketServer(InetSocketAddress(port)) {
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
                ex?.printStackTrace()
            }

            override fun onStart() {
                LogUtils.d("SocketPush.onStart")
            }
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="API">
    override fun start() {
        webSocketServer?.start()
    }

    override fun sendData(data: ByteArray) {
        webSocket?.apply {
            if (isOpen) {
                send(data)
            }
        }
    }

    override fun close() {
        webSocket?.close()
        try {
            webSocketServer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun destroy() {
        onByteMessageListener = null
        webSocketServer = null
        close()
    }


    // </editor-fold>
}