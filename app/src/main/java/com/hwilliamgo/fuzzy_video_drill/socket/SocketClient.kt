package com.hwilliamgo.fuzzy_video_drill.socket

import com.blankj.utilcode.util.LogUtils
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

/**
 * date: 2021/3/6
 * author: HWilliamgo
 * description:socket客户端
 */
class SocketClient(
    private val ip: String,
    private val port: Int,
) : ISocket {
    private var webSocketClient: WebSocketClient? = null
    private var onByteMessageListener: ISocket.OnByteMessageListener? = null

    override fun init(onByteMessageListener: ISocket.OnByteMessageListener) {
        this.onByteMessageListener = onByteMessageListener

        val uri: URI
        try {
            uri = URI("ws://$ip:$port")
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                LogUtils.d("SocketClient.onOpen")
            }

            override fun onMessage(message: String?) {
                LogUtils.d("SocketClient.onMessage")
            }

            override fun onMessage(bytes: ByteBuffer?) {
                LogUtils.d("SocketClient.onMessage->bytes length=${bytes?.remaining()}")
                bytes?.let {
                    val buf = ByteArray(it.remaining())
                    it.get(buf)
                    this@SocketClient.onByteMessageListener?.onMessage(buf)
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                LogUtils.d("SocketClient.onClose $code, $reason, $remote")
            }

            override fun onError(ex: java.lang.Exception?) {
                LogUtils.d("SocketClient.onError")
                LogUtils.e(ex)
            }
        }
    }

    override fun start() {
        webSocketClient?.connect()
    }

    override fun sendData(data: ByteArray) {
        webSocketClient?.send(data)
    }

    override fun close() {
        webSocketClient?.close()
    }

    override fun destroy() {
        close()
        onByteMessageListener = null
        webSocketClient = null
    }
}