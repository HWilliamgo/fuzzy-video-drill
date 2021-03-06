package com.hwilliamgo.fuzzy_video_drill.screenprojection

import com.blankj.utilcode.util.LogUtils
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

/**
 * date: 2021/3/6
 * author: HWilliamgo
 * description:
 */
class SocketWatch(
    private val ip: String,
    private val port: Int,
    private val callback: DataCallback
) {

    private var webSocketClient: WebSocketClient? = null

    fun start() {
        val uri: URI
        try {
            uri = URI("ws://$ip:$port")
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        webSocketClient = object : WebSocketClient(uri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                LogUtils.d("SocketWatch.onOpen")
            }

            override fun onMessage(message: String?) {
                LogUtils.d("SocketWatch.onMessage")
            }

            override fun onMessage(bytes: ByteBuffer?) {
                LogUtils.d("SocketWatch.onMessage->bytes length=${bytes?.remaining()}")
                bytes?.let {
                    val buf = ByteArray(it.remaining())
                    it.get(buf)
                    callback.onCall(buf)
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                LogUtils.d("SocketWatch.onClose $code, $reason, $remote")
            }

            override fun onError(ex: java.lang.Exception?) {
                LogUtils.d("SocketWatch.onError")
                LogUtils.e(ex)
            }
        }
        webSocketClient?.connect()
    }

    fun stop() {
        webSocketClient?.close()
    }

    fun interface DataCallback {
        fun onCall(byteArray: ByteArray)
    }
}