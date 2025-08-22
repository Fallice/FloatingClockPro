package com.floating.clock

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class FloatingClockService : Service() {
    
    private var floatingClockView: FloatingClockView? = null
    
    companion object {
        private const val TAG = "FloatingClockService"
        const val CHANNEL_ID = "FloatingClockChannel"
        const val NOTIFICATION_ID = 1
        const val ACTION_START = "START_FLOATING_CLOCK"
        const val ACTION_STOP = "STOP_FLOATING_CLOCK"
        
        fun startService(context: Context) {
            try {
                val intent = Intent(context, FloatingClockService::class.java)
                intent.action = ACTION_START
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                Log.d(TAG, "Service start requested")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting service", e)
            }
        }
        
        fun stopService(context: Context) {
            try {
                val intent = Intent(context, FloatingClockService::class.java)
                intent.action = ACTION_STOP
                context.startService(intent)
                Log.d(TAG, "Service stop requested")
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping service", e)
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        try {
            createNotificationChannel()
            floatingClockView = FloatingClockView(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ${intent?.action}")
        
        try {
            when (intent?.action) {
                ACTION_START -> {
                    Log.d(TAG, "Starting foreground service")
                    startForeground(NOTIFICATION_ID, createNotification())
                    
                    if (!PermissionHelper.hasOverlayPermission(this)) {
                        Log.e(TAG, "No overlay permission, stopping service")
                        stopSelf()
                        return START_NOT_STICKY
                    }
                    
                    Log.d(TAG, "Showing floating view")
                    floatingClockView?.show()
                    
                    // 额外的调试：同时显示一个简单测试视图确认服务工作
                    try {
                        val simpleTest = SimpleFloatingView(this)
                        Handler(Looper.getMainLooper()).postDelayed({
                            simpleTest.showTestView()
                        }, 1000)
                        Log.d(TAG, "Debug test view scheduled")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error showing debug test view", e)
                    }
                }
                ACTION_STOP -> {
                    Log.d(TAG, "Stopping service")
                    floatingClockView?.hide()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }
                    stopSelf()
                }
                else -> {
                    Log.w(TAG, "Unknown action: ${intent?.action}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onStartCommand", e)
            stopSelf()
            return START_NOT_STICKY
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        try {
            floatingClockView?.hide()
            floatingClockView = null
        } catch (e: Exception) {
            Log.e(TAG, "Error in onDestroy", e)
        }
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.service_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = getString(R.string.service_channel_description)
                    setShowBadge(false)
                    enableLights(false)
                    enableVibration(false)
                }
                
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channel", e)
            }
        }
    }
    
    private fun createNotification() = try {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text))
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()
    } catch (e: Exception) {
        Log.e(TAG, "Error creating notification", e)
        throw e
    }
}