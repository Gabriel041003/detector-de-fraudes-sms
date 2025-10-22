package com.example.detectordefraudes.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.example.detectordefraudes.receiver.SmsReceiver
import com.example.detectordefraudes.service.ProtectionService

object BackgroundToggle {

    fun enable(context: Context) {
        // 1) habilita o BroadcastReceiver
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, SmsReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        // 2) inicia o Foreground Service (notificação persistente)
        val intent = Intent(context, ProtectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun disable(context: Context) {
        // 1) desabilita o BroadcastReceiver
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, SmsReceiver::class.java),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        // 2) para o Service
        context.stopService(Intent(context, ProtectionService::class.java))
    }
}


