package com.example.tcc3.websocket

import okhttp3.*
import okio.ByteString

class WebSocketClient {
    private val client = OkHttpClient()
    private var webSocket: WebSocket? = null

    fun connectWebSocket(url: String) {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("Conexão aberta!")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Mensagem recebida: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println("Mensagem binária recebida: $bytes")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("Conexão está fechando: $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("Conexão fechada: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("Erro na conexão: ${t.message}")
            }
        })

        // Não é necessário desligar o executorService aqui
        // client.dispatcher.executorService.shutdown()
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun disconnect() {
        // Fechar a conexão WebSocket
        webSocket?.cancel()
        // Opcional: Fechar o cliente OkHttp se você não pretende usar mais
        client.dispatcher.executorService.shutdown()
    }
}
