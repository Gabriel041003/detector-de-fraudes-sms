package com.example.detectordefraudes.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.detectordefraudes.R

class ProtectionService : Service() {

    private val CHANNEL_ID = "fraud_protection_channel"
    private val NOTIF_ID = 1001

    override fun onCreate() {
        super.onCreate()
        createChannelIfNeeded()

        // ❌ NUNCA use "context: this" aqui
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("FrauDetecta")
            .setContentText("Proteção ativa: monitorando SMS.")
            // Se não tiver um ícone seu, use um placeholder do Android:
            // .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()

        startForeground(NOTIF_ID, notif)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Proteção FrauDetecta",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }
}

