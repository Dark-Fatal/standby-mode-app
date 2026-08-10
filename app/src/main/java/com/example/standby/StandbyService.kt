package com.example.standby

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import android.view.OrientationEventListener
import android.os.PowerManager

class StandbyService : Service() {

    private lateinit var orientationListener: OrientationEventListener
    private var isCharging = false
    private var currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN
    private var clockActivityStarted = false

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_POWER_CONNECTED -> {
                    isCharging = true
                    checkAndStartClock()
                }
                Intent.ACTION_POWER_DISCONNECTED -> {
                    isCharging = false
                    // La StandbyClockActivity se ferme automatiquement en observant l'état
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(
            NotificationHelper.NOTIFICATION_ID,
            NotificationHelper.createNotification(this)
        )

        // Écouter les changements d'orientation
        orientationListener = object : OrientationEventListener(this) {
            override fun onOrientationChanged(orientation: Int) {
                currentOrientation = orientation
                checkAndStartClock()
            }
        }
        orientationListener.enable()

        // Écouter la charge
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerReceiver(powerReceiver, filter)

        // Vérifier l'état initial de la batterie
        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        isCharging = plugged == android.os.BatteryManager.BATTERY_PLUGGED_AC
                || plugged == android.os.BatteryManager.BATTERY_PLUGGED_USB
                || plugged == android.os.BatteryManager.BATTERY_PLUGGED_WIRELESS
    }

    private fun checkAndStartClock() {
        // Uniquement si branché et en paysage
        if (isCharging && isLandscape()) {
            if (!clockActivityStarted) {
                clockActivityStarted = true
                val intent = Intent(this, StandbyClockActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
        }
    }

    private fun isLandscape(): Boolean {
        return currentOrientation in 60..120 || currentOrientation in 240..300
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterReceiver(powerReceiver)
        orientationListener.disable()
        super.onDestroy()
    }
}