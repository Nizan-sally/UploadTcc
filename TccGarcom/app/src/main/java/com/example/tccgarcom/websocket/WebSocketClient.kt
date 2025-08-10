package com.example.tcc3.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.tccgarcom.R
import com.example.tccgarcom.utils.Notification
import com.example.tccgarcom.models.SectionData
import com.example.tccgarcom.utils.CallsManager
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import okhttp3.*
import okio.ByteString
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WebSocketClient (var waiterSections: MutableList<SectionData>) {

    // Variáveis do WebSocket (server)
    private val client = OkHttpClient() // Cliente OkHttp que controla os protocolos de comunicação
    private var webSocket: WebSocket? = null // Recebe uma instância da classe WebSocket

    // Array associativo (Key -> Value) que vai armazenar os estados dos botões na forma (Id do botão, status)
    // Ex.: "stGar1" == true
    var btnStates: MutableMap<String, Boolean> = mutableMapOf()

    // Função que conecta o WebSocket
    fun connect(url: String, context: Context) {
        // Constrói uma requisição com a url recebida
        val request = Request.Builder().url(url).build()
        // Inicia um cliente WebSocket passando a requisição e registra um ouvinte
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            // Ao abrir a conexão:
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("Conexão aberta!")
            }

            // Ao receber uma mensagem (String)
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onMessage(webSocket: WebSocket, text: String) {
                // Nota: as mensagens referentes aos estados dos botões enviadas pelo server seguem dois formatos:
                // 1. Mesas -> "stMesa1_ON" ou "stMesa1_OFF"
                // 2. Cozinha -> "stGar1_ON" ou "stGar1_OFF"
                // O código abaixo se baseia em separar a parte que identifica a mesa/garçom (stMesa$numero)
                // e conectá-la com a parte que identifica o status (ON/OFF)

                println("Mensagem recebida: $text")

                // Verifica o índex do caractere "_" (underline) na mensagem recebida
                // (indexOf() retorna -1 caso o caractere não seja encontrado)
                if (text.indexOf("_") != -1) {
                    // Captura o Id do botão
                    val btnKey = text.substring(0, text.indexOf("_")) // Captura o texto entre o primeiro caractere e "_"
                    // Captura o Status do botão
                    val btnStateText = text.substring(text.indexOf("_") + 1) // Captura o texto adjacente ao "_"

                    // Variável que guarda o status do botão (capturado em String e convertido para Boolean)
                    var isActive = false
                    if (btnStateText == "ON") {
                        isActive = true
                    }
                    // Define um valor (isActive) para a chave (btnKey) e armazena na lista de status dos botões
                    btnStates[btnKey] = isActive

                    // Definindo variável do tipo Int que vai receber o número do botão
                    var btnNumber = 0
                    // Se houver o texto "stGar" na mensagem:
                    if (text.indexOf("stGar") != -1) {
                        // Captura o número do botão e converte para Int
                        // (indexOf() retorna o índice da primeira ocorrência, por isso a soma de 5 (são caracteres fixos))
                        // Os botões da cozinha são recebidos como 1, 2, 3 e 4 no app, mas são 9, 10, 11 e 12 no server, por isso a soma de 8
                        btnNumber = btnKey.substring(text.indexOf("stGar")+5).toInt() + 8
                    } else {
                        // Se houver o texto "stMesa" na mensagem:
                        if (text.indexOf("stMesa") != -1)  {
                            // Captura o número do botão e converte para Int
                            // (indexOf() retorna o índice da primeira ocorrência, por isso a soma de 6 (são caracteres fixos))
                            btnNumber = btnKey.substring(text.indexOf("stMesa")+6).toInt()
                        }
                    }
                    // Em cada setor vinculado ao garçom, verifica se o botão está presente como uma das mesas
                    for (section in waiterSections) {
                        // Se o botão estiver no setor:
                        if (btnNumber in section.sectionTables) {
                            // Se estiver ativo:
                            if (isActive) {
                                // Envia a notificação com o número do botão
                                val notification = Notification(context)
                                notification.buildNotification("Mesa $btnNumber chamando")
                                notification.sendNotification()

                                CallsManager().checkLastCall(btnKey, false)
                            }
                        }
                    }
                }
            }

            // Ao receber uma mensagem (ByteString)
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println("Mensagem binária recebida: $bytes")
            }

            // Ao início de um encerramento da conexão
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("Conexão está fechando: $reason")
                webSocket.close(1000, null)
            }

            // Ao encerrar a conexão
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("Conexão fechada: $reason")
            }

            // Ao falhar em estabelecer uma conexão
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("Erro na conexão: ${t.message}")
            }
        })
    }

    // Efetua o envio das mensagens de troca de status do botão
    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    // Desconecta o cliente WebSocket, fechando a conexão, e desconecta o cliente OkHttp
    fun disconnect() {
        // Fechar a conexão WebSocket
        webSocket?.cancel()
        // Opcional: Fechar o cliente OkHttp se você não pretende usar mais
        client.dispatcher.executorService.shutdown()
    }
}
