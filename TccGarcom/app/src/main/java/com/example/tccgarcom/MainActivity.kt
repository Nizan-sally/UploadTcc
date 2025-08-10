package com.example.tccgarcom

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Cria um canal para as notificações
        // Permite que o usuário gerencie configurações de notificação com mais precisão
        // Se houver um canal idêntico criado, não acumula e nem gera conflitos
        createNotificationChannel()

        findViewById<View>(R.id.root_layout).setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                requestNotificationPermission()
                true
            } else {
                false
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!areNotificationsEnabled()) {
                Toast.makeText(this, "Solicitando permissão de notificações...", Toast.LENGTH_SHORT).show()

                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 0)
//                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
//                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
//                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Permissão de notificações já concedida.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun areNotificationsEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        } else {
            true // Para versões anteriores, assume que está habilitado.
        }
    }

    override fun onResume() {
        super.onResume()
        if (!areNotificationsEnabled()) {
            Toast.makeText(this, "Permissão de notificações negada, toque na tela para tentar novamente.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        // Por questão de compatibilidade com os canais, verifica se a versão do Android é superior ao Android Oreo (na qual a função foi implementada)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Inicia o gerenciador de notificações do sistema
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "calls_channel" // Define o Id do canal (deve ser único)
            val channelName = "Chamadas" // Define o nome do canal
            val channelDescription = "Chamadas dos botões" // Descreve o canal
            val importance =
                NotificationManager.IMPORTANCE_HIGH // Define a importância das notificações (HIGH -> pode usar som)

            // Busca o áudio que vai ser usado quando houver uma notificação
            val soundUri = Uri.parse("android.resource://com.example.tccgarcom/raw/bell")
            // Configura alguns atributos do áudio
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            // Condensa todas as informações e configurações anteriores em uma variável
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription // Conecta a descrição
                setSound(soundUri, audioAttributes) // Conecta as propriedades do áudio
            }
            // Efetua a criação do canal de áudio
            notificationManager.createNotificationChannel(channel)
        }
    }
}