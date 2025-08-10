package com.example.tccgarcom.utils

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.tccgarcom.MainActivity
import com.example.tccgarcom.R
import com.google.androidgamesdk.gametextinput.Settings

class Notification (viewContext: Context) {

    val context = viewContext

    // Variáveis utilizadas para definir a ação do toque
    var intent = Intent(context, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent,
        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

    private lateinit var builder: NotificationCompat.Builder

    fun buildNotification(text: String) {
        // Construtor da notificação
        builder = NotificationCompat.Builder(context, "calls_channel") // Usa o canal criado na MainActivity
            .setSmallIcon(R.drawable.icon_status) // Define o ícone
            .setContentTitle("Solicitação de atendimento") // Define o título
            .setContentText(text) // Define o texto curto
            .setStyle(NotificationCompat.BigTextStyle().bigText(text)) // Define o texto longo
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Define a prioridade da notificação (HIGH -> Pode usar som)
            .setContentIntent(pendingIntent) // Define a intenção (declarada anteriormente)
            .setAutoCancel(true) // Define que a notificação se cancela ao toque
    }

    // Efetua o envio da notificação
    fun sendNotification() {
        with(NotificationManagerCompat.from(context)) {
            // Verifica se a permissão de enviar notificações foi dada pelo usuário
            if (ActivityCompat.checkSelfPermission(
                    context,
                    POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO ("Request permission")

                return@with
            }
            // Usa o construtor e define um ID para enviar a notificação
            notify(1, builder.build())
        }
    }
}