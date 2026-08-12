package com.indri.vsmentproject.data.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.indri.vsmentproject.R
import com.indri.vsmentproject.ui.staff.dashboard.JadwalPentingActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Mengambil title & message dari notification payload maupun data payload
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Instruksi Baru"

        val message = remoteMessage.notification?.body
            ?: remoteMessage.data["pesan"]
            ?: remoteMessage.data["body"]
            ?: "Ada notifikasi baru dari Manager."

        showNotification(title, message)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token baru dibuat/di-refresh oleh Google
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "vsment_instruksi_channel"
        val notificationId = System.currentTimeMillis().toInt()

        // Intent saat notifikasi dari atas diklik -> buka JadwalPentingActivity
        val intent = Intent(this, JadwalPentingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Pastikan ada icon vector ini di drawable
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Agar melayang di atas layar
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 (API 26) ke atas WAJIB buat NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Instruksi & Notifikasi Manager",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi instruksi kerja dari Manager"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}